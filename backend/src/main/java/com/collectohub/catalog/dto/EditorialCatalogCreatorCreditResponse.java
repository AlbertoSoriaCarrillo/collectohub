package com.collectohub.catalog.dto;

public record EditorialCatalogCreatorCreditResponse(
        Long id,
        Long creatorId,
        String creatorName,
        String creatorSlug,
        String creditRole,
        Integer creditOrder,
        String creditLabel
) {
    public static EditorialCatalogCreatorCreditResponse from(CatalogItemCreatorResponse credit) {
        return new EditorialCatalogCreatorCreditResponse(
                credit.id(), credit.creatorId(), credit.creatorName(), credit.creatorSlug(),
                credit.creditRole(), credit.creditOrder(), credit.creditLabel());
    }
}
