package com.collectohub.collections.domain;

import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.users.domain.User;
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
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 160)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionVisibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

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

    protected Collection() {
    }

    public static Collection create(
            User user,
            String name,
            String description,
            CollectionVisibility visibility,
            ProductCategory category
    ) {
        Collection collection = new Collection();
        collection.user = user;
        collection.name = name;
        collection.description = description;
        collection.visibility = visibility;
        collection.category = category;
        collection.createdAt = Instant.now();
        collection.createdBy = user.getId();
        return collection;
    }

    public void update(
            String name,
            String description,
            CollectionVisibility visibility,
            ProductCategory category,
            Long updatedBy
    ) {
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.category = category;
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

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CollectionVisibility getVisibility() {
        return visibility;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public boolean isPublic() {
        return visibility == CollectionVisibility.PUBLIC;
    }

    public boolean isOwnedBy(Long userId) {
        return user.getId().equals(userId);
    }
}
