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
        String collectionStatus,
        String physicalCondition,
        String unitNumber,
        Integer totalLimitedUnits,
        String notes,
        LocalDate acquiredAt
) {

    public static CollectionItemResponse from(CollectionItem item) {
        var masterProduct = item.getMasterProduct();
        return new CollectionItemResponse(
                item.getId(),
                item.getCollection().getId(),
                masterProduct.getId(),
                masterProduct.getName(),
                masterProduct.getCategory().getCode(),
                masterProduct.getFranchise(),
                masterProduct.getCollectionName(),
                masterProduct.getVolumeNumber(),
                item.getCollectionStatus().name(),
                item.getPhysicalCondition() == null ? null : item.getPhysicalCondition().name(),
                item.getUnitNumber(),
                item.getTotalLimitedUnits(),
                item.getNotes(),
                item.getAcquiredAt()
        );
    }
}
