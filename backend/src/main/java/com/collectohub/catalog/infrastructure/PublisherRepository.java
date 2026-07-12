package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface PublisherRepository extends JpaRepository<Publisher, Long>, JpaSpecificationExecutor<Publisher> {

    Optional<Publisher> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long excludedId);
    @Query(value = "select lower(trim(name)) as groupKey, min(name) as displayValue, string_agg(id::text, ',' order by id) as recordIds, string_agg(name, ' | ' order by id) as recordLabels from publishers where deleted_at is null group by lower(trim(name)) having count(*) > 1 order by groupKey limit :limit", nativeQuery = true)
    List<EditorialDataQualityGroup> findDuplicateNameGroups(int limit);
}
