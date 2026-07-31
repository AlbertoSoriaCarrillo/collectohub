package com.collectohub.collections.infrastructure;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionEditorialReferenceSource;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CollectionItemRepositoryProgressTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    )
            .withDatabaseName("collectohub_progress_test")
            .withUsername("collectohub")
            .withPassword("collectohub");

    @DynamicPropertySource
    static void registerPostgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Autowired
    private CollectionItemRepository repository;

    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void queryReturnsOnlyLiveCatalogReferencesForRequestedCollectionAndSeries() {
        Role userRole = entityManager.createQuery(
                "select role from Role role where role.code = 'USER'",
                Role.class
        ).getSingleResult();
        ProductCategory category = entityManager.createQuery(
                "select category from ProductCategory category where category.code = 'MANGA_COMIC'",
                ProductCategory.class
        ).getSingleResult();
        User owner = User.register("progress-owner@example.com", "hash", "Owner", userRole);
        entityManager.persist(owner);

        Collection requestedCollection = collection(owner, category, "Requested");
        Collection otherCollection = collection(owner, category, "Other");
        Collection deletedCollection = collection(owner, category, "Deleted");
        deletedCollection.softDelete(owner.getId());
        entityManager.persist(requestedCollection);
        entityManager.persist(otherCollection);
        entityManager.persist(deletedCollection);

        CatalogSeries requestedSeries = series("Requested series");
        CatalogSeries otherSeries = series("Other series");
        entityManager.persist(requestedSeries);
        entityManager.persist(otherSeries);

        CatalogItem requestedCatalogItem = catalogItem(requestedSeries, "Volume 1", BigDecimal.ONE);
        CatalogItem otherSeriesItem = catalogItem(otherSeries, "Other volume", BigDecimal.ONE);
        entityManager.persist(requestedCatalogItem);
        entityManager.persist(otherSeriesItem);
        CatalogItemEdition edition = edition(requestedCatalogItem);
        entityManager.persist(edition);

        MasterProduct masterProduct = masterProduct(category);
        entityManager.persist(masterProduct);

        CollectionItem direct = referencedItem(
                requestedCollection,
                null,
                requestedCatalogItem,
                edition,
                CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        CollectionItem verifiedBridge = referencedItem(
                requestedCollection,
                masterProduct,
                requestedCatalogItem,
                null,
                CollectionEditorialReferenceSource.VERIFIED_BRIDGE
        );
        CollectionItem fromOtherCollection = referencedItem(
                otherCollection,
                null,
                requestedCatalogItem,
                null,
                CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        CollectionItem fromOtherSeries = referencedItem(
                requestedCollection,
                null,
                otherSeriesItem,
                null,
                CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        CollectionItem deletedItem = referencedItem(
                requestedCollection,
                null,
                requestedCatalogItem,
                null,
                CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        deletedItem.softDelete(owner.getId());
        CollectionItem fromDeletedCollection = referencedItem(
                deletedCollection,
                null,
                requestedCatalogItem,
                null,
                CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        CollectionItem manual = CollectionItem.createManual(
                requestedCollection,
                "Manual",
                null,
                null,
                CollectionItemStatus.OWNED,
                null,
                null,
                null,
                null,
                null,
                owner.getId()
        );
        CollectionItem legacy = referencedItem(
                requestedCollection,
                masterProduct,
                null,
                null,
                CollectionEditorialReferenceSource.LEGACY
        );

        List.of(
                direct,
                verifiedBridge,
                fromOtherCollection,
                fromOtherSeries,
                deletedItem,
                fromDeletedCollection,
                manual,
                legacy
        ).forEach(entityManager::persist);
        entityManager.flush();
        entityManager.clear();

        List<CollectionItem> result = repository.findProgressItemsByCollectionIdAndSeriesId(
                requestedCollection.getId(),
                requestedSeries.getId()
        );
        result.forEach(entityManager::detach);

        assertThat(result).extracting(CollectionItem::getId)
                .containsExactly(direct.getId(), verifiedBridge.getId());
        assertThat(result).extracting(CollectionItem::getEditorialReferenceSource)
                .containsExactly(
                        CollectionEditorialReferenceSource.MANUAL_EDITORIAL,
                        CollectionEditorialReferenceSource.VERIFIED_BRIDGE
                );
        assertThat(result).allSatisfy(item -> {
            assertThat(item.getCatalogItem().getTitle()).isEqualTo("Volume 1");
            assertThat(item.getCatalogItem().getSeries().getTitle()).isEqualTo("Requested series");
        });
        assertThat(result.getFirst().getCatalogItemEdition().getEditionName()).isEqualTo("Paperback");
        assertThat(result.getLast().getCatalogItemEdition()).isNull();
        assertThat(result).doesNotHaveDuplicates();

        List<CollectionItem> allProgressItems = repository.findProgressItemsByCollectionId(requestedCollection.getId());
        assertThat(allProgressItems).extracting(CollectionItem::getId)
                .containsExactly(direct.getId(), verifiedBridge.getId(), fromOtherSeries.getId());

        List<CollectionItem> detailItems = repository.findDetailItemsByCollectionId(requestedCollection.getId());
        assertThat(detailItems).extracting(CollectionItem::getId)
                .containsExactly(direct.getId(), verifiedBridge.getId(), fromOtherSeries.getId(), manual.getId(), legacy.getId());
        entityManager.clear();
        assertThat(detailItems).allSatisfy(item -> assertThat(item.getCollection().getName()).isEqualTo("Requested"));
        assertThat(detailItems.getFirst().getCatalogItem().getSeries().getTitle()).isEqualTo("Requested series");
        assertThat(detailItems.getFirst().getCatalogItemEdition().getEditionName()).isEqualTo("Paperback");
        assertThat(detailItems.getLast().getMasterProduct().getCategory().getCode()).isEqualTo("MANGA_COMIC");

        List<CatalogItem> activeSeriesItems = catalogItemRepository.findActiveItemsBySeriesIds(
                Set.of(requestedSeries.getId(), otherSeries.getId()),
                CatalogRecordStatus.ACTIVE
        );
        entityManager.clear();
        assertThat(activeSeriesItems).extracting(CatalogItem::getId)
                .containsExactlyInAnyOrder(requestedCatalogItem.getId(), otherSeriesItem.getId());
        assertThat(activeSeriesItems).allSatisfy(item -> assertThat(item.getSeries().getTitle()).isNotBlank());
    }

    private Collection collection(User owner, ProductCategory category, String name) {
        return Collection.create(owner, name, null, CollectionVisibility.PRIVATE, category);
    }

    private CatalogSeries series(String title) {
        return CatalogSeries.create(
                null,
                null,
                title,
                null,
                CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED,
                null,
                "JP",
                "ja",
                1984,
                1995,
                CatalogRecordStatus.ACTIVE,
                1L
        );
    }

    private CatalogItem catalogItem(CatalogSeries series, String title, BigDecimal sortOrder) {
        return CatalogItem.create(
                series,
                title,
                null,
                "1",
                sortOrder,
                null,
                LocalDate.of(1984, 1, 1),
                1984,
                "ja",
                "JP",
                CatalogRecordStatus.ACTIVE,
                1L
        );
    }

    private CatalogItemEdition edition(CatalogItem catalogItem) {
        return CatalogItemEdition.create(
                catalogItem,
                null,
                null,
                null,
                CatalogItemEditionFormat.PAPERBACK,
                "Paperback",
                LocalDate.of(1984, 1, 1),
                1984,
                "es",
                "ES",
                192,
                null,
                CatalogRecordStatus.ACTIVE,
                1L
        );
    }

    private MasterProduct masterProduct(ProductCategory category) {
        return MasterProduct.create(
                "Legacy volume",
                null,
                category,
                "Series",
                "Collection",
                "1",
                null,
                null,
                null,
                null,
                null,
                null,
                "es",
                false,
                List.of("ES"),
                null,
                Map.of(),
                1L
        );
    }

    private CollectionItem referencedItem(
            Collection collection,
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition edition,
            CollectionEditorialReferenceSource source
    ) {
        return CollectionItem.create(
                collection,
                masterProduct,
                catalogItem,
                edition,
                source,
                CollectionItemStatus.OWNED,
                PhysicalCondition.GOOD,
                null,
                null,
                null,
                null,
                collection.getUser().getId()
        );
    }
}
