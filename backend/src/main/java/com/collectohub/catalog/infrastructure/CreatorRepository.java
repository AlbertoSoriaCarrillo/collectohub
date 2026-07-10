package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.Creator;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface CreatorRepository extends JpaRepository<Creator, Long>, JpaSpecificationExecutor<Creator> {
    Optional<Creator> findByIdAndDeletedAtIsNull(Long id);
    Optional<Creator> findBySlugAndDeletedAtIsNull(String slug);
    boolean existsBySlugAndDeletedAtIsNull(String slug);
    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long id);
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long id);
}
