package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CatalogItemEditionRepository extends
        JpaRepository<CatalogItemEdition, Long>,
        JpaSpecificationExecutor<CatalogItemEdition> {

    Optional<CatalogItemEdition> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByCatalogItem_IdAndRecordStatusAndDeletedAtIsNull(
            Long itemId,
            CatalogRecordStatus recordStatus
    );

    boolean existsByPublisher_IdAndRecordStatusAndDeletedAtIsNull(
            Long publisherId,
            CatalogRecordStatus recordStatus
    );

    boolean existsByIsbnAndDeletedAtIsNull(String isbn);

    boolean existsByIsbnAndDeletedAtIsNullAndIdNot(String isbn, Long excludedId);

    boolean existsByEanAndDeletedAtIsNull(String ean);

    boolean existsByEanAndDeletedAtIsNullAndIdNot(String ean, Long excludedId);
}
