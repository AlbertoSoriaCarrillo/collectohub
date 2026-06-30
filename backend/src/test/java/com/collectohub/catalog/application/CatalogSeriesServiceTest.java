package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogFranchise;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CreateCatalogSeriesRequest;
import com.collectohub.catalog.infrastructure.CatalogFranchiseRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.PublisherRepository;
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
class CatalogSeriesServiceTest {

    @Mock
    private CatalogSeriesRepository seriesRepository;

    @Mock
    private CatalogFranchiseRepository franchiseRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private CatalogItemRepository itemRepository;

    private CatalogSeriesService seriesService;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        seriesService = new CatalogSeriesService(
                seriesRepository,
                franchiseRepository,
                publisherRepository,
                itemRepository
        );
        admin = authenticatedUser();
    }

    @Test
    void createsSeriesWithoutFranchiseOrPublisher() {
        when(seriesRepository.save(any(CatalogSeries.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 30L));

        var response = seriesService.create(admin, request(null, null, CatalogRecordStatus.DRAFT));

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.franchiseId()).isNull();
        assertThat(response.primaryPublisherId()).isNull();
        assertThat(response.type()).isEqualTo("MANGA");
    }

    @Test
    void createsActiveSeriesWithActiveDependencies() {
        CatalogFranchise franchise = franchise(20L, CatalogRecordStatus.ACTIVE);
        Publisher publisher = publisher(10L, CatalogRecordStatus.ACTIVE);
        when(franchiseRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(franchise));
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(publisher));
        when(seriesRepository.save(any(CatalogSeries.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 30L));

        var response = seriesService.create(admin, request(20L, 10L, CatalogRecordStatus.ACTIVE));

        assertThat(response.franchiseName()).isEqualTo("Trigun");
        assertThat(response.primaryPublisherName()).isEqualTo("Dark Horse");
    }

    @Test
    void missingFranchiseIsRejected() {
        when(franchiseRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesService.create(
                admin,
                request(99L, null, CatalogRecordStatus.DRAFT)
        )).isInstanceOf(CatalogFranchiseNotFoundException.class);
    }

    @Test
    void missingPublisherIsRejected() {
        when(publisherRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesService.create(
                admin,
                request(null, 99L, CatalogRecordStatus.DRAFT)
        )).isInstanceOf(PublisherNotFoundException.class);
    }

    @Test
    void activeSeriesCannotReferenceDraftFranchise() {
        when(franchiseRepository.findByIdAndDeletedAtIsNull(20L))
                .thenReturn(Optional.of(franchise(20L, CatalogRecordStatus.DRAFT)));

        assertThatThrownBy(() -> seriesService.create(
                admin,
                request(20L, null, CatalogRecordStatus.ACTIVE)
        )).isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void duplicateSeriesCombinationIsRejected() {
        when(seriesRepository.existsByTitleIgnoreCaseAndTypeAndFranchiseIsNullAndDeletedAtIsNull(
                "Trigun Maximum",
                CatalogSeriesType.MANGA
        )).thenReturn(true);

        assertThatThrownBy(() -> seriesService.create(
                admin,
                request(null, null, CatalogRecordStatus.DRAFT)
        )).isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void publicCannotReadDraftSeries() {
        CatalogSeries series = withId(CatalogSeries.create(
                null,
                null,
                "Trigun Maximum",
                null,
                CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED,
                null,
                "JP",
                "ja",
                1997,
                2007,
                CatalogRecordStatus.DRAFT,
                1L
        ), 30L);
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(series));

        assertThatThrownBy(() -> seriesService.get(30L, null))
                .isInstanceOf(CatalogSeriesNotFoundException.class);
    }

    private CreateCatalogSeriesRequest request(
            Long franchiseId,
            Long publisherId,
            CatalogRecordStatus recordStatus
    ) {
        return new CreateCatalogSeriesRequest(
                franchiseId,
                publisherId,
                "Trigun Maximum",
                null,
                CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED,
                "Manga series",
                "JP",
                "ja",
                1997,
                2007,
                recordStatus
        );
    }

    private CatalogFranchise franchise(Long id, CatalogRecordStatus status) {
        return withId(CatalogFranchise.create("Trigun", "trigun", null, status, 1L), id);
    }

    private Publisher publisher(Long id, CatalogRecordStatus status) {
        return withId(Publisher.create("Dark Horse", "US", status, 1L), id);
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
