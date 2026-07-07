package com.collectohub.inventory.application;

public class ConflictingShopProductReferenceException extends RuntimeException {
    public ConflictingShopProductReferenceException(String message) {
        super(message);
    }
}
