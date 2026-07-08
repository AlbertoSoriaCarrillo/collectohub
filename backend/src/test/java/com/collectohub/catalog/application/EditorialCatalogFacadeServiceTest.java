package com.collectohub.catalog.application;

import com.collectohub.TestSecurityConfiguration;
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
import com.collectohub.catalog.dto.EditorialCatalogItemDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogSearchItemResponse;
import com.collectohub.catalog.dto.CatalogItemCreatorResponse;
import com.collectohub.shared.dto.PageResponse;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository.SearchPage;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository.SearchRow;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditorialCatalogFacadeServiceTest {

    @Mock private EditorialCatalogFacadeRepository facadeRepository;
    @Mock private CatalogSeriesRepository seriesRepository;
    @Mock private CatalogItemRepository itemRepository;
    @Mock private CatalogItemEditionRepository editionRepository;
    @Mock private MasterProductCatalogLinkRepository linkRepository;
    @Mock private CatalogItemCreatorService creatorService;

    private EditorialCatalogFacadeService service;

    @BeforeEach
    void setUp() {
        service = new EditorialCatalogFacadeService(
                facadeRepository, seriesRepository, itemRepository, editionRepository, linkRepository, creatorService);
    }

    @Test
    void publicSearchReturnsPaginatedEditorialResults() {
        when(facadeRepository.search(any(), any())).thenReturn(new SearchPage(List.of(new SearchRow(
                "EDITION", 1L, "Trigun", 2L, "Volume 1", 3L, "Paperback",
                "Dark Horse", "Trigun", "MANGA", "en", "US", 2004,
                "https://example.test/cover.jpg", null, null
        )), 1));

        PageResponse<EditorialCatalogSearchItemResponse> response = service.search(
                null, "trigun", "MANGA", null, null, null, "en", "US",
                2004, "EDITION", 0, 20, "title,asc");

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).singleElement()
                .satisfies(result -> {
                    assertThat(result.resultType()).isEqualTo("EDITION");
                    assertThat(result.editionId()).isEqualTo(3L);
                });
    }

    @Test
    void publicSearchCannotExposeMasterProductLinks() {
        assertThatThrownBy(() -> service.search(
                user("USER"), null, null, null, null, null, null, null,
                null, "MASTER_PRODUCT_LINK", 0, 20, "title,asc"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void invalidPaginationAndFiltersAreRejected() {
        assertThatThrownBy(() -> service.search(
                null, null, null, null, null, null, null, null,
                null, null, -1, 20, "title,asc"))
                .isInstanceOf(InvalidCatalogFilterException.class);
        assertThatThrownBy(() -> service.search(
                null, null, "MOVIE", null, null, null, null, null,
                null, null, 0, 20, "title,asc"))
                .isInstanceOf(InvalidCatalogFilterException.class);
    }

    @Test
    void itemDetailContainsOnlyActiveEditions() {
        CatalogSeries series = series(CatalogRecordStatus.ACTIVE);
        CatalogItem item = item(series, CatalogRecordStatus.ACTIVE);
        CatalogItemEdition active = edition(item, CatalogRecordStatus.ACTIVE);
        CatalogItemEdition draft = edition(item, CatalogRecordStatus.DRAFT);
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item));
        when(editionRepository
                .findAllByCatalogItem_IdAndRecordStatusAndDeletedAtIsNullOrderByPublicationYearAscIdAsc(
                        item.getId(), CatalogRecordStatus.ACTIVE))
                .thenReturn(List.of(active, draft));

        EditorialCatalogItemDetailResponse response = service.getItemDetail(2L);

        assertThat(response.editions()).hasSize(1);
        assertThat(response.editions().getFirst().editionName()).isEqualTo("Paperback");
        assertThat(response.creators()).isEmpty();
    }

    @Test
    void itemDetailContainsOrderedPublicCreatorCredits() {
        CatalogSeries series = series(CatalogRecordStatus.ACTIVE);
        CatalogItem item = item(series, CatalogRecordStatus.ACTIVE);
        when(itemRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(item));
        when(editionRepository.findAllByCatalogItem_IdAndRecordStatusAndDeletedAtIsNullOrderByPublicationYearAscIdAsc(
                item.getId(), CatalogRecordStatus.ACTIVE)).thenReturn(List.of());
        when(creatorService.listPublic(item.getId())).thenReturn(List.of(
                new CatalogItemCreatorResponse(10L, item.getId(), 20L, "Yasuhiro Nightow", "yasuhiro-nightow", "AUTHOR", 1, null),
                new CatalogItemCreatorResponse(11L, item.getId(), 21L, "Justin Burns", "justin-burns", "TRANSLATOR", 2, "English translation")
        ));

        EditorialCatalogItemDetailResponse response = service.getItemDetail(2L);

        assertThat(response.creators()).extracting("creatorName")
                .containsExactly("Yasuhiro Nightow", "Justin Burns");
        assertThat(response.creators().get(1).creditLabel()).isEqualTo("English translation");
    }

    @Test
    void seriesDetailContainsOnlyActiveItems() {
        CatalogSeries series = series(CatalogRecordStatus.ACTIVE);
        CatalogItem active = item(series, CatalogRecordStatus.ACTIVE);
        CatalogItem draft = item(series, CatalogRecordStatus.DRAFT);
        when(seriesRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(series));
        when(itemRepository.findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(
                1L, CatalogRecordStatus.ACTIVE)).thenReturn(List.of(active, draft));
        when(editionRepository
                .findAllByCatalogItem_IdAndRecordStatusAndDeletedAtIsNullOrderByPublicationYearAscIdAsc(
                        active.getId(), CatalogRecordStatus.ACTIVE))
                .thenReturn(List.of());

        assertThat(service.getSeriesDetail(1L).items()).hasSize(1);
    }

    @Test
    void adminLegacyLookupPrefersVerifiedLink() {
        MasterProductCatalogLink verified = link(MasterProductCatalogLinkStatus.VERIFIED);
        when(linkRepository.findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                8L, MasterProductCatalogLinkStatus.VERIFIED)).thenReturn(Optional.of(verified));

        assertThat(service.getLegacyLink(8L, user("ADMIN")).linkStatus()).isEqualTo("VERIFIED");
    }

    @Test
    void regularUserCannotReadProposedLegacyLink() {
        assertThatThrownBy(() -> service.getLegacyLink(8L, user("USER")))
                .isInstanceOf(AccessDeniedException.class);
    }

    private CatalogSeries series(CatalogRecordStatus status) {
        return CatalogSeries.create(
                null, null, "Trigun", null, CatalogSeriesType.MANGA,
                CatalogPublicationStatus.COMPLETED, null, "JP", "ja", 1995, 1997, status, 1L);
    }

    private CatalogItem item(CatalogSeries series, CatalogRecordStatus status) {
        return CatalogItem.create(
                series, "Volume 1", null, "1", BigDecimal.ONE, null,
                LocalDate.of(1995, 1, 1), 1995, "ja", "JP", status, 1L);
    }

    private CatalogItemEdition edition(CatalogItem item, CatalogRecordStatus status) {
        return CatalogItemEdition.create(
                item, null, "9780000000001", null, CatalogItemEditionFormat.PAPERBACK,
                "Paperback", LocalDate.of(2004, 1, 1), 2004, "en", "US", 200,
                null, status, 1L);
    }

    private MasterProductCatalogLink link(MasterProductCatalogLinkStatus status) {
        MasterProduct masterProduct = mock(MasterProduct.class);
        CatalogItem catalogItem = mock(CatalogItem.class);
        when(masterProduct.getId()).thenReturn(8L);
        when(masterProduct.getName()).thenReturn("Trigun Volume 1");
        when(catalogItem.getId()).thenReturn(2L);
        when(catalogItem.getTitle()).thenReturn("Volume 1");
        return MasterProductCatalogLink.create(
                masterProduct, catalogItem, null, status, MasterProductCatalogLinkSource.MANUAL,
                BigDecimal.ONE, "Reviewed", null, 1L);
    }

    private AuthenticatedUser user(String role) {
        return AuthenticatedUser.from(TestSecurityConfiguration.testUser("user@example.com", role));
    }
}
