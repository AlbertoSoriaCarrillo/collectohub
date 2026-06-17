package com.collectohub.recommendations.dto;

import java.math.BigDecimal;

public record RecommendedShopProductResponse(
        Long shopProductId,
        Long shopId,
        String shopName,
        Long masterProductId,
        String productName,
        String categoryCode,
        String franchise,
        String collectionName,
        String volumeNumber,
        String coverImageUrl,
        BigDecimal priceAmount,
        String currency,
        Integer stockQuantity,
        String physicalCondition,
        String commercialStatus,
        RecommendationReasonResponse recommendationReason,
        Long matchedCollectionId,
        String matchedCollectionName,
        String matchedCollectionItemStatus
) {
}
