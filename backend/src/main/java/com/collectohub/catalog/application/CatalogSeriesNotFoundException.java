package com.collectohub.catalog.application;

public class CatalogSeriesNotFoundException extends RuntimeException {

    public CatalogSeriesNotFoundException(Long id) {
        super("Catalog series not found: " + id);
    }
}
