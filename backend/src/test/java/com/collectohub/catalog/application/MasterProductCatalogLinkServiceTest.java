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
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkSource;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.dto.CreateMasterProductCatalogLinkRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterProductCatalogLinkServiceTest {

    @Mock
    private MasterProductCatalogLinkRepository linkRepository;
    @Mock
    private MasterProductRepository masterProductRepository;
    @Mock
    private CatalogItemRepository itemRepository;
    @Mock
    private CatalogItemEditionRepository editionRepository;

    private MasterProductCatalogLinkService service;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        service = new MasterProductCatalogLinkService(
                linkRepository, masterProductRepository, itemRepository, editionRepository);
        admin = admin();
    }

    @Test
    void createsProposedManualLink() {
        MasterProduct product = product(1L, "9788412345678", null);
        CatalogItem item = item(2L);
        when(masterProductRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item));
        when(linkRepository.save(any(MasterProductCatalogLink.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 4L));

        var response = service.create(admin, request(
                MasterProductCatalogLinkStatus.PROPOSED, null));

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.linkStatus()).isEqualTo("PROPOSED");
        assertThat(response.linkSource()).isEqualTo("MANUAL");
    }

    @Test
    void createsVerifiedWhenNoVerifiedLinkExists() {
        stubDependencies(null);
        when(linkRepository.save(any(MasterProductCatalogLink.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 4L));

        var response = service.create(admin, request(MasterProductCatalogLinkStatus.VERIFIED, null));

        assertThat(response.linkStatus()).isEqualTo("VERIFIED");
    }

    @Test
    void verifiedConflictIsRejected() {
        stubDependencies(null);
        when(linkRepository.existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                1L, MasterProductCatalogLinkStatus.VERIFIED)).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                admin, request(MasterProductCatalogLinkStatus.VERIFIED, null)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test
    void missingDependenciesAreRejected() {
        when(masterProductRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(admin, request(MasterProductCatalogLinkStatus.PROPOSED, null)))
                .isInstanceOf(MasterProductNotFoundException.class);

        when(masterProductRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product(1L, null, null)));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(admin, request(MasterProductCatalogLinkStatus.PROPOSED, null)))
                .isInstanceOf(CatalogItemNotFoundException.class);

        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item(2L)));
        when(editionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(admin, request(MasterProductCatalogLinkStatus.PROPOSED, 99L)))
                .isInstanceOf(CatalogItemEditionNotFoundException.class);
    }

    @Test
    void editionMustBelongToSelectedItem() {
        CatalogItem selected = item(2L);
        CatalogItemEdition edition = edition(3L, item(20L), "9788412345678", null);
        when(masterProductRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product(1L, null, null)));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(selected));
        when(editionRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(edition));

        assertThatThrownBy(() -> service.create(
                admin, request(MasterProductCatalogLinkStatus.PROPOSED, 3L)))
                .isInstanceOf(InvalidEditorialCatalogRequestException.class);
    }

    @Test
    void verifiesAndRejectsExistingLink() {
        MasterProductCatalogLink link = link(4L, MasterProductCatalogLinkStatus.PROPOSED);
        when(linkRepository.findByIdAndDeletedAtIsNull(4L)).thenReturn(Optional.of(link));

        assertThat(service.verify(4L, admin).linkStatus()).isEqualTo("VERIFIED");
        assertThat(service.reject(4L, admin).linkStatus()).isEqualTo("REJECTED");
    }

    private void stubDependencies(CatalogItemEdition edition) {
        when(masterProductRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(product(1L, null, null)));
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item(2L)));
        if (edition != null) {
            when(editionRepository.findByIdAndDeletedAtIsNull(edition.getId())).thenReturn(Optional.of(edition));
        }
    }

    private CreateMasterProductCatalogLinkRequest request(
            MasterProductCatalogLinkStatus status,
            Long editionId
    ) {
        return new CreateMasterProductCatalogLinkRequest(
                1L, 2L, editionId, status, MasterProductCatalogLinkSource.MANUAL,
                new BigDecimal("0.9000"), "Manual reconciliation", null
        );
    }

    private MasterProductCatalogLink link(Long id, MasterProductCatalogLinkStatus status) {
        return withId(MasterProductCatalogLink.create(
                product(1L, null, null), item(2L), null, status,
                MasterProductCatalogLinkSource.MANUAL, null, null, null, 1L
        ), id);
    }

    private MasterProduct product(Long id, String isbn, String ean) {
        return withId(MasterProduct.create(
                "Trigun Maximum Vol. 1", null, new ProductCategory("MANGA_COMIC", "Manga"),
                null, "Trigun Maximum", "1", null, isbn, ean,
                null, null, null, "ja", false, List.of(), null, Map.of(), 1L
        ), id);
    }

    private CatalogItem item(Long id) {
        CatalogSeries series = withId(CatalogSeries.create(
                null, null, "Trigun Maximum", null, CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED, null, "JP", "ja", 1997, 2007,
                CatalogRecordStatus.ACTIVE, 1L), 10L);
        return withId(CatalogItem.create(
                series, "Trigun Maximum Vol. 1", null, "1", null,
                null, null, 1997, "ja", "JP", CatalogRecordStatus.ACTIVE, 1L
        ), id);
    }

    private CatalogItemEdition edition(Long id, CatalogItem item, String isbn, String ean) {
        return withId(CatalogItemEdition.create(
                item, null, isbn, ean, CatalogItemEditionFormat.PAPERBACK,
                "Spanish edition", null, 2001, "es", "ES", 240, null,
                CatalogRecordStatus.ACTIVE, 1L
        ), id);
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
