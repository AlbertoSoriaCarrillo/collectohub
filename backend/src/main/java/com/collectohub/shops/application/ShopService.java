package com.collectohub.shops.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.dto.CreateShopRequest;
import com.collectohub.shops.dto.ShopMemberResponse;
import com.collectohub.shops.dto.ShopResponse;
import com.collectohub.shops.dto.UpdateShopRequest;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.auth.application.RoleNotConfiguredException;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ShopService {

    private static final List<ShopMemberRole> MANAGER_ROLES = List.of(
            ShopMemberRole.OWNER,
            ShopMemberRole.MANAGER
    );
    private static final String SHOP_OWNER_GLOBAL_ROLE = "SHOP_OWNER";

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopProperties shopProperties;

    public ShopService(
            ShopRepository shopRepository,
            ShopMemberRepository shopMemberRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ShopProperties shopProperties
    ) {
        this.shopRepository = shopRepository;
        this.shopMemberRepository = shopMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.shopProperties = shopProperties;
    }

    @Transactional
    public ShopResponse createShop(AuthenticatedUser authenticatedUser, CreateShopRequest request) {
        User owner = currentUser(authenticatedUser);
        ensureShopOwnerGlobalRole(owner);
        Shop shop = Shop.create(
                owner,
                normalizeRequired(request.name()),
                normalizeNullable(request.description()),
                normalizeEmail(request.contactEmail()),
                normalizeNullable(request.contactPhone()),
                normalizeUppercase(request.country()),
                normalizeCurrencyOrDefault(request.currency()),
                request.defaultReservationExpirationHours() == null
                        ? shopProperties.defaultReservationExpirationHours()
                        : request.defaultReservationExpirationHours(),
                normalizeNullable(request.logoUrl())
        );

        Shop savedShop = shopRepository.save(shop);
        ShopMember ownerMember = shopMemberRepository.save(ShopMember.owner(savedShop, owner));
        return ShopResponse.from(savedShop, ownerMember);
    }

    private void ensureShopOwnerGlobalRole(User owner) {
        if (owner.hasRole(SHOP_OWNER_GLOBAL_ROLE)) {
            return;
        }
        Role shopOwnerRole = roleRepository.findByCode(SHOP_OWNER_GLOBAL_ROLE)
                .orElseThrow(() -> new RoleNotConfiguredException(SHOP_OWNER_GLOBAL_ROLE));
        owner.addRole(shopOwnerRole);
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> myShops(AuthenticatedUser authenticatedUser) {
        return shopMemberRepository.findByUser_IdAndStatusAndDeletedAtIsNull(
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                ).stream()
                .filter(member -> member.getShop().isActive())
                .map(member -> ShopResponse.from(member.getShop(), member))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopResponse getPublicShop(Long shopId) {
        Shop shop = findActiveShop(shopId);
        return ShopResponse.publicFrom(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopMemberResponse> listMembers(AuthenticatedUser authenticatedUser, Long shopId) {
        findActiveShop(shopId);
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(ShopMember::canManageShop)
                .orElseThrow(() -> new AccessDeniedException("User cannot list this shop's members"));

        return shopMemberRepository.findByShop_IdAndStatusAndDeletedAtIsNullOrderByIdAsc(
                        shopId,
                        ShopMemberStatus.ACTIVE
                ).stream()
                .map(ShopMemberResponse::from)
                .toList();
    }

    @Transactional
    public ShopResponse updateShop(AuthenticatedUser authenticatedUser, Long shopId, UpdateShopRequest request) {
        Shop shop = findActiveShop(shopId);
        ShopMember member = shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(ShopMember::canManageShop)
                .orElseThrow(() -> new AccessDeniedException("User cannot manage this shop"));

        shop.update(
                normalizeRequiredOrExisting(request.name(), shop.getName()),
                normalizeNullableOrExisting(request.description(), shop.getDescription()),
                normalizeEmailOrExisting(request.contactEmail(), shop.getContactEmail()),
                normalizeNullableOrExisting(request.contactPhone(), shop.getContactPhone()),
                normalizeUppercaseOrExisting(request.country(), shop.getCountry()),
                normalizeCurrencyOrExisting(request.currency(), shop.getCurrency()),
                request.defaultReservationExpirationHours() == null
                        ? shop.getDefaultReservationExpirationHours()
                        : request.defaultReservationExpirationHours(),
                normalizeNullableOrExisting(request.logoUrl(), shop.getLogoUrl()),
                authenticatedUser.id()
        );

        return ShopResponse.from(shop, member);
    }

    private User currentUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.id())
                .filter(User::isActive)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is not available"));
    }

    private Shop findActiveShop(Long shopId) {
        return shopRepository.findByIdAndDeletedAtIsNull(shopId)
                .filter(Shop::isActive)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeRequiredOrExisting(String value, String existing) {
        if (value == null) {
            return existing;
        }
        return normalizeRequired(value);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullableOrExisting(String value, String existing) {
        return value == null ? existing : normalizeNullable(value);
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeEmailOrExisting(String value, String existing) {
        return value == null ? existing : normalizeEmail(value);
    }

    private String normalizeUppercase(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeUppercaseOrExisting(String value, String existing) {
        return value == null ? existing : normalizeUppercase(value);
    }

    private String normalizeCurrencyOrDefault(String value) {
        return Objects.requireNonNullElse(normalizeUppercase(value), shopProperties.defaultCurrency().toUpperCase(Locale.ROOT));
    }

    private String normalizeCurrencyOrExisting(String value, String existing) {
        return value == null ? existing : normalizeUppercase(value);
    }
}
