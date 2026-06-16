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
@Table(name = "shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @Column(nullable = false, length = 160)
    private String name;

    @Column
    private String description;

    @Column(name = "contact_email", length = 320)
    private String contactEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(length = 2)
    private String country;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "default_reservation_expiration_hours", nullable = false)
    private Integer defaultReservationExpirationHours;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopStatus status;

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

    protected Shop() {
    }

    public static Shop create(
            User ownerUser,
            String name,
            String description,
            String contactEmail,
            String contactPhone,
            String country,
            String currency,
            Integer defaultReservationExpirationHours,
            String logoUrl
    ) {
        Shop shop = new Shop();
        shop.ownerUser = ownerUser;
        shop.name = name;
        shop.description = description;
        shop.contactEmail = contactEmail;
        shop.contactPhone = contactPhone;
        shop.country = country;
        shop.currency = currency;
        shop.defaultReservationExpirationHours = defaultReservationExpirationHours;
        shop.logoUrl = logoUrl;
        shop.status = ShopStatus.ACTIVE;
        shop.createdAt = Instant.now();
        shop.createdBy = ownerUser.getId();
        return shop;
    }

    public void update(
            String name,
            String description,
            String contactEmail,
            String contactPhone,
            String country,
            String currency,
            Integer defaultReservationExpirationHours,
            String logoUrl,
            Long updatedBy
    ) {
        this.name = name;
        this.description = description;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.country = country;
        this.currency = currency;
        this.defaultReservationExpirationHours = defaultReservationExpirationHours;
        this.logoUrl = logoUrl;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getDefaultReservationExpirationHours() {
        return defaultReservationExpirationHours;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isActive() {
        return status == ShopStatus.ACTIVE && deletedAt == null;
    }
}
