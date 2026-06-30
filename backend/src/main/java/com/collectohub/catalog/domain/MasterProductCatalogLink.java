package com.collectohub.catalog.domain;

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
@Table(name = "master_product_catalog_links")
public class MasterProductCatalogLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_product_id", nullable = false)
    private MasterProduct masterProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_edition_id")
    private CatalogItemEdition catalogItemEdition;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_status", nullable = false, length = 30)
    private MasterProductCatalogLinkStatus linkStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_source", nullable = false, length = 40)
    private MasterProductCatalogLinkSource linkSource;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "match_reason")
    private String matchReason;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    protected MasterProductCatalogLink() {
    }

    public static MasterProductCatalogLink create(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            MasterProductCatalogLinkStatus linkStatus,
            MasterProductCatalogLinkSource linkSource,
            BigDecimal confidenceScore,
            String matchReason,
            String reviewNote,
            Long createdBy
    ) {
        MasterProductCatalogLink link = new MasterProductCatalogLink();
        link.apply(catalogItem, catalogItemEdition, linkStatus, linkSource,
                confidenceScore, matchReason, reviewNote);
        link.masterProduct = masterProduct;
        link.createdAt = Instant.now();
        link.createdBy = createdBy;
        return link;
    }

    public void update(
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            MasterProductCatalogLinkStatus linkStatus,
            MasterProductCatalogLinkSource linkSource,
            BigDecimal confidenceScore,
            String matchReason,
            String reviewNote,
            Long updatedBy
    ) {
        apply(catalogItem, catalogItemEdition, linkStatus, linkSource,
                confidenceScore, matchReason, reviewNote);
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public void changeStatus(MasterProductCatalogLinkStatus status, Long updatedBy) {
        this.linkStatus = status;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    private void apply(
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            MasterProductCatalogLinkStatus linkStatus,
            MasterProductCatalogLinkSource linkSource,
            BigDecimal confidenceScore,
            String matchReason,
            String reviewNote
    ) {
        this.catalogItem = catalogItem;
        this.catalogItemEdition = catalogItemEdition;
        this.linkStatus = linkStatus;
        this.linkSource = linkSource;
        this.confidenceScore = confidenceScore;
        this.matchReason = matchReason;
        this.reviewNote = reviewNote;
    }

    public Long getId() { return id; }
    public MasterProduct getMasterProduct() { return masterProduct; }
    public CatalogItem getCatalogItem() { return catalogItem; }
    public CatalogItemEdition getCatalogItemEdition() { return catalogItemEdition; }
    public MasterProductCatalogLinkStatus getLinkStatus() { return linkStatus; }
    public MasterProductCatalogLinkSource getLinkSource() { return linkSource; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public String getMatchReason() { return matchReason; }
    public String getReviewNote() { return reviewNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
