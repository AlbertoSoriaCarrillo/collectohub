package com.collectohub.collections.domain;

import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.inventory.domain.PhysicalCondition;
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

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "collection_items")
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

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
    private CollectionEditorialReferenceSource editorialReferenceSource;

    @Column(name = "manual_title", length = 160)
    private String manualTitle;
    @Column(name = "manual_description", length = 4000)
    private String manualDescription;
    @Column(name = "manual_type", length = 80)
    private String manualType;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_status", nullable = false, length = 30)
    private CollectionItemStatus collectionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition", length = 30)
    private PhysicalCondition physicalCondition;

    @Column(name = "unit_number", length = 50)
    private String unitNumber;

    @Column(name = "total_limited_units")
    private Integer totalLimitedUnits;

    @Column
    private String notes;

    @Column(name = "acquired_at")
    private LocalDate acquiredAt;

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

    protected CollectionItem() {
    }

    public static CollectionItem create(
            Collection collection,
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            CollectionEditorialReferenceSource editorialReferenceSource,
            CollectionItemStatus collectionStatus,
            PhysicalCondition physicalCondition,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes,
            LocalDate acquiredAt,
            Long createdBy
    ) {
        CollectionItem item = new CollectionItem();
        item.collection = collection;
        item.masterProduct = masterProduct;
        item.catalogItem = catalogItem;
        item.catalogItemEdition = catalogItemEdition;
        item.editorialReferenceSource = editorialReferenceSource;
        item.collectionStatus = collectionStatus;
        item.physicalCondition = physicalCondition;
        item.unitNumber = unitNumber;
        item.totalLimitedUnits = totalLimitedUnits;
        item.notes = notes;
        item.acquiredAt = acquiredAt;
        item.createdAt = Instant.now();
        item.createdBy = createdBy;
        return item;
    }

    public static CollectionItem createManual(Collection collection, String manualTitle, String manualDescription, String manualType, CollectionItemStatus collectionStatus, PhysicalCondition physicalCondition, String unitNumber, Integer totalLimitedUnits, String notes, LocalDate acquiredAt, Long createdBy) {
        CollectionItem item = create(collection, null, null, null, CollectionEditorialReferenceSource.MANUAL, collectionStatus, physicalCondition, unitNumber, totalLimitedUnits, notes, acquiredAt, createdBy);
        item.manualTitle = manualTitle;
        item.manualDescription = manualDescription;
        item.manualType = manualType;
        return item;
    }

    public void updateReference(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            CollectionEditorialReferenceSource editorialReferenceSource,
            Long updatedBy
    ) {
        this.masterProduct = masterProduct;
        this.catalogItem = catalogItem;
        this.catalogItemEdition = catalogItemEdition;
        this.editorialReferenceSource = editorialReferenceSource;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void updateManualMetadata(
            String manualTitle,
            String manualDescription,
            String manualType,
            Long updatedBy
    ) {
        if (!isManual()) {
            throw new IllegalStateException("Only manual collection items can update manual metadata");
        }
        if (manualTitle == null || manualTitle.isBlank()) {
            throw new IllegalArgumentException("Manual title is required");
        }
        this.manualTitle = manualTitle;
        this.manualDescription = manualDescription;
        this.manualType = manualType;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void linkToCatalog(CatalogItem catalogItem, CatalogItemEdition catalogItemEdition, Long updatedBy) {
        if (!isManual()) {
            throw new IllegalStateException("Only manual collection items can link to the catalog");
        }
        if (catalogItem == null) {
            throw new IllegalArgumentException("Catalog item is required");
        }
        this.masterProduct = null;
        this.catalogItem = catalogItem;
        this.catalogItemEdition = catalogItemEdition;
        this.editorialReferenceSource = CollectionEditorialReferenceSource.MANUAL_EDITORIAL;
        this.manualTitle = null;
        this.manualDescription = null;
        this.manualType = null;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void update(
            CollectionItemStatus collectionStatus,
            PhysicalCondition physicalCondition,
            String unitNumber,
            Integer totalLimitedUnits,
            String notes,
            LocalDate acquiredAt,
            Long updatedBy
    ) {
        this.collectionStatus = collectionStatus;
        this.physicalCondition = physicalCondition;
        this.unitNumber = unitNumber;
        this.totalLimitedUnits = totalLimitedUnits;
        this.notes = notes;
        this.acquiredAt = acquiredAt;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void softDelete(Long deletedBy) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.updatedAt = this.deletedAt;
        this.updatedBy = deletedBy;
    }

    public Long getId() {
        return id;
    }

    public Collection getCollection() {
        return collection;
    }

    public MasterProduct getMasterProduct() {
        return masterProduct;
    }

    public CatalogItem getCatalogItem() { return catalogItem; }

    public CatalogItemEdition getCatalogItemEdition() { return catalogItemEdition; }

    public CollectionEditorialReferenceSource getEditorialReferenceSource() { return editorialReferenceSource; }
    public String getManualTitle() { return manualTitle; }
    public String getManualDescription() { return manualDescription; }
    public String getManualType() { return manualType; }
    public boolean isManual() {
        return editorialReferenceSource == CollectionEditorialReferenceSource.MANUAL
                && manualTitle != null
                && !manualTitle.isBlank()
                && masterProduct == null
                && catalogItem == null
                && catalogItemEdition == null;
    }

    public CollectionItemStatus getCollectionStatus() {
        return collectionStatus;
    }

    public PhysicalCondition getPhysicalCondition() {
        return physicalCondition;
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

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }
}
