package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.MasterProductCatalogLinkSource;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMasterProductCatalogLinkRequest(
        @NotNull @Positive Long catalogItemId,
        @Positive Long catalogItemEditionId,
        @NotNull MasterProductCatalogLinkStatus linkStatus,
        @NotNull MasterProductCatalogLinkSource linkSource,
        @DecimalMin("0.0000") @DecimalMax("1.0000") @Digits(integer = 1, fraction = 4) BigDecimal confidenceScore,
        @Size(max = 4000) String matchReason,
        @Size(max = 4000) String reviewNote
) {
}
