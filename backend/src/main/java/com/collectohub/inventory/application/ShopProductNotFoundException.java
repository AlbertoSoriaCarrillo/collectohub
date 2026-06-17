package com.collectohub.inventory.application;

public class ShopProductNotFoundException extends RuntimeException {

    public ShopProductNotFoundException(Long id) {
        super("Shop product not found: " + id);
    }
}
