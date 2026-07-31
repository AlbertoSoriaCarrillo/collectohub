package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CatalogItemRepository extends
        JpaRepository<CatalogItem, Long>,
        JpaSpecificationExecutor<CatalogItem> {

    Optional<CatalogItem> findByIdAndDeletedAtIsNull(Long id);

    List<CatalogItem> findAllByTitleIgnoreCaseAndDeletedAtIsNull(String title);

    List<CatalogItem> findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(
            Long seriesId,
            CatalogRecordStatus recordStatus
    );

    @Query("""
            select item from CatalogItem item
            join fetch item.series series
            where series.id in :seriesIds
              and series.recordStatus = :recordStatus and series.deletedAt is null
              and item.recordStatus = :recordStatus and item.deletedAt is null
            """)
    List<CatalogItem> findActiveItemsBySeriesIds(
            @Param("seriesIds") Set<Long> seriesIds,
            @Param("recordStatus") CatalogRecordStatus recordStatus
    );

    boolean existsBySeries_IdAndRecordStatusAndDeletedAtIsNull(Long seriesId, CatalogRecordStatus recordStatus);

    boolean existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIgnoreCaseAndDeletedAtIsNull(
            Long seriesId,
            String title,
            String sequenceLabel
    );

    boolean existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIgnoreCaseAndDeletedAtIsNullAndIdNot(
            Long seriesId,
            String title,
            String sequenceLabel,
            Long excludedId
    );

    boolean existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIsNullAndDeletedAtIsNull(
            Long seriesId,
            String title
    );

    boolean existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIsNullAndDeletedAtIsNullAndIdNot(
            Long seriesId,
            String title,
            Long excludedId
    );
    @Query(value = "select concat(series_id, ':', lower(trim(title))) as groupKey, min(title) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(title, ' | ' order by id) as recordLabels from catalog_items where deleted_at is null group by series_id, lower(trim(title)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateTitleInSeriesGroups(int limit);
    @Query(value = "select concat(series_id, ':', lower(trim(sequence_label))) as groupKey, min(sequence_label) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(title, ' | ' order by id) as recordLabels from catalog_items where deleted_at is null and nullif(trim(sequence_label), '') is not null group by series_id, lower(trim(sequence_label)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateSequenceInSeriesGroups(int limit);
}
