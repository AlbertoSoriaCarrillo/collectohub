package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductStatus;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.dto.CreateMasterProductRequest;
import com.collectohub.catalog.dto.UpdateMasterProductRequest;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

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
class CatalogServiceTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private MasterProductRepository masterProductRepository;

    private CatalogService catalogService;
    private ProductCategory category;
    private AuthenticatedUser shopOwner;
    private AuthenticatedUser regularUser;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(productCategoryRepository, masterProductRepository);
        category = withId(new ProductCategory("MANGA_COMIC", "Manga and comic"), 10L);
        shopOwner = authenticatedUser(42L, "shop-owner@example.com", "USER", "SHOP_OWNER");
        regularUser = authenticatedUser(43L, "user@example.com", "USER");
    }

    @Test
    void shopOwnerCreatesMasterProduct() {
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(masterProductRepository.save(any(MasterProduct.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

        var response = catalogService.createMasterProduct(shopOwner, request("Dragon Ball 1", "9788490000001", null));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Dragon Ball 1");
        assertThat(response.category().code()).isEqualTo("MANGA_COMIC");
        assertThat(response.language()).isEqualTo("es");
        assertThat(response.limitedEdition()).isTrue();
        assertThat(response.limitedEditionTotalUnits()).isEqualTo(500);
        assertThat(response.publicationCountries()).containsExactly("ES", "FR");
    }

    @Test
    void regularUserCannotCreateMasterProduct() {
        assertThatThrownBy(() -> catalogService.createMasterProduct(
                regularUser,
                request("Dragon Ball 1", "9788490000001", null)
        )).isInstanceOf(AccessDeniedException.class);

        verify(masterProductRepository, never()).save(any());
    }

    @Test
    void duplicateIsbnReturnsConflictException() {
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(masterProductRepository.existsByIsbnIgnoreCaseAndStatusAndDeletedAtIsNull(
                "9788490000001",
                MasterProductStatus.ACTIVE
        )).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createMasterProduct(
                shopOwner,
                request("Dragon Ball 1", "9788490000001", null)
        )).isInstanceOf(DuplicateMasterProductException.class)
                .hasMessageContaining("isbn");
    }

    @Test
    void duplicateEanReturnsConflictException() {
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(masterProductRepository.existsByEanIgnoreCaseAndStatusAndDeletedAtIsNull(
                "9788490000002",
                MasterProductStatus.ACTIVE
        )).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createMasterProduct(
                shopOwner,
                request("Dragon Ball 1", null, "9788490000002")
        )).isInstanceOf(DuplicateMasterProductException.class)
                .hasMessageContaining("ean");
    }

    @Test
    void duplicateLogicalCombinationReturnsConflictException() {
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(masterProductRepository.existsLogicalDuplicate(
                "dragon ball 1",
                "dragon ball",
                "1",
                "es",
                MasterProductStatus.ACTIVE
        )).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createMasterProduct(
                shopOwner,
                request("Dragon Ball 1", null, null)
        )).isInstanceOf(DuplicateMasterProductException.class)
                .hasMessageContaining("logical");
    }

    @Test
    void shopOwnerUpdatesMasterProduct() {
        MasterProduct product = withId(MasterProduct.create(
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
        ), 100L);
        when(masterProductRepository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(product));

        var response = catalogService.updateMasterProduct(
                shopOwner,
                100L,
                new UpdateMasterProductRequest(
                        "Dragon Ball 01",
                        "Updated",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "9788490000002",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertThat(response.name()).isEqualTo("Dragon Ball 01");
        assertThat(response.description()).isEqualTo("Updated");
        assertThat(response.ean()).isEqualTo("9788490000002");
    }

    private CreateMasterProductRequest request(String name, String isbn, String ean) {
        return new CreateMasterProductRequest(
                name,
                "First volume",
                "MANGA_COMIC",
                "Dragon Ball",
                "Tankobon",
                "1",
                "Planeta",
                isbn,
                ean,
                null,
                null,
                null,
                "ES",
                true,
                500,
                List.of("es", "fr"),
                null,
                Map.of("format", "paperback")
        );
    }

    private AuthenticatedUser authenticatedUser(Long id, String email, String... roleCodes) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        for (String roleCode : roleCodes) {
            user.addRole(new Role(roleCode, roleCode));
        }
        ReflectionTestUtils.setField(user, "id", id);
        return AuthenticatedUser.from(user);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
