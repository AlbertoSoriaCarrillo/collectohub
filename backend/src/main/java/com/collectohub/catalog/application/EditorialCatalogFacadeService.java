package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.EditorialCatalogResultType;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import com.collectohub.catalog.dto.CatalogFranchiseResponse;
import com.collectohub.catalog.dto.CatalogItemEditionResponse;
import com.collectohub.catalog.dto.CatalogItemResponse;
import com.collectohub.catalog.dto.CatalogSeriesResponse;
import com.collectohub.catalog.dto.EditorialCatalogDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogEditionDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogCreatorCreditResponse;
import com.collectohub.catalog.dto.EditorialCatalogItemDetailResponse;
import com.collectohub.catalog.dto.EditorialCatalogSearchItemResponse;
import com.collectohub.catalog.dto.EditorialCatalogSeriesDetailResponse;
import com.collectohub.catalog.dto.EditorialLegacyBridgeResponse;
import com.collectohub.catalog.dto.PublisherResponse;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository.SearchCriteria;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository.SearchPage;
import com.collectohub.catalog.infrastructure.EditorialCatalogFacadeRepository.SearchRow;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class EditorialCatalogFacadeService {

    private static final Set<String> SORT_FIELDS = Set.of("title", "publicationYear", "resultType");

    private final EditorialCatalogFacadeRepository facadeRepository;
    private final CatalogSeriesRepository seriesRepository;
    private final CatalogItemRepository itemRepository;
    private final CatalogItemEditionRepository editionRepository;
    private final MasterProductCatalogLinkRepository linkRepository;
    private final CatalogItemCreatorService creatorService;
    private final CatalogItemRelationshipService relationshipService;

    public EditorialCatalogFacadeService(
            EditorialCatalogFacadeRepository facadeRepository,
            CatalogSeriesRepository seriesRepository,
            CatalogItemRepository itemRepository,
            CatalogItemEditionRepository editionRepository,
            MasterProductCatalogLinkRepository linkRepository,
            CatalogItemCreatorService creatorService,
            CatalogItemRelationshipService relationshipService
    ) {
        this.facadeRepository = facadeRepository;
        this.seriesRepository = seriesRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
        this.linkRepository = linkRepository;
        this.creatorService = creatorService;
        this.relationshipService = relationshipService;
    }

    @Transactional(readOnly = true)
    public PageResponse<EditorialCatalogSearchItemResponse> search(
            AuthenticatedUser user,
            String q,
            String type,
            Long franchiseId,
            Long seriesId,
            Long publisherId,
            String language,
            String country,
            Integer publicationYear,
            String resultType,
            int page,
            int size,
            String sort
    ) {
        validateId(franchiseId, "franchiseId");
        validateId(seriesId, "seriesId");
        validateId(publisherId, "publisherId");
        if (publicationYear != null && (publicationYear < 1000 || publicationYear > 3000)) {
            throw new InvalidCatalogFilterException("publicationYear must be between 1000 and 3000");
        }

        CatalogSeriesType seriesType = EditorialCatalogSupport.parseOptionalEnum(
                type, CatalogSeriesType.class, "type");
        EditorialCatalogResultType parsedResultType = EditorialCatalogSupport.parseOptionalEnum(
                resultType, EditorialCatalogResultType.class, "resultType");
        if (parsedResultType == EditorialCatalogResultType.MASTER_PRODUCT_LINK) {
            EditorialCatalogSupport.ensureAdmin(user);
        }

        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(
                page, size, sort, "title", SORT_FIELDS);
        if (pageRequest.getOffset() > Integer.MAX_VALUE) {
            throw new InvalidCatalogFilterException("Requested page is too large");
        }

        SearchCriteria criteria = new SearchCriteria(
                EditorialCatalogSupport.normalizeNullable(q),
                seriesType == null ? null : seriesType.name(),
                franchiseId,
                seriesId,
                publisherId,
                EditorialCatalogSupport.normalizeLanguage(language),
                EditorialCatalogSupport.normalizeCountry(country),
                publicationYear,
                parsedResultType
        );
        SearchPage result = facadeRepository.search(criteria, pageRequest);
        List<EditorialCatalogSearchItemResponse> content = result.content().stream()
                .map(this::toSearchResponse)
                .toList();
        int totalPages = result.totalElements() == 0
                ? 0
                : (int) Math.ceil((double) result.totalElements() / size);
        return new PageResponse<>(
                content,
                page,
                size,
                result.totalElements(),
                totalPages,
                page == 0,
                page + 1 >= totalPages
        );
    }

    @Transactional(readOnly = true)
    public EditorialCatalogSeriesDetailResponse getSeriesDetail(Long seriesId) {
        CatalogSeries series = findPublicSeries(seriesId);
        List<EditorialCatalogItemDetailResponse> items = itemRepository
                .findAllBySeries_IdAndRecordStatusAndDeletedAtIsNullOrderBySortOrderAscTitleAsc(
                        seriesId, CatalogRecordStatus.ACTIVE)
                .stream()
                .filter(CatalogItem::isPubliclyVisible)
                .map(this::toItemDetail)
                .toList();
        return new EditorialCatalogSeriesDetailResponse(context(series), items);
    }

    @Transactional(readOnly = true)
    public EditorialCatalogItemDetailResponse getItemDetail(Long itemId) {
        return toItemDetail(findPublicItem(itemId));
    }

    @Transactional(readOnly = true)
    public EditorialCatalogEditionDetailResponse getEditionDetail(Long editionId) {
        CatalogItemEdition edition = editionRepository.findByIdAndDeletedAtIsNull(editionId)
                .filter(CatalogItemEdition::isPubliclyVisible)
                .orElseThrow(() -> new CatalogItemEditionNotFoundException(editionId));
        CatalogItem item = edition.getCatalogItem();
        return new EditorialCatalogEditionDetailResponse(
                context(item.getSeries()),
                CatalogItemResponse.from(item),
                CatalogItemEditionResponse.from(edition),
                edition.getPublisher() == null ? null : PublisherResponse.from(edition.getPublisher())
        );
    }

    @Transactional(readOnly = true)
    public EditorialLegacyBridgeResponse getLegacyLink(Long masterProductId, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProductCatalogLink link = linkRepository
                .findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                        masterProductId, MasterProductCatalogLinkStatus.VERIFIED)
                .or(() -> linkRepository
                        .findFirstByMasterProduct_IdAndLinkStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
                                masterProductId, MasterProductCatalogLinkStatus.PROPOSED))
                .orElseThrow(() -> new EditorialLegacyBridgeNotFoundException(masterProductId));
        CatalogItemEdition edition = link.getCatalogItemEdition();
        return new EditorialLegacyBridgeResponse(
                link.getId(),
                link.getMasterProduct().getId(),
                link.getMasterProduct().getName(),
                link.getLinkStatus().name(),
                link.getLinkSource().name(),
                link.getConfidenceScore(),
                link.getMatchReason(),
                link.getCatalogItem().getId(),
                link.getCatalogItem().getTitle(),
                edition == null ? null : edition.getId(),
                edition == null ? null : editionLabel(edition)
        );
    }

    private CatalogSeries findPublicSeries(Long id) {
        return seriesRepository.findByIdAndDeletedAtIsNull(id)
                .filter(CatalogSeries::isPubliclyVisible)
                .orElseThrow(() -> new CatalogSeriesNotFoundException(id));
    }

    private CatalogItem findPublicItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .filter(CatalogItem::isPubliclyVisible)
                .orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    private EditorialCatalogItemDetailResponse toItemDetail(CatalogItem item) {
        List<CatalogItemEditionResponse> editions = editionRepository
                .findAllByCatalogItem_IdAndRecordStatusAndDeletedAtIsNullOrderByPublicationYearAscIdAsc(
                        item.getId(), CatalogRecordStatus.ACTIVE)
                .stream()
                .filter(CatalogItemEdition::isPubliclyVisible)
                .map(CatalogItemEditionResponse::from)
                .toList();
        return new EditorialCatalogItemDetailResponse(
                context(item.getSeries()),
                CatalogItemResponse.from(item),
                editions,
                creatorService.listPublic(item.getId()).stream()
                        .map(EditorialCatalogCreatorCreditResponse::from)
                        .toList(),
                relationshipService.listRelationships(item.getId(), null, null)
        );
    }

    private EditorialCatalogDetailResponse context(CatalogSeries series) {
        return new EditorialCatalogDetailResponse(
                CatalogSeriesResponse.from(series),
                series.getFranchise() == null ? null : CatalogFranchiseResponse.from(series.getFranchise()),
                series.getPrimaryPublisher() == null ? null : PublisherResponse.from(series.getPrimaryPublisher())
        );
    }

    private EditorialCatalogSearchItemResponse toSearchResponse(SearchRow row) {
        return new EditorialCatalogSearchItemResponse(
                row.resultType(), row.seriesId(), row.seriesTitle(), row.itemId(), row.itemTitle(),
                row.editionId(), row.editionName(), row.publisherName(), row.franchiseName(), row.type(),
                row.language(), row.country(), row.publicationYear(), row.coverImageUrl(),
                row.linkedMasterProductId(), row.linkedMasterProductName()
        );
    }

    private void validateId(Long id, String name) {
        if (id != null && id < 1) {
            throw new InvalidCatalogFilterException(name + " must be greater than 0");
        }
    }

    private String editionLabel(CatalogItemEdition edition) {
        if (edition.getEditionName() != null) {
            return edition.getEditionName();
        }
        return edition.getPublicationYear() == null
                ? edition.getFormat().name()
                : edition.getFormat().name() + " " + edition.getPublicationYear();
    }
}
