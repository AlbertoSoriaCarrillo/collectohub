package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemRelationshipType;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateCatalogItemRelationshipRequest(
        @NotNull Long targetCatalogItemId,
        @NotNull CatalogItemRelationshipType relationshipType,
        @Min(1) Integer relationshipOrder,
        String description,
        CatalogRecordStatus recordStatus
) {}
