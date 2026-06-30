package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CreateCatalogItemEditionRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogItemEditionServiceTest {

    @Mock
    private CatalogItemEditionRepository editionRepository;

    @Mock
    private CatalogItemRepository itemRepository;

    @Mock
    private PublisherRepository publisherRepository;

    private CatalogItemEditionService editionService;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        editionService = new CatalogItemEditionService(editionRepository, itemRepository, publisherRepository);
        admin = authenticatedUser("ADMIN");
    }

    @Test
    void adminCreatesActiveEditionWithNormalizedIdentifiers() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(publisher(10L, CatalogRecordStatus.ACTIVE)));
        when(editionRepository.save(any(CatalogItemEdition.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 50L));

        var response = editionService.create(40L, admin, request(
                10L, "978-84-1234-567-8", "8437012345678", CatalogRecordStatus.ACTIVE));

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.isbn()).isEqualTo("9788412345678");
        assertThat(response.ean()).isEqualTo("8437012345678");
        assertThat(response.publisherName()).isEqualTo("Dark Horse");
    }

    @Test
    void missingItemIsRejected() {
        when(itemRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editionService.create(
                99L, admin, request(null, null, null, CatalogRecordStatus.DRAFT)))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test
    void missingPublisherIsRejected() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));
        when(publisherRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(99L, null, null, CatalogRecordStatus.DRAFT)))
                .isInstanceOf(PublisherNotFoundException.class);
    }

    @Test
    void activeEditionCannotUseDraftItem() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.DRAFT, CatalogRecordStatus.ACTIVE)));

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(null, null, null, CatalogRecordStatus.ACTIVE)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void activeEditionCannotUseItemFromDraftSeries() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.DRAFT)));

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(null, null, null, CatalogRecordStatus.ACTIVE)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void activeEditionCannotUseDraftPublisher() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));
        when(publisherRepository.findByIdAndDeletedAtIsNull(10L))
                .thenReturn(Optional.of(publisher(10L, CatalogRecordStatus.DRAFT)));

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(10L, null, null, CatalogRecordStatus.ACTIVE)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void duplicateIsbnIsRejectedAfterNormalization() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));
        when(editionRepository.existsByIsbnAndDeletedAtIsNull("9788412345678")).thenReturn(true);

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(null, "978-84-1234-567-8", null, CatalogRecordStatus.DRAFT)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void duplicateEanIsRejected() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));
        when(editionRepository.existsByEanAndDeletedAtIsNull("8437012345678")).thenReturn(true);

        assertThatThrownBy(() -> editionService.create(
                40L, admin, request(null, null, "8437012345678", CatalogRecordStatus.DRAFT)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void publicCannotReadDraftEdition() {
        CatalogItemEdition edition = edition(50L, CatalogRecordStatus.DRAFT);
        when(editionRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(edition));

        assertThatThrownBy(() -> editionService.get(50L, null))
                .isInstanceOf(CatalogItemEditionNotFoundException.class);
    }

    @Test
    void recordStatusFilterRequiresAdmin() {
        when(itemRepository.findByIdAndDeletedAtIsNull(40L))
                .thenReturn(Optional.of(item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE)));

        assertThatThrownBy(() -> editionService.search(
                40L, authenticatedUser("USER"), null, null, null, null,
                null, null, null, "DRAFT", 0, 20, "publicationYear,asc"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private CreateCatalogItemEditionRequest request(
            Long publisherId,
            String isbn,
            String ean,
            CatalogRecordStatus status
    ) {
        return new CreateCatalogItemEditionRequest(
                publisherId, isbn, ean, CatalogItemEditionFormat.PAPERBACK, "Spanish edition",
                LocalDate.of(2001, 1, 1), 2001, "ES", "es", 240,
                "https://example.com/cover.jpg", status
        );
    }

    private CatalogItemEdition edition(Long id, CatalogRecordStatus status) {
        return withId(CatalogItemEdition.create(
                item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE), null,
                null, null, CatalogItemEditionFormat.PAPERBACK, null, null, 2001,
                "es", "ES", 240, null, status, 1L
        ), id);
    }

    private CatalogItem item(Long id, CatalogRecordStatus itemStatus, CatalogRecordStatus seriesStatus) {
        return withId(CatalogItem.create(
                series(30L, seriesStatus), "Trigun Maximum Vol. 1", null, "1", null,
                null, null, 1997, "ja", "JP", itemStatus, 1L
        ), id);
    }

    private CatalogSeries series(Long id, CatalogRecordStatus status) {
        return withId(CatalogSeries.create(
                null, null, "Trigun Maximum", null, CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED, null, "JP", "ja", 1997, 2007, status, 1L
        ), id);
    }

    private Publisher publisher(Long id, CatalogRecordStatus status) {
        return withId(Publisher.create("Dark Horse", "US", status, 1L), id);
    }

    private AuthenticatedUser authenticatedUser(String roleCode) {
        User user = User.register(
                roleCode.toLowerCase() + "@example.com", "$2a$10$test-password-hash", "Test User",
                new Role(roleCode, roleCode)
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return AuthenticatedUser.from(user);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
