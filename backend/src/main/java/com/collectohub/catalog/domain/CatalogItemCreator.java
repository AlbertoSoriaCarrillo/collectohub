package com.collectohub.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "catalog_item_creators")
public class CatalogItemCreator {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;
    @Enumerated(EnumType.STRING) @Column(name = "credit_role", nullable = false, length = 40)
    private CreatorCreditRole creditRole;
    @Column(name = "credit_order", nullable = false) private Integer creditOrder;
    @Column(name = "credit_label", length = 255) private String creditLabel;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_at") private Instant updatedAt;
    @Column(name = "updated_by") private Long updatedBy;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;

    protected CatalogItemCreator() {}

    public static CatalogItemCreator create(CatalogItem item, Creator creator, CreatorCreditRole role,
                                            Integer order, String label, Long actorId) {
        CatalogItemCreator credit = new CatalogItemCreator();
        credit.catalogItem = item; credit.creator = creator; credit.creditRole = role;
        credit.creditOrder = order; credit.creditLabel = label;
        credit.createdAt = Instant.now(); credit.createdBy = actorId;
        return credit;
    }

    public void update(CreatorCreditRole role, Integer order, String label, Long actorId) {
        creditRole = role; creditOrder = order; creditLabel = label;
        updatedAt = Instant.now(); updatedBy = actorId;
    }
    public void softDelete(Long actorId) { deletedAt = Instant.now(); deletedBy = actorId; }
    public Long getId() { return id; }
    public CatalogItem getCatalogItem() { return catalogItem; }
    public Creator getCreator() { return creator; }
    public CreatorCreditRole getCreditRole() { return creditRole; }
    public Integer getCreditOrder() { return creditOrder; }
    public String getCreditLabel() { return creditLabel; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isActive() { return deletedAt == null; }
}
