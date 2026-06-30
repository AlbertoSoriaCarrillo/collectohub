package com.collectohub.catalog.application;

public class DuplicateEditorialCatalogException extends RuntimeException {

    public DuplicateEditorialCatalogException(String resource, String reason) {
        super("Duplicate " + resource + ": " + reason);
    }
}
