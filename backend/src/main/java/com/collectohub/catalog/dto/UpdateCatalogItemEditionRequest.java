package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCatalogItemEditionRequest(
        @NotNull @Positive Long catalogItemId,
        @Positive Long publisherId,
        @Pattern(regexp = "^[0-9Xx\\-\\s]{8,32}$", message = "must contain 8 to 32 ISBN characters") String isbn,
        @Pattern(regexp = "^[0-9\\-\\s]{8,32}$", message = "must contain 8 to 32 EAN characters") String ean,
        @NotNull CatalogItemEditionFormat format,
        @Size(max = 240) String editionName,
        LocalDate publicationDate,
        @Min(1000) @Max(3000) Integer publicationYear,
        @Size(max = 10) String language,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter country code") String country,
        @Positive Integer pageCount,
        @Size(max = 1000) @Pattern(regexp = "^https?://.+", message = "must be an HTTP(S) URL") String coverImageUrl,
        @NotNull CatalogRecordStatus recordStatus
) {
}
