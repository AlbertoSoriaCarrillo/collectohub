package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.Publisher;

import java.time.Instant;

public record PublisherResponse(
        Long id,
        String name,
        String country,
        String recordStatus,
        Instant createdAt,
        Instant updatedAt
) {

    public static PublisherResponse from(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getName(),
                publisher.getCountry(),
                publisher.getRecordStatus().name(),
                publisher.getCreatedAt(),
                publisher.getUpdatedAt()
        );
    }
}
