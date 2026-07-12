package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.CreatorRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.*;

@Service
public class CreatorService {
    private static final Set<String> SORT_FIELDS = Set.of("name", "sortName", "country", "recordStatus", "createdAt");
    private final CreatorRepository repository;

    public CreatorService(CreatorRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public PageResponse<CreatorResponse> search(AuthenticatedUser user, String q, String requestedStatus,
                                                int page, int size, String sort) {
        CatalogRecordStatus status = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        PageRequest pageable = EditorialCatalogSupport.pageRequest(page, size, sort, "name", SORT_FIELDS);
        Specification<Creator> spec = (root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")), cb.equal(root.get("recordStatus"), status));
        String text = EditorialCatalogSupport.normalizeNullable(q);
        if (text != null) {
            String like = "%" + text.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), like));
        }
        return PageResponse.from(repository.findAll(spec, pageable).map(CreatorResponse::from));
    }

    @Transactional(readOnly = true)
    public CreatorResponse get(Long id, AuthenticatedUser user) {
        Creator creator = find(id);
        if (!creator.isPubliclyVisible() && !EditorialCatalogSupport.isEditorialAdmin(user)) throw new CreatorNotFoundException(id);
        return CreatorResponse.from(creator);
    }

    @Transactional
    public CreatorResponse create(AuthenticatedUser user, CreateCreatorRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        Values values = values(request.name(), request.slug(), request.sortName(), request.biography(),
                request.country(), request.birthYear(), request.deathYear(),
                request.recordStatus() == null ? CatalogRecordStatus.DRAFT : request.recordStatus());
        ensureUnique(values.name(), values.slug(), null);
        return CreatorResponse.from(repository.save(Creator.create(values.name(), values.slug(), values.sortName(),
                values.biography(), values.country(), values.birthYear(), values.deathYear(), values.status(), user.id())));
    }

    @Transactional
    public CreatorResponse update(Long id, AuthenticatedUser user, UpdateCreatorRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        Creator creator = find(id);
        Values values = values(request.name(), request.slug(), request.sortName(), request.biography(),
                request.country(), request.birthYear(), request.deathYear(),
                request.recordStatus() == null ? creator.getRecordStatus() : request.recordStatus());
        ensureUnique(values.name(), values.slug(), id);
        creator.update(values.name(), values.slug(), values.sortName(), values.biography(), values.country(),
                values.birthYear(), values.deathYear(), values.status(), user.id());
        return CreatorResponse.from(creator);
    }

    @Transactional
    public void delete(Long id, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        find(id).softDelete(user.id());
    }

    Creator find(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new CreatorNotFoundException(id));
    }

    private Values values(String name, String slug, String sortName, String biography, String country,
                          Integer birthYear, Integer deathYear, CatalogRecordStatus status) {
        String normalizedName = EditorialCatalogSupport.normalizeRequired(name);
        String normalizedSlug = EditorialCatalogSupport.normalizeNullable(slug);
        normalizedSlug = slugify(normalizedSlug == null ? normalizedName : normalizedSlug);
        if (normalizedSlug.isBlank()) throw new InvalidEditorialCatalogRequestException("Creator slug cannot be empty");
        if (deathYear != null && birthYear != null && deathYear < birthYear) {
            throw new InvalidEditorialCatalogRequestException("deathYear must be greater than or equal to birthYear");
        }
        return new Values(normalizedName, normalizedSlug, EditorialCatalogSupport.normalizeNullable(sortName),
                EditorialCatalogSupport.normalizeNullable(biography), EditorialCatalogSupport.normalizeCountry(country),
                birthYear, deathYear, status);
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private void ensureUnique(String name, String slug, Long excludedId) {
        boolean duplicateName = excludedId == null
                ? repository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)
                : repository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(name, excludedId);
        if (duplicateName) throw new DuplicateEditorialCatalogException("creator", "name already exists");
        boolean duplicate = excludedId == null ? repository.existsBySlugAndDeletedAtIsNull(slug)
                : repository.existsBySlugAndDeletedAtIsNullAndIdNot(slug, excludedId);
        if (duplicate) throw new DuplicateEditorialCatalogException("creator", "slug already exists");
    }

    private record Values(String name, String slug, String sortName, String biography, String country,
                          Integer birthYear, Integer deathYear, CatalogRecordStatus status) {}
}
