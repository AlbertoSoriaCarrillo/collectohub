package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CreatePublisherRequest;
import com.collectohub.catalog.dto.PublisherResponse;
import com.collectohub.catalog.dto.UpdatePublisherRequest;
import com.collectohub.catalog.infrastructure.CatalogSeriesRepository;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.PublisherRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class PublisherService {

    private static final Set<String> SORT_FIELDS = Set.of("name", "country", "recordStatus", "createdAt");

    private final PublisherRepository publisherRepository;
    private final CatalogSeriesRepository catalogSeriesRepository;
    private final CatalogItemEditionRepository editionRepository;

    public PublisherService(
            PublisherRepository publisherRepository,
            CatalogSeriesRepository catalogSeriesRepository,
            CatalogItemEditionRepository editionRepository
    ) {
        this.publisherRepository = publisherRepository;
        this.catalogSeriesRepository = catalogSeriesRepository;
        this.editionRepository = editionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PublisherResponse> search(
            AuthenticatedUser user,
            String q,
            String requestedStatus,
            int page,
            int size,
            String sort
    ) {
        CatalogRecordStatus recordStatus = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(page, size, sort, "name", SORT_FIELDS);
        Specification<Publisher> specification = visiblePublishers(recordStatus);

        String normalizedQuery = normalizeLike(q);
        if (normalizedQuery != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + normalizedQuery + "%"));
        }

        return PageResponse.from(publisherRepository.findAll(specification, pageRequest).map(PublisherResponse::from));
    }

    @Transactional(readOnly = true)
    public PublisherResponse get(Long id, AuthenticatedUser user) {
        Publisher publisher = findPublisher(id);
        if (!publisher.isPubliclyVisible() && !EditorialCatalogSupport.isAdmin(user)) {
            throw new PublisherNotFoundException(id);
        }
        return PublisherResponse.from(publisher);
    }

    @Transactional
    public PublisherResponse create(AuthenticatedUser user, CreatePublisherRequest request) {
        EditorialCatalogSupport.ensureAdmin(user);
        String name = EditorialCatalogSupport.normalizeRequired(request.name());
        ensureNameAvailable(name, null);

        Publisher publisher = Publisher.create(
                name,
                EditorialCatalogSupport.normalizeCountry(request.country()),
                request.recordStatus(),
                user.id()
        );
        return PublisherResponse.from(publisherRepository.save(publisher));
    }

    @Transactional
    public PublisherResponse update(Long id, AuthenticatedUser user, UpdatePublisherRequest request) {
        EditorialCatalogSupport.ensureAdmin(user);
        Publisher publisher = findPublisher(id);
        String name = EditorialCatalogSupport.normalizeRequired(request.name());
        ensureNameAvailable(name, id);
        ensureCanChangeStatus(publisher, request.recordStatus());

        publisher.update(
                name,
                EditorialCatalogSupport.normalizeCountry(request.country()),
                request.recordStatus(),
                user.id()
        );
        return PublisherResponse.from(publisher);
    }

    private Publisher findPublisher(Long id) {
        return publisherRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
    }

    private void ensureNameAvailable(String name, Long excludedId) {
        boolean duplicate = excludedId == null
                ? publisherRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)
                : publisherRepository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(name, excludedId);
        if (duplicate) {
            throw new DuplicateEditorialCatalogException("publisher", "name already exists");
        }
    }

    private void ensureCanChangeStatus(Publisher publisher, CatalogRecordStatus nextStatus) {
        if (publisher.getRecordStatus() != CatalogRecordStatus.ACTIVE
                || nextStatus == CatalogRecordStatus.ACTIVE) {
            return;
        }
        boolean usedBySeries = catalogSeriesRepository.existsByPrimaryPublisher_IdAndRecordStatusAndDeletedAtIsNull(
                publisher.getId(),
                CatalogRecordStatus.ACTIVE
        );
        boolean usedByEdition = editionRepository.existsByPublisher_IdAndRecordStatusAndDeletedAtIsNull(
                publisher.getId(),
                CatalogRecordStatus.ACTIVE
        );
        if (usedBySeries || usedByEdition) {
            throw new InvalidEditorialCatalogRequestException(
                    "Cannot archive a publisher referenced by active catalog content"
            );
        }
    }

    private Specification<Publisher> visiblePublishers(CatalogRecordStatus recordStatus) {
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
