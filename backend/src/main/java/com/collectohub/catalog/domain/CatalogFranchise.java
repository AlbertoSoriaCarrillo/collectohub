package com.collectohub.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "catalog_franchises")
public class CatalogFranchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 160)
    private String slug;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_status", nullable = false, length = 30)
    private CatalogRecordStatus recordStatus;

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

    protected CatalogFranchise() {
    }

    public static CatalogFranchise create(
            String name,
            String slug,
            String description,
            CatalogRecordStatus recordStatus,
            Long createdBy
    ) {
        CatalogFranchise franchise = new CatalogFranchise();
        franchise.name = name;
        franchise.slug = slug;
        franchise.description = description;
        franchise.recordStatus = recordStatus;
        franchise.createdAt = Instant.now();
        franchise.createdBy = createdBy;
        return franchise;
    }

    public void update(
            String name,
            String slug,
            String description,
            CatalogRecordStatus recordStatus,
            Long updatedBy
    ) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.recordStatus = recordStatus;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public CatalogRecordStatus getRecordStatus() {
        return recordStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isPubliclyVisible() {
        return recordStatus == CatalogRecordStatus.ACTIVE && deletedAt == null;
    }
}
