package com.collectohub.reservations.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.inventory.application.ShopProductNotFoundException;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.domain.ShopProductEditorialReferenceSource;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.reservations.domain.Reservation;
import com.collectohub.reservations.domain.ReservationStatus;
import com.collectohub.reservations.dto.CreateReservationRequest;
import com.collectohub.reservations.dto.UpdateReservationStatusRequest;
import com.collectohub.reservations.infrastructure.ReservationRepository;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ShopProductRepository shopProductRepository;

    @Mock
    private ShopMemberRepository shopMemberRepository;

    @Mock
    private UserRepository userRepository;

    private ReservationService reservationService;
    private User user;
    private User otherUser;
    private User owner;
    private User manager;
    private ProductCategory category;
    private MasterProduct masterProduct;
    private Shop shop;
    private Shop otherShop;
    private ShopProduct shopProduct;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                shopProductRepository,
                shopMemberRepository,
                userRepository
        );
        user = user(42L, "alice@example.com");
        otherUser = user(43L, "bob@example.com");
        owner = user(50L, "owner@example.com");
        manager = user(51L, "manager@example.com");
        category = withId(new ProductCategory("MANGA_COMIC", "Manga and comic"), 10L);
        masterProduct = masterProduct(200L);
        shop = shop(owner, 500L, "Collector Cave");
        otherShop = shop(owner, 501L, "Other Shop");
        shopProduct = shopProduct(900L, shop, masterProduct, ShopProductCommercialStatus.AVAILABLE, true, 3);
    }

    @Test
    void createsReservationForAvailableShopProduct() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(shopProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 700L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, null, "Please hold it")
        );

        assertThat(response.id()).isEqualTo(700L);
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.shopId()).isEqualTo(500L);
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.productName()).isEqualTo("Dragon Ball 1");
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    void createsReservationForPureEditorialShopProduct() {
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(501L);
        when(catalogItem.getTitle()).thenReturn("Akira 1");
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        ShopProduct editorialProduct = withId(ShopProduct.create(
                shop, null, catalogItem, null, ShopProductEditorialReferenceSource.MANUAL_EDITORIAL,
                BigDecimal.TEN, "EUR", 2, ShopProductCommercialStatus.AVAILABLE,
                PhysicalCondition.NEW, true, null, null, null, owner.getId()
        ), 901L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(901L)).thenReturn(Optional.of(editorialProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 701L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(901L, 1, null));

        assertThat(response.masterProductId()).isNull();
        assertThat(response.productName()).isEqualTo("Akira 1");
        assertThat(response.catalogItemId()).isEqualTo(501L);
    }

    @Test
    void createsReservationWhenInactiveLegacyReferenceHasPublicEditorialReference() {
        MasterProduct inactiveLegacy = masterProduct(201L);
        ReflectionTestUtils.setField(inactiveLegacy, "deletedAt", Instant.now());
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(501L);
        when(catalogItem.getTitle()).thenReturn("Akira 1");
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        ShopProduct bridgedProduct = referencedShopProduct(902L, inactiveLegacy, catalogItem, null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(902L)).thenReturn(Optional.of(bridgedProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 702L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(902L, 1, null));

        assertThat(response.productName()).isEqualTo("Akira 1");
        assertThat(response.catalogItemId()).isEqualTo(501L);
    }

    @Test
    void productWithoutAnyPublicReferenceCannotBeReserved() {
        MasterProduct inactiveLegacy = masterProduct(201L);
        ReflectionTestUtils.setField(inactiveLegacy, "deletedAt", Instant.now());
        CatalogItem archivedItem = mock(CatalogItem.class);
        ShopProduct privateProduct = referencedShopProduct(903L, inactiveLegacy, archivedItem, null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(903L)).thenReturn(Optional.of(privateProduct));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(903L, 1, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void reservationProductNamePrefersEditionThenEditorialItemBeforeLegacy() {
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(501L);
        when(catalogItem.getTitle()).thenReturn("Akira 1");
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        CatalogItemEdition edition = mock(CatalogItemEdition.class);
        when(edition.getId()).thenReturn(601L);
        when(edition.getEditionName()).thenReturn("Deluxe edition");
        when(edition.isPubliclyVisible()).thenReturn(true);
        ShopProduct bridgedProduct = referencedShopProduct(904L, masterProduct, catalogItem, edition);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(904L)).thenReturn(Optional.of(bridgedProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 704L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(904L, 1, null));

        assertThat(response.productName()).isEqualTo("Deluxe edition");
        assertThat(response.catalogItemTitle()).isEqualTo("Akira 1");
        assertThat(response.masterProductId()).isEqualTo(200L);
    }

    @Test
    void createsReservationForPureEditorialEditionProduct() {
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(501L);
        when(catalogItem.getTitle()).thenReturn("Akira 1");
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        CatalogItemEdition edition = mock(CatalogItemEdition.class);
        when(edition.getId()).thenReturn(601L);
        when(edition.getEditionName()).thenReturn("Deluxe edition");
        when(edition.isPubliclyVisible()).thenReturn(true);
        ShopProduct editorialProduct = referencedShopProduct(906L, null, catalogItem, edition);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(906L)).thenReturn(Optional.of(editorialProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 706L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(906L, 1, null));

        assertThat(response.masterProductId()).isNull();
        assertThat(response.catalogItemEditionId()).isEqualTo(601L);
        assertThat(response.productName()).isEqualTo("Deluxe edition");
    }

    @Test
    void nonPublicEditionMakesEditorialReferenceUnavailable() {
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        CatalogItemEdition archivedEdition = mock(CatalogItemEdition.class);
        when(archivedEdition.isPubliclyVisible()).thenReturn(false);
        ShopProduct privateProduct = referencedShopProduct(907L, null, catalogItem, archivedEdition);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(907L)).thenReturn(Optional.of(privateProduct));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(907L, 1, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void reservationProductNameFallsBackFromBlankEditionToEditorialItem() {
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(catalogItem.getId()).thenReturn(501L);
        when(catalogItem.getTitle()).thenReturn("Akira 1");
        when(catalogItem.isPubliclyVisible()).thenReturn(true);
        CatalogItemEdition edition = mock(CatalogItemEdition.class);
        when(edition.getId()).thenReturn(601L);
        when(edition.getEditionName()).thenReturn("   ");
        when(edition.isPubliclyVisible()).thenReturn(true);
        ShopProduct bridgedProduct = referencedShopProduct(905L, masterProduct, catalogItem, edition);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(905L)).thenReturn(Optional.of(bridgedProduct));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 705L));

        var response = reservationService.createReservation(
                AuthenticatedUser.from(user), new CreateReservationRequest(905L, 1, null));

        assertThat(response.productName()).isEqualTo("Akira 1");
    }

    @Test
    void creatingReservationForMissingShopProductReturnsNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, 1, null)
        )).isInstanceOf(ShopProductNotFoundException.class);
    }

    @Test
    void hiddenShopProductCannotBeReserved() {
        ShopProduct hidden = shopProduct(900L, shop, masterProduct, ShopProductCommercialStatus.AVAILABLE, false, 3);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, 1, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void soldReservedOrHiddenStatusCannotBeReserved() {
        assertUnavailableForStatus(ShopProductCommercialStatus.SOLD);
        assertUnavailableForStatus(ShopProductCommercialStatus.RESERVED);
        assertUnavailableForStatus(ShopProductCommercialStatus.HIDDEN);
    }

    @Test
    void zeroStockCannotBeReserved() {
        ShopProduct noStock = shopProduct(900L, shop, masterProduct, ShopProductCommercialStatus.AVAILABLE, true, 0);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(noStock));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, 1, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void quantityGreaterThanStockCannotBeReserved() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(shopProduct));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, 4, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void negativeQuantityIsRejected() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, -1, null)
        )).isInstanceOf(InvalidReservationRequestException.class);
    }

    @Test
    void userListsOwnReservations() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        when(reservationRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "id"))))
                .thenReturn(List.of(reservation));

        var response = reservationService.myReservations(AuthenticatedUser.from(user), "PENDING", 500L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().userId()).isEqualTo(42L);
        verify(reservationRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "id")));
    }

    @Test
    void invalidStatusFilterReturnsBadRequestException() {
        assertThatThrownBy(() -> reservationService.myReservations(AuthenticatedUser.from(user), "unknown", null))
                .isInstanceOf(InvalidReservationFilterException.class);
    }

    @Test
    void userGetsOwnReservationDetail() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.getReservation(AuthenticatedUser.from(user), 700L);

        assertThat(response.id()).isEqualTo(700L);
        assertThat(response.userId()).isEqualTo(42L);
    }

    @Test
    void userCannotGetForeignReservationDetail() {
        Reservation reservation = reservation(700L, otherUser, shopProduct, ReservationStatus.PENDING);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                500L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(AuthenticatedUser.from(user), 700L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerCanListShopReservations() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "id"))))
                .thenReturn(List.of(reservation));

        var response = reservationService.shopReservations(AuthenticatedUser.from(owner), 500L, null, null, null);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().shopId()).isEqualTo(500L);
    }

    @Test
    void userOutsideShopCannotListShopReservations() {
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                500L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.shopReservations(
                AuthenticatedUser.from(user),
                500L,
                null,
                null,
                null
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shopDoesNotAccessReservationFromAnotherShop() {
        ShopProduct otherShopProduct = shopProduct(901L, otherShop, masterProduct, ShopProductCommercialStatus.AVAILABLE, true, 3);
        Reservation reservation = reservation(700L, user, otherShopProduct, ReservationStatus.PENDING);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateReservationStatus(
                AuthenticatedUser.from(owner),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.ACCEPTED, null)
        )).isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void shopAcceptsPendingReservation() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.updateReservationStatus(
                AuthenticatedUser.from(owner),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.ACCEPTED, "Accepted")
        );

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.shopResponse()).isEqualTo("Accepted");
    }

    @Test
    void shopRejectsPendingReservation() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        whenManageShop(manager, shop, ShopMemberRole.MANAGER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.updateReservationStatus(
                AuthenticatedUser.from(manager),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.REJECTED, "No stock")
        );

        assertThat(response.status()).isEqualTo("REJECTED");
    }

    @Test
    void shopCompletesAcceptedReservation() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.ACCEPTED);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.updateReservationStatus(
                AuthenticatedUser.from(owner),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.COMPLETED, null)
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void userCancelsPendingReservation() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.cancelReservation(AuthenticatedUser.from(user), 700L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void userCancelsAcceptedReservation() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.ACCEPTED);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        var response = reservationService.cancelReservation(AuthenticatedUser.from(user), 700L);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void invalidTransitionReturnsConflictException() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.PENDING);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateReservationStatus(
                AuthenticatedUser.from(owner),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.COMPLETED, null)
        )).isInstanceOf(InvalidReservationTransitionException.class);
    }

    @Test
    void completedReservationCannotBeChanged() {
        Reservation reservation = reservation(700L, user, shopProduct, ReservationStatus.COMPLETED);
        whenManageShop(owner, shop, ShopMemberRole.OWNER);
        when(reservationRepository.findByIdAndDeletedAtIsNull(700L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateReservationStatus(
                AuthenticatedUser.from(owner),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.CANCELLED, null)
        )).isInstanceOf(InvalidReservationTransitionException.class);
    }

    @Test
    void foreignShopMemberCannotChangeReservationStatus() {
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                500L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservationStatus(
                AuthenticatedUser.from(user),
                500L,
                700L,
                new UpdateReservationStatusRequest(ReservationStatus.ACCEPTED, null)
        )).isInstanceOf(AccessDeniedException.class);
    }

    private void assertUnavailableForStatus(ShopProductCommercialStatus status) {
        ShopProduct unavailable = shopProduct(900L, shop, masterProduct, status, true, 3);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(shopProductRepository.findByIdAndDeletedAtIsNull(900L)).thenReturn(Optional.of(unavailable));

        assertThatThrownBy(() -> reservationService.createReservation(
                AuthenticatedUser.from(user),
                new CreateReservationRequest(900L, 1, null)
        )).isInstanceOf(ReservationUnavailableException.class);
    }

    private void whenManageShop(User memberUser, Shop memberShop, ShopMemberRole role) {
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                memberShop.getId(),
                memberUser.getId(),
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(memberShop, memberUser, role)));
    }

    private User user(Long id, String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Shop shop(User owner, Long id, String name) {
        return withId(Shop.create(owner, name, null, null, null, null, "EUR", 48, null), id);
    }

    private MasterProduct masterProduct(Long id) {
        return withId(MasterProduct.create(
                "Dragon Ball 1",
                null,
                category,
                "Dragon Ball",
                "Tankobon",
                "1",
                "Planeta",
                null,
                null,
                null,
                null,
                null,
                "es",
                false,
                List.of("ES"),
                null,
                Map.of(),
                owner.getId()
        ), id);
    }

    private ShopProduct shopProduct(
            Long id,
            Shop shop,
            MasterProduct masterProduct,
            ShopProductCommercialStatus commercialStatus,
            boolean visible,
            int stockQuantity
    ) {
        return withId(ShopProduct.create(
                shop,
                masterProduct,
                BigDecimal.TEN,
                "EUR",
                stockQuantity,
                commercialStatus,
                PhysicalCondition.NEW,
                visible,
                null,
                null,
                null,
                owner.getId()
        ), id);
    }

    private ShopProduct referencedShopProduct(
            Long id,
            MasterProduct legacy,
            CatalogItem catalogItem,
            CatalogItemEdition edition
    ) {
        return withId(ShopProduct.create(
                shop,
                legacy,
                catalogItem,
                edition,
                ShopProductEditorialReferenceSource.VERIFIED_BRIDGE,
                BigDecimal.TEN,
                "EUR",
                3,
                ShopProductCommercialStatus.AVAILABLE,
                PhysicalCondition.NEW,
                true,
                null,
                null,
                null,
                owner.getId()
        ), id);
    }

    private Reservation reservation(
            Long id,
            User user,
            ShopProduct shopProduct,
            ReservationStatus status
    ) {
        Reservation reservation = Reservation.create(user, shopProduct, 1, "Please hold it");
        ReflectionTestUtils.setField(reservation, "status", status);
        return withId(reservation, id);
    }

    private ShopMember member(Shop shop, User user, ShopMemberRole role) {
        ShopMember member = ShopMember.owner(shop, user);
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
