package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemCreator;

public record CatalogItemCreatorResponse(Long id, Long catalogItemId, Long creatorId,
                                         String creatorName, String creatorSlug, String creditRole,
                                         Integer creditOrder, String creditLabel) {
    public static CatalogItemCreatorResponse from(CatalogItemCreator credit) {
        return new CatalogItemCreatorResponse(credit.getId(), credit.getCatalogItem().getId(),
                credit.getCreator().getId(), credit.getCreator().getName(), credit.getCreator().getSlug(),
                credit.getCreditRole().name(), credit.getCreditOrder(), credit.getCreditLabel());
    }
}
