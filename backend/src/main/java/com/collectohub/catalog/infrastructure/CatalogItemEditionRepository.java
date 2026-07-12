package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface CatalogItemEditionRepository extends
        JpaRepository<CatalogItemEdition, Long>,
        JpaSpecificationExecutor<CatalogItemEdition> {

    Optional<CatalogItemEdition> findByIdAndDeletedAtIsNull(Long id);

    List<CatalogItemEdition> findAllByIsbnAndDeletedAtIsNull(String isbn);

    List<CatalogItemEdition> findAllByEanAndDeletedAtIsNull(String ean);

    List<CatalogItemEdition> findAllByCatalogItem_IdAndRecordStatusAndDeletedAtIsNullOrderByPublicationYearAscIdAsc(
            Long itemId,
            CatalogRecordStatus recordStatus
    );

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
    @Query(value = "select lower(trim(isbn)) as groupKey, min(isbn) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(coalesce(edition_name, format), ' | ' order by id) as recordLabels from catalog_item_editions where deleted_at is null and nullif(trim(isbn), '') is not null group by lower(trim(isbn)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateIsbnGroups(int limit);
    @Query(value = "select lower(trim(ean)) as groupKey, min(ean) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(coalesce(edition_name, format), ' | ' order by id) as recordLabels from catalog_item_editions where deleted_at is null and nullif(trim(ean), '') is not null group by lower(trim(ean)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateEanGroups(int limit);
    @Query(value = "select concat(catalog_item_id, ':', lower(trim(edition_name))) as groupKey, min(edition_name) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(edition_name, ' | ' order by id) as recordLabels from catalog_item_editions where deleted_at is null and nullif(trim(edition_name), '') is not null group by catalog_item_id, lower(trim(edition_name)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateNameInItemGroups(int limit);
}
