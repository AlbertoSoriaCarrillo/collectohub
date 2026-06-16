package com.collectohub.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "collectohub.security.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {

    public Duration accessTokenExpiration() {
        return Duration.ofMinutes(accessTokenExpirationMinutes);
    }

    public Duration refreshTokenExpiration() {
        return Duration.ofDays(refreshTokenExpirationDays);
    }
}
