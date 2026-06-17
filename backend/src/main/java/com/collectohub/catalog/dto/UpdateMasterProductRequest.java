package com.collectohub.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record UpdateMasterProductRequest(
        @Size(max = 240)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String name,

        @Size(max = 4000)
        String description,

        @Size(max = 80)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String categoryCode,

        @Size(max = 160)
        String franchise,

        @Size(max = 160)
        String collectionName,

        @Size(max = 50)
        String volumeNumber,

        @Size(max = 160)
        String publisher,

        @Size(max = 20)
        String isbn,

        @Size(max = 20)
        String ean,

        LocalDate releaseDate,

        LocalDate editionStartDate,

        LocalDate editionEndDate,

        @Size(max = 10)
        String language,

        Boolean limitedEdition,

        @Min(1)
        Integer limitedEditionTotalUnits,

        List<@Size(max = 2) String> publicationCountries,

        @Size(max = 2048)
        String coverImageUrl,

        Map<String, Object> attributes
) {
}
