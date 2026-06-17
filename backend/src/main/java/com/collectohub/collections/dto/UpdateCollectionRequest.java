package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionVisibility;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCollectionRequest(
        @Size(max = 160)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String name,

        @Size(max = 4000)
        String description,

        CollectionVisibility visibility,

        @Size(max = 80)
        String categoryCode
) {
}
