package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogFranchise;
import com.collectohub.catalog.domain.CatalogPublicationStatus;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.domain.CatalogSeriesType;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CatalogSeriesResponse;
import com.collectohub.catalog.dto.CreateCatalogSeriesRequest;
import com.collectohub.catalog.dto.UpdateCatalogSeriesRequest;
import com.collectohub.catalog.infrastructure.CatalogFranchiseRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.PublisherRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class CatalogSeriesService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "title",
            "type",
            "publicationStatus",
            "recordStatus",
            "startYear",
            "createdAt"
    );

    private final CatalogSeriesRepository seriesRepository;
    private final CatalogFranchiseRepository franchiseRepository;
    private final PublisherRepository publisherRepository;
    private final CatalogItemRepository itemRepository;

    public CatalogSeriesService(
            CatalogSeriesRepository seriesRepository,
            CatalogFranchiseRepository franchiseRepository,
            PublisherRepository publisherRepository,
            CatalogItemRepository itemRepository
    ) {
        this.seriesRepository = seriesRepository;
        this.franchiseRepository = franchiseRepository;
        this.publisherRepository = publisherRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CatalogSeriesResponse> search(
            AuthenticatedUser user,
            String q,
            Long franchiseId,
            String type,
            String publicationStatus,
            Long publisherId,
            String language,
            String country,
            String requestedStatus,
            int page,
            int size,
            String sort
    ) {
        CatalogRecordStatus recordStatus = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        CatalogSeriesType seriesType = EditorialCatalogSupport.parseOptionalEnum(
                type,
                CatalogSeriesType.class,
                "type"
        );
        CatalogPublicationStatus parsedPublicationStatus = EditorialCatalogSupport.parseOptionalEnum(
                publicationStatus,
                CatalogPublicationStatus.class,
                "publicationStatus"
        );
        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(page, size, sort, "title", SORT_FIELDS);
        Specification<CatalogSeries> specification = visibleSeries(recordStatus);

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
        if (franchiseId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("franchise").get("id"), franchiseId));
        }
        if (seriesType != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("type"), seriesType));
        }
        if (parsedPublicationStatus != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("publicationStatus"), parsedPublicationStatus));
        }
        if (publisherId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("primaryPublisher").get("id"), publisherId));
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

        return PageResponse.from(seriesRepository.findAll(specification, pageRequest).map(CatalogSeriesResponse::from));
    }

    @Transactional(readOnly = true)
    public CatalogSeriesResponse get(Long id, AuthenticatedUser user) {
        CatalogSeries series = findSeries(id);
        if (!series.isPubliclyVisible() && !EditorialCatalogSupport.isEditorialAdmin(user)) {
            throw new CatalogSeriesNotFoundException(id);
        }
        return CatalogSeriesResponse.from(series);
    }

    @Transactional
    public CatalogSeriesResponse create(AuthenticatedUser user, CreateCatalogSeriesRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        String title = EditorialCatalogSupport.normalizeRequired(request.title());
        CatalogFranchise franchise = findFranchise(request.franchiseId());
        Publisher publisher = findPublisher(request.primaryPublisherId());
        ensureDependenciesArePublishable(request.recordStatus(), franchise, publisher);
        ensureUnique(title, request.type(), franchise, null);

        CatalogSeries series = CatalogSeries.create(
                franchise,
                publisher,
                title,
                EditorialCatalogSupport.normalizeNullable(request.originalTitle()),
                request.type(),
                request.publicationStatus(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                EditorialCatalogSupport.normalizeCountry(request.originCountry()),
                EditorialCatalogSupport.normalizeLanguage(request.originalLanguage()),
                request.startYear(),
                request.endYear(),
                request.recordStatus(),
                user.id()
        );
        return CatalogSeriesResponse.from(seriesRepository.save(series));
    }

    @Transactional
    public CatalogSeriesResponse update(Long id, AuthenticatedUser user, UpdateCatalogSeriesRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogSeries series = findSeries(id);
        String title = EditorialCatalogSupport.normalizeRequired(request.title());
        CatalogFranchise franchise = findFranchise(request.franchiseId());
        Publisher publisher = findPublisher(request.primaryPublisherId());
        ensureDependenciesArePublishable(request.recordStatus(), franchise, publisher);
        ensureUnique(title, request.type(), franchise, id);
        ensureCanChangeStatus(series, request.recordStatus());

        series.update(
                franchise,
                publisher,
                title,
                EditorialCatalogSupport.normalizeNullable(request.originalTitle()),
                request.type(),
                request.publicationStatus(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                EditorialCatalogSupport.normalizeCountry(request.originCountry()),
                EditorialCatalogSupport.normalizeLanguage(request.originalLanguage()),
                request.startYear(),
                request.endYear(),
                request.recordStatus(),
                user.id()
        );
        return CatalogSeriesResponse.from(series);
    }

    private CatalogSeries findSeries(Long id) {
        return seriesRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogSeriesNotFoundException(id));
    }

    private CatalogFranchise findFranchise(Long id) {
        if (id == null) {
            return null;
        }
        return franchiseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogFranchiseNotFoundException(id));
    }

    private Publisher findPublisher(Long id) {
        if (id == null) {
            return null;
        }
        return publisherRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
    }

    private void ensureDependenciesArePublishable(
            CatalogRecordStatus recordStatus,
            CatalogFranchise franchise,
            Publisher publisher
    ) {
        if (recordStatus != CatalogRecordStatus.ACTIVE) {
            return;
        }
        if (franchise != null && !franchise.isPubliclyVisible()) {
            throw new InvalidEditorialCatalogRequestException(
                    "An active catalog series requires an active franchise"
            );
        }
        if (publisher != null && !publisher.isPubliclyVisible()) {
            throw new InvalidEditorialCatalogRequestException(
                    "An active catalog series requires an active primary publisher"
            );
        }
    }

    private void ensureCanChangeStatus(CatalogSeries series, CatalogRecordStatus nextStatus) {
        if (series.getRecordStatus() == CatalogRecordStatus.ACTIVE
                && nextStatus != CatalogRecordStatus.ACTIVE
                && itemRepository.existsBySeries_IdAndRecordStatusAndDeletedAtIsNull(
                        series.getId(),
                        CatalogRecordStatus.ACTIVE
                )) {
            throw new InvalidEditorialCatalogRequestException(
                    "Cannot archive a catalog series referenced by an active catalog item"
            );
        }
    }

    private void ensureUnique(
            String title,
            CatalogSeriesType type,
            CatalogFranchise franchise,
            Long excludedId
    ) {
        boolean duplicate;
        if (franchise == null) {
            duplicate = excludedId == null
                    ? seriesRepository.existsByTitleIgnoreCaseAndTypeAndFranchiseIsNullAndDeletedAtIsNull(title, type)
                    : seriesRepository.existsByTitleIgnoreCaseAndTypeAndFranchiseIsNullAndDeletedAtIsNullAndIdNot(
                            title,
                            type,
                            excludedId
                    );
        } else {
            duplicate = excludedId == null
                    ? seriesRepository.existsByTitleIgnoreCaseAndTypeAndFranchise_IdAndDeletedAtIsNull(
                            title,
                            type,
                            franchise.getId()
                    )
                    : seriesRepository.existsByTitleIgnoreCaseAndTypeAndFranchise_IdAndDeletedAtIsNullAndIdNot(
                            title,
                            type,
                            franchise.getId(),
                            excludedId
                    );
        }
        if (duplicate) {
            throw new DuplicateEditorialCatalogException(
                    "catalog series",
                    "title, type and franchise combination already exists"
            );
        }
    }

    private Specification<CatalogSeries> visibleSeries(CatalogRecordStatus recordStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.isNull(root.get("deletedAt")),
                criteriaBuilder.equal(root.get("recordStatus"), recordStatus)
        );
    }

    private String normalizeLike(String value) {
        String normalized = EditorialCatalogSupport.normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
