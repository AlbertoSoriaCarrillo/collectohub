package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogFranchise;

import java.time.Instant;

public record CatalogFranchiseResponse(
        Long id,
        String name,
        String slug,
        String description,
        String recordStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static CatalogFranchiseResponse from(CatalogFranchise franchise) {
        return new CatalogFranchiseResponse(
                franchise.getId(),
                franchise.getName(),
                franchise.getSlug(),
                franchise.getDescription(),
                franchise.getRecordStatus().name(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt()
        );
    }
}
