package com.collectohub.catalog.dto;

import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProductCatalogLink;

import java.math.BigDecimal;
import java.time.Instant;

public record MasterProductCatalogLinkResponse(
        Long id,
        Long masterProductId,
        String masterProductName,
        Long catalogItemId,
        String catalogItemTitle,
        Long catalogItemEditionId,
        String catalogItemEditionLabel,
        String linkStatus,
        String linkSource,
        BigDecimal confidenceScore,
        String matchReason,
        String reviewNote,
        Instant createdAt,
        Instant updatedAt
) {

    public static MasterProductCatalogLinkResponse from(MasterProductCatalogLink link) {
        CatalogItemEdition edition = link.getCatalogItemEdition();
        String label = edition == null ? null : editionLabel(edition);
        return new MasterProductCatalogLinkResponse(
                link.getId(),
                link.getMasterProduct().getId(),
                link.getMasterProduct().getName(),
                link.getCatalogItem().getId(),
                link.getCatalogItem().getTitle(),
                edition == null ? null : edition.getId(),
                label,
                link.getLinkStatus().name(),
                link.getLinkSource().name(),
                link.getConfidenceScore(),
                link.getMatchReason(),
                link.getReviewNote(),
                link.getCreatedAt(),
                link.getUpdatedAt()
        );
    }

    private static String editionLabel(CatalogItemEdition edition) {
        if (edition.getEditionName() != null) {
            return edition.getEditionName();
        }
        return edition.getPublicationYear() == null
                ? edition.getFormat().name()
                : edition.getFormat().name() + " " + edition.getPublicationYear();
    }
}
