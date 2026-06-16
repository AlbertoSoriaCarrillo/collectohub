package com.collectohub.auth.application;

public class RoleNotConfiguredException extends RuntimeException {

    public RoleNotConfiguredException(String code) {
        super("Required role is not configured: " + code);
    }
}
