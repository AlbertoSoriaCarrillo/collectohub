package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCatalogItemRequest(
        @NotNull @Positive Long seriesId,
        @NotBlank @Size(max = 240) String title,
        @Size(max = 240) String originalTitle,
        @Size(max = 50) String sequenceLabel,
        @DecimalMin("0.000") @Digits(integer = 7, fraction = 3) BigDecimal sortOrder,
        @Size(max = 4000) String description,
        LocalDate firstPublicationDate,
        @Min(1000) @Max(3000) Integer firstPublicationYear,
        @Size(max = 10) String originalLanguage,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter country code") String originCountry,
        @NotNull CatalogRecordStatus recordStatus
) {
}
