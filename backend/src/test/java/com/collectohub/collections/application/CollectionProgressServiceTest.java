package com.collectohub.collections.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogSeriesNotFoundException;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionSeriesProgressStatus;
import com.collectohub.collections.dto.CollectionSeriesProgressResponse;
import com.collectohub.collections.dto.CollectionSeriesProgressSummaryResponse;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.collections.infrastructure.CollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionProgressServiceTest {

    private static final Long COLLECTION_ID = 10L;
    private static final Long SERIES_ID = 20L;
    private static final Long OWNER_ID = 1L;

    @Mock
    private CollectionRepository collections;

    @Mock
    private CollectionItemRepository collectionItems;

    @Mock
    private CatalogSeriesRepository series;

    @Mock
    private CatalogItemRepository catalogItems;

    private CollectionProgressService service;

    @BeforeEach
    void setUp() {
        service = new CollectionProgressService(collections, collectionItems, series, catalogItems);
    }

    @Test
    void ownerGetsSeriesProgress() {
        CatalogItem item = catalogItem(100L, "Volume 1", BigDecimal.ONE);
        CollectionSeriesProgressResponse response = progress(List.of(item), List.of());

        assertThat(response.collectionId()).isEqualTo(COLLECTION_ID);
        assertThat(response.seriesId()).isEqualTo(SERIES_ID);
        assertThat(response.seriesTitle()).isEqualTo("Series");
        assertThat(response.totalCatalogItems()).isEqualTo(1);
    }

    @Test
    void anonymousServiceCallIsDenied() {
        stubCollectionFor(null, false);

        assertThatThrownBy(() -> service.getSeriesProgress(null, COLLECTION_ID, SERIES_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(series, catalogItems, collectionItems);
    }

    @Test
    void foreignUserIsDeniedBeforeCatalogQueries() {
        assertForeignUserDenied(2L);
    }

    @Test
    void foreignAdminIsDeniedBeforeCatalogQueries() {
        assertForeignUserDenied(3L);
    }

    @Test
    void foreignEditorialAdminIsDeniedBeforeCatalogQueries() {
        assertForeignUserDenied(4L);
    }

    @Test
    void missingCollectionReturnsNotFound() {
        when(collections.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSeriesProgress(
                mock(AuthenticatedUser.class),
                COLLECTION_ID,
                SERIES_ID
        ))
                .isInstanceOf(CollectionNotFoundException.class);
        verifyNoInteractions(series, catalogItems, collectionItems);
    }

    @Test
    void inactiveCollectionReturnsNotFound() {
        Collection collection = mock(Collection.class);
        when(collection.isActive()).thenReturn(false);
        when(collections.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.of(collection));

        assertThatThrownBy(() -> service.getSeriesProgress(
                mock(AuthenticatedUser.class),
                COLLECTION_ID,
                SERIES_ID
        ))
                .isInstanceOf(CollectionNotFoundException.class);
        verifyNoInteractions(series, catalogItems, collectionItems);
    }

    @Test
    void missingSeriesReturnsNotFound() {
        stubCollectionFor(OWNER_ID, true);
        when(series.findByIdAndDeletedAtIsNull(SERIES_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSeriesProgress(user(OWNER_ID), COLLECTION_ID, SERIES_ID))
                .isInstanceOf(CatalogSeriesNotFoundException.class);
        verifyNoInteractions(catalogItems, collectionItems);
    }

    @Test
    void nonPublicSeriesReturnsNotFound() {
        stubCollectionFor(OWNER_ID, true);
        CatalogSeries catalogSeries = mock(CatalogSeries.class);
        when(catalogSeries.isPubliclyVisible()).thenReturn(false);
        when(series.findByIdAndDeletedAtIsNull(SERIES_ID)).thenReturn(Optional.of(catalogSeries));

        assertThatThrownBy(() -> service.getSeriesProgress(user(OWNER_ID), COLLECTION_ID, SERIES_ID))
                .isInstanceOf(CatalogSeriesNotFoundException.class);
        verifyNoInteractions(catalogItems, collectionItems);
    }

    @Test
    void emptySeriesReturnsZeroSummary() {
        CollectionSeriesProgressResponse response = progress(List.of(), List.of());

        assertThat(response.totalCatalogItems()).isZero();
        assertThat(response.ownedItems()).isZero();
        assertThat(response.wantedItems()).isZero();
        assertThat(response.missingItems()).isZero();
        assertThat(response.completionPercentage()).isZero();
        assertThat(response.items()).isEmpty();
    }

    @Test
    void collectionWithoutEntriesCalculatesAllMissing() {
        CollectionSeriesProgressResponse response = progress(
                List.of(catalogItem(100L, "A", BigDecimal.ONE), catalogItem(101L, "B", BigDecimal.TWO)),
                List.of()
        );

        assertThat(response.missingItems()).isEqualTo(2);
        assertThat(response.items()).allMatch(item -> item.calculatedStatus() == CollectionSeriesProgressStatus.MISSING);
    }

    @Test
    void wantedEntryCalculatesWanted() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(List.of(item), List.of(entry(5L, item, CollectionItemStatus.WANTED))).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.WANTED);
        assertThat(result.wantedCollectionItemIds()).containsExactly(5L);
    }

    @Test
    void ownedEntryCalculatesOwned() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(List.of(item), List.of(entry(5L, item, CollectionItemStatus.OWNED))).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.OWNED);
        assertThat(result.ownedCollectionItemIds()).containsExactly(5L);
    }

    @Test
    void duplicatedSellableAndTradableCountAsOwned() {
        CatalogItem duplicated = catalogItem(100L, "A", BigDecimal.ONE);
        CatalogItem sellable = catalogItem(101L, "B", BigDecimal.TWO);
        CatalogItem tradable = catalogItem(102L, "C", BigDecimal.valueOf(3));
        CollectionSeriesProgressResponse response = progress(
                List.of(duplicated, sellable, tradable),
                List.of(
                        entry(1L, duplicated, CollectionItemStatus.DUPLICATED),
                        entry(2L, sellable, CollectionItemStatus.SELLABLE),
                        entry(3L, tradable, CollectionItemStatus.TRADABLE)
                )
        );

        assertThat(response.ownedItems()).isEqualTo(3);
        assertThat(response.items()).allMatch(item -> item.calculatedStatus() == CollectionSeriesProgressStatus.OWNED);
    }

    @Test
    void ownedTakesPrecedenceOverWanted() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(entry(2L, item, CollectionItemStatus.WANTED), entry(1L, item, CollectionItemStatus.OWNED))
        ).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.OWNED);
        assertThat(result.ownedCollectionItemIds()).containsExactly(1L);
        assertThat(result.wantedCollectionItemIds()).containsExactly(2L);
    }

    @Test
    void wantedTakesPrecedenceOverCalculatedMissing() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        CollectionSeriesProgressResponse response = progress(
                List.of(item),
                List.of(entry(2L, item, CollectionItemStatus.WANTED))
        );

        assertThat(response.wantedItems()).isEqualTo(1);
        assertThat(response.missingItems()).isZero();
    }

    @Test
    void legacyMissingAddsWarningWithoutIds() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(entry(9L, item, CollectionItemStatus.MISSING))
        ).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.MISSING);
        assertThat(result.legacyStatusWarning()).isTrue();
        assertThat(result.ownedCollectionItemIds()).isEmpty();
        assertThat(result.wantedCollectionItemIds()).isEmpty();
        assertThat(result.selectedEditionIds()).isEmpty();
    }

    @Test
    void ownedAndLegacyMissingKeepsOwnedAndWarning() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(entry(1L, item, CollectionItemStatus.OWNED), entry(9L, item, CollectionItemStatus.MISSING))
        ).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.OWNED);
        assertThat(result.legacyStatusWarning()).isTrue();
        assertThat(result.ownedCollectionItemIds()).containsExactly(1L);
    }

    @Test
    void wantedAndLegacyMissingKeepsWantedAndWarning() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(entry(2L, item, CollectionItemStatus.WANTED), entry(9L, item, CollectionItemStatus.MISSING))
        ).items().getFirst();

        assertThat(result.calculatedStatus()).isEqualTo(CollectionSeriesProgressStatus.WANTED);
        assertThat(result.legacyStatusWarning()).isTrue();
        assertThat(result.wantedCollectionItemIds()).containsExactly(2L);
    }

    @Test
    void multipleEntriesAreAggregatedByCatalogItem() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        CollectionSeriesProgressResponse response = progress(
                List.of(item),
                List.of(entry(1L, item, CollectionItemStatus.OWNED), entry(2L, item, CollectionItemStatus.DUPLICATED))
        );

        assertThat(response.totalCatalogItems()).isEqualTo(1);
        assertThat(response.ownedItems()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void collectionItemIdsAreUniqueAndSorted() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        CollectionItem first = entry(3L, item, CollectionItemStatus.OWNED);
        CollectionItem duplicateId = entry(3L, item, CollectionItemStatus.DUPLICATED);
        CollectionItem second = entry(1L, item, CollectionItemStatus.SELLABLE);
        CollectionItem wanted = entry(5L, item, CollectionItemStatus.WANTED);
        var result = progress(List.of(item), List.of(first, duplicateId, second, wanted)).items().getFirst();

        assertThat(result.ownedCollectionItemIds()).containsExactly(1L, 3L);
        assertThat(result.wantedCollectionItemIds()).containsExactly(5L);
    }

    @Test
    void selectedEditionIdsAreUniqueAndSorted() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(
                        entry(1L, item, CollectionItemStatus.OWNED, edition(8L)),
                        entry(2L, item, CollectionItemStatus.WANTED, edition(4L)),
                        entry(3L, item, CollectionItemStatus.DUPLICATED, edition(8L)),
                        entry(4L, item, CollectionItemStatus.OWNED)
                )
        ).items().getFirst();

        assertThat(result.selectedEditionIds()).containsExactly(4L, 8L);
    }

    @Test
    void legacyMissingEditionIsExcluded() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        var result = progress(
                List.of(item),
                List.of(
                        entry(1L, item, CollectionItemStatus.OWNED, edition(4L)),
                        entry(9L, item, CollectionItemStatus.MISSING, mock(CatalogItemEdition.class))
                )
        ).items().getFirst();

        assertThat(result.selectedEditionIds()).containsExactly(4L);
    }

    @Test
    void itemsUseDeterministicOrder() {
        CatalogItem nullOrder = catalogItem(103L, "Zero", null);
        CatalogItem titleB = catalogItem(102L, "beta", BigDecimal.ONE);
        CatalogItem titleAHighId = catalogItem(104L, "Alpha", BigDecimal.ONE);
        CatalogItem titleALowId = catalogItem(101L, "alpha", BigDecimal.ONE);
        CollectionSeriesProgressResponse response = progress(
                List.of(nullOrder, titleB, titleAHighId, titleALowId),
                List.of()
        );

        assertThat(response.items()).extracting(item -> item.catalogItemId())
                .containsExactly(101L, 104L, 102L, 103L);
    }

    @Test
    void percentageRoundsToNearestInteger() {
        CatalogItem one = catalogItem(100L, "A", BigDecimal.ONE);
        CatalogItem two = catalogItem(101L, "B", BigDecimal.TWO);
        CatalogItem three = catalogItem(102L, "C", BigDecimal.valueOf(3));
        CollectionSeriesProgressResponse response = progress(
                List.of(one, two, three),
                List.of(entry(1L, one, CollectionItemStatus.OWNED), entry(2L, two, CollectionItemStatus.OWNED))
        );

        assertThat(response.completionPercentage()).isEqualTo(67);
        assertThat(response.ownedItems() + response.wantedItems() + response.missingItems())
                .isEqualTo(response.totalCatalogItems());
    }

    @Test
    void calculationNeverPersists() {
        CatalogItem item = catalogItem(100L, "A", BigDecimal.ONE);
        progress(List.of(item), List.of());

        verify(collectionItems).findProgressItemsByCollectionIdAndSeriesId(COLLECTION_ID, SERIES_ID);
        verify(collectionItems, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ownerGetsAllParticipatingSeriesProgressInOneBatch() {
        stubCollectionFor(OWNER_ID, true);
        CatalogSeries alpha = catalogSeries(20L, "Alpha", true);
        CatalogSeries beta = catalogSeries(21L, "beta", true);
        CatalogItem alphaOne = catalogItem(100L, "Volume 1", BigDecimal.ONE, alpha);
        CatalogItem alphaTwo = catalogItem(101L, "Volume 2", BigDecimal.TWO, alpha);
        CatalogItem betaOne = catalogItem(102L, "Volume 1", BigDecimal.ONE, beta);
        List<CollectionItem> entries = List.of(
                entry(1L, alphaOne, CollectionItemStatus.OWNED),
                entry(2L, betaOne, CollectionItemStatus.WANTED)
        );
        when(collectionItems.findProgressItemsByCollectionId(COLLECTION_ID)).thenReturn(entries);
        when(catalogItems.findActiveItemsBySeriesIds(Set.of(20L, 21L), CatalogRecordStatus.ACTIVE))
                .thenReturn(List.of(betaOne, alphaTwo, alphaOne));

        List<CollectionSeriesProgressSummaryResponse> response = service.getSeriesProgressSummary(
                user(OWNER_ID), COLLECTION_ID
        );

        assertThat(response).extracting(CollectionSeriesProgressSummaryResponse::seriesId)
                .containsExactly(20L, 21L);
        assertThat(response.getFirst())
                .extracting(
                        CollectionSeriesProgressSummaryResponse::totalCatalogItems,
                        CollectionSeriesProgressSummaryResponse::ownedItems,
                        CollectionSeriesProgressSummaryResponse::wantedItems,
                        CollectionSeriesProgressSummaryResponse::missingItems,
                        CollectionSeriesProgressSummaryResponse::completionPercentage
                )
                .containsExactly(2, 1, 0, 1, 50);
        assertThat(response.get(1))
                .extracting(
                        CollectionSeriesProgressSummaryResponse::totalCatalogItems,
                        CollectionSeriesProgressSummaryResponse::ownedItems,
                        CollectionSeriesProgressSummaryResponse::wantedItems,
                        CollectionSeriesProgressSummaryResponse::missingItems,
                        CollectionSeriesProgressSummaryResponse::completionPercentage
                )
                .containsExactly(1, 0, 1, 0, 0);
        verify(collectionItems).findProgressItemsByCollectionId(COLLECTION_ID);
        verify(catalogItems).findActiveItemsBySeriesIds(Set.of(20L, 21L), CatalogRecordStatus.ACTIVE);
        verifyNoInteractions(series);
    }

    @Test
    void allSeriesProgressIgnoresArchivedSeriesAndReturnsEmptyWithoutCatalogQuery() {
        stubCollectionFor(OWNER_ID, true);
        CatalogSeries archived = catalogSeries(30L, "Archived", false);
        CatalogItem item = catalogItem(110L, "Volume", BigDecimal.ONE, archived);
        CollectionItem archivedEntry = entry(1L, item, CollectionItemStatus.OWNED);
        when(collectionItems.findProgressItemsByCollectionId(COLLECTION_ID))
                .thenReturn(List.of(archivedEntry));

        List<CollectionSeriesProgressSummaryResponse> response = service.getSeriesProgressSummary(
                user(OWNER_ID), COLLECTION_ID
        );

        assertThat(response).isEmpty();
        verifyNoInteractions(catalogItems, series);
    }

    @Test
    void allSeriesProgressDeniesForeignUserBeforeLoadingEntries() {
        stubCollectionFor(2L, false);

        assertThatThrownBy(() -> service.getSeriesProgressSummary(user(2L), COLLECTION_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(collectionItems, catalogItems, series);
    }

    private CollectionSeriesProgressResponse progress(
            List<CatalogItem> activeCatalogItems,
            List<CollectionItem> entries
    ) {
        stubCollectionFor(OWNER_ID, true);
        stubPublicSeries();
        when(catalogItems.findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(
                SERIES_ID,
                CatalogRecordStatus.ACTIVE
        )).thenReturn(activeCatalogItems);
        when(collectionItems.findProgressItemsByCollectionIdAndSeriesId(COLLECTION_ID, SERIES_ID))
                .thenReturn(entries);
        return service.getSeriesProgress(user(OWNER_ID), COLLECTION_ID, SERIES_ID);
    }

    private void stubCollectionFor(Long userId, boolean owned) {
        Collection collection = mock(Collection.class);
        when(collection.isActive()).thenReturn(true);
        if (userId != null) {
            when(collection.isOwnedBy(userId)).thenReturn(owned);
        }
        when(collections.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.of(collection));
    }

    private void stubPublicSeries() {
        CatalogSeries catalogSeries = mock(CatalogSeries.class);
        when(catalogSeries.isPubliclyVisible()).thenReturn(true);
        when(catalogSeries.getTitle()).thenReturn("Series");
        when(series.findByIdAndDeletedAtIsNull(SERIES_ID)).thenReturn(Optional.of(catalogSeries));
    }

    private void assertForeignUserDenied(Long userId) {
        stubCollectionFor(userId, false);

        assertThatThrownBy(() -> service.getSeriesProgress(user(userId), COLLECTION_ID, SERIES_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(series, catalogItems, collectionItems);
    }

    private AuthenticatedUser user(Long id) {
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(user.id()).thenReturn(id);
        return user;
    }

    private CatalogItem catalogItem(Long id, String title, BigDecimal sortOrder) {
        CatalogItem item = mock(CatalogItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getTitle()).thenReturn(title);
        lenient().when(item.getSortOrder()).thenReturn(sortOrder);
        lenient().when(item.isPubliclyVisible()).thenReturn(true);
        return item;
    }

    private CatalogSeries catalogSeries(Long id, String title, boolean visible) {
        CatalogSeries catalogSeries = mock(CatalogSeries.class);
        lenient().when(catalogSeries.getId()).thenReturn(id);
        lenient().when(catalogSeries.getTitle()).thenReturn(title);
        when(catalogSeries.isPubliclyVisible()).thenReturn(visible);
        return catalogSeries;
    }

    private CatalogItem catalogItem(Long id, String title, BigDecimal sortOrder, CatalogSeries catalogSeries) {
        CatalogItem item = catalogItem(id, title, sortOrder);
        lenient().when(item.getSeries()).thenReturn(catalogSeries);
        return item;
    }

    private CatalogItemEdition edition(Long id) {
        CatalogItemEdition edition = mock(CatalogItemEdition.class);
        when(edition.getId()).thenReturn(id);
        return edition;
    }

    private CollectionItem entry(Long id, CatalogItem catalogItem, CollectionItemStatus status) {
        return entry(id, catalogItem, status, null);
    }

    private CollectionItem entry(
            Long id,
            CatalogItem catalogItem,
            CollectionItemStatus status,
            CatalogItemEdition edition
    ) {
        CollectionItem item = mock(CollectionItem.class);
        lenient().when(item.getCatalogItem()).thenReturn(catalogItem);
        lenient().when(item.getCollectionStatus()).thenReturn(status);
        if (status != CollectionItemStatus.MISSING) {
            lenient().when(item.getId()).thenReturn(id);
            lenient().when(item.getCatalogItemEdition()).thenReturn(edition);
        }
        return item;
    }
}
