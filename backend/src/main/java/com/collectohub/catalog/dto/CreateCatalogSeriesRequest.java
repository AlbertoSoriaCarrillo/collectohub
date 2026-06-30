package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeriesType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCatalogSeriesRequest(
        Long franchiseId,

        Long primaryPublisherId,

        @NotBlank
        @Size(max = 240)
        String title,

        @Size(max = 240)
        String originalTitle,

        @NotNull
        CatalogSeriesType type,

        @NotNull
        CatalogPublicationStatus publicationStatus,

        @Size(max = 4000)
        String description,

        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter country code")
        String originCountry,

        @Size(max = 10)
        String originalLanguage,

        @Min(1000)
        @Max(3000)
        Integer startYear,

        @Min(1000)
        @Max(3000)
        Integer endYear,

        @NotNull
        CatalogRecordStatus recordStatus
) {

    @AssertTrue(message = "endYear must be greater than or equal to startYear")
    public boolean isYearRangeValid() {
        return startYear == null || endYear == null || endYear >= startYear;
    }
}
