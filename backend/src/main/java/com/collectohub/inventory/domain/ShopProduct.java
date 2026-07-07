package com.collectohub.inventory.domain;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.shops.domain.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shop_products")
public class ShopProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_product_id")
    private MasterProduct masterProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id")
    private CatalogItem catalogItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_edition_id")
    private CatalogItemEdition catalogItemEdition;

    @Enumerated(EnumType.STRING)
    @Column(name = "editorial_reference_source", nullable = false, length = 40)
    private ShopProductEditorialReferenceSource editorialReferenceSource;

    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "commercial_status", nullable = false, length = 30)
    private ShopProductCommercialStatus commercialStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition", nullable = false, length = 30)
    private PhysicalCondition physicalCondition;

    @Column(nullable = false)
    private boolean visible;

    @Column(name = "unit_number", length = 50)
    private String unitNumber;

    @Column(name = "total_limited_units")
    private Integer totalLimitedUnits;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    protected ShopProduct() {
    }

    public static ShopProduct create(
            Shop shop,
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            ShopProductEditorialReferenceSource editorialReferenceSource,
            BigDecimal priceAmount,
            String currency,
            Integer stockQuantity,
            ShopProductCommercialStatus commercialStatus,
            PhysicalCondition physicalCondition,
            boolean visible,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes,
            Long createdBy
    ) {
        ShopProduct shopProduct = new ShopProduct();
        shopProduct.shop = shop;
        shopProduct.masterProduct = masterProduct;
        shopProduct.catalogItem = catalogItem;
        shopProduct.catalogItemEdition = catalogItemEdition;
        shopProduct.editorialReferenceSource = editorialReferenceSource;
        shopProduct.priceAmount = priceAmount;
        shopProduct.currency = currency;
        shopProduct.stockQuantity = stockQuantity;
        shopProduct.commercialStatus = commercialStatus;
        shopProduct.physicalCondition = physicalCondition;
        shopProduct.visible = visible;
        shopProduct.unitNumber = unitNumber;
        shopProduct.totalLimitedUnits = totalLimitedUnits;
        shopProduct.notes = notes;
        shopProduct.createdAt = Instant.now();
        shopProduct.createdBy = createdBy;
        return shopProduct;
    }

    public static ShopProduct create(
            Shop shop,
            MasterProduct masterProduct,
            BigDecimal priceAmount,
            String currency,
            Integer stockQuantity,
            ShopProductCommercialStatus commercialStatus,
            PhysicalCondition physicalCondition,
            boolean visible,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes,
            Long createdBy
    ) {
        return create(
                shop,
                masterProduct,
                null,
                null,
                ShopProductEditorialReferenceSource.LEGACY,
                priceAmount,
                currency,
                stockQuantity,
                commercialStatus,
                physicalCondition,
                visible,
                unitNumber,
                totalLimitedUnits,
                notes,
                createdBy
        );
    }

    public void updateReference(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            ShopProductEditorialReferenceSource editorialReferenceSource,
            Long updatedBy
    ) {
        this.masterProduct = masterProduct;
        this.catalogItem = catalogItem;
        this.catalogItemEdition = catalogItemEdition;
        this.editorialReferenceSource = editorialReferenceSource;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void update(
            BigDecimal priceAmount,
            String currency,
            Integer stockQuantity,
            ShopProductCommercialStatus commercialStatus,
            PhysicalCondition physicalCondition,
            boolean visible,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes,
            Long updatedBy
    ) {
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.stockQuantity = stockQuantity;
        this.commercialStatus = commercialStatus;
        this.physicalCondition = physicalCondition;
        this.visible = visible;
        this.unitNumber = unitNumber;
        this.totalLimitedUnits = totalLimitedUnits;
        this.notes = notes;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public Shop getShop() {
        return shop;
    }

    public MasterProduct getMasterProduct() {
        return masterProduct;
    }

    public CatalogItem getCatalogItem() { return catalogItem; }

    public CatalogItemEdition getCatalogItemEdition() { return catalogItemEdition; }

    public ShopProductEditorialReferenceSource getEditorialReferenceSource() { return editorialReferenceSource; }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public ShopProductCommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public PhysicalCondition getPhysicalCondition() {
        return physicalCondition;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public Integer getTotalLimitedUnits() {
        return totalLimitedUnits;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public boolean isPubliclyVisible() {
        return isActive() && visible && commercialStatus == ShopProductCommercialStatus.AVAILABLE;
    }
}
