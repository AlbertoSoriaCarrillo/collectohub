package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.Creator;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface CreatorRepository extends JpaRepository<Creator, Long>, JpaSpecificationExecutor<Creator> {
    Optional<Creator> findByIdAndDeletedAtIsNull(Long id);
    Optional<Creator> findBySlugAndDeletedAtIsNull(String slug);
    boolean existsBySlugAndDeletedAtIsNull(String slug);
    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long id);
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long id);
    @Query(value = "select lower(trim(name)) as groupKey, min(name) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(name, ' | ' order by id) as recordLabels from creators where deleted_at is null group by lower(trim(name)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateNameGroups(int limit);
    @Query(value = "select slug as groupKey, min(slug) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(name, ' | ' order by id) as recordLabels from creators where deleted_at is null group by slug having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateSlugGroups(int limit);
}
