package com.collectohub.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "creators")
public class Creator {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 255) private String name;
    @Column(nullable = false, length = 255) private String slug;
    @Column(name = "sort_name", length = 255) private String sortName;
    @Column private String biography;
    @Column(length = 2) private String country;
    @Column(name = "birth_year") private Integer birthYear;
    @Column(name = "death_year") private Integer deathYear;
    @Enumerated(EnumType.STRING) @Column(name = "record_status", nullable = false, length = 30)
    private CatalogRecordStatus recordStatus;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_at") private Instant updatedAt;
    @Column(name = "updated_by") private Long updatedBy;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    protected Creator() {}

    public static Creator create(String name, String slug, String sortName, String biography,
                                 String country, Integer birthYear, Integer deathYear,
                                 CatalogRecordStatus status, Long actorId) {
        Creator creator = new Creator();
        creator.apply(name, slug, sortName, biography, country, birthYear, deathYear, status);
        creator.createdAt = Instant.now();
        creator.createdBy = actorId;
        return creator;
    }

    public void update(String name, String slug, String sortName, String biography, String country,
                       Integer birthYear, Integer deathYear, CatalogRecordStatus status, Long actorId) {
        apply(name, slug, sortName, biography, country, birthYear, deathYear, status);
        updatedAt = Instant.now(); updatedBy = actorId;
    }

    public void softDelete(Long actorId) { deletedAt = Instant.now(); deletedBy = actorId; }

    private void apply(String name, String slug, String sortName, String biography, String country,
                       Integer birthYear, Integer deathYear, CatalogRecordStatus status) {
        this.name = name; this.slug = slug; this.sortName = sortName; this.biography = biography;
        this.country = country; this.birthYear = birthYear; this.deathYear = deathYear;
        this.recordStatus = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getSortName() { return sortName; }
    public String getBiography() { return biography; }
    public String getCountry() { return country; }
    public Integer getBirthYear() { return birthYear; }
    public Integer getDeathYear() { return deathYear; }
    public CatalogRecordStatus getRecordStatus() { return recordStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isPubliclyVisible() { return recordStatus == CatalogRecordStatus.ACTIVE && deletedAt == null; }
}
