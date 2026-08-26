package com.collectohub.shops.application;

public class InvalidShopMemberRoleException extends RuntimeException {

    public InvalidShopMemberRoleException() {
        super("Shop members can only be assigned MANAGER or EMPLOYEE");
    }
}
