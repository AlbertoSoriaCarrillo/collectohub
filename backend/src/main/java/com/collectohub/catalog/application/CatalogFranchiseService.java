package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogFranchise;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.dto.CatalogFranchiseResponse;
import com.collectohub.catalog.dto.CreateCatalogFranchiseRequest;
import com.collectohub.catalog.dto.UpdateCatalogFranchiseRequest;
import com.collectohub.catalog.infrastructure.CatalogFranchiseRepository;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class CatalogFranchiseService {

    private static final Set<String> SORT_FIELDS = Set.of("name", "slug", "recordStatus", "createdAt");

    private final CatalogFranchiseRepository franchiseRepository;
    private final CatalogSeriesRepository catalogSeriesRepository;

    public CatalogFranchiseService(
            CatalogFranchiseRepository franchiseRepository,
            CatalogSeriesRepository catalogSeriesRepository
    ) {
        this.franchiseRepository = franchiseRepository;
        this.catalogSeriesRepository = catalogSeriesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CatalogFranchiseResponse> search(
            AuthenticatedUser user,
            String q,
            String requestedStatus,
            int page,
            int size,
            String sort
    ) {
        CatalogRecordStatus recordStatus = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(page, size, sort, "name", SORT_FIELDS);
        Specification<CatalogFranchise> specification = visibleFranchises(recordStatus);

        String normalizedQuery = normalizeLike(q);
        if (normalizedQuery != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("name")),
                                    "%" + normalizedQuery + "%"
                            ),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("slug")),
                                    "%" + normalizedQuery + "%"
                            )
                    ));
        }

        return PageResponse.from(
                franchiseRepository.findAll(specification, pageRequest).map(CatalogFranchiseResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public CatalogFranchiseResponse get(Long id, AuthenticatedUser user) {
        CatalogFranchise franchise = findFranchise(id);
        if (!franchise.isPubliclyVisible() && !EditorialCatalogSupport.isEditorialAdmin(user)) {
            throw new CatalogFranchiseNotFoundException(id);
        }
        return CatalogFranchiseResponse.from(franchise);
    }

    @Transactional
    public CatalogFranchiseResponse create(AuthenticatedUser user, CreateCatalogFranchiseRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        String name = EditorialCatalogSupport.normalizeRequired(request.name());
        String slug = EditorialCatalogSupport.normalizeSlug(request.slug());
        ensureUnique(name, slug, null);

        CatalogFranchise franchise = CatalogFranchise.create(
                name,
                slug,
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.recordStatus(),
                user.id()
        );
        return CatalogFranchiseResponse.from(franchiseRepository.save(franchise));
    }

    @Transactional
    public CatalogFranchiseResponse update(
            Long id,
            AuthenticatedUser user,
            UpdateCatalogFranchiseRequest request
    ) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogFranchise franchise = findFranchise(id);
        String name = EditorialCatalogSupport.normalizeRequired(request.name());
        String slug = EditorialCatalogSupport.normalizeSlug(request.slug());
        ensureUnique(name, slug, id);
        ensureCanChangeStatus(franchise, request.recordStatus());

        franchise.update(
                name,
                slug,
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.recordStatus(),
                user.id()
        );
        return CatalogFranchiseResponse.from(franchise);
    }

    private CatalogFranchise findFranchise(Long id) {
        return franchiseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogFranchiseNotFoundException(id));
    }

    private void ensureUnique(String name, String slug, Long excludedId) {
        boolean duplicateSlug = excludedId == null
                ? franchiseRepository.existsBySlugAndDeletedAtIsNull(slug)
                : franchiseRepository.existsBySlugAndDeletedAtIsNullAndIdNot(slug, excludedId);
        if (duplicateSlug) {
            throw new DuplicateEditorialCatalogException("catalog franchise", "slug already exists");
        }

        boolean duplicateName = excludedId == null
                ? franchiseRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)
                : franchiseRepository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(name, excludedId);
        if (duplicateName) {
            throw new DuplicateEditorialCatalogException("catalog franchise", "name already exists");
        }
    }

    private void ensureCanChangeStatus(CatalogFranchise franchise, CatalogRecordStatus nextStatus) {
        if (franchise.getRecordStatus() == CatalogRecordStatus.ACTIVE
                && nextStatus != CatalogRecordStatus.ACTIVE
                && catalogSeriesRepository.existsByFranchise_IdAndRecordStatusAndDeletedAtIsNull(
                        franchise.getId(),
                        CatalogRecordStatus.ACTIVE
                )) {
            throw new InvalidEditorialCatalogRequestException(
                    "Cannot archive a franchise referenced by an active catalog series"
            );
        }
    }

    private Specification<CatalogFranchise> visibleFranchises(CatalogRecordStatus recordStatus) {
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
