package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemRelationship;

public record CatalogItemRelationshipResponse(
        Long id,
        Long sourceCatalogItemId,
        String sourceCatalogItemTitle,
        Long sourceCatalogSeriesId,
        String sourceCatalogSeriesTitle,
        Long targetCatalogItemId,
        String targetCatalogItemTitle,
        Long targetCatalogSeriesId,
        String targetCatalogSeriesTitle,
        String relationshipType,
        Integer relationshipOrder,
        String description,
        String recordStatus,
        String direction
) {
    public static CatalogItemRelationshipResponse from(CatalogItemRelationship relationship, Long perspectiveItemId) {
        CatalogItem source = relationship.getSourceCatalogItem();
        CatalogItem target = relationship.getTargetCatalogItem();
        return new CatalogItemRelationshipResponse(
                relationship.getId(), source.getId(), source.getTitle(), source.getSeries().getId(),
                source.getSeries().getTitle(), target.getId(), target.getTitle(), target.getSeries().getId(),
                target.getSeries().getTitle(), relationship.getRelationshipType().name(),
                relationship.getRelationshipOrder(), relationship.getDescription(),
                relationship.getRecordStatus().name(), source.getId().equals(perspectiveItemId) ? "OUTGOING" : "INCOMING"
        );
    }
}
