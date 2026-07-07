package com.collectohub.collections.dto;

import com.collectohub.collections.domain.CollectionItem;

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
        String collectionStatus,
        String physicalCondition,
        String unitNumber,
        Integer totalLimitedUnits,
        String notes,
        LocalDate acquiredAt
) {

    public static CollectionItemResponse from(CollectionItem item) {
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
                item.getCollectionStatus().name(),
                item.getPhysicalCondition() == null ? null : item.getPhysicalCondition().name(),
                item.getUnitNumber(),
                item.getTotalLimitedUnits(),
                item.getNotes(),
                item.getAcquiredAt()
        );
    }
}
