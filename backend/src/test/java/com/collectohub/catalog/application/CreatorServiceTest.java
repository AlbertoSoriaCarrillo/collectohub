package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.CreatorRepository;
import com.collectohub.users.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatorServiceTest {
    @Mock CreatorRepository repository;
    CreatorService service;
    AuthenticatedUser admin;

    @BeforeEach void setUp() { service = new CreatorService(repository); admin = user("ADMIN"); }

    @Test void createsAndNormalizesCreator() {
        when(repository.save(any())).thenAnswer(i -> withId(i.getArgument(0), 10L));
        CreatorResponse response = service.create(admin, new CreateCreatorRequest(
                "  Akira Toriyama  ", null, "Toriyama, Akira", null, "jp", 1955, null, CatalogRecordStatus.ACTIVE));
        assertThat(response.slug()).isEqualTo("akira-toriyama");
        assertThat(response.country()).isEqualTo("JP");
    }

    @Test void duplicateSlugIsRejected() {
        when(repository.existsBySlugAndDeletedAtIsNull("akira-toriyama")).thenReturn(true);
        assertThatThrownBy(() -> service.create(admin, new CreateCreatorRequest(
                "Akira Toriyama", "Akira-Toriyama", null, null, null, null, null, null)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test void duplicateNormalizedNameIsRejectedOnCreateAndUpdate() {
        when(repository.existsByNameIgnoreCaseAndDeletedAtIsNull("Akira Toriyama")).thenReturn(true);
        assertThatThrownBy(() -> service.create(admin, new CreateCreatorRequest(
                "  Akira Toriyama  ", null, null, null, null, null, null, null)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);

        Creator creator = creator(10L, CatalogRecordStatus.ACTIVE);
        when(repository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(creator));
        when(repository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot("Akira Toriyama", 10L)).thenReturn(true);
        assertThatThrownBy(() -> service.update(10L, admin, new UpdateCreatorRequest(
                "Akira Toriyama", "akira-t", null, null, null, null, null, null)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test void invalidLifeYearsAreRejected() {
        assertThatThrownBy(() -> service.create(admin, new CreateCreatorRequest(
                "Creator", null, null, null, null, 2000, 1990, null)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test void publicDetailOnlyExposesActiveCreator() {
        when(repository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(creator(10L, CatalogRecordStatus.DRAFT)));
        assertThatThrownBy(() -> service.get(10L, null)).isInstanceOf(CreatorNotFoundException.class);
        assertThat(service.get(10L, admin).recordStatus()).isEqualTo("DRAFT");
    }

    @Test void updatesAndSoftDeletesCreator() {
        Creator creator = creator(10L, CatalogRecordStatus.ACTIVE);
        when(repository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(creator));
        CreatorResponse updated = service.update(10L, admin, new UpdateCreatorRequest(
                "Akira T.", "akira-t", null, null, "jp", 1955, null, CatalogRecordStatus.ACTIVE));
        assertThat(updated.slug()).isEqualTo("akira-t");
        service.delete(10L, admin);
        assertThat(creator.getDeletedAt()).isNotNull();
    }

    private Creator creator(Long id, CatalogRecordStatus status) {
        return withId(Creator.create("Akira Toriyama", "akira-toriyama", null, null,
                "JP", 1955, null, status, 1L), id);
    }
    private AuthenticatedUser user(String role) {
        User user = User.register("admin@example.com", "hash", "Admin", new Role(role, role));
        ReflectionTestUtils.setField(user, "id", 1L); return AuthenticatedUser.from(user);
    }
    private <T> T withId(T value, Long id) { ReflectionTestUtils.setField(value, "id", id); return value; }
}
