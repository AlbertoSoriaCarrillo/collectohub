package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCatalogFranchiseRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @NotBlank
        @Size(max = 160)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "must contain only lowercase letters, numbers and single hyphens"
        )
        String slug,

        @Size(max = 4000)
        String description,

        @NotNull
        CatalogRecordStatus recordStatus
) {
}
