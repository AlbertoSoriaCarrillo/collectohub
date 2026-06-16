package com.collectohub.shops.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collectohub.shops")
public record ShopProperties(
        String defaultCurrency,
        int defaultReservationExpirationHours
) {
}
