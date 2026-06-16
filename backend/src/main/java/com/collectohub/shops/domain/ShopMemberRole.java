package com.collectohub.shops.domain;

public enum ShopMemberRole {
    OWNER,
    MANAGER,
    EMPLOYEE;

    public boolean canManageShop() {
        return this == OWNER || this == MANAGER;
    }
}
