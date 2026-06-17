package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.inventory.domain.PhysicalCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCollectionItemRequest(
        @NotNull
        Long masterProductId,

        @NotNull
        CollectionItemStatus collectionStatus,

        PhysicalCondition physicalCondition,

        @Size(max = 50)
        String unitNumber,

        @Positive
        Integer totalLimitedUnits,

        @Size(max = 4000)
        String notes,

        LocalDate acquiredAt
) {
}
