package com.collectohub.shops.domain;

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
@Table(name = "shop_members")
public class ShopMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopMemberStatus status;

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

    protected ShopMember() {
    }

    public static ShopMember owner(Shop shop, User user) {
        ShopMember member = new ShopMember();
        member.shop = shop;
        member.user = user;
        member.role = ShopMemberRole.OWNER;
        member.status = ShopMemberStatus.ACTIVE;
        member.createdAt = Instant.now();
        member.createdBy = user.getId();
        return member;
    }

    public Long getId() {
        return id;
    }

    public Shop getShop() {
        return shop;
    }

    public User getUser() {
        return user;
    }

    public ShopMemberRole getRole() {
        return role;
    }

    public ShopMemberStatus getStatus() {
        return status;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean canManageShop() {
        return status == ShopMemberStatus.ACTIVE && deletedAt == null && role.canManageShop();
    }
}
