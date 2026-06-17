package com.collectohub.collections.dto;

import com.collectohub.collections.domain.Collection;

import java.util.List;

public record CollectionResponse(
        Long id,
        Long userId,
        String name,
        String description,
        String visibility,
        String categoryCode,
        String categoryName,
        List<CollectionItemResponse> items
) {

    public static CollectionResponse from(Collection collection) {
        return from(collection, List.of());
    }

    public static CollectionResponse from(Collection collection, List<CollectionItemResponse> items) {
        var category = collection.getCategory();
        return new CollectionResponse(
                collection.getId(),
                collection.getUser().getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getVisibility().name(),
                category == null ? null : category.getCode(),
                category == null ? null : category.getName(),
                items
        );
    }
}
