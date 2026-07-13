package com.collectohub.collections.domain;

import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionItemTest {

    @Test
    void createManualBuildsACompleteManualCollectionItem() {
        CollectionItem item = CollectionItem.createManual(
                collection(), "Manual item", "Personal description", "CUSTOM",
                CollectionItemStatus.OWNED, PhysicalCondition.LIKE_NEW, "7", 25,
                "Private note", LocalDate.of(2026, 7, 13), 42L
        );

        assertThat(item.getMasterProduct()).isNull();
        assertThat(item.getCatalogItem()).isNull();
        assertThat(item.getCatalogItemEdition()).isNull();
        assertThat(item.getEditorialReferenceSource()).isEqualTo(CollectionEditorialReferenceSource.MANUAL);
        assertThat(item.getManualTitle()).isEqualTo("Manual item");
        assertThat(item.getManualDescription()).isEqualTo("Personal description");
        assertThat(item.getManualType()).isEqualTo("CUSTOM");
        assertThat(item.getCollectionStatus()).isEqualTo(CollectionItemStatus.OWNED);
        assertThat(item.getPhysicalCondition()).isEqualTo(PhysicalCondition.LIKE_NEW);
        assertThat(item.getUnitNumber()).isEqualTo("7");
        assertThat(item.getTotalLimitedUnits()).isEqualTo(25);
        assertThat(item.getNotes()).isEqualTo("Private note");
        assertThat(item.getAcquiredAt()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(ReflectionTestUtils.getField(item, "createdAt")).isNotNull();
        assertThat(ReflectionTestUtils.getField(item, "createdBy")).isEqualTo(42L);
        assertThat(item.isManual()).isTrue();
    }

    @Test
    void isManualRejectsLegacyEditorialAndIncoherentItems() {
        Collection collection = collection();

        CollectionItem legacy = CollectionItem.create(
                collection, null, null, null, CollectionEditorialReferenceSource.LEGACY,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );
        CollectionItem editorial = CollectionItem.create(
                collection, null, null, null, CollectionEditorialReferenceSource.MANUAL_EDITORIAL,
                CollectionItemStatus.OWNED, null, null, null, null, null, 42L
        );
        CollectionItem incoherent = CollectionItem.createManual(
                collection, "   ", null, null, CollectionItemStatus.OWNED,
                null, null, null, null, null, 42L
        );

        assertThat(legacy.isManual()).isFalse();
        assertThat(editorial.isManual()).isFalse();
        assertThat(incoherent.isManual()).isFalse();
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
}
