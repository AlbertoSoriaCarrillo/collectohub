package com.collectohub.recommendations.dto;

public record RecommendationReasonResponse(
        String code,
        String message
) {

    public static RecommendationReasonResponse fromCollectionStatus(String collectionStatus) {
        String message = switch (collectionStatus) {
            case "MISSING" -> "Product marked as missing in one of your collections";
            case "WANTED" -> "Product marked as wanted in one of your collections";
            default -> "Product matches one of your collection interests";
        };
        return new RecommendationReasonResponse("COLLECTION_ITEM_" + collectionStatus, message);
    }
}
