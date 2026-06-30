package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogFranchise;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.dto.CreateCatalogFranchiseRequest;
import com.collectohub.catalog.infrastructure.CatalogFranchiseRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogFranchiseServiceTest {

    @Mock
    private CatalogFranchiseRepository franchiseRepository;

    @Mock
    private CatalogSeriesRepository catalogSeriesRepository;

    private CatalogFranchiseService franchiseService;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        franchiseService = new CatalogFranchiseService(franchiseRepository, catalogSeriesRepository);
        admin = authenticatedUser();
    }

    @Test
    void adminCreatesFranchise() {
        when(franchiseRepository.save(any(CatalogFranchise.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 20L));

        var response = franchiseService.create(
                admin,
                new CreateCatalogFranchiseRequest(
                        "Dragon Ball",
                        "dragon-ball",
                        "Created by Akira Toriyama",
                        CatalogRecordStatus.ACTIVE
                )
        );

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.slug()).isEqualTo("dragon-ball");
    }

    @Test
    void duplicateSlugIsRejected() {
        when(franchiseRepository.existsBySlugAndDeletedAtIsNull("dragon-ball")).thenReturn(true);

        assertThatThrownBy(() -> franchiseService.create(
                admin,
                new CreateCatalogFranchiseRequest(
                        "Dragon Ball",
                        "dragon-ball",
                        null,
                        CatalogRecordStatus.ACTIVE
                )
        )).isInstanceOf(DuplicateEditorialCatalogException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void publicCannotReadArchivedFranchise() {
        CatalogFranchise franchise = withId(CatalogFranchise.create(
                "Dragon Ball",
                "dragon-ball",
                null,
                CatalogRecordStatus.ARCHIVED,
                1L
        ), 20L);
        when(franchiseRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(franchise));

        assertThatThrownBy(() -> franchiseService.get(20L, null))
                .isInstanceOf(CatalogFranchiseNotFoundException.class);
    }

    private AuthenticatedUser authenticatedUser() {
        User user = User.register(
                "admin@example.com",
                "$2a$10$test-password-hash",
                "Admin",
                new Role("ADMIN", "Administrator")
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return AuthenticatedUser.from(user);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
