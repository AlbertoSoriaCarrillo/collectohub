package com.collectohub.catalog.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "catalog_item_relationships")
public class CatalogItemRelationship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_catalog_item_id", nullable = false) private CatalogItem sourceCatalogItem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_catalog_item_id", nullable = false) private CatalogItem targetCatalogItem;
    @Enumerated(EnumType.STRING) @Column(name = "relationship_type", nullable = false, length = 40)
    private CatalogItemRelationshipType relationshipType;
    @Column(name = "relationship_order", nullable = false) private Integer relationshipOrder;
    @Column private String description;
    @Enumerated(EnumType.STRING) @Column(name = "record_status", nullable = false, length = 30)
    private CatalogRecordStatus recordStatus;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_at") private Instant updatedAt;
    @Column(name = "updated_by") private Long updatedBy;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    protected CatalogItemRelationship() {}

    public static CatalogItemRelationship create(CatalogItem source, CatalogItem target,
            CatalogItemRelationshipType type, Integer order, String description,
            CatalogRecordStatus status, Long actorId) {
        validate(source, target, type, order, status);
        CatalogItemRelationship relationship = new CatalogItemRelationship();
        relationship.apply(target, type, order, description, status);
        relationship.sourceCatalogItem = source;
        relationship.createdAt = Instant.now();
        relationship.createdBy = actorId;
        return relationship;
    }

    public void update(CatalogItem target, CatalogItemRelationshipType type, Integer order,
            String description, CatalogRecordStatus status, Long actorId) {
        validate(sourceCatalogItem, target, type, order, status);
        apply(target, type, order, description, status);
        updatedAt = Instant.now();
        updatedBy = actorId;
    }

    private void apply(CatalogItem target, CatalogItemRelationshipType type, Integer order,
            String description, CatalogRecordStatus status) {
        targetCatalogItem = target;
        relationshipType = type;
        relationshipOrder = order;
        this.description = description;
        recordStatus = status;
    }

    private static void validate(CatalogItem source, CatalogItem target, CatalogItemRelationshipType type,
            Integer order, CatalogRecordStatus status) {
        if (source == null || target == null || type == null || order == null || order <= 0 || status == null) {
            throw new IllegalArgumentException("Invalid catalog item relationship");
        }
        if (source == target || source.getId() != null && source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Source and target catalog items must be different");
        }
    }

    public void softDelete(Long actorId) { deletedAt = Instant.now(); deletedBy = actorId; }
    public Long getId() { return id; }
    public CatalogItem getSourceCatalogItem() { return sourceCatalogItem; }
    public CatalogItem getTargetCatalogItem() { return targetCatalogItem; }
    public CatalogItemRelationshipType getRelationshipType() { return relationshipType; }
    public Integer getRelationshipOrder() { return relationshipOrder; }
    public String getDescription() { return description; }
    public CatalogRecordStatus getRecordStatus() { return recordStatus; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isActive() { return deletedAt == null; }
    public boolean isPubliclyVisible() { return recordStatus == CatalogRecordStatus.ACTIVE && deletedAt == null; }
}
