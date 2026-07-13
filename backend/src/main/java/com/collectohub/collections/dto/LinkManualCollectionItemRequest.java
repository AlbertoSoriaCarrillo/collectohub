package com.collectohub.collections.dto;

import jakarta.validation.constraints.NotNull;

public record LinkManualCollectionItemRequest(
        @NotNull Long catalogItemId,
        Long catalogItemEditionId
) {
}
