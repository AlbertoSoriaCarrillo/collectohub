package com.collectohub.collections.dto;

import java.util.List;

public record CollectionSeriesProgressResponse(
        Long collectionId, Long seriesId, String seriesTitle, int totalCatalogItems,
        int ownedItems, int wantedItems, int missingItems, int completionPercentage,
        List<CollectionSeriesProgressItemResponse> items
) { }
