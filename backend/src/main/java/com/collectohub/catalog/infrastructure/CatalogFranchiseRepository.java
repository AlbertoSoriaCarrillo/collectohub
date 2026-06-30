package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CatalogFranchiseRepository extends
        JpaRepository<CatalogFranchise, Long>,
        JpaSpecificationExecutor<CatalogFranchise> {

    Optional<CatalogFranchise> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, Long excludedId);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long excludedId);
}
