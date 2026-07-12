package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.dto.CatalogItemResponse;
import com.collectohub.catalog.dto.CreateCatalogItemRequest;
import com.collectohub.catalog.dto.UpdateCatalogItemRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class CatalogItemService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "sortOrder",
            "title",
            "firstPublicationYear",
            "originalLanguage",
            "originCountry",
            "recordStatus",
            "createdAt"
    );

    private final CatalogItemRepository itemRepository;
    private final CatalogSeriesRepository seriesRepository;
    private final CatalogItemEditionRepository editionRepository;

    public CatalogItemService(
            CatalogItemRepository itemRepository,
            CatalogSeriesRepository seriesRepository,
            CatalogItemEditionRepository editionRepository
    ) {
        this.itemRepository = itemRepository;
        this.seriesRepository = seriesRepository;
        this.editionRepository = editionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CatalogItemResponse> search(
            Long seriesId,
            AuthenticatedUser user,
            String q,
            Integer publicationYear,
            String language,
            String country,
            String requestedStatus,
            int page,
            int size,
            String sort
    ) {
        findSeriesForRead(seriesId, user);
        CatalogRecordStatus recordStatus = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        PageRequest pageRequest = itemPageRequest(page, size, sort);
        Specification<CatalogItem> specification = visibleItems(seriesId, recordStatus);

        String normalizedQuery = normalizeLike(q);
        if (normalizedQuery != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + normalizedQuery + "%"),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("originalTitle")),
                            "%" + normalizedQuery + "%"
                    )
            ));
        }
        if (publicationYear != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("firstPublicationYear"), publicationYear));
        }
        String normalizedLanguage = EditorialCatalogSupport.normalizeLanguage(language);
        if (normalizedLanguage != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("originalLanguage")), normalizedLanguage));
        }
        String normalizedCountry = EditorialCatalogSupport.normalizeCountry(country);
        if (normalizedCountry != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get("originCountry")), normalizedCountry));
        }

        return PageResponse.from(itemRepository.findAll(specification, pageRequest).map(CatalogItemResponse::from));
    }

    @Transactional(readOnly = true)
    public CatalogItemResponse get(Long id, AuthenticatedUser user) {
        CatalogItem item = findItem(id);
        if (!item.isPubliclyVisible() && !EditorialCatalogSupport.isEditorialAdmin(user)) {
            throw new CatalogItemNotFoundException(id);
        }
        return CatalogItemResponse.from(item);
    }

    @Transactional
    public CatalogItemResponse create(
            Long seriesId,
            AuthenticatedUser user,
            CreateCatalogItemRequest request
    ) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogSeries series = findSeries(seriesId);
        String title = EditorialCatalogSupport.normalizeRequired(request.title());
        String sequenceLabel = EditorialCatalogSupport.normalizeNullable(request.sequenceLabel());
        ensureDependencyIsPublishable(request.recordStatus(), series);
        ensureUnique(seriesId, title, sequenceLabel, null);

        CatalogItem item = CatalogItem.create(
                series,
                title,
                EditorialCatalogSupport.normalizeNullable(request.originalTitle()),
                sequenceLabel,
                request.sortOrder(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.firstPublicationDate(),
                request.firstPublicationYear(),
                EditorialCatalogSupport.normalizeLanguage(request.originalLanguage()),
                EditorialCatalogSupport.normalizeCountry(request.originCountry()),
                request.recordStatus(),
                user.id()
        );
        return CatalogItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public CatalogItemResponse update(Long id, AuthenticatedUser user, UpdateCatalogItemRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogItem item = findItem(id);
        CatalogSeries series = findSeries(request.seriesId());
        String title = EditorialCatalogSupport.normalizeRequired(request.title());
        String sequenceLabel = EditorialCatalogSupport.normalizeNullable(request.sequenceLabel());
        ensureDependencyIsPublishable(request.recordStatus(), series);
        ensureUnique(series.getId(), title, sequenceLabel, id);
        ensureCanChangeStatus(item, request.recordStatus());

        item.update(
                series,
                title,
                EditorialCatalogSupport.normalizeNullable(request.originalTitle()),
                sequenceLabel,
                request.sortOrder(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.firstPublicationDate(),
                request.firstPublicationYear(),
                EditorialCatalogSupport.normalizeLanguage(request.originalLanguage()),
                EditorialCatalogSupport.normalizeCountry(request.originCountry()),
                request.recordStatus(),
                user.id()
        );
        return CatalogItemResponse.from(item);
    }

    private CatalogItem findItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    private CatalogSeries findSeries(Long id) {
        return seriesRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogSeriesNotFoundException(id));
    }

    private CatalogSeries findSeriesForRead(Long id, AuthenticatedUser user) {
        CatalogSeries series = findSeries(id);
        if (!series.isPubliclyVisible() && !EditorialCatalogSupport.isEditorialAdmin(user)) {
            throw new CatalogSeriesNotFoundException(id);
        }
        return series;
    }

    private void ensureDependencyIsPublishable(CatalogRecordStatus status, CatalogSeries series) {
        if (status == CatalogRecordStatus.ACTIVE && !series.isPubliclyVisible()) {
            throw new InvalidEditorialCatalogRequestException(
                    "An active catalog item requires an active catalog series"
            );
        }
    }

    private void ensureCanChangeStatus(CatalogItem item, CatalogRecordStatus nextStatus) {
        if (item.getRecordStatus() == CatalogRecordStatus.ACTIVE
                && nextStatus != CatalogRecordStatus.ACTIVE
                && editionRepository.existsByCatalogItem_IdAndRecordStatusAndDeletedAtIsNull(
                        item.getId(),
                        CatalogRecordStatus.ACTIVE
                )) {
            throw new InvalidEditorialCatalogRequestException(
                    "Cannot archive a catalog item referenced by an active edition"
            );
        }
    }

    private void ensureUnique(Long seriesId, String title, String sequenceLabel, Long excludedId) {
        boolean duplicate;
        if (sequenceLabel == null) {
            duplicate = excludedId == null
                    ? itemRepository.existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIsNullAndDeletedAtIsNull(
                            seriesId, title)
                    : itemRepository.existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIsNullAndDeletedAtIsNullAndIdNot(
                            seriesId, title, excludedId);
        } else {
            duplicate = excludedId == null
                    ? itemRepository.existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIgnoreCaseAndDeletedAtIsNull(
                            seriesId, title, sequenceLabel)
                    : itemRepository.existsBySeries_IdAndTitleIgnoreCaseAndSequenceLabelIgnoreCaseAndDeletedAtIsNullAndIdNot(
                            seriesId, title, sequenceLabel, excludedId);
        }
        if (duplicate) {
            throw new DuplicateEditorialCatalogException(
                    "catalog item",
                    "series, title and sequence label combination already exists"
            );
        }
    }

    private Specification<CatalogItem> visibleItems(Long seriesId, CatalogRecordStatus recordStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.isNull(root.get("deletedAt")),
                criteriaBuilder.equal(root.get("series").get("id"), seriesId),
                criteriaBuilder.equal(root.get("recordStatus"), recordStatus)
        );
    }

    private PageRequest itemPageRequest(int page, int size, String sort) {
        PageRequest request = EditorialCatalogSupport.pageRequest(
                page, size, sort, "sortOrder", SORT_FIELDS);
        if (sort == null || sort.isBlank() || "sortOrder,asc".equalsIgnoreCase(sort.trim())) {
            return PageRequest.of(
                    page,
                    size,
                    Sort.by(
                            Sort.Order.asc("sortOrder").nullsLast(),
                            Sort.Order.asc("title")
                    )
            );
        }
        return request;
    }

    private String normalizeLike(String value) {
        String normalized = EditorialCatalogSupport.normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
