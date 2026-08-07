package com.collectohub.shops.dto;

import com.collectohub.shops.domain.ShopMemberRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddShopMemberRequest(
        @NotBlank @Email String email,
        @NotNull ShopMemberRole role
) {

    @AssertTrue(message = "role must be MANAGER or EMPLOYEE")
    public boolean isAssignableRole() {
        return role == null || role == ShopMemberRole.MANAGER || role == ShopMemberRole.EMPLOYEE;
    }
}
