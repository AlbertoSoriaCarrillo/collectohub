package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogFranchise;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.Publisher;

import java.time.Instant;

public record CatalogSeriesResponse(
        Long id,
        Long franchiseId,
        String franchiseName,
        Long primaryPublisherId,
        String primaryPublisherName,
        String title,
        String originalTitle,
        String type,
        String publicationStatus,
        String description,
        String originCountry,
        String originalLanguage,
        Integer startYear,
        Integer endYear,
        String recordStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static CatalogSeriesResponse from(CatalogSeries series) {
        CatalogFranchise franchise = series.getFranchise();
        Publisher publisher = series.getPrimaryPublisher();
        return new CatalogSeriesResponse(
                series.getId(),
                franchise == null ? null : franchise.getId(),
                franchise == null ? null : franchise.getName(),
                publisher == null ? null : publisher.getId(),
                publisher == null ? null : publisher.getName(),
                series.getTitle(),
                series.getOriginalTitle(),
                series.getType().name(),
                series.getPublicationStatus().name(),
                series.getDescription(),
                series.getOriginCountry(),
                series.getOriginalLanguage(),
                series.getStartYear(),
                series.getEndYear(),
                series.getRecordStatus().name(),
                series.getCreatedAt(),
                series.getUpdatedAt()
        );
    }
}
