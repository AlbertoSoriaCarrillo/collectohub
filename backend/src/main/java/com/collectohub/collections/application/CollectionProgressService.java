package com.collectohub.collections.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.CatalogSeriesNotFoundException;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionSeriesProgressStatus;
import com.collectohub.collections.dto.CollectionSeriesProgressItemResponse;
import com.collectohub.collections.dto.CollectionSeriesProgressResponse;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.collections.infrastructure.CollectionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollectionProgressService {
    private static final Set<CollectionItemStatus> OWNERSHIP = EnumSet.of(CollectionItemStatus.OWNED, CollectionItemStatus.DUPLICATED, CollectionItemStatus.SELLABLE, CollectionItemStatus.TRADABLE);
    private final CollectionRepository collections;
    private final CollectionItemRepository collectionItems;
    private final CatalogSeriesRepository series;
    private final CatalogItemRepository catalogItems;

    public CollectionProgressService(CollectionRepository collections, CollectionItemRepository collectionItems, CatalogSeriesRepository series, CatalogItemRepository catalogItems) {
        this.collections = collections; this.collectionItems = collectionItems; this.series = series; this.catalogItems = catalogItems;
    }

    @Transactional(readOnly = true)
    public CollectionSeriesProgressResponse getSeriesProgress(AuthenticatedUser user, Long collectionId, Long seriesId) {
        Collection collection = collections.findByIdAndDeletedAtIsNull(collectionId).filter(Collection::isActive).orElseThrow(() -> new CollectionNotFoundException(collectionId));
        if (user == null || !collection.isOwnedBy(user.id())) throw new AccessDeniedException("User cannot read collection progress");
        CatalogSeries catalogSeries = series.findByIdAndDeletedAtIsNull(seriesId).filter(CatalogSeries::isPubliclyVisible).orElseThrow(() -> new CatalogSeriesNotFoundException(seriesId));
        List<CatalogItem> active = catalogItems.findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(seriesId, CatalogRecordStatus.ACTIVE).stream()
                .filter(CatalogItem::isPubliclyVisible).sorted(Comparator.comparing(CatalogItem::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(CatalogItem::getTitle, String.CASE_INSENSITIVE_ORDER).thenComparing(CatalogItem::getId)).toList();
        Map<Long, List<CollectionItem>> byCatalogItem = collectionItems.findProgressItemsByCollectionIdAndSeriesId(collectionId, seriesId).stream().collect(Collectors.groupingBy(item -> item.getCatalogItem().getId()));
        List<CollectionSeriesProgressItemResponse> result = active.stream().map(item -> response(item, byCatalogItem.getOrDefault(item.getId(), List.of()))).toList();
        int owned = (int) result.stream().filter(item -> item.calculatedStatus() == CollectionSeriesProgressStatus.OWNED).count();
        int wanted = (int) result.stream().filter(item -> item.calculatedStatus() == CollectionSeriesProgressStatus.WANTED).count();
        int missing = result.size() - owned - wanted;
        int percentage = result.isEmpty() ? 0 : Math.round(100.0f * owned / result.size());
        return new CollectionSeriesProgressResponse(collectionId, seriesId, catalogSeries.getTitle(), result.size(), owned, wanted, missing, percentage, List.copyOf(result));
    }

    private CollectionSeriesProgressItemResponse response(CatalogItem item, List<CollectionItem> entries) {
        List<CollectionItem> owned = entries.stream().filter(entry -> OWNERSHIP.contains(entry.getCollectionStatus())).toList();
        List<CollectionItem> wanted = entries.stream().filter(entry -> entry.getCollectionStatus() == CollectionItemStatus.WANTED).toList();
        boolean legacy = entries.stream().anyMatch(entry -> entry.getCollectionStatus() == CollectionItemStatus.MISSING);
        CollectionSeriesProgressStatus status = !owned.isEmpty() ? CollectionSeriesProgressStatus.OWNED : !wanted.isEmpty() ? CollectionSeriesProgressStatus.WANTED : CollectionSeriesProgressStatus.MISSING;
        return new CollectionSeriesProgressItemResponse(item.getId(), item.getTitle(), item.getSequenceLabel(), item.getSortOrder(), item.getFirstPublicationYear(), status,
                ids(owned), ids(wanted), editionIds(owned, wanted), legacy);
    }
    private List<Long> ids(List<CollectionItem> entries) { return entries.stream().map(CollectionItem::getId).distinct().sorted().toList(); }
    private List<Long> editionIds(List<CollectionItem> owned, List<CollectionItem> wanted) { return java.util.stream.Stream.concat(owned.stream(), wanted.stream()).map(CollectionItem::getCatalogItemEdition).filter(java.util.Objects::nonNull).map(edition -> edition.getId()).distinct().sorted().toList(); }
}
