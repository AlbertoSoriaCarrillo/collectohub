package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.infrastructure.*;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EditorialDataQualityServiceTest {
    @Mock CreatorRepository creators; @Mock MasterProductCatalogLinkRepository links;
    @Mock PublisherRepository publishers; @Mock CatalogFranchiseRepository franchises;
    @Mock CatalogSeriesRepository series; @Mock CatalogItemRepository items; @Mock CatalogItemEditionRepository editions;
    EditorialDataQualityService service; AuthenticatedUser admin;

    @BeforeEach void setUp() {
        service = new EditorialDataQualityService(creators, links, publishers, franchises, series, items, editions);
        admin = user();
        when(publishers.findDuplicateNameGroups(anyInt())).thenReturn(List.of());
        when(franchises.findDuplicateNameGroups(anyInt())).thenReturn(List.of()); when(franchises.findDuplicateSlugGroups(anyInt())).thenReturn(List.of());
        when(series.findDuplicateTitleInFranchiseGroups(anyInt())).thenReturn(List.of());
        when(items.findDuplicateTitleInSeriesGroups(anyInt())).thenReturn(List.of()); when(items.findDuplicateSequenceInSeriesGroups(anyInt())).thenReturn(List.of());
        when(editions.findDuplicateIsbnGroups(anyInt())).thenReturn(List.of()); when(editions.findDuplicateEanGroups(anyInt())).thenReturn(List.of()); when(editions.findDuplicateNameInItemGroups(anyInt())).thenReturn(List.of());
        when(creators.findDuplicateNameGroups(anyInt())).thenReturn(List.of()); when(creators.findDuplicateSlugGroups(anyInt())).thenReturn(List.of());
        when(links.findMultipleVerifiedGroups(anyInt())).thenReturn(List.of()); when(links.findExactDuplicateGroups(anyInt())).thenReturn(List.of());
    }

    @Test void returnsEmptyReportForAllScopesAndCapsLimit() {
        var report = service.report(admin, "ALL", 500);
        assertThat(report.totalChecks()).isEqualTo(13); assertThat(report.totalFindings()).isZero();
        verify(publishers).findDuplicateNameGroups(200);
    }
    @Test void limitsScopeToCreatorsAndRejectsInvalidScope() {
        var report = service.report(admin, "CREATORS", 0);
        assertThat(report.checks()).extracting("entityType").containsOnly("CREATOR");
        verify(creators).findDuplicateNameGroups(1);
        assertThatThrownBy(() -> service.report(admin, "unknown", 50)).isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }
    @Test void editorialAdminCanReadQualityReportAndUserIsRejected() {
        assertThat(service.report(user("EDITORIAL_ADMIN"), "ALL", 50).totalChecks()).isEqualTo(13);
        assertThatThrownBy(() -> service.report(user("USER"), "ALL", 50))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
    private AuthenticatedUser user() { User user = User.register("admin@example.com", "hash", "Admin", new Role("ADMIN", "ADMIN")); ReflectionTestUtils.setField(user, "id", 1L); return AuthenticatedUser.from(user); }
    private AuthenticatedUser user(String role) { User user = User.register(role.toLowerCase() + "@example.com", "hash", role, new Role(role, role)); ReflectionTestUtils.setField(user, "id", 1L); return AuthenticatedUser.from(user); }
}
