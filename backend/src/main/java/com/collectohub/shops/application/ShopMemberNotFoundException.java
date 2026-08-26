package com.collectohub.shops.application;

public class ShopMemberNotFoundException extends RuntimeException {

    public ShopMemberNotFoundException() {
        super("Shop member not found");
    }
}
