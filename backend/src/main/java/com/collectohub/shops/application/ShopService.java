package com.collectohub.shops.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.dto.CreateShopRequest;
import com.collectohub.shops.dto.AddShopMemberRequest;
import com.collectohub.shops.dto.ChangeShopMemberRoleRequest;
import com.collectohub.shops.dto.ManagedShopResponse;
import com.collectohub.shops.dto.PublicShopResponse;
import com.collectohub.shops.dto.ShopMemberResponse;
import com.collectohub.shops.dto.UpdateShopRequest;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.auth.application.RoleNotConfiguredException;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
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
    private static final String SHOP_MEMBERSHIP_UNIQUE_CONSTRAINT = "uk_shop_members_shop_user";

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
    public ManagedShopResponse createShop(AuthenticatedUser authenticatedUser, CreateShopRequest request) {
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
        return ManagedShopResponse.from(savedShop, ownerMember);
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
    public List<ManagedShopResponse> myShops(AuthenticatedUser authenticatedUser) {
        return shopMemberRepository.findByUser_IdAndStatusAndDeletedAtIsNull(
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                ).stream()
                .filter(member -> member.getShop().isActive())
                .map(member -> ManagedShopResponse.from(member.getShop(), member))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicShopResponse getPublicShop(Long shopId) {
        Shop shop = findActiveShop(shopId);
        return PublicShopResponse.from(shop);
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
    public ShopMemberResponse addMember(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            AddShopMemberRequest request
    ) {
        Shop shop = findActiveShop(shopId);
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getRole() == ShopMemberRole.OWNER)
                .orElseThrow(() -> new AccessDeniedException("User cannot add members to this shop"));

        if (request.role() == ShopMemberRole.OWNER) {
            throw new InvalidShopMemberRoleException();
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(User::isActive)
                .orElseThrow(ShopMemberCandidateNotFoundException::new);
        if (shopMemberRepository.findByShop_IdAndUser_Id(shopId, user.getId()).isPresent()) {
            throw new ShopMembershipAlreadyExistsException();
        }

        try {
            ShopMember member = shopMemberRepository.saveAndFlush(ShopMember.active(
                    shop,
                    user,
                    request.role(),
                    authenticatedUser.id()
            ));
            return ShopMemberResponse.from(member);
        } catch (DataIntegrityViolationException ex) {
            if (isConstraintViolation(ex, SHOP_MEMBERSHIP_UNIQUE_CONSTRAINT)) {
                throw new ShopMembershipAlreadyExistsException();
            }
            throw ex;
        }
    }

    @Transactional
    public ShopMemberResponse changeMemberRole(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            Long memberId,
            ChangeShopMemberRoleRequest request
    ) {
        findActiveShop(shopId);
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getRole() == ShopMemberRole.OWNER)
                .orElseThrow(() -> new AccessDeniedException("User cannot change members in this shop"));

        if (request.role() == ShopMemberRole.OWNER) {
            throw new InvalidShopMemberRoleException();
        }

        ShopMember member = shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                        memberId,
                        shopId,
                        ShopMemberStatus.ACTIVE
                )
                .orElseThrow(ShopMemberNotFoundException::new);
        if (member.getRole() == ShopMemberRole.OWNER) {
            throw new InvalidShopMemberRoleException();
        }

        member.changeRole(request.role(), authenticatedUser.id());
        return ShopMemberResponse.from(member);
    }

    @Transactional
    public void deactivateMember(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            Long memberId
    ) {
        findActiveShop(shopId);
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getRole() == ShopMemberRole.OWNER)
                .orElseThrow(() -> new AccessDeniedException("User cannot deactivate members in this shop"));

        ShopMember member = shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                        memberId,
                        shopId,
                        ShopMemberStatus.ACTIVE
                )
                .orElseThrow(ShopMemberNotFoundException::new);
        if (member.getRole() == ShopMemberRole.OWNER) {
            throw new ShopOwnerCannotBeDeactivatedException();
        }

        member.deactivate(authenticatedUser.id());
    }

    @Transactional
    public ManagedShopResponse updateShop(AuthenticatedUser authenticatedUser, Long shopId, UpdateShopRequest request) {
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

        return ManagedShopResponse.from(shop, member);
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

    private boolean isConstraintViolation(Throwable throwable, String constraintName) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && constraintName.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
