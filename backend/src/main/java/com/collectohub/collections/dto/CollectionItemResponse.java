package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemReferenceKind;

import java.time.LocalDate;

public record CollectionItemResponse(
        Long id,
        Long collectionId,
        Long masterProductId,
        String masterProductName,
        String masterProductCategoryCode,
        String masterProductFranchise,
        String masterProductCollectionName,
        String masterProductVolumeNumber,
        Long catalogItemId,
        String catalogItemTitle,
        String catalogItemSequenceLabel,
        Long catalogSeriesId,
        String catalogSeriesTitle,
        Long catalogItemEditionId,
        String catalogItemEditionName,
        String catalogItemEditionFormat,
        String catalogItemEditionIsbn,
        String catalogItemEditionEan,
        String catalogItemEditionCoverImageUrl,
        String catalogPublisherName,
        String catalogFranchiseName,
        String editorialReferenceSource,
        String referenceKind,
        String manualTitle,
        String manualDescription,
        String manualType,
        String collectionStatus,
        String physicalCondition,
        String unitNumber,
        Integer totalLimitedUnits,
        String notes,
        LocalDate acquiredAt
) {

    public static CollectionItemResponse from(CollectionItem item) {
        return from(item, true);
    }

    public static CollectionItemResponse from(CollectionItem item, boolean includePrivateFields) {
        var masterProduct = item.getMasterProduct();
        var catalogItem = item.getCatalogItem();
        var edition = item.getCatalogItemEdition();
        var series = catalogItem == null ? null : catalogItem.getSeries();
        var publisher = edition != null && edition.getPublisher() != null
                ? edition.getPublisher()
                : series == null ? null : series.getPrimaryPublisher();
        return new CollectionItemResponse(
                item.getId(),
                item.getCollection().getId(),
                masterProduct == null ? null : masterProduct.getId(),
                masterProduct == null ? null : masterProduct.getName(),
                masterProduct == null ? null : masterProduct.getCategory().getCode(),
                masterProduct == null ? null : masterProduct.getFranchise(),
                masterProduct == null ? null : masterProduct.getCollectionName(),
                masterProduct == null ? null : masterProduct.getVolumeNumber(),
                catalogItem == null ? null : catalogItem.getId(),
                catalogItem == null ? null : catalogItem.getTitle(),
                catalogItem == null ? null : catalogItem.getSequenceLabel(),
                series == null ? null : series.getId(),
                series == null ? null : series.getTitle(),
                edition == null ? null : edition.getId(),
                edition == null ? null : edition.getEditionName(),
                edition == null ? null : edition.getFormat().name(),
                edition == null ? null : edition.getIsbn(),
                edition == null ? null : edition.getEan(),
                edition == null ? null : edition.getCoverImageUrl(),
                publisher == null ? null : publisher.getName(),
                series == null || series.getFranchise() == null ? null : series.getFranchise().getName(),
                item.getEditorialReferenceSource().name(),
                referenceKind(item).name(),
                item.getManualTitle(),
                item.getManualDescription(),
                item.getManualType(),
                item.getCollectionStatus().name(),
                item.getPhysicalCondition() == null ? null : item.getPhysicalCondition().name(),
                item.getUnitNumber(),
                item.getTotalLimitedUnits(),
                includePrivateFields ? item.getNotes() : null,
                includePrivateFields ? item.getAcquiredAt() : null
        );
    }

    private static CollectionItemReferenceKind referenceKind(CollectionItem item) {
        if (item.getCatalogItem() != null) {
            return item.getEditorialReferenceSource().name().equals("VERIFIED_BRIDGE")
                    ? CollectionItemReferenceKind.VERIFIED_BRIDGE
                    : CollectionItemReferenceKind.DIRECT_CATALOG;
        }
        if (item.getMasterProduct() != null) {
            return CollectionItemReferenceKind.LEGACY_UNRESOLVED;
        }
        return item.isManual()
                ? CollectionItemReferenceKind.MANUAL
                : CollectionItemReferenceKind.INVALID_REFERENCE;
    }
}
