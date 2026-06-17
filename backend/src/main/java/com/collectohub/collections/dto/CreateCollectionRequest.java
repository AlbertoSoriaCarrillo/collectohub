package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCollectionRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @Size(max = 4000)
        String description,

        CollectionVisibility visibility,

        @Size(max = 80)
        String categoryCode
) {
}
