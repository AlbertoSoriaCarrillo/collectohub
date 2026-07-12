package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface MasterProductCatalogLinkRepository extends
        JpaRepository<MasterProductCatalogLink, Long>,
        JpaSpecificationExecutor<MasterProductCatalogLink> {

    Optional<MasterProductCatalogLink> findByIdAndDeletedAtIsNull(Long id);

    Optional<MasterProductCatalogLink> findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
            Long masterProductId,
            MasterProductCatalogLinkStatus status
    );

    Optional<MasterProductCatalogLink> findFirstByMasterProduct_IdAndLinkStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
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

    @Query(value = "select master_product_id::text as groupKey, master_product_id::text as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(catalog_item_id::text, ' | ' order by id) as recordLabels from master_product_catalog_links where deleted_at is null and link_status = 'VERIFIED' group by master_product_id having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findMultipleVerifiedGroups(int limit);

    @Query(value = "select concat(master_product_id, ':', catalog_item_id, ':', coalesce(catalog_item_edition_id, 0)) as groupKey, concat(master_product_id, ' / ', catalog_item_id) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(link_status, ' | ' order by id) as recordLabels from master_product_catalog_links where deleted_at is null group by master_product_id, catalog_item_id, coalesce(catalog_item_edition_id, 0) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findExactDuplicateGroups(int limit);
}
