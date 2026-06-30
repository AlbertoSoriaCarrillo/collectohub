package com.collectohub.catalog.application;

public class MasterProductCatalogLinkNotFoundException extends RuntimeException {

    public MasterProductCatalogLinkNotFoundException(Long id) {
        super("Master product catalog link not found: " + id);
    }
}
