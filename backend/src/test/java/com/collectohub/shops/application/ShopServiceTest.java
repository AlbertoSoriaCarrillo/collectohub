package com.collectohub.shops.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.dto.CreateShopRequest;
import com.collectohub.shops.dto.UpdateShopRequest;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopMemberRepository shopMemberRepository;

    @Mock
    private UserRepository userRepository;

    private ShopService shopService;

    private User owner;
    private AuthenticatedUser authenticatedOwner;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(
                shopRepository,
                shopMemberRepository,
                userRepository,
                new ShopProperties("EUR", 48)
        );
        owner = user(42L, "owner@example.com");
        authenticatedOwner = AuthenticatedUser.from(owner);
    }

    @Test
    void authenticatedUserCreatesShopAndOwnerMember() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 200L));

        var response = shopService.createShop(authenticatedOwner, new CreateShopRequest(
                " Collector Cave ",
                "Rare items",
                "SHOP@EXAMPLE.COM",
                "+34 600 000 000",
                null,
                null,
                null,
                null
        ));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Collector Cave");
        assertThat(response.contactEmail()).isEqualTo("shop@example.com");
        assertThat(response.country()).isNull();
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.defaultReservationExpirationHours()).isEqualTo(48);
        assertThat(response.currentUserMembership().role()).isEqualTo("OWNER");

        ArgumentCaptor<ShopMember> memberCaptor = ArgumentCaptor.forClass(ShopMember.class);
        verify(shopMemberRepository).save(memberCaptor.capture());
        ShopMember member = memberCaptor.getValue();
        assertThat(member.getUser().getId()).isEqualTo(42L);
        assertThat(member.getShop().getId()).isEqualTo(100L);
        assertThat(member.getRole()).isEqualTo(ShopMemberRole.OWNER);
    }

    @Test
    void userCanListAssociatedShops() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember member = withId(ShopMember.owner(shop, owner), 200L);
        when(shopMemberRepository.findByUser_IdAndStatusAndDeletedAtIsNull(42L, ShopMemberStatus.ACTIVE))
                .thenReturn(List.of(member));

        var shops = shopService.myShops(authenticatedOwner);

        assertThat(shops).hasSize(1);
        assertThat(shops.getFirst().id()).isEqualTo(100L);
        assertThat(shops.getFirst().currentUserMembership().role()).isEqualTo("OWNER");
    }

    @Test
    void userCannotModifyShopWhenNotMember() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.updateShop(
                authenticatedOwner,
                100L,
                new UpdateShopRequest("New name", null, null, null, null, null, null, null)
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerCanModifyShop() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember member = withId(ShopMember.owner(shop, owner), 200L);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(member));

        var response = shopService.updateShop(
                authenticatedOwner,
                100L,
                new UpdateShopRequest(
                        "Updated Shop",
                        "Updated description",
                        "contact@example.com",
                        null,
                        "es",
                        "eur",
                        72,
                        null
                )
        );

        assertThat(response.name()).isEqualTo("Updated Shop");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.country()).isEqualTo("ES");
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.defaultReservationExpirationHours()).isEqualTo(72);
        assertThat(response.currentUserMembership().role()).isEqualTo("OWNER");
    }

    private User user(Long id, String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Shop shop(User owner, Long id, String name) {
        Shop shop = Shop.create(owner, name, null, null, null, null, "EUR", 48, null);
        return withId(shop, id);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
