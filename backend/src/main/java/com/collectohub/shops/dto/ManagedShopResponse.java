package com.collectohub.shops.dto;

import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;

public record ManagedShopResponse(
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

    public static ManagedShopResponse from(Shop shop, ShopMember currentUserMembership) {
        return new ManagedShopResponse(
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
                ShopMemberResponse.from(currentUserMembership)
        );
    }
}
