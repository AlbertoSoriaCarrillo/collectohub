package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.ProductCategory;

public record ProductCategoryResponse(
        Long id,
        String code,
        String name,
        Long parentId
) {

    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getParent() == null ? null : category.getParent().getId()
        );
    }
}
