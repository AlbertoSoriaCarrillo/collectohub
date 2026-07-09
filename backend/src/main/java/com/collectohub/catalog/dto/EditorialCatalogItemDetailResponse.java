package com.collectohub.catalog.dto;

import java.util.List;

public record EditorialCatalogItemDetailResponse(
        EditorialCatalogDetailResponse catalog,
        CatalogItemResponse item,
        List<CatalogItemEditionResponse> editions,
        List<EditorialCatalogCreatorCreditResponse> creators,
        List<CatalogItemRelationshipResponse> relationships
) {
}
