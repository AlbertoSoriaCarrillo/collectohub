package com.collectohub.reservations.domain;

import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.shops.domain.Shop;
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

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_product_id", nullable = false)
    private ShopProduct shopProduct;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "user_message")
    private String userMessage;

    @Column(name = "shop_response")
    private String shopResponse;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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

    protected Reservation() {
    }

    public static Reservation create(User user, ShopProduct shopProduct, Integer quantity, String userMessage) {
        Instant now = Instant.now();
        Reservation reservation = new Reservation();
        reservation.user = user;
        reservation.shop = shopProduct.getShop();
        reservation.shopProduct = shopProduct;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.PENDING;
        reservation.userMessage = userMessage;
        reservation.expiresAt = now.plus(Duration.ofHours(reservation.shop.getDefaultReservationExpirationHours()));
        reservation.createdAt = now;
        reservation.createdBy = user.getId();
        return reservation;
    }

    public void updateFromShop(ReservationStatus newStatus, String shopResponse, Long updatedBy) {
        this.status = newStatus;
        this.shopResponse = shopResponse;
        Instant now = Instant.now();
        this.updatedAt = now;
        this.updatedBy = updatedBy;
        if (newStatus == ReservationStatus.COMPLETED) {
            this.completedAt = now;
        }
    }

    public void cancelByUser(Long updatedBy) {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Shop getShop() {
        return shop;
    }

    public ShopProduct getShopProduct() {
        return shopProduct;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getShopResponse() {
        return shopResponse;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public boolean isOwnedBy(Long userId) {
        return user.getId().equals(userId);
    }

    public boolean belongsToShop(Long shopId) {
        return shop.getId().equals(shopId);
    }

    public boolean canUserCancel() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.ACCEPTED;
    }
}
