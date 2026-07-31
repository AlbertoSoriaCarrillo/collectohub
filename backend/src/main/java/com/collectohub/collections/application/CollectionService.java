package com.collectohub.collections.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.catalog.application.ProductCategoryNotFoundException;
import com.collectohub.catalog.application.CatalogItemNotFoundException;
import com.collectohub.catalog.application.CatalogItemEditionNotFoundException;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemReferenceKind;
import com.collectohub.collections.domain.CollectionItemSort;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionEditorialReferenceSource;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.collections.dto.CollectionItemResponse;
import com.collectohub.collections.dto.CollectionResponse;
import com.collectohub.collections.dto.CreateCollectionItemRequest;
import com.collectohub.collections.dto.CreateCollectionRequest;
import com.collectohub.collections.dto.UpdateCollectionItemRequest;
import com.collectohub.collections.dto.UpdateCollectionRequest;
import com.collectohub.collections.dto.LinkManualCollectionItemRequest;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.collections.infrastructure.CollectionRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.users.domain.User;
import com.collectohub.users.infrastructure.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final MasterProductRepository masterProductRepository;
    private final CatalogItemRepository catalogItemRepository;
    private final CatalogItemEditionRepository catalogItemEditionRepository;
    private final MasterProductCatalogLinkRepository masterProductCatalogLinkRepository;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionItemRepository collectionItemRepository,
            UserRepository userRepository,
            ProductCategoryRepository productCategoryRepository,
            MasterProductRepository masterProductRepository,
            CatalogItemRepository catalogItemRepository,
            CatalogItemEditionRepository catalogItemEditionRepository,
            MasterProductCatalogLinkRepository masterProductCatalogLinkRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.collectionItemRepository = collectionItemRepository;
        this.userRepository = userRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.masterProductRepository = masterProductRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.catalogItemEditionRepository = catalogItemEditionRepository;
        this.masterProductCatalogLinkRepository = masterProductCatalogLinkRepository;
    }

    @Transactional
    public CollectionResponse createCollection(AuthenticatedUser authenticatedUser, CreateCollectionRequest request) {
        User owner = currentUser(authenticatedUser);
        ProductCategory category = findCategoryIfProvided(request.categoryCode());
        Collection collection = Collection.create(
                owner,
                normalizeRequired(request.name()),
                normalizeNullable(request.description()),
                Objects.requireNonNullElse(request.visibility(), CollectionVisibility.PRIVATE),
                category
        );
        return CollectionResponse.from(collectionRepository.save(collection));
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> myCollections(
            AuthenticatedUser authenticatedUser,
            CollectionVisibility visibility,
            String categoryCode
    ) {
        Long userId = requireAuthenticated(authenticatedUser).id();
        ProductCategory category = findCategoryIfProvided(categoryCode);
        Specification<Collection> specification = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("user").get("id"), userId),
                criteriaBuilder.isNull(root.get("deletedAt"))
        );
        if (visibility != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("visibility"), visibility));
        }
        if (category != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("id"), category.getId()));
        }
        return collectionRepository.findAll(specification, Sort.by("id").ascending()).stream()
                .map(CollectionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollection(AuthenticatedUser authenticatedUser, Long collectionId) {
        Collection collection = findActiveCollection(collectionId);
        ensureCanRead(authenticatedUser, collection);
        return CollectionResponse.from(collection, itemResponses(collectionId, isOwner(authenticatedUser, collection)));
    }

    @Transactional
    public CollectionResponse updateCollection(
            AuthenticatedUser authenticatedUser,
            Long collectionId,
            UpdateCollectionRequest request
    ) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        ProductCategory category = request.categoryCode() == null
                ? collection.getCategory()
                : findCategoryIfProvided(request.categoryCode());
        collection.update(
                normalizeRequiredOrExisting(request.name(), collection.getName()),
                normalizeNullableOrExisting(request.description(), collection.getDescription()),
                Objects.requireNonNullElse(request.visibility(), collection.getVisibility()),
                category,
                authenticatedUser.id()
        );
        return CollectionResponse.from(collection, itemResponses(collectionId, true));
    }

    @Transactional
    public void deleteCollection(AuthenticatedUser authenticatedUser, Long collectionId) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        collection.softDelete(authenticatedUser.id());
    }

    @Transactional
    public CollectionItemResponse addItem(
            AuthenticatedUser authenticatedUser,
            Long collectionId,
            CreateCollectionItemRequest request
    ) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        rejectCalculatedMissing(request.collectionStatus());
        if (hasManualInput(request)) {
            validateNoMixedManualReference(
                    request.masterProductId(), request.catalogItemId(), request.catalogItemEditionId()
            );
            CollectionItem item = CollectionItem.createManual(
                    collection,
                    normalizeRequiredManualTitle(request.manualTitle()),
                    normalizeNullable(request.manualDescription()),
                    normalizeNullable(request.manualType()),
                    request.collectionStatus(),
                    request.physicalCondition(),
                    normalizeNullable(request.unitNumber()),
                    request.totalLimitedUnits(),
                    normalizeNullable(request.notes()),
                    request.acquiredAt(),
                    authenticatedUser.id()
            );
            return CollectionItemResponse.from(collectionItemRepository.save(item));
        }
        ResolvedReference reference = resolveReference(
                request.masterProductId(),
                request.catalogItemId(),
                request.catalogItemEditionId()
        );
        CollectionItem item = CollectionItem.create(
                collection,
                reference.masterProduct(),
                reference.catalogItem(),
                reference.catalogItemEdition(),
                reference.source(),
                request.collectionStatus(),
                request.physicalCondition(),
                normalizeNullable(request.unitNumber()),
                request.totalLimitedUnits(),
                normalizeNullable(request.notes()),
                request.acquiredAt(),
                authenticatedUser.id()
        );
        return CollectionItemResponse.from(collectionItemRepository.save(item));
    }

    @Transactional
    public CollectionItemResponse updateItem(
            AuthenticatedUser authenticatedUser,
            Long collectionId,
            Long itemId,
            UpdateCollectionItemRequest request
    ) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        CollectionItem item = collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(itemId, collectionId)
                .orElseThrow(() -> new CollectionItemNotFoundException(itemId));
        if (request.collectionStatus() == CollectionItemStatus.MISSING) rejectCalculatedMissing(request.collectionStatus());
        if (item.isManual()) {
            updateManualItem(item, request, authenticatedUser.id());
        } else {
            rejectManualInputForReferencedItem(request);
            if (hasExplicitReference(request)) {
                ResolvedReference reference = resolveUpdatedReference(item, request);
                item.updateReference(
                        reference.masterProduct(),
                        reference.catalogItem(),
                        reference.catalogItemEdition(),
                        reference.source(),
                        authenticatedUser.id()
                );
            }
        }
        item.update(
                Objects.requireNonNullElse(request.collectionStatus(), item.getCollectionStatus()),
                nullablePhysicalConditionOrExisting(request.physicalCondition(), item.getPhysicalCondition()),
                normalizeNullableOrExisting(request.unitNumber(), item.getUnitNumber()),
                request.totalLimitedUnits() == null ? item.getTotalLimitedUnits() : request.totalLimitedUnits(),
                normalizeNullableOrExisting(request.notes(), item.getNotes()),
                nullableDateOrExisting(request.acquiredAt(), item.getAcquiredAt()),
                authenticatedUser.id()
        );
        return CollectionItemResponse.from(item);
    }

    @Transactional
    public CollectionItemResponse linkManualItemToCatalog(
            AuthenticatedUser authenticatedUser,
            Long collectionId,
            Long itemId,
            LinkManualCollectionItemRequest request
    ) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        CollectionItem item = collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(itemId, collectionId)
                .orElseThrow(() -> new CollectionItemNotFoundException(itemId));
        if (isExactCatalogLinkRepeat(item, request)) {
            return CollectionItemResponse.from(item);
        }
        if (!item.isManual()) {
            throw new ConflictingCollectionItemReferenceException(
                    "Only a manual collection item can be linked to the catalog"
            );
        }
        CatalogItem catalogItem = findActiveCatalogItem(request.catalogItemId());
        CatalogItemEdition edition = request.catalogItemEditionId() == null
                ? null
                : findActiveCatalogItemEdition(request.catalogItemEditionId());
        validateEditionBelongsToItem(edition, catalogItem);
        item.linkToCatalog(catalogItem, edition, authenticatedUser.id());
        return CollectionItemResponse.from(item);
    }

    private boolean isExactCatalogLinkRepeat(CollectionItem item, LinkManualCollectionItemRequest request) {
        return item.getMasterProduct() == null
                && item.getCatalogItem() != null
                && item.getCatalogItem().getId().equals(request.catalogItemId())
                && sameId(item.getCatalogItemEdition(), request.catalogItemEditionId())
                && item.getEditorialReferenceSource() == CollectionEditorialReferenceSource.MANUAL_EDITORIAL
                && item.getManualTitle() == null
                && item.getManualDescription() == null
                && item.getManualType() == null;
    }

    private boolean sameId(CatalogItemEdition edition, Long id) {
        return edition == null ? id == null : edition.getId().equals(id);
    }

    private void updateManualItem(CollectionItem item, UpdateCollectionItemRequest request, Long updatedBy) {
        if (hasExplicitReference(request)) {
            throw new InvalidCollectionItemReferenceException(
                    "Linking a manual collection item to the catalog requires the dedicated operation"
            );
        }
        if (!hasManualInput(request)) {
            return;
        }
        String title = request.manualTitle() == null
                ? item.getManualTitle()
                : normalizeRequiredManualTitle(request.manualTitle());
        String description = request.manualDescription() == null
                ? item.getManualDescription()
                : normalizeNullable(request.manualDescription());
        String type = request.manualType() == null
                ? item.getManualType()
                : normalizeNullable(request.manualType());
        item.updateManualMetadata(title, description, type, updatedBy);
    }

    private void rejectManualInputForReferencedItem(UpdateCollectionItemRequest request) {
        if (hasManualInput(request)) {
            throw new InvalidCollectionItemReferenceException(
                    "Manual metadata is only valid for manual collection items"
            );
        }
    }

    private boolean hasManualInput(CreateCollectionItemRequest request) {
        return request.manualTitle() != null
                || request.manualDescription() != null
                || request.manualType() != null;
    }

    private void rejectCalculatedMissing(CollectionItemStatus status) {
        if (status == CollectionItemStatus.MISSING) {
            throw new InvalidCollectionItemStatusException("MISSING is calculated and cannot be persisted by collection item write operations");
        }
    }

    private boolean hasManualInput(UpdateCollectionItemRequest request) {
        return request.manualTitle() != null
                || request.manualDescription() != null
                || request.manualType() != null;
    }

    private void validateNoMixedManualReference(Long masterProductId, Long catalogItemId, Long catalogItemEditionId) {
        if (masterProductId != null || catalogItemId != null || catalogItemEditionId != null) {
            throw new InvalidCollectionItemReferenceException(
                    "Manual collection items cannot include catalog or legacy references"
            );
        }
    }

    private String normalizeRequiredManualTitle(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new InvalidCollectionItemReferenceException("Manual title is required");
        }
        return normalized;
    }

    @Transactional
    public void deleteItem(AuthenticatedUser authenticatedUser, Long collectionId, Long itemId) {
        Collection collection = findActiveCollection(collectionId);
        ensureOwner(authenticatedUser, collection);
        CollectionItem item = collectionItemRepository.findByIdAndCollection_IdAndDeletedAtIsNull(itemId, collectionId)
                .orElseThrow(() -> new CollectionItemNotFoundException(itemId));
        item.softDelete(authenticatedUser.id());
    }

    @Transactional(readOnly = true)
    public List<CollectionItemResponse> listItems(AuthenticatedUser authenticatedUser, Long collectionId) {
        return listItems(authenticatedUser, collectionId, null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<CollectionItemResponse> listItems(
            AuthenticatedUser authenticatedUser,
            Long collectionId,
            String query,
            List<String> statuses,
            List<String> referenceKinds,
            Long seriesId,
            String sort
    ) {
        Collection collection = findActiveCollection(collectionId);
        ensureCanRead(authenticatedUser, collection);
        String normalizedQuery = normalizeListingQuery(query);
        Set<CollectionItemStatus> statusFilter = parseEnumFilter(statuses, CollectionItemStatus.class, "status");
        Set<CollectionItemReferenceKind> referenceKindFilter = parseEnumFilter(
                referenceKinds,
                CollectionItemReferenceKind.class,
                "referenceKind"
        );
        Long normalizedSeriesId = normalizeSeriesId(seriesId);
        CollectionItemSort normalizedSort = parseSort(sort);
        boolean includePrivateFields = isOwner(authenticatedUser, collection);

        return collectionItemRepository.findDetailItemsByCollectionId(collectionId).stream()
                .map(item -> new ListingItem(item, CollectionItemResponse.from(item, includePrivateFields)))
                .filter(item -> statusFilter.isEmpty()
                        || statusFilter.contains(item.item().getCollectionStatus()))
                .filter(item -> referenceKindFilter.isEmpty()
                        || referenceKindFilter.contains(
                                CollectionItemReferenceKind.valueOf(item.response().referenceKind())
                        ))
                .filter(item -> normalizedSeriesId == null
                        || normalizedSeriesId.equals(item.response().catalogSeriesId()))
                .filter(item -> normalizedQuery == null || matchesQuery(item.response(), normalizedQuery))
                .sorted(listingComparator(normalizedSort))
                .map(ListingItem::response)
                .toList();
    }

    private User currentUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(requireAuthenticated(authenticatedUser).id())
                .filter(User::isActive)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is not available"));
    }

    private AuthenticatedUser requireAuthenticated(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authenticatedUser;
    }

    private Collection findActiveCollection(Long collectionId) {
        return collectionRepository.findByIdAndDeletedAtIsNull(collectionId)
                .filter(Collection::isActive)
                .orElseThrow(() -> new CollectionNotFoundException(collectionId));
    }

    private ProductCategory findCategoryIfProvided(String categoryCode) {
        String normalized = normalizeCode(categoryCode);
        if (normalized == null) {
            return null;
        }
        return productCategoryRepository.findByCodeAndDeletedAtIsNull(normalized)
                .orElseThrow(() -> new ProductCategoryNotFoundException(normalized));
    }

    private MasterProduct findActiveMasterProduct(Long masterProductId) {
        return masterProductRepository.findByIdAndDeletedAtIsNull(masterProductId)
                .filter(MasterProduct::isActive)
                .orElseThrow(() -> new MasterProductNotFoundException(masterProductId));
    }

    private ResolvedReference resolveReference(Long masterProductId, Long catalogItemId, Long editionId) {
        if (masterProductId == null && catalogItemId == null) {
            throw new InvalidCollectionItemReferenceException(
                    "A master product or editorial catalog item is required"
            );
        }
        if (editionId != null && catalogItemId == null) {
            throw new InvalidCollectionItemReferenceException(
                    "An editorial edition requires an editorial catalog item"
            );
        }

        MasterProduct masterProduct = masterProductId == null ? null : findActiveMasterProduct(masterProductId);
        CatalogItem catalogItem = catalogItemId == null ? null : findActiveCatalogItem(catalogItemId);
        CatalogItemEdition edition = editionId == null ? null : findActiveCatalogItemEdition(editionId);
        validateEditionBelongsToItem(edition, catalogItem);

        if (catalogItem != null) {
            validateVerifiedBridgeDoesNotConflict(masterProduct, catalogItem, edition);
            return new ResolvedReference(
                    masterProduct,
                    catalogItem,
                    edition,
                    CollectionEditorialReferenceSource.MANUAL_EDITORIAL
            );
        }

        return resolveEditorialReferenceFromVerifiedBridge(masterProduct);
    }

    private CatalogItem findActiveCatalogItem(Long catalogItemId) {
        return catalogItemRepository.findByIdAndDeletedAtIsNull(catalogItemId)
                .filter(CatalogItem::isPubliclyVisible)
                .orElseThrow(() -> new CatalogItemNotFoundException(catalogItemId));
    }

    private CatalogItemEdition findActiveCatalogItemEdition(Long editionId) {
        return catalogItemEditionRepository.findByIdAndDeletedAtIsNull(editionId)
                .filter(CatalogItemEdition::isPubliclyVisible)
                .orElseThrow(() -> new CatalogItemEditionNotFoundException(editionId));
    }

    private void validateEditionBelongsToItem(CatalogItemEdition edition, CatalogItem catalogItem) {
        if (edition != null && !edition.getCatalogItem().getId().equals(catalogItem.getId())) {
            throw new InvalidCollectionItemReferenceException(
                    "The editorial edition does not belong to the selected catalog item"
            );
        }
    }

    private ResolvedReference resolveEditorialReferenceFromVerifiedBridge(MasterProduct masterProduct) {
        return verifiedBridge(masterProduct.getId())
                .filter(link -> link.getCatalogItem().isPubliclyVisible())
                .filter(link -> link.getCatalogItemEdition() == null
                        || link.getCatalogItemEdition().isPubliclyVisible())
                .map(link -> new ResolvedReference(
                        masterProduct,
                        link.getCatalogItem(),
                        link.getCatalogItemEdition(),
                        CollectionEditorialReferenceSource.VERIFIED_BRIDGE
                ))
                .orElseGet(() -> new ResolvedReference(
                        masterProduct,
                        null,
                        null,
                        CollectionEditorialReferenceSource.LEGACY
                ));
    }

    private void validateVerifiedBridgeDoesNotConflict(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition edition
    ) {
        if (masterProduct == null) {
            return;
        }
        verifiedBridge(masterProduct.getId()).ifPresent(link -> {
            boolean itemConflict = !link.getCatalogItem().getId().equals(catalogItem.getId());
            boolean editionConflict = link.getCatalogItemEdition() != null
                    && edition != null
                    && !link.getCatalogItemEdition().getId().equals(edition.getId());
            if (itemConflict || editionConflict) {
                throw new ConflictingCollectionItemReferenceException(
                        "The editorial reference conflicts with the verified master product bridge"
                );
            }
        });
    }

    private java.util.Optional<MasterProductCatalogLink> verifiedBridge(Long masterProductId) {
        return masterProductCatalogLinkRepository
                .findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                        masterProductId,
                        MasterProductCatalogLinkStatus.VERIFIED
                );
    }

    private boolean hasExplicitReference(UpdateCollectionItemRequest request) {
        return request.masterProductId() != null
                || request.catalogItemId() != null
                || request.catalogItemEditionId() != null;
    }

    private ResolvedReference resolveUpdatedReference(
            CollectionItem item,
            UpdateCollectionItemRequest request
    ) {
        if (request.catalogItemId() != null) {
            return resolveReference(
                    request.masterProductId() == null
                            ? idOf(item.getMasterProduct())
                            : request.masterProductId(),
                    request.catalogItemId(),
                    request.catalogItemEditionId()
            );
        }
        if (request.masterProductId() != null) {
            return resolveReference(request.masterProductId(), null, null);
        }
        return resolveReference(
                idOf(item.getMasterProduct()),
                idOf(item.getCatalogItem()),
                request.catalogItemEditionId()
        );
    }

    private Long idOf(MasterProduct value) { return value == null ? null : value.getId(); }
    private Long idOf(CatalogItem value) { return value == null ? null : value.getId(); }
    private Long idOf(CatalogItemEdition value) { return value == null ? null : value.getId(); }

    private void ensureOwner(AuthenticatedUser authenticatedUser, Collection collection) {
        Long userId = requireAuthenticated(authenticatedUser).id();
        if (!collection.isOwnedBy(userId)) {
            throw new AccessDeniedException("User cannot manage this collection");
        }
    }

    private void ensureCanRead(AuthenticatedUser authenticatedUser, Collection collection) {
        if (collection.isPublic()) {
            return;
        }
        if (authenticatedUser != null && collection.isOwnedBy(authenticatedUser.id())) {
            return;
        }
        throw new CollectionNotFoundException(collection.getId());
    }

    private boolean isOwner(AuthenticatedUser authenticatedUser, Collection collection) {
        return authenticatedUser != null && collection.isOwnedBy(authenticatedUser.id());
    }

    private List<CollectionItemResponse> itemResponses(Long collectionId, boolean includePrivateFields) {
        return collectionItemRepository.findByCollection_IdAndDeletedAtIsNullOrderByIdAsc(collectionId).stream()
                .map(item -> CollectionItemResponse.from(item, includePrivateFields))
                .toList();
    }

    private String normalizeListingQuery(String value) {
        String normalized = normalizeNullable(value);
        if (normalized != null && normalized.length() > 100) {
            throw new InvalidCollectionItemFilterException("q must contain at most 100 characters");
        }
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private Long normalizeSeriesId(Long seriesId) {
        if (seriesId != null && seriesId <= 0) {
            throw new InvalidCollectionItemFilterException("seriesId must be greater than 0");
        }
        return seriesId;
    }

    private CollectionItemSort parseSort(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return CollectionItemSort.CATALOG_ORDER;
        }
        try {
            return CollectionItemSort.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCollectionItemFilterException("Unsupported sort: " + value);
        }
    }

    private <E extends Enum<E>> Set<E> parseEnumFilter(
            List<String> values,
            Class<E> enumType,
            String filterName
    ) {
        EnumSet<E> result = EnumSet.noneOf(enumType);
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String candidate : value.split(",")) {
                String normalized = normalizeCode(candidate);
                if (normalized == null) {
                    continue;
                }
                try {
                    result.add(Enum.valueOf(enumType, normalized));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidCollectionItemFilterException(
                            "Unsupported " + filterName + ": " + candidate
                    );
                }
            }
        }
        return result;
    }

    private boolean matchesQuery(CollectionItemResponse response, String query) {
        return Stream.of(
                        response.catalogItemTitle(),
                        response.catalogSeriesTitle(),
                        response.catalogItemEditionName(),
                        response.catalogItemEditionFormat(),
                        response.catalogItemEditionIsbn(),
                        response.catalogItemEditionEan(),
                        response.catalogPublisherName(),
                        response.catalogFranchiseName(),
                        response.masterProductName(),
                        response.masterProductCategoryCode(),
                        response.masterProductFranchise(),
                        response.masterProductCollectionName(),
                        response.masterProductVolumeNumber(),
                        response.manualTitle(),
                        response.manualDescription(),
                        response.manualType()
                )
                .filter(Objects::nonNull)
                .map(field -> field.toLowerCase(Locale.ROOT))
                .anyMatch(field -> field.contains(query));
    }

    private Comparator<ListingItem> listingComparator(CollectionItemSort sort) {
        Comparator<ListingItem> catalogOrder = Comparator
                .comparing(this::seriesTitle, textAscending())
                .thenComparing(this::catalogSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(this::visibleTitle, textAscending())
                .thenComparing(item -> item.response().id());
        return switch (sort) {
            case CATALOG_ORDER -> catalogOrder;
            case TITLE_ASC -> Comparator
                    .comparing(this::visibleTitle, textAscending())
                    .thenComparing(item -> item.response().id());
            case TITLE_DESC -> Comparator
                    .comparing(this::visibleTitle, textDescending())
                    .thenComparing(item -> item.response().id());
            case STATUS_ASC -> Comparator
                    .comparingInt(this::statusRank)
                    .thenComparing(catalogOrder);
            case NEWEST_ENTRY -> Comparator.comparing(
                    (ListingItem item) -> item.response().id(),
                    Comparator.reverseOrder()
            );
        };
    }

    private Comparator<String> textAscending() {
        return Comparator.nullsLast(
                String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder())
        );
    }

    private Comparator<String> textDescending() {
        return Comparator.nullsLast(
                String.CASE_INSENSITIVE_ORDER.reversed().thenComparing(Comparator.reverseOrder())
        );
    }

    private String seriesTitle(ListingItem item) {
        return item.response().catalogSeriesTitle();
    }

    private BigDecimal catalogSortOrder(ListingItem item) {
        return item.item().getCatalogItem() == null ? null : item.item().getCatalogItem().getSortOrder();
    }

    private String visibleTitle(ListingItem item) {
        CollectionItemResponse response = item.response();
        if (response.catalogItemTitle() != null) {
            return response.catalogItemTitle();
        }
        if (response.manualTitle() != null) {
            return response.manualTitle();
        }
        return response.masterProductName();
    }

    private int statusRank(ListingItem item) {
        return switch (item.item().getCollectionStatus()) {
            case OWNED -> 0;
            case WANTED -> 1;
            case MISSING -> 2;
            case DUPLICATED -> 3;
            case SELLABLE -> 4;
            case TRADABLE -> 5;
        };
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeRequiredOrExisting(String value, String existing) {
        return value == null ? existing : normalizeRequired(value);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullableOrExisting(String value, String existing) {
        return value == null ? existing : normalizeNullable(value);
    }

    private String normalizeCode(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private PhysicalCondition nullablePhysicalConditionOrExisting(PhysicalCondition value, PhysicalCondition existing) {
        return value == null ? existing : value;
    }

    private LocalDate nullableDateOrExisting(LocalDate value, LocalDate existing) {
        return value == null ? existing : value;
    }

    private record ResolvedReference(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            CollectionEditorialReferenceSource source
    ) {
    }

    private record ListingItem(CollectionItem item, CollectionItemResponse response) {
    }
}
