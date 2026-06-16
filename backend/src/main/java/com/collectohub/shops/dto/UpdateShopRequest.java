package com.collectohub.shops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateShopRequest(
        @Pattern(regexp = ".*\\S.*")
        @Size(max = 160)
        String name,

        @Size(max = 4000)
        String description,

        @Email
        @Size(max = 320)
        String contactEmail,

        @Size(max = 40)
        String contactPhone,

        @Pattern(regexp = "^[A-Za-z]{2}$")
        String country,

        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency,

        @Min(1)
        Integer defaultReservationExpirationHours,

        @Size(max = 2048)
        String logoUrl
) {
}
