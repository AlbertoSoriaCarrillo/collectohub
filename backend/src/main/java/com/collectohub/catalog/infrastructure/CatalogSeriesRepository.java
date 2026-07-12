package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeriesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface CatalogSeriesRepository extends
        JpaRepository<CatalogSeries, Long>,
        JpaSpecificationExecutor<CatalogSeries> {

    Optional<CatalogSeries> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByFranchise_IdAndRecordStatusAndDeletedAtIsNull(
            Long franchiseId,
            CatalogRecordStatus recordStatus
    );

    boolean existsByPrimaryPublisher_IdAndRecordStatusAndDeletedAtIsNull(
            Long publisherId,
            CatalogRecordStatus recordStatus
    );

    boolean existsByTitleIgnoreCaseAndTypeAndFranchise_IdAndDeletedAtIsNull(
            String title,
            CatalogSeriesType type,
            Long franchiseId
    );

    boolean existsByTitleIgnoreCaseAndTypeAndFranchise_IdAndDeletedAtIsNullAndIdNot(
            String title,
            CatalogSeriesType type,
            Long franchiseId,
            Long excludedId
    );

    boolean existsByTitleIgnoreCaseAndTypeAndFranchiseIsNullAndDeletedAtIsNull(
            String title,
            CatalogSeriesType type
    );

    boolean existsByTitleIgnoreCaseAndTypeAndFranchiseIsNullAndDeletedAtIsNullAndIdNot(
            String title,
            CatalogSeriesType type,
            Long excludedId
    );
    @Query(value = "select concat(franchise_id, ':', lower(trim(title))) as groupKey, min(title) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(title, ' | ' order by id) as recordLabels from catalog_series where deleted_at is null and franchise_id is not null group by franchise_id, lower(trim(title)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateTitleInFranchiseGroups(int limit);
}
