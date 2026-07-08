package com.collectohub.catalog.application;

public class CatalogItemCreatorNotFoundException extends RuntimeException {
    public CatalogItemCreatorNotFoundException(Long id) { super("Catalog item creator credit not found: " + id); }
}
