package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemEdition;

import java.time.Instant;
import java.time.LocalDate;

public record CatalogItemEditionResponse(
        Long id,
        Long catalogItemId,
        String catalogItemTitle,
        Long publisherId,
        String publisherName,
        String isbn,
        String ean,
        String format,
        String editionName,
        LocalDate publicationDate,
        Integer publicationYear,
        String language,
        String country,
        Integer pageCount,
        String coverImageUrl,
        String recordStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static CatalogItemEditionResponse from(CatalogItemEdition edition) {
        return new CatalogItemEditionResponse(
                edition.getId(),
                edition.getCatalogItem().getId(),
                edition.getCatalogItem().getTitle(),
                edition.getPublisher() == null ? null : edition.getPublisher().getId(),
                edition.getPublisher() == null ? null : edition.getPublisher().getName(),
                edition.getIsbn(),
                edition.getEan(),
                edition.getFormat().name(),
                edition.getEditionName(),
                edition.getPublicationDate(),
                edition.getPublicationYear(),
                edition.getLanguage(),
                edition.getCountry(),
                edition.getPageCount(),
                edition.getCoverImageUrl(),
                edition.getRecordStatus().name(),
                edition.getCreatedAt(),
                edition.getUpdatedAt()
        );
    }
}
