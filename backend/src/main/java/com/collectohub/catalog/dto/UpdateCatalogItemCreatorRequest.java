package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CreatorCreditRole;
import jakarta.validation.constraints.*;

public record UpdateCatalogItemCreatorRequest(
        @NotNull CreatorCreditRole creditRole,
        @NotNull @Min(1) Integer creditOrder,
        @Size(max = 255) String creditLabel
) {}
