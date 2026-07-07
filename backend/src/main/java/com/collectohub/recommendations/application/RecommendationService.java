package com.collectohub.recommendations.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.application.ProductCategoryNotFoundException;
import com.collectohub.catalog.domain.MasterProductStatus;
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

        Map<Long, CollectionItem> bestItemsByMasterProduct = targetItems.stream()
                .filter(item -> item.getMasterProduct() != null)
                .collect(Collectors.toMap(
                        item -> item.getMasterProduct().getId(),
                        item -> item,
                        this::bestMatch,
                        LinkedHashMap::new
                ));

        if (bestItemsByMasterProduct.isEmpty()) {
            return List.of();
        }

        List<ShopProduct> candidates = shopProductRepository.findRecommendationCandidates(
                bestItemsByMasterProduct.keySet(),
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
                .collect(Collectors.toMap(
                        ShopProduct::getId,
                        shopProduct -> toResponse(shopProduct, bestItemsByMasterProduct.get(shopProduct.getMasterProduct().getId())),
                        (first, ignored) -> first,
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
                && shopProduct.getMasterProduct().isActive()
                && matchesCategory(shopProduct, filters.categoryCode())
                && matchesMaxPrice(shopProduct, filters.maxPrice())
                && matchesCurrency(shopProduct, filters.currency())
                && matchesPhysicalCondition(shopProduct, filters.physicalCondition())
                && matchesShop(shopProduct, filters.shopId());
    }

    private boolean matchesCategory(ShopProduct shopProduct, String categoryCode) {
        return categoryCode == null
                || categoryCode.equalsIgnoreCase(shopProduct.getMasterProduct().getCategory().getCode());
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

    private RecommendedShopProductResponse toResponse(ShopProduct shopProduct, CollectionItem matchedItem) {
        var shop = shopProduct.getShop();
        var masterProduct = shopProduct.getMasterProduct();
        String matchedStatus = matchedItem.getCollectionStatus().name();

        return new RecommendedShopProductResponse(
                shopProduct.getId(),
                shop.getId(),
                shop.getName(),
                masterProduct.getId(),
                masterProduct.getName(),
                masterProduct.getCategory().getCode(),
                masterProduct.getFranchise(),
                masterProduct.getCollectionName(),
                masterProduct.getVolumeNumber(),
                masterProduct.getCoverImageUrl(),
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

    private CollectionItem bestMatch(CollectionItem first, CollectionItem second) {
        return collectionItemComparator().compare(first, second) <= 0 ? first : second;
    }

    private Comparator<CollectionItem> collectionItemComparator() {
        return Comparator
                .comparingInt((CollectionItem item) -> statusPriority(item.getCollectionStatus()))
                .thenComparing(item -> item.getCollection().getName(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(CollectionItem::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<RecommendedShopProductResponse> recommendationComparator() {
        return Comparator
                .comparingInt((RecommendedShopProductResponse response) ->
                        statusPriority(response.matchedCollectionItemStatus()))
                .thenComparing(RecommendedShopProductResponse::productName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(RecommendedShopProductResponse::priceAmount, Comparator.nullsLast(BigDecimal::compareTo))
                .thenComparing(RecommendedShopProductResponse::shopProductId, Comparator.nullsLast(Long::compareTo));
    }

    private int statusPriority(CollectionItemStatus status) {
        return status == CollectionItemStatus.MISSING ? 0 : 1;
    }

    private int statusPriority(String status) {
        return CollectionItemStatus.MISSING.name().equals(status) ? 0 : 1;
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
}
