package com.collectohub.reservations.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.inventory.application.ShopProductNotFoundException;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.reservations.domain.Reservation;
import com.collectohub.reservations.domain.ReservationStatus;
import com.collectohub.reservations.dto.CreateReservationRequest;
import com.collectohub.reservations.dto.ReservationResponse;
import com.collectohub.reservations.dto.UpdateReservationStatusRequest;
import com.collectohub.reservations.infrastructure.ReservationRepository;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ShopProductRepository shopProductRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ShopProductRepository shopProductRepository,
            ShopMemberRepository shopMemberRepository,
            UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.shopProductRepository = shopProductRepository;
        this.shopMemberRepository = shopMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse createReservation(
            AuthenticatedUser authenticatedUser,
            CreateReservationRequest request
    ) {
        User user = currentUser(authenticatedUser);
        Integer quantity = validateQuantity(Objects.requireNonNullElse(request.quantity(), 1));
        if (request.shopProductId() == null) {
            throw new InvalidReservationRequestException("shopProductId is required");
        }
        ShopProduct shopProduct = findReservableShopProduct(request.shopProductId());
        if (quantity > shopProduct.getStockQuantity()) {
            throw new ReservationUnavailableException("Requested quantity exceeds available stock");
        }

        Reservation reservation = Reservation.create(
                user,
                shopProduct,
                quantity,
                normalizeNullable(request.userMessage())
        );
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(
            AuthenticatedUser authenticatedUser,
            String status,
            Long shopId
    ) {
        Long userId = requireAuthenticated(authenticatedUser).id();
        Specification<Reservation> specification = activeReservations()
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId));
        ReservationStatus parsedStatus = parseStatus(status);
        if (parsedStatus != null) {
            specification = specification.and(statusEquals(parsedStatus));
        }
        if (shopId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("shop").get("id"), shopId));
        }
        return reservationRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(AuthenticatedUser authenticatedUser, Long reservationId) {
        AuthenticatedUser user = requireAuthenticated(authenticatedUser);
        Reservation reservation = findActiveReservation(reservationId);
        if (!reservation.isOwnedBy(user.id()) && !canManageShop(user, reservation.getShop().getId())) {
            throw new AccessDeniedException("User cannot access this reservation");
        }
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> shopReservations(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            String status,
            Long userId,
            Long shopProductId
    ) {
        ensureCanManageShop(authenticatedUser, shopId);
        Specification<Reservation> specification = activeReservations()
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("shop").get("id"), shopId));
        ReservationStatus parsedStatus = parseStatus(status);
        if (parsedStatus != null) {
            specification = specification.and(statusEquals(parsedStatus));
        }
        if (userId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("user").get("id"), userId));
        }
        if (shopProductId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("shopProduct").get("id"), shopProductId));
        }
        return reservationRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse updateReservationStatus(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            Long reservationId,
            UpdateReservationStatusRequest request
    ) {
        ensureCanManageShop(authenticatedUser, shopId);
        Reservation reservation = findActiveReservation(reservationId);
        if (!reservation.belongsToShop(shopId)) {
            throw new ReservationNotFoundException(reservationId);
        }
        ReservationStatus newStatus = request.status();
        if (!canShopTransition(reservation.getStatus(), newStatus)) {
            throw new InvalidReservationTransitionException(
                    "Invalid reservation status transition: " + reservation.getStatus() + " -> " + newStatus
            );
        }
        reservation.updateFromShop(newStatus, normalizeNullable(request.shopResponse()), authenticatedUser.id());
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(AuthenticatedUser authenticatedUser, Long reservationId) {
        AuthenticatedUser user = requireAuthenticated(authenticatedUser);
        Reservation reservation = findActiveReservation(reservationId);
        if (!reservation.isOwnedBy(user.id())) {
            throw new AccessDeniedException("User cannot cancel this reservation");
        }
        if (!reservation.canUserCancel()) {
            throw new InvalidReservationTransitionException("Reservation cannot be cancelled from status " + reservation.getStatus());
        }
        reservation.cancelByUser(user.id());
        return ReservationResponse.from(reservation);
    }

    private User currentUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(requireAuthenticated(authenticatedUser).id())
                .filter(User::isActive)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is not available"));
    }

    private AuthenticatedUser requireAuthenticated(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authenticatedUser;
    }

    private ShopProduct findReservableShopProduct(Long shopProductId) {
        ShopProduct shopProduct = shopProductRepository.findByIdAndDeletedAtIsNull(shopProductId)
                .orElseThrow(() -> new ShopProductNotFoundException(shopProductId));
        if (!shopProduct.isPubliclyVisible()
                || !shopProduct.getShop().isActive()
                || !shopProduct.hasPublicReference()) {
            throw new ReservationUnavailableException("Shop product cannot be reserved");
        }
        if (shopProduct.getStockQuantity() <= 0) {
            throw new ReservationUnavailableException("Shop product has no stock available");
        }
        return shopProduct;
    }

    private Reservation findActiveReservation(Long reservationId) {
        return reservationRepository.findByIdAndDeletedAtIsNull(reservationId)
                .filter(Reservation::isActive)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    private Integer validateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new InvalidReservationRequestException("quantity must be greater than 0");
        }
        return quantity;
    }

    private ReservationStatus parseStatus(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidReservationFilterException("Unsupported reservation status: " + value);
        }
    }

    private boolean canShopTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == ReservationStatus.ACCEPTED || newStatus == ReservationStatus.REJECTED;
            case ACCEPTED -> newStatus == ReservationStatus.COMPLETED || newStatus == ReservationStatus.CANCELLED;
            case REJECTED, CANCELLED, EXPIRED, COMPLETED -> false;
        };
    }

    private void ensureCanManageShop(AuthenticatedUser authenticatedUser, Long shopId) {
        if (!canManageShop(requireAuthenticated(authenticatedUser), shopId)) {
            throw new AccessDeniedException("User cannot manage this shop reservations");
        }
    }

    private boolean canManageShop(AuthenticatedUser authenticatedUser, Long shopId) {
        return shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getShop().isActive())
                .filter(member -> member.getUser().isActive())
                .filter(member -> member.getRole().canManageShop())
                .isPresent();
    }

    private Specification<Reservation> activeReservations() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    private Specification<Reservation> statusEquals(ReservationStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    private String normalizeCode(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
