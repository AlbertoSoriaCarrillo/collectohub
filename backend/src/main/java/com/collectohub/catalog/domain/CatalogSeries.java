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

import java.time.Instant;

@Entity
@Table(name = "catalog_series")
public class CatalogSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id")
    private CatalogFranchise franchise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_publisher_id")
    private Publisher primaryPublisher;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(name = "original_title", length = 240)
    private String originalTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CatalogSeriesType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 30)
    private CatalogPublicationStatus publicationStatus;

    @Column
    private String description;

    @Column(name = "origin_country", length = 2)
    private String originCountry;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

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

    protected CatalogSeries() {
    }

    public static CatalogSeries create(
            CatalogFranchise franchise,
            Publisher primaryPublisher,
            String title,
            String originalTitle,
            CatalogSeriesType type,
            CatalogPublicationStatus publicationStatus,
            String description,
            String originCountry,
            String originalLanguage,
            Integer startYear,
            Integer endYear,
            CatalogRecordStatus recordStatus,
            Long createdBy
    ) {
        CatalogSeries series = new CatalogSeries();
        series.apply(
                franchise,
                primaryPublisher,
                title,
                originalTitle,
                type,
                publicationStatus,
                description,
                originCountry,
                originalLanguage,
                startYear,
                endYear,
                recordStatus
        );
        series.createdAt = Instant.now();
        series.createdBy = createdBy;
        return series;
    }

    public void update(
            CatalogFranchise franchise,
            Publisher primaryPublisher,
            String title,
            String originalTitle,
            CatalogSeriesType type,
            CatalogPublicationStatus publicationStatus,
            String description,
            String originCountry,
            String originalLanguage,
            Integer startYear,
            Integer endYear,
            CatalogRecordStatus recordStatus,
            Long updatedBy
    ) {
        apply(
                franchise,
                primaryPublisher,
                title,
                originalTitle,
                type,
                publicationStatus,
                description,
                originCountry,
                originalLanguage,
                startYear,
                endYear,
                recordStatus
        );
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    private void apply(
            CatalogFranchise franchise,
            Publisher primaryPublisher,
            String title,
            String originalTitle,
            CatalogSeriesType type,
            CatalogPublicationStatus publicationStatus,
            String description,
            String originCountry,
            String originalLanguage,
            Integer startYear,
            Integer endYear,
            CatalogRecordStatus recordStatus
    ) {
        this.franchise = franchise;
        this.primaryPublisher = primaryPublisher;
        this.title = title;
        this.originalTitle = originalTitle;
        this.type = type;
        this.publicationStatus = publicationStatus;
        this.description = description;
        this.originCountry = originCountry;
        this.originalLanguage = originalLanguage;
        this.startYear = startYear;
        this.endYear = endYear;
        this.recordStatus = recordStatus;
    }

    public Long getId() {
        return id;
    }

    public CatalogFranchise getFranchise() {
        return franchise;
    }

    public Publisher getPrimaryPublisher() {
        return primaryPublisher;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public CatalogSeriesType getType() {
        return type;
    }

    public CatalogPublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Integer getEndYear() {
        return endYear;
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
