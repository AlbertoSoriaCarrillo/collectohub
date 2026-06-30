package com.collectohub.catalog.application;

public class CatalogItemEditionNotFoundException extends RuntimeException {

    public CatalogItemEditionNotFoundException(Long id) {
        super("Catalog item edition not found: " + id);
    }
}
