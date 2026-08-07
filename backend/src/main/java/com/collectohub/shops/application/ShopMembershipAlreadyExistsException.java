package com.collectohub.shops.application;

public class ShopMembershipAlreadyExistsException extends RuntimeException {

    public ShopMembershipAlreadyExistsException() {
        super("Shop membership already exists");
    }
}
