package com.collectohub.catalog.application;

public class MasterProductNotFoundException extends RuntimeException {

    public MasterProductNotFoundException(Long id) {
        super("Master product not found: " + id);
    }
}
