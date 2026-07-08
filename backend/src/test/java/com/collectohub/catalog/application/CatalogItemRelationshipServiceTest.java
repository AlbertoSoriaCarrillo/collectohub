package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.*;
import com.collectohub.users.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogItemRelationshipServiceTest {
    @Mock CatalogItemRelationshipRepository repository;
    @Mock CatalogItemRepository itemRepository;
    CatalogItemRelationshipService service;
    AuthenticatedUser admin;
    CatalogItem source;
    CatalogItem target;

    @BeforeEach void setUp() {
        service = new CatalogItemRelationshipService(repository, itemRepository);
        admin = user("ADMIN");
        source = item(10L, "Source", true);
        target = item(20L, "Target", true);
    }

    @Test void createsRelationshipAsAdminWithDefaults() {
        itemsExist();
        when(repository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 30L));
        var response = service.create(10L, admin,
                new CreateCatalogItemRelationshipRequest(20L, CatalogItemRelationshipType.ADAPTATION, null, null, null));
        assertThat(response.relationshipOrder()).isEqualTo(1);
        assertThat(response.recordStatus()).isEqualTo("DRAFT");
        assertThat(response.direction()).isEqualTo("OUTGOING");
    }

    @Test void rejectsMissingSourceAndMissingTarget() {
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(10L, admin, request(20L)))
                .isInstanceOf(CatalogItemNotFoundException.class);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(10L, admin, request(20L)))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test void rejectsSelfRelationshipAndDuplicate() {
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        assertThatThrownBy(() -> service.create(10L, admin, request(10L)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
        itemsExist();
        when(repository.existsBySourceCatalogItem_IdAndTargetCatalogItem_IdAndRelationshipTypeAndDeletedAtIsNull(
                10L, 20L, CatalogItemRelationshipType.SEQUEL)).thenReturn(true);
        assertThatThrownBy(() -> service.create(10L, admin, request(20L)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test void updatesRelationshipAndSoftDeletes() {
        CatalogItemRelationship relationship = relationship(30L, source, target, CatalogRecordStatus.DRAFT, 1);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(repository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(relationship));
        var response = service.update(10L, 30L, admin,
                new UpdateCatalogItemRelationshipRequest(null, CatalogItemRelationshipType.REMAKE, 2,
                        "Reimagining", CatalogRecordStatus.ACTIVE));
        assertThat(response.relationshipType()).isEqualTo("REMAKE");
        assertThat(response.recordStatus()).isEqualTo("ACTIVE");
        service.delete(10L, 30L, admin);
        assertThat(relationship.isActive()).isFalse();
    }

    @Test void publicListReturnsOnlyActiveRelationshipsWithPublicItems() {
        CatalogItemRelationship active = relationship(30L, source, target, CatalogRecordStatus.ACTIVE, 1);
        CatalogItemRelationship draft = relationship(31L, source, target, CatalogRecordStatus.DRAFT, 2);
        CatalogItem hiddenTarget = item(21L, "Hidden", false);
        CatalogItemRelationship hidden = relationship(32L, source, hiddenTarget, CatalogRecordStatus.ACTIVE, 3);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(repository.findBySourceCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of(active, draft, hidden));
        when(repository.findByTargetCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of());
        assertThat(service.listRelationships(10L, null, null)).extracting("id").containsExactly(30L);
    }

    @Test void publicListRejectsHiddenPerspectiveItem() {
        CatalogItem hiddenSource = item(10L, "Hidden", false);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(hiddenSource));
        assertThatThrownBy(() -> service.listRelationships(10L, null, null))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test void adminCanListDraftAndArchivedRelationships() {
        CatalogItemRelationship draft = relationship(30L, source, target, CatalogRecordStatus.DRAFT, 1);
        CatalogItemRelationship archived = relationship(31L, source, target, CatalogRecordStatus.ARCHIVED, 2);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(repository.findBySourceCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of(draft, archived));
        when(repository.findByTargetCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of());
        assertThat(service.listRelationships(10L, admin, "DRAFT")).extracting("id").containsExactly(30L);
        assertThat(service.listRelationships(10L, admin, "ARCHIVED")).extracting("id").containsExactly(31L);
    }

    @Test void marksOutgoingAndIncomingAndOrdersByOrderThenRelatedTitle() {
        CatalogItem alpha = item(21L, "Alpha", true);
        CatalogItemRelationship outgoingSecond = relationship(31L, source, target, CatalogRecordStatus.ACTIVE, 2);
        CatalogItemRelationship outgoingAlpha = relationship(30L, source, alpha, CatalogRecordStatus.ACTIVE, 1);
        CatalogItemRelationship incoming = relationship(32L, target, source, CatalogRecordStatus.ACTIVE, 1);
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(repository.findBySourceCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of(outgoingSecond, outgoingAlpha));
        when(repository.findByTargetCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(10L))
                .thenReturn(List.of(incoming));
        var responses = service.listRelationships(10L, null, null);
        assertThat(responses).extracting(CatalogItemRelationshipResponse::id).containsExactly(30L, 32L, 31L);
        assertThat(responses).extracting(CatalogItemRelationshipResponse::direction)
                .containsExactly("OUTGOING", "INCOMING", "OUTGOING");
    }

    private void itemsExist() {
        when(itemRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(source));
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(target));
    }
    private CreateCatalogItemRelationshipRequest request(Long targetId) {
        return new CreateCatalogItemRelationshipRequest(targetId, CatalogItemRelationshipType.SEQUEL, 1, null,
                CatalogRecordStatus.ACTIVE);
    }
    private CatalogItemRelationship relationship(Long id, CatalogItem from, CatalogItem to,
            CatalogRecordStatus status, int order) {
        return withId(CatalogItemRelationship.create(from, to, CatalogItemRelationshipType.SEQUEL,
                order, null, status, 1L), id);
    }
    private CatalogItem item(Long id, String title, boolean visible) {
        CatalogItem item = mock(CatalogItem.class);
        CatalogSeries series = mock(CatalogSeries.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getTitle()).thenReturn(title);
        lenient().when(item.getSeries()).thenReturn(series);
        lenient().when(item.isPubliclyVisible()).thenReturn(visible);
        lenient().when(series.getId()).thenReturn(id + 100);
        lenient().when(series.getTitle()).thenReturn(title + " Series");
        return item;
    }
    private AuthenticatedUser user(String roleCode) {
        User user = User.register("admin@example.com", "hash", "Admin", new Role(roleCode, roleCode));
        ReflectionTestUtils.setField(user, "id", 1L);
        return AuthenticatedUser.from(user);
    }
    private <T> T withId(T value, Long id) { ReflectionTestUtils.setField(value, "id", id); return value; }
}
