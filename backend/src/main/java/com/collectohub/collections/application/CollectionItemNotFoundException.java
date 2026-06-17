package com.collectohub.collections.application;

public class CollectionItemNotFoundException extends RuntimeException {

    public CollectionItemNotFoundException(Long id) {
        super("Collection item not found: " + id);
    }
}
