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
import java.time.LocalDate;

@Entity
@Table(name = "catalog_item_editions")
public class CatalogItemEdition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Column(length = 32)
    private String isbn;

    @Column(length = 32)
    private String ean;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CatalogItemEditionFormat format;

    @Column(name = "edition_name", length = 240)
    private String editionName;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(length = 10)
    private String language;

    @Column(length = 2)
    private String country;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

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

    protected CatalogItemEdition() {
    }

    public static CatalogItemEdition create(
            CatalogItem catalogItem,
            Publisher publisher,
            String isbn,
            String ean,
            CatalogItemEditionFormat format,
            String editionName,
            LocalDate publicationDate,
            Integer publicationYear,
            String language,
            String country,
            Integer pageCount,
            String coverImageUrl,
            CatalogRecordStatus recordStatus,
            Long createdBy
    ) {
        CatalogItemEdition edition = new CatalogItemEdition();
        edition.apply(catalogItem, publisher, isbn, ean, format, editionName, publicationDate,
                publicationYear, language, country, pageCount, coverImageUrl, recordStatus);
        edition.createdAt = Instant.now();
        edition.createdBy = createdBy;
        return edition;
    }

    public void update(
            CatalogItem catalogItem,
            Publisher publisher,
            String isbn,
            String ean,
            CatalogItemEditionFormat format,
            String editionName,
            LocalDate publicationDate,
            Integer publicationYear,
            String language,
            String country,
            Integer pageCount,
            String coverImageUrl,
            CatalogRecordStatus recordStatus,
            Long updatedBy
    ) {
        apply(catalogItem, publisher, isbn, ean, format, editionName, publicationDate,
                publicationYear, language, country, pageCount, coverImageUrl, recordStatus);
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    private void apply(
            CatalogItem catalogItem,
            Publisher publisher,
            String isbn,
            String ean,
            CatalogItemEditionFormat format,
            String editionName,
            LocalDate publicationDate,
            Integer publicationYear,
            String language,
            String country,
            Integer pageCount,
            String coverImageUrl,
            CatalogRecordStatus recordStatus
    ) {
        this.catalogItem = catalogItem;
        this.publisher = publisher;
        this.isbn = isbn;
        this.ean = ean;
        this.format = format;
        this.editionName = editionName;
        this.publicationDate = publicationDate;
        this.publicationYear = publicationYear;
        this.language = language;
        this.country = country;
        this.pageCount = pageCount;
        this.coverImageUrl = coverImageUrl;
        this.recordStatus = recordStatus;
    }

    public Long getId() { return id; }
    public CatalogItem getCatalogItem() { return catalogItem; }
    public Publisher getPublisher() { return publisher; }
    public String getIsbn() { return isbn; }
    public String getEan() { return ean; }
    public CatalogItemEditionFormat getFormat() { return format; }
    public String getEditionName() { return editionName; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }
    public Integer getPageCount() { return pageCount; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public CatalogRecordStatus getRecordStatus() { return recordStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    public boolean isPubliclyVisible() {
        return recordStatus == CatalogRecordStatus.ACTIVE
                && deletedAt == null
                && catalogItem != null
                && catalogItem.isPubliclyVisible()
                && (publisher == null || publisher.isPubliclyVisible());
    }
}
