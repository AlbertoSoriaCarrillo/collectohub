package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MasterProductCatalogLinkRepository extends
        JpaRepository<MasterProductCatalogLink, Long>,
        JpaSpecificationExecutor<MasterProductCatalogLink> {

    Optional<MasterProductCatalogLink> findByIdAndDeletedAtIsNull(Long id);

    Optional<MasterProductCatalogLink> findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
            Long masterProductId,
            MasterProductCatalogLinkStatus status
    );

    boolean existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
            Long masterProductId,
            MasterProductCatalogLinkStatus status
    );

    boolean existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNullAndIdNot(
            Long masterProductId,
            MasterProductCatalogLinkStatus status,
            Long excludedId
    );

    boolean existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEdition_IdAndLinkStatusAndDeletedAtIsNull(
            Long masterProductId,
            Long catalogItemId,
            Long editionId,
            MasterProductCatalogLinkStatus status
    );

    boolean existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEdition_IdAndLinkStatusAndDeletedAtIsNullAndIdNot(
            Long masterProductId,
            Long catalogItemId,
            Long editionId,
            MasterProductCatalogLinkStatus status,
            Long excludedId
    );

    boolean existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEditionIsNullAndLinkStatusAndDeletedAtIsNull(
            Long masterProductId,
            Long catalogItemId,
            MasterProductCatalogLinkStatus status
    );

    boolean existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEditionIsNullAndLinkStatusAndDeletedAtIsNullAndIdNot(
            Long masterProductId,
            Long catalogItemId,
            MasterProductCatalogLinkStatus status,
            Long excludedId
    );
}
