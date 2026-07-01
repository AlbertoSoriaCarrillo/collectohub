package com.collectohub.catalog.dto;

import java.util.List;

public record EditorialCatalogSeriesDetailResponse(
        EditorialCatalogDetailResponse catalog,
        List<EditorialCatalogItemDetailResponse> items
) {
}
