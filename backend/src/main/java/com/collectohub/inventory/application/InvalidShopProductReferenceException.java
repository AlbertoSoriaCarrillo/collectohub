package com.collectohub.inventory.application;

public class InvalidShopProductReferenceException extends RuntimeException {
    public InvalidShopProductReferenceException(String message) {
        super(message);
    }
}
