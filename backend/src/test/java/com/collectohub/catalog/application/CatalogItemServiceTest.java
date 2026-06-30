package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.dto.CreateCatalogItemRequest;
import com.collectohub.catalog.dto.UpdateCatalogItemRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogItemServiceTest {

    @Mock
    private CatalogItemRepository itemRepository;

    @Mock
    private CatalogSeriesRepository seriesRepository;

    @Mock
    private CatalogItemEditionRepository editionRepository;

    private CatalogItemService itemService;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        itemService = new CatalogItemService(itemRepository, seriesRepository, editionRepository);
        admin = authenticatedUser("ADMIN");
    }

    @Test
    void adminCreatesActiveItemInActiveSeries() {
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L))
                .thenReturn(Optional.of(series(30L, CatalogRecordStatus.ACTIVE)));
        when(itemRepository.save(any(CatalogItem.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 40L));

        var response = itemService.create(30L, admin, createRequest(CatalogRecordStatus.ACTIVE, " 1 "));

        assertThat(response.id()).isEqualTo(40L);
        assertThat(response.seriesId()).isEqualTo(30L);
        assertThat(response.sequenceLabel()).isEqualTo("1");
        assertThat(response.originalLanguage()).isEqualTo("ja");
        assertThat(response.originCountry()).isEqualTo("JP");
    }

    @Test
    void missingSeriesIsRejected() {
        when(seriesRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(
                99L, admin, createRequest(CatalogRecordStatus.DRAFT, "1")))
                .isInstanceOf(CatalogSeriesNotFoundException.class);
    }

    @Test
    void activeItemCannotUseDraftSeries() {
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L))
                .thenReturn(Optional.of(series(30L, CatalogRecordStatus.DRAFT)));

        assertThatThrownBy(() -> itemService.create(
                30L, admin, createRequest(CatalogRecordStatus.ACTIVE, "1")))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void duplicateItemCombinationIsRejected() {
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L))
                .thenReturn(Optional.of(series(30L, CatalogRecordStatus.ACTIVE)));
        when(itemRepository.existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIgnoreCaseAndDeletedAtIsNull(
                30L, "Trigun Maximum Vol. 1", "1"))
                .thenReturn(true);

        assertThatThrownBy(() -> itemService.create(
                30L, admin, createRequest(CatalogRecordStatus.DRAFT, "1")))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void publicCannotReadDraftItem() {
        CatalogItem item = item(40L, CatalogRecordStatus.DRAFT, CatalogRecordStatus.ACTIVE);
        when(itemRepository.findByIdAndDeletedAtIsNull(40L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.get(40L, null))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test
    void publicCannotReadActiveItemFromDraftSeries() {
        CatalogItem item = item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.DRAFT);
        when(itemRepository.findByIdAndDeletedAtIsNull(40L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.get(40L, null))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test
    void recordStatusFilterRequiresAdmin() {
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L))
                .thenReturn(Optional.of(series(30L, CatalogRecordStatus.ACTIVE)));

        assertThatThrownBy(() -> itemService.search(
                30L, authenticatedUser("USER"), null, null, null, null,
                "DRAFT", 0, 20, "sortOrder,asc"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void activeItemWithActiveEditionCannotBeArchived() {
        CatalogItem item = item(40L, CatalogRecordStatus.ACTIVE, CatalogRecordStatus.ACTIVE);
        when(itemRepository.findByIdAndDeletedAtIsNull(40L)).thenReturn(Optional.of(item));
        when(seriesRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(item.getSeries()));
        when(editionRepository.existsByCatalogItem_IdAndRecordStatusAndDeletedAtIsNull(
                40L, CatalogRecordStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> itemService.update(
                40L, admin, updateRequest(CatalogRecordStatus.ARCHIVED)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    private CreateCatalogItemRequest createRequest(CatalogRecordStatus status, String sequenceLabel) {
        return new CreateCatalogItemRequest(
                "Trigun Maximum Vol. 1", null, sequenceLabel, new BigDecimal("1.000"),
                "First volume", LocalDate.of(1997, 1, 1), 1997, "JA", "jp", status
        );
    }

    private UpdateCatalogItemRequest updateRequest(CatalogRecordStatus status) {
        return new UpdateCatalogItemRequest(
                30L, "Trigun Maximum Vol. 1", null, "1", new BigDecimal("1.000"),
                null, null, 1997, "ja", "JP", status
        );
    }

    private CatalogItem item(Long id, CatalogRecordStatus itemStatus, CatalogRecordStatus seriesStatus) {
        return withId(CatalogItem.create(
                series(30L, seriesStatus), "Trigun Maximum Vol. 1", null, "1",
                new BigDecimal("1.000"), null, null, 1997, "ja", "JP", itemStatus, 1L
        ), id);
    }

    private CatalogSeries series(Long id, CatalogRecordStatus status) {
        return withId(CatalogSeries.create(
                null, null, "Trigun Maximum", null, CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED, null, "JP", "ja", 1997, 2007, status, 1L
        ), id);
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
