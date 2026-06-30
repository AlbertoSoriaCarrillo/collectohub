package com.collectohub.catalog.application;

public class CatalogItemNotFoundException extends RuntimeException {

    public CatalogItemNotFoundException(Long id) {
        super("Catalog item not found: " + id);
    }
}
