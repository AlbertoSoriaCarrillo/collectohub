package com.collectohub.auth.application;

public class UnsupportedInterfaceLanguageException extends RuntimeException {

    public UnsupportedInterfaceLanguageException(String language) {
        super("Unsupported interface language: " + language);
    }
}
