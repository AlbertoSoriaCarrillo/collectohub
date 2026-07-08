package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.*;

public record UpdateCreatorRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String slug,
        @Size(max = 255) String sortName,
        String biography,
        @Pattern(regexp = "^[A-Za-z]{2}$") String country,
        @Min(0) Integer birthYear,
        @Min(0) Integer deathYear,
        CatalogRecordStatus recordStatus
) {}
