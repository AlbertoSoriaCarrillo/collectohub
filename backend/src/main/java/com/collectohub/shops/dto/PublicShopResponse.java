package com.collectohub.shops.dto;

import com.collectohub.shops.domain.Shop;

public record PublicShopResponse(
        Long id,
        String name,
        String description,
        String contactEmail,
        String contactPhone,
        String country,
        String currency,
        Integer defaultReservationExpirationHours,
        String logoUrl,
        String status
) {

    public static PublicShopResponse from(Shop shop) {
        return new PublicShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getContactEmail(),
                shop.getContactPhone(),
                shop.getCountry(),
                shop.getCurrency(),
                shop.getDefaultReservationExpirationHours(),
                shop.getLogoUrl(),
                shop.getStatus().name()
        );
    }
}
