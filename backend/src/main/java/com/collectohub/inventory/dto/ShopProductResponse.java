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

    public static ShopProductResponse from(ShopProduct shopProduct) {
        var masterProduct = shopProduct.getMasterProduct();
        return new ShopProductResponse(
                shopProduct.getId(),
                shopProduct.getShop().getId(),
                masterProduct.getId(),
                masterProduct.getName(),
                masterProduct.getCategory().getCode(),
                masterProduct.getFranchise(),
                masterProduct.getCollectionName(),
                masterProduct.getVolumeNumber(),
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
