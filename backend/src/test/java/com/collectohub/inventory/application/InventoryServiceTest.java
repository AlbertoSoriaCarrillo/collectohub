package com.collectohub.inventory.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.dto.CreateShopProductRequest;
import com.collectohub.inventory.dto.UpdateShopProductRequest;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopMemberRepository shopMemberRepository;

    @Mock
    private MasterProductRepository masterProductRepository;

    @Mock
    private ShopProductRepository shopProductRepository;

    private InventoryService inventoryService;
    private User owner;
    private User manager;
    private User employee;
    private Shop shop;
    private MasterProduct masterProduct;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                shopRepository,
                shopMemberRepository,
                masterProductRepository,
                shopProductRepository
        );
        owner = user(42L, "owner@example.com");
        manager = user(43L, "manager@example.com");
        employee = user(44L, "employee@example.com");
        shop = shop(owner, 100L);
        masterProduct = masterProduct(200L);
    }

    @Test
    void ownerCreatesShopProduct() {
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(shop, owner, ShopMemberRole.OWNER)));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(shopProductRepository.save(any(ShopProduct.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 300L));

        var response = inventoryService.createShopProduct(
                AuthenticatedUser.from(owner),
                100L,
                createRequest(null, null, null)
        );

        assertThat(response.id()).isEqualTo(300L);
        assertThat(response.shopId()).isEqualTo(100L);
        assertThat(response.masterProductId()).isEqualTo(200L);
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.commercialStatus()).isEqualTo("AVAILABLE");
        assertThat(response.visible()).isTrue();
    }

    @Test
    void managerCreatesShopProduct() {
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                43L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(shop, manager, ShopMemberRole.MANAGER)));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.of(masterProduct));
        when(shopProductRepository.save(any(ShopProduct.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 300L));

        var response = inventoryService.createShopProduct(
                AuthenticatedUser.from(manager),
                100L,
                createRequest("usd", ShopProductCommercialStatus.RESERVED, false)
        );

        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.commercialStatus()).isEqualTo("RESERVED");
        assertThat(response.visible()).isFalse();
    }

    @Test
    void userOutsideShopCannotCreateShopProduct() {
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                44L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createShopProduct(
                AuthenticatedUser.from(employee),
                100L,
                createRequest(null, null, null)
        )).isInstanceOf(AccessDeniedException.class);

        verify(shopProductRepository, never()).save(any());
    }

    @Test
    void missingMasterProductReturnsNotFound() {
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(shop, owner, ShopMemberRole.OWNER)));
        when(masterProductRepository.findByIdAndDeletedAtIsNull(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createShopProduct(
                AuthenticatedUser.from(owner),
                100L,
                createRequest(null, null, null)
        )).isInstanceOf(MasterProductNotFoundException.class);
    }

    @Test
    void memberCanListOwnShopInventory() {
        ShopProduct hiddenProduct = withId(ShopProduct.create(
                shop,
                masterProduct,
                BigDecimal.TEN,
                "EUR",
                1,
                ShopProductCommercialStatus.HIDDEN,
                PhysicalCondition.GOOD,
                false,
                null,
                null,
                "internal",
                42L
        ), 300L);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                44L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(shop, employee, ShopMemberRole.EMPLOYEE)));
        when(shopProductRepository.findByShop_IdAndDeletedAtIsNullOrderByIdAsc(100L)).thenReturn(List.of(hiddenProduct));

        var response = inventoryService.myShopProducts(AuthenticatedUser.from(employee), 100L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().visible()).isFalse();
        assertThat(response.getFirst().commercialStatus()).isEqualTo("HIDDEN");
    }

    @Test
    void ownerUpdatesShopProduct() {
        ShopProduct shopProduct = shopProduct(300L, shop, masterProduct);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(shop, owner, ShopMemberRole.OWNER)));
        when(shopProductRepository.findByIdAndShop_IdAndDeletedAtIsNull(300L, 100L)).thenReturn(Optional.of(shopProduct));

        var response = inventoryService.updateShopProduct(
                AuthenticatedUser.from(owner),
                100L,
                300L,
                new UpdateShopProductRequest(
                        new BigDecimal("12.50"),
                        "usd",
                        3,
                        ShopProductCommercialStatus.RESERVED,
                        PhysicalCondition.LIKE_NEW,
                        false,
                        "7",
                        100,
                        "Updated"
                )
        );

        assertThat(response.priceAmount()).isEqualByComparingTo("12.50");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.stockQuantity()).isEqualTo(3);
        assertThat(response.commercialStatus()).isEqualTo("RESERVED");
        assertThat(response.visible()).isFalse();
    }

    @Test
    void userOutsideShopCannotUpdateShopProduct() {
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                44L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.updateShopProduct(
                AuthenticatedUser.from(employee),
                100L,
                300L,
                new UpdateShopProductRequest(null, null, 1, null, null, null, null, null, null)
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cannotUpdateShopProductThroughAnotherShopId() {
        Shop otherShop = shop(owner, 101L);
        when(shopRepository.findByIdAndDeletedAtIsNull(101L)).thenReturn(Optional.of(otherShop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                101L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member(otherShop, owner, ShopMemberRole.OWNER)));
        when(shopProductRepository.findByIdAndShop_IdAndDeletedAtIsNull(300L, 101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.updateShopProduct(
                AuthenticatedUser.from(owner),
                101L,
                300L,
                new UpdateShopProductRequest(null, null, 1, null, null, null, null, null, null)
        )).isInstanceOf(ShopProductNotFoundException.class);
    }

    @Test
    void publicDetailDoesNotExposeHiddenProducts() {
        ShopProduct hiddenProduct = withId(ShopProduct.create(
                shop,
                masterProduct,
                BigDecimal.TEN,
                "EUR",
                1,
                ShopProductCommercialStatus.HIDDEN,
                PhysicalCondition.GOOD,
                false,
                null,
                null,
                null,
                42L
        ), 300L);
        when(shopProductRepository.findByIdAndDeletedAtIsNull(300L)).thenReturn(Optional.of(hiddenProduct));

        assertThatThrownBy(() -> inventoryService.getPublicShopProduct(300L))
                .isInstanceOf(ShopProductNotFoundException.class);
    }

    private CreateShopProductRequest createRequest(
            String currency,
            ShopProductCommercialStatus commercialStatus,
            Boolean visible
    ) {
        return new CreateShopProductRequest(
                200L,
                new BigDecimal("9.99"),
                currency,
                2,
                commercialStatus,
                PhysicalCondition.NEW,
                visible,
                null,
                null,
                null
        );
    }

    private User user(Long id, String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Shop shop(User owner, Long id) {
        return withId(Shop.create(owner, "Collector Cave", null, null, null, null, "EUR", 48, null), id);
    }

    private MasterProduct masterProduct(Long id) {
        ProductCategory category = withId(new ProductCategory("MANGA_COMIC", "Manga and comic"), 10L);
        return withId(MasterProduct.create(
                "Dragon Ball 1",
                null,
                category,
                "Dragon Ball",
                "Tankobon",
                "1",
                "Planeta",
                "9788490000001",
                null,
                null,
                null,
                null,
                "es",
                false,
                List.of("ES"),
                null,
                Map.of(),
                42L
        ), id);
    }

    private ShopMember member(Shop shop, User user, ShopMemberRole role) {
        ShopMember member = ShopMember.owner(shop, user);
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }

    private ShopProduct shopProduct(Long id, Shop shop, MasterProduct masterProduct) {
        return withId(ShopProduct.create(
                shop,
                masterProduct,
                BigDecimal.TEN,
                "EUR",
                1,
                ShopProductCommercialStatus.AVAILABLE,
                PhysicalCondition.GOOD,
                true,
                null,
                null,
                null,
                42L
        ), id);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
