package com.collectohub.shops.dto;

import com.collectohub.shops.domain.ShopMember;

public record ShopMemberResponse(
        Long id,
        Long userId,
        String role,
        String status
) {

    public static ShopMemberResponse from(ShopMember member) {
        return new ShopMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getRole().name(),
                member.getStatus().name()
        );
    }
}
