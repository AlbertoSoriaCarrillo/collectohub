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
import java.time.LocalDate;

@Entity
@Table(name = "catalog_items")
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private CatalogSeries series;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(name = "original_title", length = 240)
    private String originalTitle;

    @Column(name = "sequence_label", length = 50)
    private String sequenceLabel;

    @Column(name = "sort_order", precision = 10, scale = 3)
    private BigDecimal sortOrder;

    @Column
    private String description;

    @Column(name = "first_publication_date")
    private LocalDate firstPublicationDate;

    @Column(name = "first_publication_year")
    private Integer firstPublicationYear;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    @Column(name = "origin_country", length = 2)
    private String originCountry;

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

    protected CatalogItem() {
    }

    public static CatalogItem create(
            CatalogSeries series,
            String title,
            String originalTitle,
            String sequenceLabel,
            BigDecimal sortOrder,
            String description,
            LocalDate firstPublicationDate,
            Integer firstPublicationYear,
            String originalLanguage,
            String originCountry,
            CatalogRecordStatus recordStatus,
            Long createdBy
    ) {
        CatalogItem item = new CatalogItem();
        item.apply(series, title, originalTitle, sequenceLabel, sortOrder, description,
                firstPublicationDate, firstPublicationYear, originalLanguage, originCountry, recordStatus);
        item.createdAt = Instant.now();
        item.createdBy = createdBy;
        return item;
    }

    public void update(
            CatalogSeries series,
            String title,
            String originalTitle,
            String sequenceLabel,
            BigDecimal sortOrder,
            String description,
            LocalDate firstPublicationDate,
            Integer firstPublicationYear,
            String originalLanguage,
            String originCountry,
            CatalogRecordStatus recordStatus,
            Long updatedBy
    ) {
        apply(series, title, originalTitle, sequenceLabel, sortOrder, description,
                firstPublicationDate, firstPublicationYear, originalLanguage, originCountry, recordStatus);
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    private void apply(
            CatalogSeries series,
            String title,
            String originalTitle,
            String sequenceLabel,
            BigDecimal sortOrder,
            String description,
            LocalDate firstPublicationDate,
            Integer firstPublicationYear,
            String originalLanguage,
            String originCountry,
            CatalogRecordStatus recordStatus
    ) {
        this.series = series;
        this.title = title;
        this.originalTitle = originalTitle;
        this.sequenceLabel = sequenceLabel;
        this.sortOrder = sortOrder;
        this.description = description;
        this.firstPublicationDate = firstPublicationDate;
        this.firstPublicationYear = firstPublicationYear;
        this.originalLanguage = originalLanguage;
        this.originCountry = originCountry;
        this.recordStatus = recordStatus;
    }

    public Long getId() { return id; }
    public CatalogSeries getSeries() { return series; }
    public String getTitle() { return title; }
    public String getOriginalTitle() { return originalTitle; }
    public String getSequenceLabel() { return sequenceLabel; }
    public BigDecimal getSortOrder() { return sortOrder; }
    public String getDescription() { return description; }
    public LocalDate getFirstPublicationDate() { return firstPublicationDate; }
    public Integer getFirstPublicationYear() { return firstPublicationYear; }
    public String getOriginalLanguage() { return originalLanguage; }
    public String getOriginCountry() { return originCountry; }
    public CatalogRecordStatus getRecordStatus() { return recordStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    public boolean isPubliclyVisible() {
        return recordStatus == CatalogRecordStatus.ACTIVE
                && deletedAt == null
                && series != null
                && series.isPubliclyVisible();
    }
}
