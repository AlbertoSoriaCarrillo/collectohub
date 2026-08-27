package com.collectohub.reservations.dto;

import com.collectohub.reservations.domain.Reservation;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long userId,
        String userDisplayName,
        Long shopId,
        String shopName,
        Long shopProductId,
        Long masterProductId,
        String productName,
        Long catalogItemId,
        String catalogItemTitle,
        Long catalogItemEditionId,
        String catalogItemEditionName,
        Integer quantity,
        String status,
        String userMessage,
        String shopResponse,
        Instant expiresAt,
        Instant completedAt,
        Instant createdAt
) {

    public static ReservationResponse from(Reservation reservation) {
        var shopProduct = reservation.getShopProduct();
        var masterProduct = shopProduct.getMasterProduct();
        var catalogItem = shopProduct.getCatalogItem();
        var edition = shopProduct.getCatalogItemEdition();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getDisplayName(),
                reservation.getShop().getId(),
                reservation.getShop().getName(),
                shopProduct.getId(),
                masterProduct == null ? null : masterProduct.getId(),
                firstNonBlank(
                        edition == null ? null : edition.getEditionName(),
                        catalogItem == null ? null : catalogItem.getTitle(),
                        masterProduct == null ? null : masterProduct.getName()
                ),
                catalogItem == null ? null : catalogItem.getId(),
                catalogItem == null ? null : catalogItem.getTitle(),
                edition == null ? null : edition.getId(),
                edition == null ? null : edition.getEditionName(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getUserMessage(),
                reservation.getShopResponse(),
                reservation.getExpiresAt(),
                reservation.getCompletedAt(),
                reservation.getCreatedAt()
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public ReservationResponse(
            Long id, Long userId, String userDisplayName, Long shopId, String shopName,
            Long shopProductId, Long masterProductId, String productName, Integer quantity,
            String status, String userMessage, String shopResponse, Instant expiresAt,
            Instant completedAt, Instant createdAt
    ) {
        this(id, userId, userDisplayName, shopId, shopName, shopProductId, masterProductId,
                productName, null, null, null, null, quantity, status, userMessage, shopResponse,
                expiresAt, completedAt, createdAt);
    }
}
