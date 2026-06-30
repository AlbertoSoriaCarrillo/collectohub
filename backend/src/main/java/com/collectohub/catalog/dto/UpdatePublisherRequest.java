package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePublisherRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter country code")
        String country,

        @NotNull
        CatalogRecordStatus recordStatus
) {
}
