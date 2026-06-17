package com.collectohub.catalog.application;

public class DuplicateMasterProductException extends RuntimeException {

    public DuplicateMasterProductException(String reason) {
        super("Duplicate master product: " + reason);
    }
}
