package com.collectohub.shops.application;

public class ShopNotFoundException extends RuntimeException {

    public ShopNotFoundException(Long shopId) {
        super("Shop not found: " + shopId);
    }
}
