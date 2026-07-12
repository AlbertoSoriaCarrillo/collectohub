package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface CatalogFranchiseRepository extends
        JpaRepository<CatalogFranchise, Long>,
        JpaSpecificationExecutor<CatalogFranchise> {

    Optional<CatalogFranchise> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long excludedId);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long excludedId);
    @Query(value = "select lower(trim(name)) as groupKey, min(name) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(name, ' | ' order by id) as recordLabels from catalog_franchises where deleted_at is null group by lower(trim(name)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateNameGroups(int limit);
    @Query(value = "select slug as groupKey, min(slug) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(name, ' | ' order by id) as recordLabels from catalog_franchises where deleted_at is null group by slug having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateSlugGroups(int limit);
}
