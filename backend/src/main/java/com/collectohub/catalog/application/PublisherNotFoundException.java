package com.collectohub.catalog.application;

public class PublisherNotFoundException extends RuntimeException {

    public PublisherNotFoundException(Long id) {
        super("Publisher not found: " + id);
    }
}
