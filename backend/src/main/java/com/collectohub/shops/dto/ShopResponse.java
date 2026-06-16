package com.collectohub.shops.dto;

import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;

public record ShopResponse(
        Long id,
        String name,
        String description,
        String contactEmail,
        String contactPhone,
        String country,
        String currency,
        Integer defaultReservationExpirationHours,
        String logoUrl,
        String status,
        ShopMemberResponse currentUserMembership
) {

    public static ShopResponse publicFrom(Shop shop) {
        return from(shop, null);
    }

    public static ShopResponse from(Shop shop, ShopMember currentUserMembership) {
        return new ShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getContactEmail(),
                shop.getContactPhone(),
                shop.getCountry(),
                shop.getCurrency(),
                shop.getDefaultReservationExpirationHours(),
                shop.getLogoUrl(),
                shop.getStatus().name(),
                currentUserMembership == null ? null : ShopMemberResponse.from(currentUserMembership)
        );
    }
}
