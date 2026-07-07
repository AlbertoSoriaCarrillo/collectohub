package com.collectohub.inventory.dto;

import com.collectohub.inventory.domain.ShopProduct;

import java.math.BigDecimal;

public record ShopProductResponse(
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
        Integer stockQuantity,
        String commercialStatus,
        String physicalCondition,
        boolean visible,
        String unitNumber,
        Integer totalLimitedUnits,
        String notes
) {

    public ShopProductResponse(
            Long id,
            Long shopId,
            Long masterProductId,
            String masterProductName,
            String masterProductCategoryCode,
            String masterProductFranchise,
            String masterProductCollectionName,
            String masterProductVolumeNumber,
            BigDecimal priceAmount,
            String currency,
            Integer stockQuantity,
            String commercialStatus,
            String physicalCondition,
            boolean visible,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes
    ) {
        this(id, shopId, masterProductId, masterProductName, masterProductCategoryCode,
                masterProductFranchise, masterProductCollectionName, masterProductVolumeNumber,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, "LEGACY", priceAmount, currency, stockQuantity, commercialStatus,
                physicalCondition, visible, unitNumber, totalLimitedUnits, notes);
    }

    public static ShopProductResponse from(ShopProduct shopProduct) {
        var masterProduct = shopProduct.getMasterProduct();
        var catalogItem = shopProduct.getCatalogItem();
        var edition = shopProduct.getCatalogItemEdition();
        var series = catalogItem == null ? null : catalogItem.getSeries();
        var publisher = edition != null && edition.getPublisher() != null
                ? edition.getPublisher()
                : series == null ? null : series.getPrimaryPublisher();
        return new ShopProductResponse(
                shopProduct.getId(),
                shopProduct.getShop().getId(),
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
                shopProduct.getEditorialReferenceSource().name(),
                shopProduct.getPriceAmount(),
                shopProduct.getCurrency(),
                shopProduct.getStockQuantity(),
                shopProduct.getCommercialStatus().name(),
                shopProduct.getPhysicalCondition().name(),
                shopProduct.isVisible(),
                shopProduct.getUnitNumber(),
                shopProduct.getTotalLimitedUnits(),
                shopProduct.getNotes()
        );
    }
}
