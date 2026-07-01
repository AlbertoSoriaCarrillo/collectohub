package com.collectohub.catalog.application;

public class EditorialLegacyBridgeNotFoundException extends RuntimeException {

    public EditorialLegacyBridgeNotFoundException(Long masterProductId) {
        super("Editorial catalog link not found for master product: " + masterProductId);
    }
}
