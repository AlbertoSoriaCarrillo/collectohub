package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.inventory.domain.PhysicalCondition;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCollectionItemRequest(
        Long masterProductId,

        Long catalogItemId,

        Long catalogItemEditionId,

        CollectionItemStatus collectionStatus,

        PhysicalCondition physicalCondition,

        @Size(max = 50)
        String unitNumber,

        @Positive
        Integer totalLimitedUnits,

        @Size(max = 4000)
        String notes,

        LocalDate acquiredAt,
        @Size(max = 160) String manualTitle,
        @Size(max = 4000) String manualDescription,
        @Size(max = 80) String manualType
) {
}
