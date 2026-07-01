package com.collectohub.catalog.dto;

public record EditorialCatalogDetailResponse(
        CatalogSeriesResponse series,
        CatalogFranchiseResponse franchise,
        PublisherResponse primaryPublisher
) {
}
