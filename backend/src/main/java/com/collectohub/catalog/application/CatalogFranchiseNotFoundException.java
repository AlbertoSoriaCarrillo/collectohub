package com.collectohub.catalog.application;

public class CatalogFranchiseNotFoundException extends RuntimeException {

    public CatalogFranchiseNotFoundException(Long id) {
        super("Catalog franchise not found: " + id);
    }
}
