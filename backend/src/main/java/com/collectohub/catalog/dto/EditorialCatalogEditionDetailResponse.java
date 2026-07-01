package com.collectohub.catalog.dto;

public record EditorialCatalogEditionDetailResponse(
        EditorialCatalogDetailResponse catalog,
        CatalogItemResponse item,
        CatalogItemEditionResponse edition,
        PublisherResponse publisher
) {
}
