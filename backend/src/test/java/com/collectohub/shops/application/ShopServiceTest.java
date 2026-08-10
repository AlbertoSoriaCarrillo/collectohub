package com.collectohub.shops.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMember;
import com.collectohub.shops.domain.ShopMemberRole;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.dto.AddShopMemberRequest;
import com.collectohub.shops.dto.ChangeShopMemberRoleRequest;
import com.collectohub.shops.dto.CreateShopRequest;
import com.collectohub.shops.dto.UpdateShopRequest;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Mock
    private RoleRepository roleRepository;

    private ShopService shopService;

    private User owner;
    private AuthenticatedUser authenticatedOwner;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(
                shopRepository,
                shopMemberRepository,
                userRepository,
                roleRepository,
                new ShopProperties("EUR", 48)
        );
        owner = user(42L, "owner@example.com");
        authenticatedOwner = AuthenticatedUser.from(owner);
    }

    @Test
    void authenticatedUserCreatesShopAndOwnerMember() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(roleRepository.findByCode("SHOP_OWNER")).thenReturn(Optional.of(new Role("SHOP_OWNER", "Shop owner")));
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
        assertThat(owner.getRoles()).extracting(Role::getCode).containsExactlyInAnyOrder("USER", "SHOP_OWNER");

        ArgumentCaptor<ShopMember> memberCaptor = ArgumentCaptor.forClass(ShopMember.class);
        verify(shopMemberRepository).save(memberCaptor.capture());
        ShopMember member = memberCaptor.getValue();
        assertThat(member.getUser().getId()).isEqualTo(42L);
        assertThat(member.getShop().getId()).isEqualTo(100L);
        assertThat(member.getRole()).isEqualTo(ShopMemberRole.OWNER);
    }

    @Test
    void firstShopCreationAssignsGlobalShopOwnerRole() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(roleRepository.findByCode("SHOP_OWNER")).thenReturn(Optional.of(new Role("SHOP_OWNER", "Shop owner")));
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 200L));

        shopService.createShop(authenticatedOwner, new CreateShopRequest(
                "Collector Cave",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(owner.getRoles()).extracting(Role::getCode).containsExactlyInAnyOrder("USER", "SHOP_OWNER");
    }

    @Test
    void shopOwnerGlobalRoleIsNotDuplicatedWhenAlreadyPresent() {
        owner.addRole(new Role("SHOP_OWNER", "Shop owner"));
        when(userRepository.findById(42L)).thenReturn(Optional.of(owner));
        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));
        when(shopMemberRepository.save(any(ShopMember.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 200L));

        shopService.createShop(authenticatedOwner, new CreateShopRequest(
                "Collector Cave",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(owner.getRoles()).extracting(Role::getCode)
                .containsExactlyInAnyOrder("USER", "SHOP_OWNER");
        verify(roleRepository, never()).findByCode("SHOP_OWNER");
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

    @Test
    void managerCanListActiveShopMembersInRepositoryOrder() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember manager = member(shop, owner, 200L, ShopMemberRole.MANAGER);
        User employeeUser = user(43L, "employee@example.com");
        ShopMember employee = member(shop, employeeUser, 201L, ShopMemberRole.EMPLOYEE);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(manager));
        when(shopMemberRepository.findByShop_IdAndStatusAndDeletedAtIsNullOrderByIdAsc(
                100L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(List.of(manager, employee));

        var members = shopService.listMembers(authenticatedOwner, 100L);

        assertThat(members).extracting(response -> response.id()).containsExactly(200L, 201L);
        assertThat(members).extracting(response -> response.userId()).containsExactly(42L, 43L);
        assertThat(members).extracting(response -> response.role()).containsExactly("MANAGER", "EMPLOYEE");
    }

    @Test
    void employeeCannotListShopMembers() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember employee = member(shop, owner, 200L, ShopMemberRole.EMPLOYEE);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L,
                42L,
                ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> shopService.listMembers(authenticatedOwner, 100L))
                .isInstanceOf(AccessDeniedException.class);

        verify(shopMemberRepository, never())
                .findByShop_IdAndStatusAndDeletedAtIsNullOrderByIdAsc(any(), any());
    }

    @Test
    void ownerAddsExistingActiveUserAsEmployeeUsingNormalizedEmail() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employee = user(43L, "employee@example.com");
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(shopMemberRepository.findByShop_IdAndUser_Id(100L, 43L)).thenReturn(Optional.empty());
        when(shopMemberRepository.saveAndFlush(any(ShopMember.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 201L));

        var response = shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest(" EMPLOYEE@EXAMPLE.COM ", ShopMemberRole.EMPLOYEE)
        );

        assertThat(response.id()).isEqualTo(201L);
        assertThat(response.userId()).isEqualTo(43L);
        assertThat(response.role()).isEqualTo("EMPLOYEE");
        ArgumentCaptor<ShopMember> captor = ArgumentCaptor.forClass(ShopMember.class);
        verify(shopMemberRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopMemberStatus.ACTIVE);
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(42L);
    }

    @Test
    void managerCannotAddShopMember() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember manager = member(shop, owner, 200L, ShopMemberRole.MANAGER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.EMPLOYEE)
        )).isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findByEmailIgnoreCaseAndDeletedAtIsNull(any());
    }

    @Test
    void ownerChangesActiveEmployeeToManager() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employeeUser = user(43L, "employee@example.com");
        ShopMember employee = member(shop, employeeUser, 201L, ShopMemberRole.EMPLOYEE);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                201L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(employee));

        var response = shopService.changeMemberRole(
                authenticatedOwner,
                100L,
                201L,
                new ChangeShopMemberRoleRequest(ShopMemberRole.MANAGER)
        );

        assertThat(response.id()).isEqualTo(201L);
        assertThat(response.userId()).isEqualTo(43L);
        assertThat(response.role()).isEqualTo("MANAGER");
        assertThat(employee.getRole()).isEqualTo(ShopMemberRole.MANAGER);
        assertThat(employee.getUpdatedBy()).isEqualTo(42L);
    }

    @Test
    void managerCannotChangeShopMemberRole() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember manager = member(shop, owner, 200L, ShopMemberRole.MANAGER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> shopService.changeMemberRole(
                authenticatedOwner,
                100L,
                201L,
                new ChangeShopMemberRoleRequest(ShopMemberRole.MANAGER)
        )).isInstanceOf(AccessDeniedException.class);

        verify(shopMemberRepository, never())
                .findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(any(), any(), any());
    }

    @Test
    void ownerMembershipCannotBeChangedThroughRoleEndpoint() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                200L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> shopService.changeMemberRole(
                authenticatedOwner,
                100L,
                200L,
                new ChangeShopMemberRoleRequest(ShopMemberRole.EMPLOYEE)
        )).isInstanceOf(InvalidShopMemberRoleException.class);

        assertThat(ownerMember.getRole()).isEqualTo(ShopMemberRole.OWNER);
    }

    @Test
    void inactiveOrForeignMemberCannotBeChanged() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                999L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.changeMemberRole(
                authenticatedOwner,
                100L,
                999L,
                new ChangeShopMemberRoleRequest(ShopMemberRole.MANAGER)
        )).isInstanceOf(ShopMemberNotFoundException.class)
                .hasMessage("Shop member not found");
    }

    @Test
    void ownerDeactivatesActiveEmployeeWithAudit() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employeeUser = user(43L, "employee@example.com");
        ShopMember employee = member(shop, employeeUser, 201L, ShopMemberRole.EMPLOYEE);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                201L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(employee));

        shopService.deactivateMember(authenticatedOwner, 100L, 201L);

        assertThat(employee.getStatus()).isEqualTo(ShopMemberStatus.INACTIVE);
        assertThat(employee.getUpdatedBy()).isEqualTo(42L);
    }

    @Test
    void membershipMutationFinderUsesPessimisticWriteLock() throws NoSuchMethodException {
        var method = ShopMemberRepository.class.getMethod(
                "findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull",
                Long.class,
                Long.class,
                ShopMemberStatus.class
        );

        assertThat(method.getAnnotation(Lock.class))
                .isNotNull()
                .extracting(Lock::value)
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void managerCannotDeactivateShopMember() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember manager = member(shop, owner, 200L, ShopMemberRole.MANAGER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> shopService.deactivateMember(authenticatedOwner, 100L, 201L))
                .isInstanceOf(AccessDeniedException.class);

        verify(shopMemberRepository, never())
                .findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(any(), any(), any());
    }

    @Test
    void ownerMembershipCannotBeDeactivated() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                200L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> shopService.deactivateMember(authenticatedOwner, 100L, 200L))
                .isInstanceOf(ShopOwnerCannotBeDeactivatedException.class)
                .hasMessage("Shop owner membership cannot be deactivated");

        assertThat(ownerMember.getStatus()).isEqualTo(ShopMemberStatus.ACTIVE);
    }

    @Test
    void inactiveOrForeignMemberCannotBeDeactivated() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(shopMemberRepository.findForUpdateByIdAndShop_IdAndStatusAndDeletedAtIsNull(
                999L, 100L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.deactivateMember(authenticatedOwner, 100L, 999L))
                .isInstanceOf(ShopMemberNotFoundException.class)
                .hasMessage("Shop member not found");
    }

    @Test
    void duplicateShopMembershipReturnsStableConflict() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employee = user(43L, "employee@example.com");
        ShopMember existing = member(shop, employee, 201L, ShopMemberRole.EMPLOYEE);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(shopMemberRepository.findByShop_IdAndUser_Id(100L, 43L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.EMPLOYEE)
        )).isInstanceOf(ShopMembershipAlreadyExistsException.class);

        verify(shopMemberRepository, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateShopMembershipReturnsStableConflict() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employee = user(43L, "employee@example.com");
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(shopMemberRepository.findByShop_IdAndUser_Id(100L, 43L)).thenReturn(Optional.empty());
        when(shopMemberRepository.saveAndFlush(any(ShopMember.class)))
                .thenThrow(integrityViolation("uk_shop_members_shop_user"));

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.EMPLOYEE)
        )).isInstanceOf(ShopMembershipAlreadyExistsException.class)
                .hasMessage("Shop membership already exists");
    }

    @Test
    void unrelatedIntegrityViolationIsNotReportedAsDuplicateMembership() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employee = user(43L, "employee@example.com");
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("employee@example.com"))
                .thenReturn(Optional.of(employee));
        when(shopMemberRepository.findByShop_IdAndUser_Id(100L, 43L)).thenReturn(Optional.empty());
        DataIntegrityViolationException violation = integrityViolation("fk_shop_members_user");
        when(shopMemberRepository.saveAndFlush(any(ShopMember.class))).thenThrow(violation);

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.EMPLOYEE)
        )).isSameAs(violation);
    }

    @Test
    void ownerRoleCannotBeAssignedThroughMemberCreation() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.OWNER)
        )).isInstanceOf(InvalidShopMemberRoleException.class);

        verify(userRepository, never()).findByEmailIgnoreCaseAndDeletedAtIsNull(any());
    }

    @Test
    void inactiveCandidateIsNotEligibleForMembership() {
        Shop shop = shop(owner, 100L, "Collector Cave");
        ShopMember ownerMember = member(shop, owner, 200L, ShopMemberRole.OWNER);
        User employee = user(43L, "employee@example.com");
        ReflectionTestUtils.setField(employee, "status", "INACTIVE");
        when(shopRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(shop));
        when(shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                100L, 42L, ShopMemberStatus.ACTIVE
        )).thenReturn(Optional.of(ownerMember));
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("employee@example.com"))
                .thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> shopService.addMember(
                authenticatedOwner,
                100L,
                new AddShopMemberRequest("employee@example.com", ShopMemberRole.EMPLOYEE)
        )).isInstanceOf(ShopMemberCandidateNotFoundException.class)
                .hasMessage("Eligible user not found");
    }

    private User user(Long id, String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private DataIntegrityViolationException integrityViolation(String constraintName) {
        var cause = new org.hibernate.exception.ConstraintViolationException(
                "constraint violation",
                new SQLException("constraint violation"),
                constraintName
        );
        return new DataIntegrityViolationException("constraint violation", cause);
    }

    private Shop shop(User owner, Long id, String name) {
        Shop shop = Shop.create(owner, name, null, null, null, null, "EUR", 48, null);
        return withId(shop, id);
    }

    private ShopMember member(Shop shop, User user, Long id, ShopMemberRole role) {
        ShopMember member = withId(ShopMember.owner(shop, user), id);
        ReflectionTestUtils.setField(member, "role", role);
        return member;
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
