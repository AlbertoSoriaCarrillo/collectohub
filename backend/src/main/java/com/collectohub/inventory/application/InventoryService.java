package com.collectohub.inventory.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.application.CatalogItemNotFoundException;
import com.collectohub.catalog.application.CatalogItemEditionNotFoundException;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.domain.ShopProductEditorialReferenceSource;
import com.collectohub.inventory.dto.CreateShopProductRequest;
import com.collectohub.inventory.dto.PublicShopProductResponse;
import com.collectohub.inventory.dto.ShopProductResponse;
import com.collectohub.inventory.dto.UpdateShopProductRequest;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.shops.application.ShopNotFoundException;
import com.collectohub.shops.domain.Shop;
import com.collectohub.shops.domain.ShopMemberStatus;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class InventoryService {

    private final ShopRepository shopRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final MasterProductRepository masterProductRepository;
    private final CatalogItemRepository catalogItemRepository;
    private final CatalogItemEditionRepository catalogItemEditionRepository;
    private final MasterProductCatalogLinkRepository masterProductCatalogLinkRepository;
    private final ShopProductRepository shopProductRepository;

    public InventoryService(
            ShopRepository shopRepository,
            ShopMemberRepository shopMemberRepository,
            MasterProductRepository masterProductRepository,
            CatalogItemRepository catalogItemRepository,
            CatalogItemEditionRepository catalogItemEditionRepository,
            MasterProductCatalogLinkRepository masterProductCatalogLinkRepository,
            ShopProductRepository shopProductRepository
    ) {
        this.shopRepository = shopRepository;
        this.shopMemberRepository = shopMemberRepository;
        this.masterProductRepository = masterProductRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.catalogItemEditionRepository = catalogItemEditionRepository;
        this.masterProductCatalogLinkRepository = masterProductCatalogLinkRepository;
        this.shopProductRepository = shopProductRepository;
    }

    @Transactional
    public ShopProductResponse createShopProduct(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            CreateShopProductRequest request
    ) {
        Shop shop = findActiveShop(shopId);
        ensureCanManageShop(authenticatedUser, shopId);
        ResolvedReference reference = resolveReference(
                request.masterProductId(),
                request.catalogItemId(),
                request.catalogItemEditionId()
        );

        ShopProduct shopProduct = ShopProduct.create(
                shop,
                reference.masterProduct(),
                reference.catalogItem(),
                reference.catalogItemEdition(),
                reference.source(),
                request.priceAmount(),
                normalizeCurrencyOrDefault(request.currency(), shop.getCurrency()),
                request.stockQuantity(),
                Objects.requireNonNullElse(request.commercialStatus(), ShopProductCommercialStatus.AVAILABLE),
                request.physicalCondition(),
                Objects.requireNonNullElse(request.visible(), true),
                normalizeNullable(request.unitNumber()),
                request.totalLimitedUnits(),
                normalizeNullable(request.notes()),
                authenticatedUser.id()
        );

        return ShopProductResponse.from(shopProductRepository.save(shopProduct));
    }

    @Transactional
    public ShopProductResponse updateShopProduct(
            AuthenticatedUser authenticatedUser,
            Long shopId,
            Long shopProductId,
            UpdateShopProductRequest request
    ) {
        findActiveShop(shopId);
        ensureCanManageShop(authenticatedUser, shopId);
        ShopProduct shopProduct = shopProductRepository.findByIdAndShop_IdAndDeletedAtIsNull(shopProductId, shopId)
                .orElseThrow(() -> new ShopProductNotFoundException(shopProductId));

        if (hasExplicitReference(request)) {
            ResolvedReference reference = resolveUpdatedReference(shopProduct, request);
            shopProduct.updateReference(
                    reference.masterProduct(),
                    reference.catalogItem(),
                    reference.catalogItemEdition(),
                    reference.source(),
                    authenticatedUser.id()
            );
        }

        shopProduct.update(
                Objects.requireNonNullElse(request.priceAmount(), shopProduct.getPriceAmount()),
                normalizeCurrencyOrExisting(request.currency(), shopProduct.getCurrency()),
                Objects.requireNonNullElse(request.stockQuantity(), shopProduct.getStockQuantity()),
                Objects.requireNonNullElse(request.commercialStatus(), shopProduct.getCommercialStatus()),
                Objects.requireNonNullElse(request.physicalCondition(), shopProduct.getPhysicalCondition()),
                Objects.requireNonNullElse(request.visible(), shopProduct.isVisible()),
                normalizeNullableOrExisting(request.unitNumber(), shopProduct.getUnitNumber()),
                request.totalLimitedUnits() == null ? shopProduct.getTotalLimitedUnits() : request.totalLimitedUnits(),
                normalizeNullableOrExisting(request.notes(), shopProduct.getNotes()),
                authenticatedUser.id()
        );

        return ShopProductResponse.from(shopProduct);
    }

    @Transactional(readOnly = true)
    public List<ShopProductResponse> myShopProducts(AuthenticatedUser authenticatedUser, Long shopId) {
        findActiveShop(shopId);
        ensureShopMember(authenticatedUser, shopId);
        return shopProductRepository.findByShop_IdAndDeletedAtIsNullOrderByIdAsc(shopId).stream()
                .map(ShopProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicShopProductResponse> publicShopProducts(
            Long shopId,
            Long masterProductId,
            String categoryCode,
            String name,
            String franchise,
            String collectionName,
            String physicalCondition,
            String commercialStatus
    ) {
        findActiveShop(shopId);
        Specification<ShopProduct> specification = publicProductsForShop(shopId);

        if (masterProductId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("masterProduct").get("id"), masterProductId));
        }

        String normalizedCategoryCode = normalizeCode(categoryCode);
        if (normalizedCategoryCode != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(
                            criteriaBuilder.upper(root.get("masterProduct").get("category").get("code")),
                            normalizedCategoryCode
                    ));
        }

        String normalizedName = normalizeLike(name);
        if (normalizedName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("masterProduct").get("name")),
                            "%" + normalizedName + "%"
                    ));
        }

        String normalizedFranchise = normalizeLike(franchise);
        if (normalizedFranchise != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("masterProduct").get("franchise")),
                            "%" + normalizedFranchise + "%"
                    ));
        }

        String normalizedCollectionName = normalizeLike(collectionName);
        if (normalizedCollectionName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("masterProduct").get("collectionName")),
                            "%" + normalizedCollectionName + "%"
                    ));
        }

        PhysicalCondition parsedPhysicalCondition = parsePhysicalCondition(physicalCondition);
        if (parsedPhysicalCondition != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("physicalCondition"), parsedPhysicalCondition));
        }

        ShopProductCommercialStatus parsedCommercialStatus = parseCommercialStatus(commercialStatus);
        if (parsedCommercialStatus != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("commercialStatus"), parsedCommercialStatus));
        }

        return shopProductRepository.findAll(specification, Sort.by("id").ascending()).stream()
                .filter(this::hasPublicReference)
                .map(PublicShopProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicShopProductResponse getPublicShopProduct(Long shopProductId) {
        ShopProduct shopProduct = shopProductRepository.findByIdAndDeletedAtIsNull(shopProductId)
                .filter(ShopProduct::isPubliclyVisible)
                .filter(product -> product.getShop().isActive())
                .filter(this::hasPublicReference)
                .orElseThrow(() -> new ShopProductNotFoundException(shopProductId));
        return PublicShopProductResponse.from(shopProduct);
    }

    private Shop findActiveShop(Long shopId) {
        return shopRepository.findByIdAndDeletedAtIsNull(shopId)
                .filter(Shop::isActive)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
    }

    private MasterProduct findActiveMasterProduct(Long masterProductId) {
        return masterProductRepository.findByIdAndDeletedAtIsNull(masterProductId)
                .filter(MasterProduct::isActive)
                .orElseThrow(() -> new MasterProductNotFoundException(masterProductId));
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

    private ResolvedReference resolveReference(Long masterProductId, Long catalogItemId, Long editionId) {
        if (masterProductId == null && catalogItemId == null) {
            throw new InvalidShopProductReferenceException(
                    "A master product or editorial catalog item is required"
            );
        }
        if (editionId != null && catalogItemId == null) {
            throw new InvalidShopProductReferenceException(
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
                    ShopProductEditorialReferenceSource.MANUAL_EDITORIAL
            );
        }
        return resolveEditorialReferenceFromVerifiedBridge(masterProduct);
    }

    private ResolvedReference resolveUpdatedReference(
            ShopProduct shopProduct,
            UpdateShopProductRequest request
    ) {
        if (request.catalogItemId() != null) {
            return resolveReference(
                    request.masterProductId() == null
                            ? idOf(shopProduct.getMasterProduct())
                            : request.masterProductId(),
                    request.catalogItemId(),
                    request.catalogItemEditionId()
            );
        }
        if (request.masterProductId() != null) {
            return resolveReference(request.masterProductId(), null, null);
        }
        return resolveReference(
                idOf(shopProduct.getMasterProduct()),
                idOf(shopProduct.getCatalogItem()),
                request.catalogItemEditionId()
        );
    }

    private void validateEditionBelongsToItem(CatalogItemEdition edition, CatalogItem catalogItem) {
        if (edition != null && !edition.getCatalogItem().getId().equals(catalogItem.getId())) {
            throw new InvalidShopProductReferenceException(
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
                        ShopProductEditorialReferenceSource.VERIFIED_BRIDGE
                ))
                .orElseGet(() -> new ResolvedReference(
                        masterProduct,
                        null,
                        null,
                        ShopProductEditorialReferenceSource.LEGACY
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
                throw new ConflictingShopProductReferenceException(
                        "The editorial reference conflicts with the verified master product bridge"
                );
            }
        });
    }

    private Optional<MasterProductCatalogLink> verifiedBridge(Long masterProductId) {
        return masterProductCatalogLinkRepository
                .findByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                        masterProductId,
                        MasterProductCatalogLinkStatus.VERIFIED
                );
    }

    private boolean hasExplicitReference(UpdateShopProductRequest request) {
        return request.masterProductId() != null
                || request.catalogItemId() != null
                || request.catalogItemEditionId() != null;
    }

    private boolean hasPublicReference(ShopProduct product) {
        boolean legacyPublic = product.getMasterProduct() != null && product.getMasterProduct().isActive();
        boolean editorialPublic = product.getCatalogItem() != null
                && product.getCatalogItem().isPubliclyVisible()
                && (product.getCatalogItemEdition() == null
                || product.getCatalogItemEdition().isPubliclyVisible());
        return legacyPublic || editorialPublic;
    }

    private Long idOf(MasterProduct value) { return value == null ? null : value.getId(); }
    private Long idOf(CatalogItem value) { return value == null ? null : value.getId(); }

    private void ensureCanManageShop(AuthenticatedUser authenticatedUser, Long shopId) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getShop().isActive())
                .filter(member -> member.getUser().isActive())
                .filter(member -> member.getRole().canManageShop())
                .orElseThrow(() -> new AccessDeniedException("User cannot manage this shop inventory"));
    }

    private void ensureShopMember(AuthenticatedUser authenticatedUser, Long shopId) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        shopMemberRepository.findByShop_IdAndUser_IdAndStatusAndDeletedAtIsNull(
                        shopId,
                        authenticatedUser.id(),
                        ShopMemberStatus.ACTIVE
                )
                .filter(member -> member.getShop().isActive())
                .filter(member -> member.getUser().isActive())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this shop"));
    }

    private Specification<ShopProduct> publicProductsForShop(Long shopId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("shop").get("id"), shopId),
                criteriaBuilder.isNull(root.get("deletedAt")),
                criteriaBuilder.isTrue(root.get("visible")),
                criteriaBuilder.equal(root.get("commercialStatus"), ShopProductCommercialStatus.AVAILABLE),
                criteriaBuilder.isNull(root.get("shop").get("deletedAt"))
        );
    }

    private String normalizeCurrencyOrDefault(String value, String defaultCurrency) {
        String normalized = normalizeCode(value);
        return normalized == null ? defaultCurrency.toUpperCase(Locale.ROOT) : normalized;
    }

    private String normalizeCurrencyOrExisting(String value, String existing) {
        String normalized = normalizeCode(value);
        return normalized == null ? existing : normalized;
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

    private String normalizeLike(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
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

    private ShopProductCommercialStatus parseCommercialStatus(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            return null;
        }
        try {
            return ShopProductCommercialStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCatalogFilterException("Unsupported commercial status: " + value);
        }
    }

    private record ResolvedReference(
            MasterProduct masterProduct,
            CatalogItem catalogItem,
            CatalogItemEdition catalogItemEdition,
            ShopProductEditorialReferenceSource source
    ) {
    }
}
