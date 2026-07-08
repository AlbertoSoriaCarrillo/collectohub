package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemRelationshipType;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCatalogItemRelationshipRequest(
        Long targetCatalogItemId,
        @NotNull CatalogItemRelationshipType relationshipType,
        @NotNull @Min(1) Integer relationshipOrder,
        String description,
        CatalogRecordStatus recordStatus
) {}
