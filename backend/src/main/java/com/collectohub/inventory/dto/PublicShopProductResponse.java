package com.collectohub.inventory.dto;

import com.collectohub.inventory.domain.ShopProduct;

import java.math.BigDecimal;

public record PublicShopProductResponse(
        Long id,
        Long shopId,
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
        BigDecimal priceAmount,
        String currency,
        String commercialStatus,
        String physicalCondition,
        boolean visible,
        String unitNumber,
        Integer totalLimitedUnits
) {

    public static PublicShopProductResponse from(ShopProduct shopProduct) {
        return from(ShopProductResponse.from(shopProduct));
    }

    public static PublicShopProductResponse from(ShopProductResponse managed) {
        return new PublicShopProductResponse(
                managed.id(), managed.shopId(), managed.masterProductId(), managed.masterProductName(),
                managed.masterProductCategoryCode(), managed.masterProductFranchise(),
                managed.masterProductCollectionName(), managed.masterProductVolumeNumber(),
                managed.catalogItemId(), managed.catalogItemTitle(), managed.catalogItemSequenceLabel(),
                managed.catalogSeriesId(), managed.catalogSeriesTitle(), managed.catalogItemEditionId(),
                managed.catalogItemEditionName(), managed.catalogItemEditionFormat(), managed.catalogItemEditionIsbn(),
                managed.catalogItemEditionEan(), managed.catalogItemEditionCoverImageUrl(), managed.catalogPublisherName(),
                managed.catalogFranchiseName(), managed.editorialReferenceSource(), managed.priceAmount(),
                managed.currency(), managed.commercialStatus(), managed.physicalCondition(),
                managed.visible(), managed.unitNumber(), managed.totalLimitedUnits()
        );
    }
}
