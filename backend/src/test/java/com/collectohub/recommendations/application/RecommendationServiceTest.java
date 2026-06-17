package com.collectohub.recommendations.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.ProductCategory;
import com.collectohub.catalog.infrastructure.ProductCategoryRepository;
import com.collectohub.collections.domain.Collection;
import com.collectohub.collections.domain.CollectionItem;
import com.collectohub.collections.domain.CollectionItemStatus;
import com.collectohub.collections.domain.CollectionVisibility;
import com.collectohub.collections.infrastructure.CollectionItemRepository;
import com.collectohub.inventory.domain.PhysicalCondition;
import com.collectohub.inventory.domain.ShopProduct;
import com.collectohub.inventory.domain.ShopProductCommercialStatus;
import com.collectohub.inventory.infrastructure.ShopProductRepository;
import com.collectohub.shops.domain.Shop;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CollectionItemRepository collectionItemRepository;

    @Mock
    private ShopProductRepository shopProductRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    private RecommendationService recommendationService;
    private User user;
    private ProductCategory category;
    private ProductCategory figureCategory;
    private Collection collection;
    private Shop shop;
    private MasterProduct masterProduct;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                collectionItemRepository,
                shopProductRepository,
                productCategoryRepository
        );
        user = user(42L);
        category = withId(new ProductCategory("MANGA_COMIC", "Manga and comic"), 10L);
        figureCategory = withId(new ProductCategory("FIGURE", "Figure"), 11L);
        collection = collection(100L, "Wishlist");
        shop = shop(500L, "Collector Cave");
        masterProduct = masterProduct(200L, category, "Dragon Ball 1", "1");
    }

    @Test
    void userWithoutCollectionsGetsEmptyRecommendations() {
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of());

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
        assertThat(response.totalRecommendations()).isZero();
        verify(shopProductRepository, never()).findRecommendationCandidates(anySet(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void missingItemWithAvailableShopProductGetsRecommendation() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        ShopProduct shopProduct = shopProduct(900L, masterProduct, BigDecimal.TEN, 2);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(shopProduct));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).hasSize(1);
        var recommendation = response.recommendations().getFirst();
        assertThat(recommendation.shopProductId()).isEqualTo(900L);
        assertThat(recommendation.shopName()).isEqualTo("Collector Cave");
        assertThat(recommendation.productName()).isEqualTo("Dragon Ball 1");
        assertThat(recommendation.matchedCollectionItemStatus()).isEqualTo("MISSING");
        assertThat(recommendation.recommendationReason().code()).isEqualTo("COLLECTION_ITEM_MISSING");
    }

    @Test
    void wantedItemWithAvailableShopProductGetsRecommendation() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.WANTED);
        ShopProduct shopProduct = shopProduct(900L, masterProduct, BigDecimal.TEN, 2);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(shopProduct));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().getFirst().matchedCollectionItemStatus()).isEqualTo("WANTED");
        assertThat(response.recommendations().getFirst().recommendationReason().code()).isEqualTo("COLLECTION_ITEM_WANTED");
    }

    @Test
    void hiddenShopProductIsNotRecommended() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        ShopProduct hidden = shopProduct(
                900L,
                masterProduct,
                BigDecimal.TEN,
                2,
                ShopProductCommercialStatus.AVAILABLE,
                PhysicalCondition.NEW,
                false
        );
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(hidden));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void shopProductWithoutStockIsNotRecommended() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        ShopProduct noStock = shopProduct(900L, masterProduct, BigDecimal.TEN, 0);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(noStock));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void reservedSoldAndHiddenProductsAreNotRecommended() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(
                shopProduct(900L, masterProduct, BigDecimal.TEN, 2, ShopProductCommercialStatus.RESERVED, PhysicalCondition.NEW, true),
                shopProduct(901L, masterProduct, BigDecimal.TEN, 2, ShopProductCommercialStatus.SOLD, PhysicalCondition.NEW, true),
                shopProduct(902L, masterProduct, BigDecimal.TEN, 2, ShopProductCommercialStatus.HIDDEN, PhysicalCondition.NEW, true)
        ));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void deletedShopOrMasterProductIsNotRecommended() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        ShopProduct deletedShopProduct = shopProduct(900L, masterProduct, BigDecimal.TEN, 2);
        ReflectionTestUtils.setField(deletedShopProduct.getShop(), "deletedAt", Instant.now());
        MasterProduct deletedMasterProduct = masterProduct(201L, category, "Dragon Ball 2", "2");
        ReflectionTestUtils.setField(deletedMasterProduct, "deletedAt", Instant.now());
        ShopProduct deletedMasterShopProduct = shopProduct(901L, deletedMasterProduct, BigDecimal.TEN, 2);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(
                item,
                collectionItem(301L, deletedMasterProduct, CollectionItemStatus.MISSING)
        ));
        whenCandidatesReturn(List.of(deletedShopProduct, deletedMasterShopProduct));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void ownedItemsAreNotRecommended() {
        CollectionItem ownedItem = collectionItem(300L, masterProduct, CollectionItemStatus.OWNED);
        ShopProduct shopProduct = shopProduct(900L, masterProduct, BigDecimal.TEN, 2);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(ownedItem));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).isEmpty();
        verify(shopProductRepository, never()).findRecommendationCandidates(anySet(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void categoryFilterWorks() {
        MasterProduct figure = masterProduct(201L, figureCategory, "Goku Figure", null);
        when(productCategoryRepository.findByCodeAndDeletedAtIsNull("MANGA_COMIC")).thenReturn(Optional.of(category));
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(
                collectionItem(300L, masterProduct, CollectionItemStatus.MISSING),
                collectionItem(301L, figure, CollectionItemStatus.MISSING)
        ));
        whenCandidatesReturn(List.of(
                shopProduct(900L, masterProduct, BigDecimal.TEN, 2),
                shopProduct(901L, figure, BigDecimal.TEN, 2)
        ));

        var response = recommendationService.myRecommendations(authenticatedUser(), "manga_comic", null, null, null, null);

        assertThat(response.recommendations()).extracting("categoryCode").containsExactly("MANGA_COMIC");
    }

    @Test
    void maxPriceFilterWorksWithoutCurrencyConversion() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(
                shopProduct(900L, masterProduct, new BigDecimal("9.99"), 2),
                shopProduct(901L, masterProduct, new BigDecimal("20.00"), 2)
        ));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, "10.00", null, null, null);

        assertThat(response.recommendations()).extracting("shopProductId").containsExactly(900L);
    }

    @Test
    void physicalConditionFilterWorks() {
        CollectionItem item = collectionItem(300L, masterProduct, CollectionItemStatus.MISSING);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(item));
        whenCandidatesReturn(List.of(
                shopProduct(900L, masterProduct, BigDecimal.TEN, 2, ShopProductCommercialStatus.AVAILABLE, PhysicalCondition.NEW, true),
                shopProduct(901L, masterProduct, BigDecimal.TEN, 2, ShopProductCommercialStatus.AVAILABLE, PhysicalCondition.GOOD, true)
        ));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, "good", null);

        assertThat(response.recommendations()).extracting("shopProductId").containsExactly(901L);
        assertThat(response.recommendations().getFirst().physicalCondition()).isEqualTo("GOOD");
    }

    @Test
    void invalidFiltersReturnControlledError() {
        assertThatThrownBy(() -> recommendationService.myRecommendations(
                authenticatedUser(),
                null,
                "-1",
                null,
                null,
                null
        )).isInstanceOf(InvalidCatalogFilterException.class);

        assertThatThrownBy(() -> recommendationService.myRecommendations(
                authenticatedUser(),
                null,
                null,
                "EURO",
                null,
                null
        )).isInstanceOf(InvalidCatalogFilterException.class);

        assertThatThrownBy(() -> recommendationService.myRecommendations(
                authenticatedUser(),
                null,
                null,
                null,
                "sealed",
                null
        )).isInstanceOf(InvalidCatalogFilterException.class);
    }

    @Test
    void summaryReturnsExpectedCounters() {
        MasterProduct wantedProduct = masterProduct(201L, category, "One Piece 1", "1");
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(
                collectionItem(300L, masterProduct, CollectionItemStatus.MISSING),
                collectionItem(301L, wantedProduct, CollectionItemStatus.WANTED)
        ));
        whenCandidatesReturn(List.of(
                shopProduct(900L, masterProduct, BigDecimal.TEN, 2),
                shopProduct(901L, wantedProduct, BigDecimal.ONE, 1)
        ));

        var response = recommendationService.mySummary(authenticatedUser(), null, null, null, null, null);

        assertThat(response.missingCollectionItems()).isEqualTo(1);
        assertThat(response.wantedCollectionItems()).isEqualTo(1);
        assertThat(response.recommendedProducts()).isEqualTo(2);
        assertThat(response.matchedShops()).isEqualTo(1);
        assertThat(response.matchedCategoryCodes()).containsExactly("MANGA_COMIC");
    }

    @Test
    void sameShopProductIsNotDuplicatedAndMissingWinsOverWanted() {
        Collection missingCollection = collection(100L, "Missing list");
        Collection wantedCollection = collection(101L, "Wanted list");
        CollectionItem wanted = collectionItem(300L, wantedCollection, masterProduct, CollectionItemStatus.WANTED);
        CollectionItem missing = collectionItem(301L, missingCollection, masterProduct, CollectionItemStatus.MISSING);
        ShopProduct shopProduct = shopProduct(900L, masterProduct, BigDecimal.TEN, 2);
        when(collectionItemRepository.findRecommendationItemsForUser(42L, targetStatuses())).thenReturn(List.of(wanted, missing));
        whenCandidatesReturn(List.of(shopProduct, shopProduct));

        var response = recommendationService.myRecommendations(authenticatedUser(), null, null, null, null, null);

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().getFirst().matchedCollectionId()).isEqualTo(100L);
        assertThat(response.recommendations().getFirst().matchedCollectionItemStatus()).isEqualTo("MISSING");
    }

    private void whenCandidatesReturn(List<ShopProduct> candidates) {
        when(shopProductRepository.findRecommendationCandidates(anySet(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(candidates);
    }

    private AuthenticatedUser authenticatedUser() {
        return AuthenticatedUser.from(user);
    }

    private Set<CollectionItemStatus> targetStatuses() {
        return Set.of(CollectionItemStatus.MISSING, CollectionItemStatus.WANTED);
    }

    private User user(Long id) {
        User user = User.register("alice@example.com", "$2a$10$test-password-hash", "Alice", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Collection collection(Long id, String name) {
        return withId(Collection.create(user, name, null, CollectionVisibility.PRIVATE, category), id);
    }

    private Shop shop(Long id, String name) {
        return withId(Shop.create(user, name, null, null, null, null, "EUR", 48, null), id);
    }

    private MasterProduct masterProduct(Long id, ProductCategory category, String name, String volumeNumber) {
        return withId(MasterProduct.create(
                name,
                null,
                category,
                "Dragon Ball",
                "Tankobon",
                volumeNumber,
                "Planeta",
                null,
                null,
                null,
                null,
                null,
                "es",
                false,
                List.of("ES"),
                "https://example.test/cover.jpg",
                Map.of(),
                user.getId()
        ), id);
    }

    private CollectionItem collectionItem(Long id, MasterProduct masterProduct, CollectionItemStatus status) {
        return collectionItem(id, collection, masterProduct, status);
    }

    private CollectionItem collectionItem(
            Long id,
            Collection collection,
            MasterProduct masterProduct,
            CollectionItemStatus status
    ) {
        return withId(CollectionItem.create(
                collection,
                masterProduct,
                status,
                null,
                null,
                null,
                null,
                null,
                user.getId()
        ), id);
    }

    private ShopProduct shopProduct(Long id, MasterProduct masterProduct, BigDecimal price, int stockQuantity) {
        return shopProduct(id, masterProduct, price, stockQuantity, ShopProductCommercialStatus.AVAILABLE, PhysicalCondition.NEW, true);
    }

    private ShopProduct shopProduct(
            Long id,
            MasterProduct masterProduct,
            BigDecimal price,
            int stockQuantity,
            ShopProductCommercialStatus commercialStatus,
            PhysicalCondition physicalCondition,
            boolean visible
    ) {
        return withId(ShopProduct.create(
                shop,
                masterProduct,
                price,
                "EUR",
                stockQuantity,
                commercialStatus,
                physicalCondition,
                visible,
                null,
                null,
                null,
                user.getId()
        ), id);
    }

    private <T> T withId(T target, Long id) {
        ReflectionTestUtils.setField(target, "id", id);
        return target;
    }
}
