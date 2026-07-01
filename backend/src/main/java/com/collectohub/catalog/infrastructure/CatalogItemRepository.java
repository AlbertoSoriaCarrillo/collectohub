package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface CatalogItemRepository extends
        JpaRepository<CatalogItem, Long>,
        JpaSpecificationExecutor<CatalogItem> {

    Optional<CatalogItem> findByIdAndDeletedAtIsNull(Long id);

    List<CatalogItem> findAllByTitleIgnoreCaseAndDeletedAtIsNull(String title);

    List<CatalogItem> findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(
            Long seriesId,
            CatalogRecordStatus recordStatus
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
}
