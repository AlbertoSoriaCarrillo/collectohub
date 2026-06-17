package com.collectohub.recommendations.dto;

import java.util.List;

public record UserRecommendationResponse(
        List<RecommendedShopProductResponse> recommendations,
        int totalRecommendations
) {

    public static UserRecommendationResponse from(List<RecommendedShopProductResponse> recommendations) {
        return new UserRecommendationResponse(List.copyOf(recommendations), recommendations.size());
    }
}
