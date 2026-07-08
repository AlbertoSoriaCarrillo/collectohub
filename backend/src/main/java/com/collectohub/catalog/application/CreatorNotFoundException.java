package com.collectohub.catalog.application;

public class CreatorNotFoundException extends RuntimeException {
    public CreatorNotFoundException(Long id) { super("Creator not found: " + id); }
}
