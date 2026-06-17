package com.collectohub.inventory.infrastructure;

import com.collectohub.catalog.domain.MasterProductStatus;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.shops.domain.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long>, JpaSpecificationExecutor<ShopProduct> {

    List<ShopProduct> findByShop_IdAndDeletedAtIsNullOrderByIdAsc(Long shopId);

    List<ShopProduct> findByMasterProduct_IdAndDeletedAtIsNull(Long masterProductId);

    Optional<ShopProduct> findByIdAndDeletedAtIsNull(Long id);

    Optional<ShopProduct> findByIdAndShop_IdAndDeletedAtIsNull(Long id, Long shopId);

    Optional<ShopProduct> findByIdAndVisibleTrueAndCommercialStatusAndDeletedAtIsNull(
            Long id,
            ShopProductCommercialStatus commercialStatus
    );

    @Query("""
            select shopProduct
            from ShopProduct shopProduct
            join fetch shopProduct.shop shop
            join fetch shopProduct.masterProduct masterProduct
            join fetch masterProduct.category category
            where masterProduct.id in :masterProductIds
              and shopProduct.deletedAt is null
              and shopProduct.visible = true
              and shopProduct.commercialStatus = :commercialStatus
              and shopProduct.stockQuantity > 0
              and shop.deletedAt is null
              and shop.status = :shopStatus
              and masterProduct.deletedAt is null
              and masterProduct.status = :masterProductStatus
              and (:categoryCode is null or upper(category.code) = :categoryCode)
              and (:currency is null or upper(shopProduct.currency) = :currency)
              and (:maxPrice is null or shopProduct.priceAmount <= :maxPrice)
              and (:physicalCondition is null or shopProduct.physicalCondition = :physicalCondition)
              and (:shopId is null or shop.id = :shopId)
            order by masterProduct.name asc, shopProduct.priceAmount asc, shopProduct.id asc
            """)
    List<ShopProduct> findRecommendationCandidates(
            @Param("masterProductIds") Set<Long> masterProductIds,
            @Param("commercialStatus") ShopProductCommercialStatus commercialStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("masterProductStatus") MasterProductStatus masterProductStatus,
            @Param("categoryCode") String categoryCode,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("currency") String currency,
            @Param("physicalCondition") PhysicalCondition physicalCondition,
            @Param("shopId") Long shopId
    );
}
