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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "master_products")
public class MasterProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 240)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(length = 160)
    private String franchise;

    @Column(name = "collection_name", length = 160)
    private String collectionName;

    @Column(name = "volume_number", length = 50)
    private String volumeNumber;

    @Column(length = 160)
    private String publisher;

    @Column(length = 20)
    private String isbn;

    @Column(length = 20)
    private String ean;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "edition_start_date")
    private LocalDate editionStartDate;

    @Column(name = "edition_end_date")
    private LocalDate editionEndDate;

    @Column(name = "product_language", length = 10)
    private String productLanguage;

    @Column(name = "is_limited_edition", nullable = false)
    private boolean limitedEdition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "publication_countries", nullable = false, columnDefinition = "jsonb")
    private List<String> publicationCountries = new ArrayList<>();

    @Column(name = "cover_image_url", length = 2048)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MasterProductStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new LinkedHashMap<>();

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

    protected MasterProduct() {
    }

    public static MasterProduct create(
            String name,
            String description,
            ProductCategory category,
            String franchise,
            String collectionName,
            String volumeNumber,
            String publisher,
            String isbn,
            String ean,
            LocalDate releaseDate,
            LocalDate editionStartDate,
            LocalDate editionEndDate,
            String productLanguage,
            boolean limitedEdition,
            List<String> publicationCountries,
            String coverImageUrl,
            Map<String, Object> attributes,
            Long createdBy
    ) {
        MasterProduct product = new MasterProduct();
        product.name = name;
        product.description = description;
        product.category = category;
        product.franchise = franchise;
        product.collectionName = collectionName;
        product.volumeNumber = volumeNumber;
        product.publisher = publisher;
        product.isbn = isbn;
        product.ean = ean;
        product.releaseDate = releaseDate;
        product.editionStartDate = editionStartDate;
        product.editionEndDate = editionEndDate;
        product.productLanguage = productLanguage;
        product.limitedEdition = limitedEdition;
        product.publicationCountries = new ArrayList<>(publicationCountries);
        product.coverImageUrl = coverImageUrl;
        product.status = MasterProductStatus.ACTIVE;
        product.attributes = new LinkedHashMap<>(attributes);
        product.createdAt = Instant.now();
        product.createdBy = createdBy;
        return product;
    }

    public void update(
            String name,
            String description,
            ProductCategory category,
            String franchise,
            String collectionName,
            String volumeNumber,
            String publisher,
            String isbn,
            String ean,
            LocalDate releaseDate,
            LocalDate editionStartDate,
            LocalDate editionEndDate,
            String productLanguage,
            boolean limitedEdition,
            List<String> publicationCountries,
            String coverImageUrl,
            Map<String, Object> attributes,
            Long updatedBy
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.franchise = franchise;
        this.collectionName = collectionName;
        this.volumeNumber = volumeNumber;
        this.publisher = publisher;
        this.isbn = isbn;
        this.ean = ean;
        this.releaseDate = releaseDate;
        this.editionStartDate = editionStartDate;
        this.editionEndDate = editionEndDate;
        this.productLanguage = productLanguage;
        this.limitedEdition = limitedEdition;
        this.publicationCountries = new ArrayList<>(publicationCountries);
        this.coverImageUrl = coverImageUrl;
        this.attributes = new LinkedHashMap<>(attributes);
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public String getFranchise() {
        return franchise;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getVolumeNumber() {
        return volumeNumber;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getEan() {
        return ean;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public LocalDate getEditionStartDate() {
        return editionStartDate;
    }

    public LocalDate getEditionEndDate() {
        return editionEndDate;
    }

    public String getProductLanguage() {
        return productLanguage;
    }

    public boolean isLimitedEdition() {
        return limitedEdition;
    }

    public List<String> getPublicationCountries() {
        return List.copyOf(publicationCountries);
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public MasterProductStatus getStatus() {
        return status;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return status == MasterProductStatus.ACTIVE && deletedAt == null;
    }
}
