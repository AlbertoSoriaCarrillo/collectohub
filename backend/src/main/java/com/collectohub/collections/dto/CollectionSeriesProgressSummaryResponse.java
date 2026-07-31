package com.collectohub.collections.dto;

public record CollectionSeriesProgressSummaryResponse(
        Long seriesId,
        String seriesTitle,
        int totalCatalogItems,
        int ownedItems,
        int wantedItems,
        int missingItems,
        int completionPercentage
) { }
