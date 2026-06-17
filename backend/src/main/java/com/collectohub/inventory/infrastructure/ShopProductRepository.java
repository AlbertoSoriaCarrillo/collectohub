package com.collectohub.inventory.infrastructure;

import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long>, JpaSpecificationExecutor<ShopProduct> {

    List<ShopProduct> findByShop_IdAndDeletedAtIsNullOrderByIdAsc(Long shopId);

    List<ShopProduct> findByMasterProduct_IdAndDeletedAtIsNull(Long masterProductId);

    Optional<ShopProduct> findByIdAndDeletedAtIsNull(Long id);

    Optional<ShopProduct> findByIdAndShop_IdAndDeletedAtIsNull(Long id, Long shopId);

    Optional<ShopProduct> findByIdAndVisibleTrueAndCommercialStatusAndDeletedAtIsNull(
            Long id,
            ShopProductCommercialStatus commercialStatus
    );
}
