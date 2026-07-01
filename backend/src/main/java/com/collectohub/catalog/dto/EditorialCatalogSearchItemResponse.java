package com.collectohub.catalog.dto;

public record EditorialCatalogSearchItemResponse(
        String resultType,
        Long seriesId,
        String seriesTitle,
        Long itemId,
        String itemTitle,
        Long editionId,
        String editionName,
        String publisherName,
        String franchiseName,
        String type,
        String language,
        String country,
        Integer publicationYear,
        String coverImageUrl,
        Long linkedMasterProductId,
        String linkedMasterProductName
) {
}
