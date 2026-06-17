package com.collectohub.catalog.application;

public class ProductCategoryNotFoundException extends RuntimeException {

    public ProductCategoryNotFoundException(String code) {
        super("Product category not found: " + code);
    }
}
