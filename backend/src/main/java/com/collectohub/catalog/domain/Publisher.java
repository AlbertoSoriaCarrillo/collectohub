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
@Table(name = "publishers")
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2)
    private String country;

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

    protected Publisher() {
    }

    public static Publisher create(
            String name,
            String country,
            CatalogRecordStatus recordStatus,
            Long createdBy
    ) {
        Publisher publisher = new Publisher();
        publisher.name = name;
        publisher.country = country;
        publisher.recordStatus = recordStatus;
        publisher.createdAt = Instant.now();
        publisher.createdBy = createdBy;
        return publisher;
    }

    public void update(
            String name,
            String country,
            CatalogRecordStatus recordStatus,
            Long updatedBy
    ) {
        this.name = name;
        this.country = country;
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

    public String getCountry() {
        return country;
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
