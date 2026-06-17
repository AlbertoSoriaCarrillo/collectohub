package com.collectohub.recommendations.dto;

import java.util.List;

public record UserRecommendationSummaryResponse(
        long missingCollectionItems,
        long wantedCollectionItems,
        int recommendedProducts,
        int matchedShops,
        List<String> matchedCategoryCodes
) {
}
