package com.collectohub.inventory.dto;

import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShopProductRequest(
        @NotNull
        Long masterProductId,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal priceAmount,

        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency,

        @NotNull
        @Min(0)
        Integer stockQuantity,

        ShopProductCommercialStatus commercialStatus,

        @NotNull
        PhysicalCondition physicalCondition,

        Boolean visible,

        @Size(max = 50)
        String unitNumber,

        @Positive
        Integer totalLimitedUnits,

        @Size(max = 4000)
        String notes
) {
}
