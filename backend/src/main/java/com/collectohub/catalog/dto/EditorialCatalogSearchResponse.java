package com.collectohub.catalog.dto;

import java.util.List;

public record EditorialCatalogSearchResponse(
        List<EditorialCatalogSearchItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
