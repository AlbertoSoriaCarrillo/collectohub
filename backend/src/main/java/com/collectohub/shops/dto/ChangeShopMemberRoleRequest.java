package com.collectohub.shops.dto;

import com.collectohub.shops.domain.ShopMemberRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ChangeShopMemberRoleRequest(
        @NotNull ShopMemberRole role
) {

    @AssertTrue(message = "role must be MANAGER or EMPLOYEE")
    public boolean isAssignableRole() {
        return role == null || role == ShopMemberRole.MANAGER || role == ShopMemberRole.EMPLOYEE;
    }
}
