package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterProductCatalogBackfillServiceTest {

    @Mock private MasterProductRepository masterProductRepository;
    @Mock private CatalogItemRepository itemRepository;
    @Mock private CatalogItemEditionRepository editionRepository;
    @Mock private MasterProductCatalogLinkService linkService;

    private MasterProductCatalogBackfillService service;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        service = new MasterProductCatalogBackfillService(
                masterProductRepository, itemRepository, editionRepository, linkService);
        admin = admin();
    }

    @Test
    void uniqueIsbnCreatesProposedLink() {
        MasterProduct product = product(1L, "978-84-1234-567-8", null);
        CatalogItemEdition edition = edition(3L, "9788412345678", null);
        when(masterProductRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(product));
        when(editionRepository.findAllByIsbnAndDeletedAtIsNull("9788412345678"))
                .thenReturn(List.of(edition));
        when(linkService.createProposal(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        var result = service.run(admin);

        assertThat(result.proposed()).isEqualTo(1);
        verify(masterProductRepository, never()).save(any(MasterProduct.class));
        verify(itemRepository, never()).save(any(CatalogItem.class));
        verify(editionRepository, never()).save(any(CatalogItemEdition.class));
    }

    @Test
    void uniqueEanCreatesProposedLink() {
        MasterProduct product = product(1L, null, "8437012345678");
        CatalogItemEdition edition = edition(3L, null, "8437012345678");
        when(masterProductRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(product));
        when(editionRepository.findAllByEanAndDeletedAtIsNull("8437012345678"))
                .thenReturn(List.of(edition));
        when(linkService.createProposal(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        assertThat(service.run(admin).proposed()).isEqualTo(1);
    }

    @Test
    void repeatedBackfillDoesNotDuplicateLink() {
        MasterProduct product = product(1L, "9788412345678", null);
        CatalogItemEdition edition = edition(3L, "9788412345678", null);
        when(masterProductRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(product));
        when(editionRepository.findAllByIsbnAndDeletedAtIsNull("9788412345678"))
                .thenReturn(List.of(edition));
        when(linkService.createProposal(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);

        var result = service.run(admin);

        assertThat(result.proposed()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
    }

    private MasterProduct product(Long id, String isbn, String ean) {
        return withId(MasterProduct.create(
                "Trigun Maximum Vol. 1", null, new ProductCategory("MANGA_COMIC", "Manga"),
                null, "Trigun Maximum", "1", null, isbn, ean,
                null, null, null, "ja", false, List.of(), null, Map.of(), 1L
        ), id);
    }

    private CatalogItemEdition edition(Long id, String isbn, String ean) {
        CatalogSeries series = withId(CatalogSeries.create(
                null, null, "Trigun Maximum", null, CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED, null, "JP", "ja", 1997, 2007,
                CatalogRecordStatus.ACTIVE, 1L), 10L);
        CatalogItem item = withId(CatalogItem.create(
                series, "Trigun Maximum Vol. 1", null, "1", null,
                null, null, 1997, "ja", "JP", CatalogRecordStatus.ACTIVE, 1L), 2L);
        return withId(CatalogItemEdition.create(
                item, null, isbn, ean, CatalogItemEditionFormat.PAPERBACK,
                null, null, 2001, "es", "ES", 240, null,
                CatalogRecordStatus.ACTIVE, 1L), id);
    }

    private AuthenticatedUser admin() {
        User user = User.register(
                "admin@example.com", "$2a$10$test-password-hash", "Admin",
                new Role("ADMIN", "Administrator"));
        ReflectionTestUtils.setField(user, "id", 1L);
        return AuthenticatedUser.from(user);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
