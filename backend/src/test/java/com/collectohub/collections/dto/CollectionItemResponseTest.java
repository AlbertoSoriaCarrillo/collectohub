package com.collectohub.collections.dto;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionEditorialReferenceSource;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollectionItemResponseTest {

    @Test
    void manualFieldsRemainPublicWhilePersonalFieldsAreSanitized() {
        CollectionItem item = CollectionItem.createManual(
                collection(), "Manual item", "Personal description", "CUSTOM",
                CollectionItemStatus.OWNED, PhysicalCondition.GOOD, "4", 12,
                "Private note", LocalDate.of(2026, 6, 1), 42L
        );
        ReflectionTestUtils.setField(item, "id", 500L);

        CollectionItemResponse ownerResponse = CollectionItemResponse.from(item, true);
        CollectionItemResponse publicResponse = CollectionItemResponse.from(item, false);

        assertThat(publicResponse.editorialReferenceSource()).isEqualTo("MANUAL");
        assertThat(publicResponse.referenceKind()).isEqualTo("MANUAL");
        assertThat(publicResponse.manualTitle()).isEqualTo("Manual item");
        assertThat(publicResponse.manualDescription()).isEqualTo("Personal description");
        assertThat(publicResponse.manualType()).isEqualTo("CUSTOM");
        assertThat(ownerResponse.notes()).isEqualTo("Private note");
        assertThat(ownerResponse.acquiredAt()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(publicResponse.notes()).isNull();
        assertThat(publicResponse.acquiredAt()).isNull();
    }

    @Test
    void referenceKindKeepsLegacyDirectVerifiedAndInvalidContracts() {
        Collection collection = collection();
        MasterProduct masterProduct = masterProduct();
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(300L);
        when(catalogItem.getTitle()).thenReturn("Catalog item");

        CollectionItem legacy = CollectionItem.create(
                collection, masterProduct, null, null, CollectionEditorialReferenceSource.LEGACY,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );
        CollectionItem direct = CollectionItem.create(
                collection, null, catalogItem, null, CollectionEditorialReferenceSource.MANUAL_EDITORIAL,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );
        CollectionItem verified = CollectionItem.create(
                collection, null, catalogItem, null, CollectionEditorialReferenceSource.VERIFIED_BRIDGE,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );
        CollectionItem invalid = CollectionItem.create(
                collection, null, null, null, CollectionEditorialReferenceSource.LEGACY,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );

        assertThat(CollectionItemResponse.from(legacy).referenceKind()).isEqualTo("LEGACY_UNRESOLVED");
        assertThat(CollectionItemResponse.from(direct).referenceKind()).isEqualTo("DIRECT_CATALOG");
        assertThat(CollectionItemResponse.from(verified).referenceKind()).isEqualTo("VERIFIED_BRIDGE");
        assertThat(CollectionItemResponse.from(invalid).referenceKind()).isEqualTo("INVALID_REFERENCE");
    }

    private Collection collection() {
        User user = User.register("owner@example.com", "hash", "Owner", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", 42L);
        ProductCategory category = new ProductCategory("MANGA_COMIC", "Manga");
        ReflectionTestUtils.setField(category, "id", 10L);
        Collection collection = Collection.create(user, "Collection", null, CollectionVisibility.PRIVATE, category);
        ReflectionTestUtils.setField(collection, "id", 100L);
        return collection;
    }

    private MasterProduct masterProduct() {
        ProductCategory category = new ProductCategory("MANGA_COMIC", "Manga");
        ReflectionTestUtils.setField(category, "id", 10L);
        MasterProduct product = MasterProduct.create(
                "Legacy item", null, category, null, null, null, null, null, null,
                null, null, null, "es", false, List.of(), null, Map.of(), 42L
        );
        ReflectionTestUtils.setField(product, "id", 200L);
        return product;
    }
}
