package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeriesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

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
}
