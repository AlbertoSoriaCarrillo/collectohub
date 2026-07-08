package com.collectohub.catalog.application;

public class CatalogItemRelationshipNotFoundException extends RuntimeException {
    public CatalogItemRelationshipNotFoundException(Long id) {
        super("Catalog item relationship not found: " + id);
    }
}
