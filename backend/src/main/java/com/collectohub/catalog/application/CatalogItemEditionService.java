package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.CatalogItemEditionFormat;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import com.collectohub.catalog.domain.Publisher;
import com.collectohub.catalog.dto.CatalogItemEditionResponse;
import com.collectohub.catalog.dto.CreateCatalogItemEditionRequest;
import com.collectohub.catalog.dto.UpdateCatalogItemEditionRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.PublisherRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class CatalogItemEditionService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "publicationYear",
            "editionName",
            "format",
            "language",
            "country",
            "recordStatus",
            "createdAt"
    );

    private final CatalogItemEditionRepository editionRepository;
    private final CatalogItemRepository itemRepository;
    private final PublisherRepository publisherRepository;

    public CatalogItemEditionService(
            CatalogItemEditionRepository editionRepository,
            CatalogItemRepository itemRepository,
            PublisherRepository publisherRepository
    ) {
        this.editionRepository = editionRepository;
        this.itemRepository = itemRepository;
        this.publisherRepository = publisherRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CatalogItemEditionResponse> search(
            Long itemId,
            AuthenticatedUser user,
            Long publisherId,
            String isbn,
            String ean,
            String format,
            String language,
            String country,
            Integer publicationYear,
            String requestedStatus,
            int page,
            int size,
            String sort
    ) {
        findItemForRead(itemId, user);
        CatalogRecordStatus recordStatus = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        CatalogItemEditionFormat parsedFormat = EditorialCatalogSupport.parseOptionalEnum(
                format, CatalogItemEditionFormat.class, "format");
        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(
                page, size, sort, "publicationYear", SORT_FIELDS);
        Specification<CatalogItemEdition> specification = visibleEditions(itemId, recordStatus);

        if (publisherId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
        }
        String normalizedIsbn = normalizeIdentifier(isbn);
        if (normalizedIsbn != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("isbn"), normalizedIsbn));
        }
        String normalizedEan = normalizeIdentifier(ean);
        if (normalizedEan != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("ean"), normalizedEan));
        }
        if (parsedFormat != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("format"), parsedFormat));
        }
        String normalizedLanguage = EditorialCatalogSupport.normalizeLanguage(language);
        if (normalizedLanguage != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("language")), normalizedLanguage));
        }
        String normalizedCountry = EditorialCatalogSupport.normalizeCountry(country);
        if (normalizedCountry != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get("country")), normalizedCountry));
        }
        if (publicationYear != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("publicationYear"), publicationYear));
        }

        return PageResponse.from(
                editionRepository.findAll(specification, pageRequest).map(CatalogItemEditionResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public CatalogItemEditionResponse get(Long id, AuthenticatedUser user) {
        CatalogItemEdition edition = findEdition(id);
        if (!edition.isPubliclyVisible() && !EditorialCatalogSupport.isAdmin(user)) {
            throw new CatalogItemEditionNotFoundException(id);
        }
        return CatalogItemEditionResponse.from(edition);
    }

    @Transactional
    public CatalogItemEditionResponse create(
            Long itemId,
            AuthenticatedUser user,
            CreateCatalogItemEditionRequest request
    ) {
        EditorialCatalogSupport.ensureAdmin(user);
        CatalogItem item = findItem(itemId);
        Publisher publisher = findPublisher(request.publisherId());
        String isbn = normalizeIdentifier(request.isbn());
        String ean = normalizeIdentifier(request.ean());
        ensureDependenciesArePublishable(request.recordStatus(), item, publisher);
        ensureIdentifiersAvailable(isbn, ean, null);

        CatalogItemEdition edition = CatalogItemEdition.create(
                item,
                publisher,
                isbn,
                ean,
                request.format(),
                EditorialCatalogSupport.normalizeNullable(request.editionName()),
                request.publicationDate(),
                request.publicationYear(),
                EditorialCatalogSupport.normalizeLanguage(request.language()),
                EditorialCatalogSupport.normalizeCountry(request.country()),
                request.pageCount(),
                EditorialCatalogSupport.normalizeNullable(request.coverImageUrl()),
                request.recordStatus(),
                user.id()
        );
        return CatalogItemEditionResponse.from(editionRepository.save(edition));
    }

    @Transactional
    public CatalogItemEditionResponse update(
            Long id,
            AuthenticatedUser user,
            UpdateCatalogItemEditionRequest request
    ) {
        EditorialCatalogSupport.ensureAdmin(user);
        CatalogItemEdition edition = findEdition(id);
        CatalogItem item = findItem(request.catalogItemId());
        Publisher publisher = findPublisher(request.publisherId());
        String isbn = normalizeIdentifier(request.isbn());
        String ean = normalizeIdentifier(request.ean());
        ensureDependenciesArePublishable(request.recordStatus(), item, publisher);
        ensureIdentifiersAvailable(isbn, ean, id);

        edition.update(
                item,
                publisher,
                isbn,
                ean,
                request.format(),
                EditorialCatalogSupport.normalizeNullable(request.editionName()),
                request.publicationDate(),
                request.publicationYear(),
                EditorialCatalogSupport.normalizeLanguage(request.language()),
                EditorialCatalogSupport.normalizeCountry(request.country()),
                request.pageCount(),
                EditorialCatalogSupport.normalizeNullable(request.coverImageUrl()),
                request.recordStatus(),
                user.id()
        );
        return CatalogItemEditionResponse.from(edition);
    }

    private CatalogItemEdition findEdition(Long id) {
        return editionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogItemEditionNotFoundException(id));
    }

    private CatalogItem findItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    private CatalogItem findItemForRead(Long id, AuthenticatedUser user) {
        CatalogItem item = findItem(id);
        if (!item.isPubliclyVisible() && !EditorialCatalogSupport.isAdmin(user)) {
            throw new CatalogItemNotFoundException(id);
        }
        return item;
    }

    private Publisher findPublisher(Long id) {
        if (id == null) {
            return null;
        }
        return publisherRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new PublisherNotFoundException(id));
    }

    private void ensureDependenciesArePublishable(
            CatalogRecordStatus status,
            CatalogItem item,
            Publisher publisher
    ) {
        if (status != CatalogRecordStatus.ACTIVE) {
            return;
        }
        if (!item.isPubliclyVisible()) {
            throw new InvalidEditorialCatalogRequestException(
                    "An active catalog item edition requires an active catalog item and series"
            );
        }
        if (publisher != null && !publisher.isPubliclyVisible()) {
            throw new InvalidEditorialCatalogRequestException(
                    "An active catalog item edition requires an active publisher"
            );
        }
    }

    private void ensureIdentifiersAvailable(String isbn, String ean, Long excludedId) {
        if (isbn != null) {
            boolean duplicate = excludedId == null
                    ? editionRepository.existsByIsbnAndDeletedAtIsNull(isbn)
                    : editionRepository.existsByIsbnAndDeletedAtIsNullAndIdNot(isbn, excludedId);
            if (duplicate) {
                throw new DuplicateEditorialCatalogException("catalog item edition", "ISBN already exists");
            }
        }
        if (ean != null) {
            boolean duplicate = excludedId == null
                    ? editionRepository.existsByEanAndDeletedAtIsNull(ean)
                    : editionRepository.existsByEanAndDeletedAtIsNullAndIdNot(ean, excludedId);
            if (duplicate) {
                throw new DuplicateEditorialCatalogException("catalog item edition", "EAN already exists");
            }
        }
    }

    private Specification<CatalogItemEdition> visibleEditions(Long itemId, CatalogRecordStatus recordStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.isNull(root.get("deletedAt")),
                criteriaBuilder.equal(root.get("catalogItem").get("id"), itemId),
                criteriaBuilder.equal(root.get("recordStatus"), recordStatus)
        );
    }

    private String normalizeIdentifier(String value) {
        String normalized = EditorialCatalogSupport.normalizeNullable(value);
        return normalized == null
                ? null
                : normalized.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }
}
