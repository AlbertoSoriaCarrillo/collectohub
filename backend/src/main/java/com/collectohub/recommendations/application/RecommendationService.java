package com.collectohub.recommendations.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.application.ProductCategoryNotFoundException;
import com.collectohub.catalog.domain.MasterProductStatus;
import com.collectohub.catalog.domain.CatalogSeries;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.recommendations.dto.RecommendationReasonResponse;
import com.collectohub.recommendations.dto.RecommendedShopProductResponse;
import com.collectohub.recommendations.dto.UserRecommendationResponse;
import com.collectohub.recommendations.dto.UserRecommendationSummaryResponse;
import com.collectohub.shops.domain.ShopStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Set<CollectionItemStatus> TARGET_STATUSES = Set.of(
            CollectionItemStatus.MISSING,
            CollectionItemStatus.WANTED
    );

    private final CollectionItemRepository collectionItemRepository;
    private final ShopProductRepository shopProductRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public RecommendationService(
            CollectionItemRepository collectionItemRepository,
            ShopProductRepository shopProductRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        this.collectionItemRepository = collectionItemRepository;
        this.shopProductRepository = shopProductRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Transactional(readOnly = true)
    public UserRecommendationResponse myRecommendations(
            AuthenticatedUser authenticatedUser,
            String categoryCode,
            String maxPrice,
            String currency,
            String physicalCondition,
            String shopId
    ) {
        RecommendationFilters filters = parseFilters(categoryCode, maxPrice, currency, physicalCondition, shopId);
        List<CollectionItem> targetItems = findTargetItems(authenticatedUser);
        return UserRecommendationResponse.from(buildRecommendations(targetItems, filters));
    }

    @Transactional(readOnly = true)
    public UserRecommendationSummaryResponse mySummary(
            AuthenticatedUser authenticatedUser,
            String categoryCode,
            String maxPrice,
            String currency,
            String physicalCondition,
            String shopId
    ) {
        RecommendationFilters filters = parseFilters(categoryCode, maxPrice, currency, physicalCondition, shopId);
        List<CollectionItem> targetItems = findTargetItems(authenticatedUser);
        List<RecommendedShopProductResponse> recommendations = buildRecommendations(targetItems, filters);
        List<String> matchedCategoryCodes = recommendations.stream()
                .map(RecommendedShopProductResponse::categoryCode)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        return new UserRecommendationSummaryResponse(
                countStatus(targetItems, CollectionItemStatus.MISSING),
                countStatus(targetItems, CollectionItemStatus.WANTED),
                recommendations.size(),
                (int) recommendations.stream().map(RecommendedShopProductResponse::shopId).distinct().count(),
                matchedCategoryCodes
        );
    }

    private List<CollectionItem> findTargetItems(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        return collectionItemRepository.findRecommendationItemsForUser(authenticatedUser.id(), TARGET_STATUSES).stream()
                .filter(item -> TARGET_STATUSES.contains(item.getCollectionStatus()))
                .toList();
    }

    private List<RecommendedShopProductResponse> buildRecommendations(
            List<CollectionItem> targetItems,
            RecommendationFilters filters
    ) {
        if (targetItems.isEmpty()) {
            return List.of();
        }

        Set<Long> masterProductIds = ids(targetItems, ReferenceKind.MASTER_PRODUCT);
        Set<Long> catalogItemIds = ids(targetItems, ReferenceKind.CATALOG_ITEM);
        Set<Long> catalogItemEditionIds = ids(targetItems, ReferenceKind.CATALOG_ITEM_EDITION);
        if (masterProductIds.isEmpty() && catalogItemIds.isEmpty() && catalogItemEditionIds.isEmpty()) {
            return List.of();
        }

        List<ShopProduct> candidates = shopProductRepository.findRecommendationCandidates(
                nonEmpty(masterProductIds),
                nonEmpty(catalogItemIds),
                nonEmpty(catalogItemEditionIds),
                ShopProductCommercialStatus.AVAILABLE,
                ShopStatus.ACTIVE,
                MasterProductStatus.ACTIVE,
                filters.categoryCode(),
                filters.maxPrice(),
                filters.currency(),
                filters.physicalCondition(),
                filters.shopId()
        );

        return candidates.stream()
                .filter(shopProduct -> isRecommendableCandidate(shopProduct, filters))
                .map(shopProduct -> bestMatch(shopProduct, targetItems))
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(
                        match -> match.shopProduct().getId(),
                        this::toResponse,
                        this::betterMatch,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .sorted(recommendationComparator())
                .toList();
    }

    private boolean isRecommendableCandidate(ShopProduct shopProduct, RecommendationFilters filters) {
        return shopProduct.isPubliclyVisible()
                && shopProduct.getStockQuantity() > 0
                && shopProduct.getShop().isActive()
                && hasPublicReference(shopProduct)
                && matchesCategory(shopProduct, filters.categoryCode())
                && matchesMaxPrice(shopProduct, filters.maxPrice())
                && matchesCurrency(shopProduct, filters.currency())
                && matchesPhysicalCondition(shopProduct, filters.physicalCondition())
                && matchesShop(shopProduct, filters.shopId());
    }

    private boolean matchesCategory(ShopProduct shopProduct, String categoryCode) {
        return categoryCode == null
                || (shopProduct.getMasterProduct() != null
                    && shopProduct.getMasterProduct().isActive()
                    && categoryCode.equalsIgnoreCase(shopProduct.getMasterProduct().getCategory().getCode()));
    }

    private boolean matchesMaxPrice(ShopProduct shopProduct, BigDecimal maxPrice) {
        return maxPrice == null || shopProduct.getPriceAmount().compareTo(maxPrice) <= 0;
    }

    private boolean matchesCurrency(ShopProduct shopProduct, String currency) {
        return currency == null || currency.equalsIgnoreCase(shopProduct.getCurrency());
    }

    private boolean matchesPhysicalCondition(ShopProduct shopProduct, PhysicalCondition physicalCondition) {
        return physicalCondition == null || shopProduct.getPhysicalCondition() == physicalCondition;
    }

    private boolean matchesShop(ShopProduct shopProduct, Long shopId) {
        return shopId == null || shopProduct.getShop().getId().equals(shopId);
    }

    private RecommendedShopProductResponse toResponse(Match match) {
        ShopProduct shopProduct = match.shopProduct();
        CollectionItem matchedItem = match.collectionItem();
        var shop = shopProduct.getShop();
        var masterProduct = shopProduct.getMasterProduct();
        var catalogItem = shopProduct.getCatalogItem();
        var edition = shopProduct.getCatalogItemEdition();
        var series = catalogItem == null ? null : catalogItem.getSeries();
        String matchedStatus = matchedItem.getCollectionStatus().name();

        return new RecommendedShopProductResponse(
                shopProduct.getId(),
                shop.getId(),
                shop.getName(),
                masterProduct == null ? null : masterProduct.getId(),
                firstNonBlank(masterProduct == null ? null : masterProduct.getName(), catalogItem == null ? null : catalogItem.getTitle()),
                masterProduct == null ? null : masterProduct.getCategory().getCode(),
                firstNonBlank(masterProduct == null ? null : masterProduct.getFranchise(), franchiseName(series)),
                firstNonBlank(masterProduct == null ? null : masterProduct.getCollectionName(), series == null ? null : series.getTitle()),
                firstNonBlank(masterProduct == null ? null : masterProduct.getVolumeNumber(), catalogItem == null ? null : catalogItem.getSequenceLabel()),
                edition != null && edition.getCoverImageUrl() != null
                        ? edition.getCoverImageUrl()
                        : masterProduct == null ? null : masterProduct.getCoverImageUrl(),
                catalogItem == null ? null : catalogItem.getId(),
                catalogItem == null ? null : catalogItem.getTitle(),
                catalogItem == null ? null : catalogItem.getSequenceLabel(),
                series == null ? null : series.getId(),
                series == null ? null : series.getTitle(),
                edition == null ? null : edition.getId(),
                edition == null ? null : edition.getEditionName(),
                edition == null || edition.getFormat() == null ? null : edition.getFormat().name(),
                edition == null ? null : edition.getIsbn(),
                edition == null ? null : edition.getEan(),
                edition == null ? null : edition.getCoverImageUrl(),
                edition == null || edition.getPublisher() == null ? null : edition.getPublisher().getName(),
                franchiseName(series),
                shopProduct.getEditorialReferenceSource().name(),
                match.matchType().name(),
                shopProduct.getPriceAmount(),
                shopProduct.getCurrency(),
                shopProduct.getStockQuantity(),
                shopProduct.getPhysicalCondition().name(),
                shopProduct.getCommercialStatus().name(),
                RecommendationReasonResponse.fromCollectionStatus(matchedStatus),
                matchedItem.getCollection().getId(),
                matchedItem.getCollection().getName(),
                matchedStatus
        );
    }

    private java.util.Optional<Match> bestMatch(ShopProduct shopProduct, List<CollectionItem> targetItems) {
        return targetItems.stream()
                .map(item -> match(shopProduct, item))
                .flatMap(java.util.Optional::stream)
                .min(matchComparator());
    }

    private java.util.Optional<Match> match(ShopProduct shopProduct, CollectionItem item) {
        if (item.getCatalogItemEdition() != null && shopProduct.getCatalogItemEdition() != null
                && Objects.equals(item.getCatalogItemEdition().getId(), shopProduct.getCatalogItemEdition().getId())) {
            return java.util.Optional.of(new Match(shopProduct, item, MatchType.EDITION_EXACT));
        }
        if (item.getCatalogItem() != null && shopProduct.getCatalogItem() != null
                && Objects.equals(item.getCatalogItem().getId(), shopProduct.getCatalogItem().getId())) {
            return java.util.Optional.of(new Match(shopProduct, item, MatchType.ITEM_EXACT));
        }
        if (item.getMasterProduct() != null && shopProduct.getMasterProduct() != null
                && Objects.equals(item.getMasterProduct().getId(), shopProduct.getMasterProduct().getId())) {
            return java.util.Optional.of(new Match(shopProduct, item, MatchType.LEGACY_MASTER_PRODUCT));
        }
        return java.util.Optional.empty();
    }

    private Set<Long> ids(List<CollectionItem> items, ReferenceKind kind) {
        return items.stream()
                .map(item -> switch (kind) {
                    case MASTER_PRODUCT -> item.getMasterProduct() == null ? null : item.getMasterProduct().getId();
                    case CATALOG_ITEM -> item.getCatalogItem() == null ? null : item.getCatalogItem().getId();
                    case CATALOG_ITEM_EDITION -> item.getCatalogItemEdition() == null
                            ? null : item.getCatalogItemEdition().getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> nonEmpty(Set<Long> ids) {
        return ids.isEmpty() ? Set.of(-1L) : ids;
    }

    private boolean hasPublicReference(ShopProduct shopProduct) {
        boolean legacy = shopProduct.getMasterProduct() != null && shopProduct.getMasterProduct().isActive();
        boolean item = shopProduct.getCatalogItem() != null && shopProduct.getCatalogItem().isPubliclyVisible();
        boolean edition = shopProduct.getCatalogItemEdition() != null
                && shopProduct.getCatalogItemEdition().isPubliclyVisible()
                && shopProduct.getCatalogItem() != null
                && Objects.equals(shopProduct.getCatalogItemEdition().getCatalogItem().getId(),
                        shopProduct.getCatalogItem().getId());
        return legacy || item || edition;
    }

    private String franchiseName(CatalogSeries series) {
        return series == null || series.getFranchise() == null ? null : series.getFranchise().getName();
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private RecommendedShopProductResponse betterMatch(
            RecommendedShopProductResponse first,
            RecommendedShopProductResponse second
    ) {
        return responseMatchComparator().compare(first, second) <= 0 ? first : second;
    }

    private Comparator<Match> matchComparator() {
        return Comparator
                .comparingInt((Match match) -> match.matchType().priority)
                .thenComparingInt(match -> statusPriority(match.collectionItem().getCollectionStatus()))
                .thenComparing(match -> match.collectionItem().getCollection().getName(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(match -> match.collectionItem().getId(), Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<RecommendedShopProductResponse> responseMatchComparator() {
        return Comparator
                .comparingInt((RecommendedShopProductResponse response) -> matchPriority(response.matchType()))
                .thenComparingInt(response -> statusPriority(response.matchedCollectionItemStatus()))
                .thenComparing(RecommendedShopProductResponse::matchedCollectionName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(RecommendedShopProductResponse::matchedCollectionId,
                        Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<RecommendedShopProductResponse> recommendationComparator() {
        return Comparator
                .comparingInt((RecommendedShopProductResponse response) ->
                        statusPriority(response.matchedCollectionItemStatus()))
                .thenComparing(RecommendedShopProductResponse::priceAmount, Comparator.nullsLast(BigDecimal::compareTo))
                .thenComparing(RecommendedShopProductResponse::productName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(RecommendedShopProductResponse::shopProductId, Comparator.nullsLast(Long::compareTo));
    }

    private int statusPriority(CollectionItemStatus status) {
        return status == CollectionItemStatus.MISSING ? 0 : 1;
    }

    private int statusPriority(String status) {
        return CollectionItemStatus.MISSING.name().equals(status) ? 0 : 1;
    }

    private int matchPriority(String matchType) {
        return MatchType.valueOf(matchType).priority;
    }

    private long countStatus(List<CollectionItem> targetItems, CollectionItemStatus status) {
        return targetItems.stream()
                .filter(item -> item.getCollectionStatus() == status)
                .count();
    }

    private RecommendationFilters parseFilters(
            String categoryCode,
            String maxPrice,
            String currency,
            String physicalCondition,
            String shopId
    ) {
        return new RecommendationFilters(
                parseCategoryCode(categoryCode),
                parseMaxPrice(maxPrice),
                parseCurrency(currency),
                parsePhysicalCondition(physicalCondition),
                parseShopId(shopId)
        );
    }

    private String parseCategoryCode(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return null;
        }
        productCategoryRepository.findByCodeAndDeletedAtIsNull(normalized)
                .orElseThrow(() -> new ProductCategoryNotFoundException(normalized));
        return normalized;
    }

    private BigDecimal parseMaxPrice(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(normalized);
            if (parsed.signum() < 0) {
                throw new InvalidCatalogFilterException("maxPrice must be greater than or equal to 0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new InvalidCatalogFilterException("Invalid maxPrice: " + value);
        }
    }

    private String parseCurrency(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return null;
        }
        if (!normalized.matches("[A-Z]{3}")) {
            throw new InvalidCatalogFilterException("currency must be an ISO 4217 code");
        }
        return normalized;
    }

    private PhysicalCondition parsePhysicalCondition(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return null;
        }
        try {
            return PhysicalCondition.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCatalogFilterException("Unsupported physical condition: " + value);
        }
    }

    private Long parseShopId(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        try {
            Long parsed = Long.valueOf(normalized);
            if (parsed <= 0) {
                throw new InvalidCatalogFilterException("shopId must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new InvalidCatalogFilterException("Invalid shopId: " + value);
        }
    }

    private String normalizeCode(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record RecommendationFilters(
            String categoryCode,
            BigDecimal maxPrice,
            String currency,
            PhysicalCondition physicalCondition,
            Long shopId
    ) {
    }

    private record Match(ShopProduct shopProduct, CollectionItem collectionItem, MatchType matchType) {
    }

    private enum MatchType {
        EDITION_EXACT(0),
        ITEM_EXACT(1),
        LEGACY_MASTER_PRODUCT(2);

        private final int priority;

        MatchType(int priority) {
            this.priority = priority;
        }
    }

    private enum ReferenceKind {
        MASTER_PRODUCT,
        CATALOG_ITEM,
        CATALOG_ITEM_EDITION
    }
}
