package com.collectohub.collections.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.collections.dto.CreateCollectionItemRequest;
import com.collectohub.collections.dto.CreateCollectionRequest;
import com.collectohub.collections.dto.UpdateCollectionItemRequest;
import com.collectohub.collections.dto.UpdateCollectionRequest;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.collections.infrastructure.CollectionRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionItemRepository collectionItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private MasterProductRepository masterProductRepository;

    @Mock
    private CatalogItemRepository catalogItemRepository;

    @Mock
    private CatalogItemEditionRepository catalogItemEditionRepository;

    @Mock
    private MasterProductCatalogLinkRepository masterProductCatalogLinkRepository;

    private CollectionService collectionService;
    private User owner;
    private User otherUser;
    private ProductCategory category;
    private MasterProduct masterProduct;
    private Collection privateCollection;
    private Collection publicCollection;

    @BeforeEach
    void setUp() {
        collectionService = new CollectionService(
                collectionRepository,
                collectionItemRepository,
                userRepository,
                productCategoryRepository,
                masterProductRepository,
                catalogItemRepository,
                catalogItemEditionRepository,
                masterProductCatalogLinkRepository
        );
        owner = user(42L, "owner@example.com");
        otherUser = user(43L, "other@example.com");
        category = withId(new ProductCategory("MANGA_COMIC", "Manga and comic"), 10L);
        masterProduct = masterProduct(200L);
        privateCollection = collection(100L, owner, CollectionVisibility.PRIVATE);
        publicCollection = collection(101L, owner, CollectionVisibility.PUBLIC);
    }

    @Test
    void authenticatedUserCreatesCollection() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(collectionRepository.save(any(Collection.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

        var response = collectionService.createCollection(
                AuthenticatedUser.from(owner),
                new CreateCollectionRequest(" Manga ", "My manga", CollectionVisibility.PUBLIC, "manga_comic")
        );

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Manga");
        assertThat(response.visibility()).isEqualTo("PUBLIC");
        assertThat(response.categoryCode()).isEqualTo("MANGA_COMIC");
    }

    @Test
    void listMyCollectionsCanFilterByVisibility() {
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(collectionRepository.findAll(any(Specification.class), eq(Sort.by("id").ascending())))
                .thenReturn(List.of(publicCollection));

        var response = collectionService.myCollections(
                AuthenticatedUser.from(owner),
                CollectionVisibility.PUBLIC,
                "MANGA_COMIC"
        );

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void publicCollectionCanBeReadWithoutToken() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(publicCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(101L)).thenReturn(List.of());

        var response = collectionService.getCollection(null, 101L);

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void ownerReceivesPrivateCollectionItemFields() {
        CollectionItem item = collectionItem(300L, publicCollection);
        when(collectionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(publicCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(101L)).thenReturn(List.of(item));

        var response = collectionService.getCollection(AuthenticatedUser.from(owner), 101L).items().getFirst();

        assertThat(response.notes()).isEqualTo("First print");
        assertThat(response.acquiredAt()).isEqualTo(LocalDate.of(2023, 6, 1));
    }

    @Test
    void publicCollectionSanitizesPrivateFieldsForAnonymousAndNonOwnersRegardlessOfRole() {
        CollectionItem item = editorialCollectionItem(300L, publicCollection,
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.VERIFIED_BRIDGE);
        User admin = user(44L, "admin@example.com");
        admin.addRole(new Role("ADMIN", "Administrator"));
        User editorialAdmin = user(45L, "editorial@example.com");
        editorialAdmin.addRole(new Role("EDITORIAL_ADMIN", "Editorial administrator"));
        when(collectionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(publicCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(101L)).thenReturn(List.of(item));

        var anonymous = collectionService.getCollection(null, 101L).items().getFirst();
        var otherUserResponse = collectionService.getCollection(AuthenticatedUser.from(otherUser), 101L).items().getFirst();
        var adminResponse = collectionService.getCollection(AuthenticatedUser.from(admin), 101L).items().getFirst();
        var editorialAdminResponse = collectionService.getCollection(AuthenticatedUser.from(editorialAdmin), 101L).items().getFirst();

        assertSanitizedPublicItem(anonymous);
        assertSanitizedPublicItem(otherUserResponse);
        assertSanitizedPublicItem(adminResponse);
        assertSanitizedPublicItem(editorialAdminResponse);
    }

    @Test
    void collectionItemReferenceKindsAreCalculatedWithoutPersistingReadSideChanges() {
        CollectionItem directCatalog = editorialCollectionItem(300L, publicCollection,
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.MANUAL_EDITORIAL);
        CollectionItem verifiedBridge = editorialCollectionItem(301L, publicCollection,
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.VERIFIED_BRIDGE);
        CollectionItem legacy = collectionItem(302L, publicCollection);
        when(collectionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(publicCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(101L))
                .thenReturn(List.of(directCatalog, verifiedBridge, legacy));

        var response = collectionService.getCollection(AuthenticatedUser.from(owner), 101L);

        assertThat(response.items()).extracting(item -> item.referenceKind())
                .containsExactly("DIRECT_CATALOG", "VERIFIED_BRIDGE", "LEGACY_UNRESOLVED");
        verify(collectionItemRepository, never()).save(any(CollectionItem.class));
    }

    @Test
    void privateCollectionFromAnotherUserIsNotExposed() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.getCollection(AuthenticatedUser.from(otherUser), 100L))
                .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    void ownerUpdatesCollection() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(100L)).thenReturn(List.of());

        var response = collectionService.updateCollection(
                AuthenticatedUser.from(owner),
                100L,
                new UpdateCollectionRequest("Updated", "Updated description", CollectionVisibility.PUBLIC, null)
        );

        assertThat(response.name()).isEqualTo("Updated");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void otherUserCannotUpdateCollection() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.updateCollection(
                AuthenticatedUser.from(otherUser),
                100L,
                new UpdateCollectionRequest("Updated", null, null, null)
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerSoftDeletesCollection() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        collectionService.deleteCollection(AuthenticatedUser.from(owner), 100L);

        assertThat(privateCollection.getDeletedAt()).isNotNull();
    }

    @Test
    void ownerAddsItemToCollection() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(collectionItemRepository.save(any(CollectionItem.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 300L));

        var response = collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                createItemRequest()
        );

        assertThat(response.id()).isEqualTo(300L);
        assertThat(response.masterProductId()).isEqualTo(200L);
        assertThat(response.collectionStatus()).isEqualTo("OWNED");
        assertThat(response.physicalCondition()).isEqualTo("LIKE_NEW");
    }

    @Test
    void ownerAddsPureEditorialItem() {
        CatalogItem catalogItem = editorialItem(500L);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(500L)).thenReturn(Optional.of(catalogItem));
        when(collectionItemRepository.save(any(CollectionItem.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 301L));

        var response = collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(null, 500L, null, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        );

        assertThat(response.masterProductId()).isNull();
        assertThat(response.catalogItemId()).isEqualTo(500L);
        assertThat(response.editorialReferenceSource()).isEqualTo("MANUAL_EDITORIAL");
    }

    @Test
    void ownerAddsEditorialEdition() {
        CatalogItem catalogItem = editorialItem(500L);
        CatalogItemEdition edition = editorialEdition(600L, catalogItem);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(500L)).thenReturn(Optional.of(catalogItem));
        when(catalogItemEditionRepository.findByIdAndDeletedAtIsNull(600L)).thenReturn(Optional.of(edition));
        when(collectionItemRepository.save(any(CollectionItem.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 303L));

        var response = collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(null, 500L, 600L, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        );

        assertThat(response.catalogItemEditionId()).isEqualTo(600L);
        assertThat(response.catalogItemEditionFormat()).isEqualTo("PAPERBACK");
    }

    @Test
    void verifiedBridgeEnrichesLegacyItem() {
        CatalogItem catalogItem = editorialItem(500L);
        MasterProductCatalogLink link = mock(MasterProductCatalogLink.class);
        when(link.getCatalogItem()).thenReturn(catalogItem);
        when(link.getCatalogItemEdition()).thenReturn(null);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(masterProductCatalogLinkRepository.findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                200L, com.collectohub.catalog.domain.MasterProductCatalogLinkStatus.VERIFIED))
                .thenReturn(Optional.of(link));
        when(collectionItemRepository.save(any(CollectionItem.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 302L));

        var response = collectionService.addItem(
                AuthenticatedUser.from(owner), 100L, createItemRequest()
        );

        assertThat(response.catalogItemId()).isEqualTo(500L);
        assertThat(response.editorialReferenceSource()).isEqualTo("VERIFIED_BRIDGE");
    }

    @Test
    void itemRequiresLegacyOrEditorialReference() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(null, null, null, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        )).isInstanceOf(InvalidCollectionItemReferenceException.class);
    }

    @Test
    void archivedEditorialItemIsRejected() {
        CatalogItem catalogItem = editorialItem(500L);
        when(catalogItem.isPubliclyVisible()).thenReturn(false);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(500L)).thenReturn(Optional.of(catalogItem));

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(null, 500L, null, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        )).isInstanceOf(com.collectohub.catalog.application.CatalogItemNotFoundException.class);
    }

    @Test
    void explicitEditorialReferenceCannotContradictVerifiedBridge() {
        CatalogItem verifiedItem = editorialItem(500L);
        CatalogItem selectedItem = editorialItem(501L);
        MasterProductCatalogLink link = mock(MasterProductCatalogLink.class);
        when(link.getCatalogItem()).thenReturn(verifiedItem);
        when(link.getCatalogItemEdition()).thenReturn(null);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(501L)).thenReturn(Optional.of(selectedItem));
        when(masterProductCatalogLinkRepository.findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                200L, com.collectohub.catalog.domain.MasterProductCatalogLinkStatus.VERIFIED))
                .thenReturn(Optional.of(link));

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(200L, 501L, null, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        )).isInstanceOf(ConflictingCollectionItemReferenceException.class);
    }

    @Test
    void editionMustBelongToSelectedItem() {
        CatalogItem catalogItem = editorialItem(500L);
        CatalogItem otherItem = editorialItem(501L);
        CatalogItemEdition edition = editorialEdition(600L, otherItem);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(500L)).thenReturn(Optional.of(catalogItem));
        when(catalogItemEditionRepository.findByIdAndDeletedAtIsNull(600L)).thenReturn(Optional.of(edition));

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                new CreateCollectionItemRequest(null, 500L, 600L, CollectionItemStatus.OWNED,
                        null, null, null, null, null, null, null, null)
        )).isInstanceOf(InvalidCollectionItemReferenceException.class);
    }

    @Test
    void otherUserCannotAddItemToCollection() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(otherUser),
                100L,
                createItemRequest()
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void missingMasterProductReturnsNotFoundWhenAddingItem() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.addItem(
                AuthenticatedUser.from(owner),
                100L,
                createItemRequest()
        )).isInstanceOf(MasterProductNotFoundException.class);
    }

    @Test
    void ownerListsCollectionItems() {
        CollectionItem item = collectionItem(300L, privateCollection);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(100L)).thenReturn(List.of(item));

        var response = collectionService.listItems(AuthenticatedUser.from(owner), 100L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(300L);
        assertThat(response.getFirst().notes()).isEqualTo("First print");
        assertThat(response.getFirst().acquiredAt()).isEqualTo(LocalDate.of(2023, 6, 1));
    }

    @Test
    void publicCollectionItemsCanBeListedWithoutToken() {
        CollectionItem item = editorialCollectionItem(300L, publicCollection,
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.VERIFIED_BRIDGE);
        when(collectionRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(publicCollection));
        when(collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(101L)).thenReturn(List.of(item));

        var response = collectionService.listItems(null, 101L);

        assertThat(response).hasSize(1);
        assertSanitizedPublicItem(response.getFirst());
    }

    @Test
    void privateCollectionItemsFromAnotherUserAreNotExposed() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.listItems(AuthenticatedUser.from(otherUser), 100L))
                .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    void ownerUpdatesItem() {
        CollectionItem item = collectionItem(300L, privateCollection);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(300L, 100L)).thenReturn(Optional.of(item));

        var response = collectionService.updateItem(
                AuthenticatedUser.from(owner),
                100L,
                300L,
                new UpdateCollectionItemRequest(
                        null,
                        null,
                        null,
                        CollectionItemStatus.SELLABLE,
                        PhysicalCondition.GOOD,
                        "2",
                        100,
                        "Updated",
                        LocalDate.of(2024, 1, 1), null, null, null
                )
        );

        assertThat(response.collectionStatus()).isEqualTo("SELLABLE");
        assertThat(response.physicalCondition()).isEqualTo("GOOD");
        assertThat(response.unitNumber()).isEqualTo("2");
    }

    @Test
    void ownerChangesEditorialItemToLegacyReference() {
        CollectionItem item = collectionItem(300L, privateCollection);
        ReflectionTestUtils.setField(item, "catalogItem", editorialItem(500L));
        ReflectionTestUtils.setField(
                item,
                "editorialReferenceSource",
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        MasterProduct replacement = masterProduct(201L);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(300L, 100L)).thenReturn(Optional.of(item));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(201L)).thenReturn(Optional.of(replacement));
        when(masterProductCatalogLinkRepository.findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                201L, com.collectohub.catalog.domain.MasterProductCatalogLinkStatus.VERIFIED))
                .thenReturn(Optional.empty());

        var response = collectionService.updateItem(
                AuthenticatedUser.from(owner),
                100L,
                300L,
                new UpdateCollectionItemRequest(201L, null, null, null, null, null, null, null, null, null, null, null)
        );

        assertThat(response.masterProductId()).isEqualTo(201L);
        assertThat(response.catalogItemId()).isNull();
        assertThat(response.catalogItemEditionId()).isNull();
        assertThat(response.editorialReferenceSource()).isEqualTo("LEGACY");
    }

    @Test
    void ownerChangesEditorialEditionToItemReference() {
        CatalogItem catalogItem = editorialItem(500L);
        CollectionItem item = collectionItem(300L, privateCollection);
        ReflectionTestUtils.setField(item, "catalogItem", catalogItem);
        ReflectionTestUtils.setField(item, "catalogItemEdition", editorialEdition(600L, catalogItem));
        ReflectionTestUtils.setField(
                item,
                "editorialReferenceSource",
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.MANUAL_EDITORIAL
        );
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(300L, 100L)).thenReturn(Optional.of(item));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(catalogItemRepository.findByIdAndDeletedAtIsNull(500L)).thenReturn(Optional.of(catalogItem));
        when(masterProductCatalogLinkRepository.findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                200L, com.collectohub.catalog.domain.MasterProductCatalogLinkStatus.VERIFIED))
                .thenReturn(Optional.empty());

        var response = collectionService.updateItem(
                AuthenticatedUser.from(owner),
                100L,
                300L,
                new UpdateCollectionItemRequest(null, 500L, null, null, null, null, null, null, null, null, null, null)
        );

        assertThat(response.catalogItemId()).isEqualTo(500L);
        assertThat(response.catalogItemEditionId()).isNull();
        assertThat(response.editorialReferenceSource()).isEqualTo("MANUAL_EDITORIAL");
    }

    @Test
    void otherUserCannotUpdateItem() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));

        assertThatThrownBy(() -> collectionService.updateItem(
                AuthenticatedUser.from(otherUser),
                100L,
                300L,
                new UpdateCollectionItemRequest(null, null, null, CollectionItemStatus.WANTED, null, null, null, null, null, null, null, null)
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerSoftDeletesItem() {
        CollectionItem item = collectionItem(300L, privateCollection);
        when(collectionRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(privateCollection));
        when(collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(300L, 100L)).thenReturn(Optional.of(item));

        collectionService.deleteItem(AuthenticatedUser.from(owner), 100L, 300L);

        assertThat(item.getDeletedAt()).isNotNull();
    }

    private CreateCollectionItemRequest createItemRequest() {
        return new CreateCollectionItemRequest(
                200L,
                null,
                null,
                CollectionItemStatus.OWNED,
                PhysicalCondition.LIKE_NEW,
                "1",
                50,
                "First print",
                LocalDate.of(2023, 6, 1), null, null, null
        );
    }

    private User user(Long id, String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Collection collection(Long id, User user, CollectionVisibility visibility) {
        return withId(Collection.create(user, "Manga", "My manga", visibility, category), id);
    }

    private CollectionItem collectionItem(Long id, Collection collection) {
        return withId(CollectionItem.create(
                collection,
                masterProduct,
                null,
                null,
                com.collectohub.collections.domain.CollectionEditorialReferenceSource.LEGACY,
                CollectionItemStatus.OWNED,
                PhysicalCondition.LIKE_NEW,
                "1",
                50,
                "First print",
                LocalDate.of(2023, 6, 1),
                collection.getUser().getId()
        ), id);
    }

    private CollectionItem editorialCollectionItem(
            Long id,
            Collection collection,
            com.collectohub.collections.domain.CollectionEditorialReferenceSource source
    ) {
        CatalogItem catalogItem = editorialItem(500L);
        CatalogItemEdition edition = editorialEdition(600L, catalogItem);
        return withId(CollectionItem.create(
                collection,
                masterProduct,
                catalogItem,
                edition,
                source,
                CollectionItemStatus.OWNED,
                PhysicalCondition.LIKE_NEW,
                "1",
                50,
                "First print",
                LocalDate.of(2023, 6, 1),
                collection.getUser().getId()
        ), id);
    }

    private void assertSanitizedPublicItem(com.collectohub.collections.dto.CollectionItemResponse response) {
        assertThat(response.notes()).isNull();
        assertThat(response.acquiredAt()).isNull();
        assertThat(response.masterProductId()).isEqualTo(200L);
        assertThat(response.catalogItemId()).isEqualTo(500L);
        assertThat(response.catalogItemEditionId()).isEqualTo(600L);
        assertThat(response.editorialReferenceSource()).isEqualTo("VERIFIED_BRIDGE");
        assertThat(response.referenceKind()).isEqualTo("VERIFIED_BRIDGE");
        assertThat(response.collectionStatus()).isEqualTo("OWNED");
    }

    private MasterProduct masterProduct(Long id) {
        return withId(MasterProduct.create(
                "Dragon Ball 1",
                null,
                category,
                "Dragon Ball",
                "Tankobon",
                "1",
                "Planeta",
                "9788490000001",
                null,
                null,
                null,
                null,
                "es",
                false,
                List.of("ES"),
                null,
                Map.of(),
                42L
        ), id);
    }

    private CatalogItem editorialItem(Long id) {
        CatalogSeries series = mock(CatalogSeries.class);
        lenient().when(series.getId()).thenReturn(400L);
        lenient().when(series.getTitle()).thenReturn("Dragon Ball");
        lenient().when(series.getFranchise()).thenReturn(null);
        CatalogItem item = mock(CatalogItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getTitle()).thenReturn("Dragon Ball 1");
        lenient().when(item.getSequenceLabel()).thenReturn("1");
        lenient().when(item.getSeries()).thenReturn(series);
        lenient().when(item.isPubliclyVisible()).thenReturn(true);
        return item;
    }

    private CatalogItemEdition editorialEdition(Long id, CatalogItem item) {
        CatalogItemEdition edition = mock(CatalogItemEdition.class);
        lenient().when(edition.getId()).thenReturn(id);
        lenient().when(edition.getCatalogItem()).thenReturn(item);
        lenient().when(edition.getFormat()).thenReturn(CatalogItemEditionFormat.PAPERBACK);
        lenient().when(edition.isPubliclyVisible()).thenReturn(true);
        return edition;
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
