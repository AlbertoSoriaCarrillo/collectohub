package com.collectohub.collections.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionSeriesProgressStatus;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.collections.infrastructure.CollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CollectionProgressServiceTest {
    @Mock private CollectionRepository collections;
    @Mock private CollectionItemRepository collectionItems;
    @Mock private CatalogSeriesRepository series;
    @Mock private CatalogItemRepository catalogItems;
    @Mock private Collection collection;
    @Mock private CatalogSeries catalogSeries;
    @Mock private AuthenticatedUser owner;
    private CollectionProgressService service;

    @BeforeEach void setUp() {
        service = new CollectionProgressService(collections, collectionItems, series, catalogItems);
        when(owner.id()).thenReturn(1L); when(collection.isActive()).thenReturn(true); when(collection.isOwnedBy(1L)).thenReturn(true);
        when(collections.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(collection));
        when(catalogSeries.isPubliclyVisible()).thenReturn(true); when(catalogSeries.getTitle()).thenReturn("Series");
        when(series.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(catalogSeries));
    }

    @Test void ownerGetsCalculatedMissingWhenCollectionHasNoEntries() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        when(catalogItems.findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(20L, CatalogRecordStatus.ACTIVE)).thenReturn(List.of(item));
        when(collectionItems.findProgressItemsByCollectionIdAndSeriesId(10L, 20L)).thenReturn(List.of());
        var response = service.getSeriesProgress(owner, 10L, 20L);
        assertThat(response.missingItems()).isEqualTo(1);
        assertThat(response.items().getFirst().calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.MISSING);
        verify(collectionItems, never()).save(any());
    }

    @Test void ownershipTakesPrecedenceOverWantedAndLegacyMissing() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        CollectionItem owned = entry(1L, item, CollectionItemStatus.OWNED);
        CollectionItem wanted = entry(2L, item, CollectionItemStatus.WANTED);
        CollectionItem legacyMissing = entry(3L, item, CollectionItemStatus.MISSING);
        when(catalogItems.findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(20L, CatalogRecordStatus.ACTIVE)).thenReturn(List.of(item));
        when(collectionItems.findProgressItemsByCollectionIdAndSeriesId(10L, 20L))
                .thenReturn(List.of(owned, wanted, legacyMissing));
        var result = service.getSeriesProgress(owner, 10L, 20L).items().getFirst();
        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.OWNED);
        assertThat(result.legacyStatusWarning()).isTrue();
        assertThat(result.ownedCollectionItemIds()).containsExactly(1L);
        assertThat(result.wantedCollectionItemIds()).containsExactly(2L);
    }

    @Test void foreignUserIsDeniedBeforeCatalogQueries() {
        AuthenticatedUser other = mock(AuthenticatedUser.class); when(other.id()).thenReturn(2L); when(collection.isOwnedBy(2L)).thenReturn(false);
        assertThatThrownBy(() -> service.getSeriesProgress(other, 10L, 20L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(series, catalogItems, collectionItems);
    }

    private CatalogItem catalogItem(Long id, String title, BigDecimal order) { CatalogItem item = mock(CatalogItem.class); when(item.getId()).thenReturn(id); when(item.getTitle()).thenReturn(title); when(item.getSortOrder()).thenReturn(order); when(item.isPubliclyVisible()).thenReturn(true); return item; }
    private CollectionItem entry(Long id, CatalogItem catalogItem, CollectionItemStatus status) { CollectionItem item = mock(CollectionItem.class); when(item.getId()).thenReturn(id); when(item.getCatalogItem()).thenReturn(catalogItem); when(item.getCatalogItemEdition()).thenReturn(null); when(item.getCollectionStatus()).thenReturn(status); return item; }
}
