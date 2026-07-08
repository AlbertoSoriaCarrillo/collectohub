package com.collectohub.catalog.infrastructure;

import com.collectohub.catalog.domain.CatalogItemRelationship;
import com.collectohub.catalog.domain.CatalogItemRelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogItemRelationshipRepository extends JpaRepository<CatalogItemRelationship, Long> {
    Optional<CatalogItemRelationship> findByIdAndDeletedAtIsNull(Long id);
    boolean existsBySourceCatalogItem_IdAndTargetCatalogItem_IdAndRelationshipTypeAndDeletedAtIsNull(
            Long sourceId, Long targetId, CatalogItemRelationshipType type);
    boolean existsBySourceCatalogItem_IdAndTargetCatalogItem_IdAndRelationshipTypeAndDeletedAtIsNullAndIdNot(
            Long sourceId, Long targetId, CatalogItemRelationshipType type, Long id);
    List<CatalogItemRelationship> findBySourceCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(
            Long sourceId);
    List<CatalogItemRelationship> findByTargetCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(
            Long targetId);
}
