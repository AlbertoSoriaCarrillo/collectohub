package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CreatePublisherRequest;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.PublisherRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private CatalogSeriesRepository catalogSeriesRepository;

    @Mock
    private CatalogItemEditionRepository editionRepository;

    private PublisherService publisherService;
    private AuthenticatedUser admin;
    private AuthenticatedUser regularUser;

    @BeforeEach
    void setUp() {
        publisherService = new PublisherService(publisherRepository, catalogSeriesRepository, editionRepository);
        admin = authenticatedUser(1L, "admin@example.com", "ADMIN");
        regularUser = authenticatedUser(2L, "user@example.com", "USER");
    }

    @Test
    void publicReadsActivePublisher() {
        Publisher publisher = publisher(10L, CatalogRecordStatus.ACTIVE);
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(publisher));

        assertThat(publisherService.get(10L, null).name()).isEqualTo("Planeta");
    }

    @Test
    void publicCannotReadDraftPublisher() {
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(publisher(10L, CatalogRecordStatus.DRAFT)));

        assertThatThrownBy(() -> publisherService.get(10L, null))
                .isInstanceOf(PublisherNotFoundException.class);
    }

    @Test
    void adminCanReadDraftPublisher() {
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(publisher(10L, CatalogRecordStatus.DRAFT)));

        assertThat(publisherService.get(10L, admin).recordStatus()).isEqualTo("DRAFT");
    }

    @Test
    void adminCreatesNormalizedPublisher() {
        when(publisherRepository.save(any(Publisher.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 10L));

        var response = publisherService.create(
                admin,
                new CreatePublisherRequest("  Planeta  ", "es", CatalogRecordStatus.ACTIVE)
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Planeta");
        assertThat(response.country()).isEqualTo("ES");
    }

    @Test
    void duplicatePublisherNameIsRejected() {
        when(publisherRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("Planeta")).thenReturn(true);

        assertThatThrownBy(() -> publisherService.create(
                admin,
                new CreatePublisherRequest("Planeta", "ES", CatalogRecordStatus.ACTIVE)
        )).isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void regularUserCannotCreatePublisher() {
        assertThatThrownBy(() -> publisherService.create(
                regularUser,
                new CreatePublisherRequest("Planeta", "ES", CatalogRecordStatus.ACTIVE)
        )).isInstanceOf(AccessDeniedException.class);

        verify(publisherRepository, never()).save(any());
    }

    @Test
    void recordStatusFilterRequiresAdmin() {
        assertThatThrownBy(() -> publisherService.search(
                regularUser,
                null,
                "DRAFT",
                0,
                20,
                "name,asc"
        )).isInstanceOf(AccessDeniedException.class);
    }

    private Publisher publisher(Long id, CatalogRecordStatus status) {
        return withId(Publisher.create("Planeta", "ES", status, 1L), id);
    }

    private AuthenticatedUser authenticatedUser(Long id, String email, String roleCode) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role(roleCode, roleCode));
        ReflectionTestUtils.setField(user, "id", id);
        return AuthenticatedUser.from(user);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
