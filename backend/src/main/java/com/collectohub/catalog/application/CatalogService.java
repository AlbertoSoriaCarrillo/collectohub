package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductStatus;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.dto.CreateMasterProductRequest;
import com.collectohub.catalog.dto.MasterProductResponse;
import com.collectohub.catalog.dto.ProductCategoryResponse;
import com.collectohub.catalog.dto.UpdateMasterProductRequest;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CatalogService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SHOP_OWNER_ROLE = "SHOP_OWNER";
    private static final String LIMITED_EDITION_TOTAL_UNITS_ATTRIBUTE = "limitedEditionTotalUnits";

    private final ProductCategoryRepository productCategoryRepository;
    private final MasterProductRepository masterProductRepository;

    public CatalogService(
            ProductCategoryRepository productCategoryRepository,
            MasterProductRepository masterProductRepository
    ) {
        this.productCategoryRepository = productCategoryRepository;
        this.masterProductRepository = masterProductRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> listCategories() {
        return productCategoryRepository.findByDeletedAtIsNullOrderByCodeAsc().stream()
                .map(ProductCategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MasterProductResponse> searchMasterProducts(
            String categoryCode,
            String name,
            String franchise,
            String collectionName,
            String language,
            String status
    ) {
        MasterProductStatus productStatus = parseStatus(status);
        Specification<MasterProduct> specification = activeProducts(productStatus);

        String normalizedCategoryCode = normalizeCode(categoryCode);
        if (normalizedCategoryCode != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(
                            criteriaBuilder.upper(root.get("category").get("code")),
                            normalizedCategoryCode
                    ));
        }

        String normalizedName = normalizeLike(name);
        if (normalizedName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + normalizedName + "%"));
        }

        String normalizedFranchise = normalizeLike(franchise);
        if (normalizedFranchise != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("franchise")), "%" + normalizedFranchise + "%"));
        }

        String normalizedCollectionName = normalizeLike(collectionName);
        if (normalizedCollectionName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("collectionName")), "%" + normalizedCollectionName + "%"));
        }

        String normalizedLanguage = normalizeLanguage(language);
        if (normalizedLanguage != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("productLanguage")), normalizedLanguage));
        }

        return masterProductRepository.findAll(specification, Sort.by("name").ascending()).stream()
                .map(MasterProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MasterProductResponse getMasterProduct(Long id) {
        MasterProduct product = findActiveProduct(id);
        return MasterProductResponse.from(product);
    }

    @Transactional
    public MasterProductResponse createMasterProduct(
            AuthenticatedUser authenticatedUser,
            CreateMasterProductRequest request
    ) {
        ensureCanManageCatalog(authenticatedUser);

        String name = normalizeRequired(request.name());
        ProductCategory category = findCategory(normalizeCode(request.categoryCode()));
        String franchise = normalizeNullable(request.franchise());
        String collectionName = normalizeNullable(request.collectionName());
        String volumeNumber = normalizeNullable(request.volumeNumber());
        String publisher = normalizeNullable(request.publisher());
        String isbn = normalizeUppercase(request.isbn());
        String ean = normalizeUppercase(request.ean());
        String language = normalizeLanguage(request.language());
        List<String> publicationCountries = normalizeCountries(request.publicationCountries());
        Map<String, Object> attributes = normalizeAttributes(request.attributes());
        applyLimitedEditionTotalUnits(attributes, request.limitedEditionTotalUnits());

        ensureNoDuplicateForCreate(name, franchise, volumeNumber, language, isbn, ean);

        MasterProduct product = MasterProduct.create(
                name,
                normalizeNullable(request.description()),
                category,
                franchise,
                collectionName,
                volumeNumber,
                publisher,
                isbn,
                ean,
                request.releaseDate(),
                request.editionStartDate(),
                request.editionEndDate(),
                language,
                Boolean.TRUE.equals(request.limitedEdition()),
                publicationCountries,
                normalizeNullable(request.coverImageUrl()),
                attributes,
                authenticatedUser.id()
        );

        return MasterProductResponse.from(masterProductRepository.save(product));
    }

    @Transactional
    public MasterProductResponse updateMasterProduct(
            AuthenticatedUser authenticatedUser,
            Long id,
            UpdateMasterProductRequest request
    ) {
        ensureCanManageCatalog(authenticatedUser);
        MasterProduct product = findActiveProduct(id);

        String name = normalizeRequiredOrExisting(request.name(), product.getName());
        ProductCategory category = request.categoryCode() == null
                ? product.getCategory()
                : findCategory(normalizeCode(request.categoryCode()));
        String franchise = normalizeNullableOrExisting(request.franchise(), product.getFranchise());
        String collectionName = normalizeNullableOrExisting(request.collectionName(), product.getCollectionName());
        String volumeNumber = normalizeNullableOrExisting(request.volumeNumber(), product.getVolumeNumber());
        String publisher = normalizeNullableOrExisting(request.publisher(), product.getPublisher());
        String isbn = normalizeUppercaseOrExisting(request.isbn(), product.getIsbn());
        String ean = normalizeUppercaseOrExisting(request.ean(), product.getEan());
        String language = normalizeLanguageOrExisting(request.language(), product.getProductLanguage());
        List<String> publicationCountries = request.publicationCountries() == null
                ? product.getPublicationCountries()
                : normalizeCountries(request.publicationCountries());
        Map<String, Object> attributes = request.attributes() == null
                ? new LinkedHashMap<>(product.getAttributes())
                : normalizeAttributes(request.attributes());
        applyLimitedEditionTotalUnits(attributes, request.limitedEditionTotalUnits());

        ensureNoDuplicateForUpdate(id, name, franchise, volumeNumber, language, isbn, ean);

        product.update(
                name,
                normalizeNullableOrExisting(request.description(), product.getDescription()),
                category,
                franchise,
                collectionName,
                volumeNumber,
                publisher,
                isbn,
                ean,
                request.releaseDate() == null ? product.getReleaseDate() : request.releaseDate(),
                request.editionStartDate() == null ? product.getEditionStartDate() : request.editionStartDate(),
                request.editionEndDate() == null ? product.getEditionEndDate() : request.editionEndDate(),
                language,
                request.limitedEdition() == null ? product.isLimitedEdition() : request.limitedEdition(),
                publicationCountries,
                normalizeNullableOrExisting(request.coverImageUrl(), product.getCoverImageUrl()),
                attributes,
                authenticatedUser.id()
        );

        return MasterProductResponse.from(product);
    }

    private void ensureCanManageCatalog(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null
                || authenticatedUser.roles().stream().noneMatch(role -> ADMIN_ROLE.equals(role) || SHOP_OWNER_ROLE.equals(role))) {
            throw new AccessDeniedException("User cannot manage master products");
        }
    }

    private MasterProduct findActiveProduct(Long id) {
        return masterProductRepository.findByIdAndDeletedAtIsNull(id)
                .filter(MasterProduct::isActive)
                .orElseThrow(() -> new MasterProductNotFoundException(id));
    }

    private ProductCategory findCategory(String categoryCode) {
        return productCategoryRepository.findByCodeAndDeletedAtIsNull(categoryCode)
                .orElseThrow(() -> new ProductCategoryNotFoundException(categoryCode));
    }

    private Specification<MasterProduct> activeProducts(MasterProductStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("status"), status),
                criteriaBuilder.isNull(root.get("deletedAt"))
        );
    }

    private void ensureNoDuplicateForCreate(
            String name,
            String franchise,
            String volumeNumber,
            String language,
            String isbn,
            String ean
    ) {
        if (isbn != null && masterProductRepository.existsByIsbnIgnoreCaseAndStatusAndDeletedAtIsNull(
                isbn,
                MasterProductStatus.ACTIVE
        )) {
            throw new DuplicateMasterProductException("isbn already exists");
        }
        if (ean != null && masterProductRepository.existsByEanIgnoreCaseAndStatusAndDeletedAtIsNull(
                ean,
                MasterProductStatus.ACTIVE
        )) {
            throw new DuplicateMasterProductException("ean already exists");
        }
        if (masterProductRepository.existsLogicalDuplicate(
                normalizeForComparison(name),
                normalizeForComparison(franchise),
                normalizeForComparison(volumeNumber),
                normalizeForComparison(language),
                MasterProductStatus.ACTIVE
        )) {
            throw new DuplicateMasterProductException("logical combination already exists");
        }
    }

    private void ensureNoDuplicateForUpdate(
            Long productId,
            String name,
            String franchise,
            String volumeNumber,
            String language,
            String isbn,
            String ean
    ) {
        if (isbn != null && masterProductRepository.existsByIsbnIgnoreCaseAndStatusAndDeletedAtIsNullAndIdNot(
                isbn,
                MasterProductStatus.ACTIVE,
                productId
        )) {
            throw new DuplicateMasterProductException("isbn already exists");
        }
        if (ean != null && masterProductRepository.existsByEanIgnoreCaseAndStatusAndDeletedAtIsNullAndIdNot(
                ean,
                MasterProductStatus.ACTIVE,
                productId
        )) {
            throw new DuplicateMasterProductException("ean already exists");
        }
        if (masterProductRepository.existsLogicalDuplicateExcludingId(
                normalizeForComparison(name),
                normalizeForComparison(franchise),
                normalizeForComparison(volumeNumber),
                normalizeForComparison(language),
                MasterProductStatus.ACTIVE,
                productId
        )) {
            throw new DuplicateMasterProductException("logical combination already exists");
        }
    }

    private MasterProductStatus parseStatus(String value) {
        String normalized = normalizeUppercase(value);
        if (normalized == null) {
            return MasterProductStatus.ACTIVE;
        }
        try {
            return MasterProductStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCatalogFilterException("Unsupported product status: " + value);
        }
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

    private String normalizeUppercase(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeUppercaseOrExisting(String value, String existing) {
        return value == null ? existing : normalizeUppercase(value);
    }

    private String normalizeLanguage(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguageOrExisting(String value, String existing) {
        return value == null ? existing : normalizeLanguage(value);
    }

    private String normalizeLike(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeForComparison(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    private List<String> normalizeCountries(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::normalizeUppercase)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private Map<String, Object> normalizeAttributes(Map<String, Object> values) {
        return values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }

    private void applyLimitedEditionTotalUnits(Map<String, Object> attributes, Integer limitedEditionTotalUnits) {
        if (limitedEditionTotalUnits != null) {
            attributes.put(LIMITED_EDITION_TOTAL_UNITS_ATTRIBUTE, limitedEditionTotalUnits);
        }
    }
}
