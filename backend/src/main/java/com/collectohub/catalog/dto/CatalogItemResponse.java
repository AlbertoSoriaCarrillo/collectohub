package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CatalogItemResponse(
        Long id,
        Long seriesId,
        String seriesTitle,
        String title,
        String originalTitle,
        String sequenceLabel,
        BigDecimal sortOrder,
        String description,
        LocalDate firstPublicationDate,
        Integer firstPublicationYear,
        String originalLanguage,
        String originCountry,
        String recordStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static CatalogItemResponse from(CatalogItem item) {
        return new CatalogItemResponse(
                item.getId(),
                item.getSeries().getId(),
                item.getSeries().getTitle(),
                item.getTitle(),
                item.getOriginalTitle(),
                item.getSequenceLabel(),
                item.getSortOrder(),
                item.getDescription(),
                item.getFirstPublicationDate(),
                item.getFirstPublicationYear(),
                item.getOriginalLanguage(),
                item.getOriginCountry(),
                item.getRecordStatus().name(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
