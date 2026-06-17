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
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getDisplayName(),
                reservation.getShop().getId(),
                reservation.getShop().getName(),
                shopProduct.getId(),
                masterProduct.getId(),
                masterProduct.getName(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getUserMessage(),
                reservation.getShopResponse(),
                reservation.getExpiresAt(),
                reservation.getCompletedAt(),
                reservation.getCreatedAt()
        );
    }
}
