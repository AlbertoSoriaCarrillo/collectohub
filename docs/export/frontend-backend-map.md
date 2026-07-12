# Frontend to backend map

This map follows actual HTTP calls made by routed Angular components through
the services in `frontend/src/app/core`. It contains 61 relationships: 56 API
calls and 5 routes with no direct API call. Local-only calls such as
`AuthService.currentUser()`, logout, translation and error formatting are not
listed as backend relationships.

## Public and authentication

| Route/component | Angular service | Backend operation | Status | Notes |
| --- | --- | --- | --- | --- |
| `/home` - `HomeComponent` | None | No direct API call | `MVP1_VISIBLE` | Static/localized product content. |
| `/login` - `LoginComponent` | `AuthService` | `POST /api/auth/login` | `MVP1_VISIBLE` | Saves returned session. |
| `/register` - `RegisterComponent` | `AuthService` | `POST /api/auth/register` | `MVP1_VISIBLE` | `confirmPassword` stays frontend-only. |
| `/profile` - `ProfileComponent` | `AuthService` | `GET /api/users/me` | `MVP1_VISIBLE` | Refreshes current profile. |

## Catalog

| Route/component | Angular service | Backend operation | Status |
| --- | --- | --- | --- |
| `/catalog` - `CatalogListComponent` | `CatalogService` | `GET /api/master-products` | `MVP1_VISIBLE` |
| `/catalog` - `CatalogListComponent` | `CatalogService` | `GET /api/product-categories` | `MVP1_VISIBLE` |
| `/catalog/new` - `MasterProductCreateComponent` | `CatalogService` | `GET /api/product-categories` | `MVP1_VISIBLE` |
| `/catalog/new` - `MasterProductCreateComponent` | `CatalogService` | `POST /api/master-products` | `MVP1_VISIBLE` |
| `/catalog/:id` - `MasterProductDetailComponent` | `CatalogService` | `GET /api/master-products/{id}` | `MVP1_VISIBLE` |

`CatalogService.updateMasterProduct()` exists for the backend `PUT` operation,
but no current routed component calls it.

## Editorial catalog

| Route/component | Angular service | Backend operation | Status |
| --- | --- | --- | --- |
| `/catalog/editorial` - `EditorialSearchComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/search` | `MVP2_VISIBLE` |
| `/catalog/editorial/series/:seriesId` - `EditorialSeriesDetailComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/series/{seriesId}/detail` | `MVP2_VISIBLE` |
| `/catalog/editorial/items/:itemId` - `EditorialItemDetailComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/items/{itemId}/detail` | `MVP2_VISIBLE` |
| `/catalog/editorial/editions/:editionId` - `EditorialEditionDetailComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/editions/{editionId}/detail` | `MVP2_VISIBLE` |

`GET /api/catalog/editorial/items/{itemId}/detail` now returns editions,
creators and relationships; the item detail renders all three when present.
`EditorialCatalogService.getMasterProductLink()` models the ADMIN endpoint for
future use, but no public route or component invokes or exposes it.

## Collections

| Route/component | Angular service | Backend operation | Notes |
| --- | --- | --- | --- |
| `/collections` - `MyCollectionsComponent` | `CollectionService` | `GET /api/collections/my` | List/filter. |
| `/collections` - `MyCollectionsComponent` | `CollectionService` | `DELETE /api/collections/{collectionId}` | Owner action. |
| `/collections` - `MyCollectionsComponent` | `CatalogService` | `GET /api/product-categories` | Filter options. |
| `/collections/new` - `CollectionCreateComponent` | `CatalogService` | `GET /api/product-categories` | Form options. |
| `/collections/new` - `CollectionCreateComponent` | `CollectionService` | `POST /api/collections` | Create. |
| `/collections/:collectionId/edit` - `CollectionEditComponent` | `CollectionService` | `GET /api/collections/{collectionId}` | Load. |
| `/collections/:collectionId/edit` - `CollectionEditComponent` | `CatalogService` | `GET /api/product-categories` | Form options. |
| `/collections/:collectionId/edit` - `CollectionEditComponent` | `CollectionService` | `PUT /api/collections/{collectionId}` | Update. |
| `/collections/:collectionId/items/new` - `CollectionItemCreateComponent` | `CatalogService` | `GET /api/master-products` | Candidate search. |
| `/collections/:collectionId/items/new` - `CollectionItemCreateComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/search` | Editorial item/edition search. |
| `/collections/:collectionId/items/new` - `CollectionItemCreateComponent` | `CollectionService` | `POST /api/collections/{collectionId}/items` | Add item. |
| `/collections/:collectionId/items/:itemId/edit` - `CollectionItemEditComponent` | `CollectionService` | `GET /api/collections/{collectionId}/items` | Select item. |
| `/collections/:collectionId/items/:itemId/edit` - `CollectionItemEditComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/search` | Optional editorial reference replacement. |
| `/collections/:collectionId/items/:itemId/edit` - `CollectionItemEditComponent` | `CollectionService` | `PUT /api/collections/{collectionId}/items/{itemId}` | Update item. |
| `/collections/:collectionId` - `CollectionDetailComponent` | `CollectionService` | `GET /api/collections/{collectionId}` | Load readable collection. |
| `/collections/:collectionId` - `CollectionDetailComponent` | `CollectionService` | `GET /api/collections/{collectionId}/items` | Load items. |
| `/collections/:collectionId` - `CollectionDetailComponent` | `AuthService` | `GET /api/users/me` | Conditional owner resolution. |
| `/collections/:collectionId` - `CollectionDetailComponent` | `CollectionService` | `DELETE /api/collections/{collectionId}/items/{itemId}` | Owner action. |

Legacy collection calls remain `MVP1_VISIBLE`; the two editorial search calls
are `MVP2_VISIBLE`.

## Wanted/Recommendations

| Route/component | Angular service | Backend operation | Status |
| --- | --- | --- | --- |
| `/wanted` - `RecommendationsComponent` | `RecommendationService` | `GET /api/recommendations/my` | `MVP1_VISIBLE` |
| `/wanted` - `RecommendationsComponent` | `RecommendationService` | `GET /api/recommendations/my/summary` | `MVP1_VISIBLE` |
| `/wanted` - `RecommendationsComponent` | `CatalogService` | `GET /api/product-categories` | `MVP1_VISIBLE` |

The current screen presents collector matches and hides commercial filters and
links, even though the backend calculation still reads future inventory data.

## Shops and inventory

| Route/component | Angular service | Backend operation |
| --- | --- | --- |
| `/shops` - `MyShopsComponent` | `ShopService` | `GET /api/shops/my` |
| `/shops/new` - `ShopCreateComponent` | `ShopService` | `POST /api/shops` |
| `/shops/:shopId/inventory` - `ShopInventoryComponent` | `ShopService` | `GET /api/shops/{shopId}` |
| `/shops/:shopId/inventory` - `ShopInventoryComponent` | `InventoryService` | `GET /api/shops/{shopId}/products/my` |
| `/shops/:shopId/inventory/new` - `ShopProductCreateComponent` | `CatalogService` | `GET /api/master-products` |
| `/shops/:shopId/inventory/new` - `ShopProductCreateComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/search` |
| `/shops/:shopId/inventory/new` - `ShopProductCreateComponent` | `InventoryService` | `POST /api/shops/{shopId}/products` |
| `/shops/:shopId/inventory/:shopProductId/edit` - `ShopProductEditComponent` | `InventoryService` | `GET /api/shops/{shopId}/products/my` |
| `/shops/:shopId/inventory/:shopProductId/edit` - `ShopProductEditComponent` | `EditorialCatalogService` | `GET /api/catalog/editorial/search` |
| `/shops/:shopId/inventory/:shopProductId/edit` - `ShopProductEditComponent` | `InventoryService` | `PUT /api/shops/{shopId}/products/{shopProductId}` |
| `/shops/:id` - `ShopDetailComponent` | `ShopService` | `GET /api/shops/{shopId}` |
| `/shops/:id` - `ShopDetailComponent` | `InventoryService` | `GET /api/shops/{shopId}/products` |
| `/shops/:id` - `ShopDetailComponent` | `ShopService` | `GET /api/shops/my` (conditional membership check) |
| `/shop-products/:shopProductId` - `ShopProductDetailComponent` | `InventoryService` | `GET /api/shop-products/{shopProductId}` |
| `/shop-products/:shopProductId` - `ShopProductDetailComponent` | `ShopService` | `GET /api/shops/{shopId}` |
| `/shop-products/:shopProductId` - `ShopProductDetailComponent` | `ReservationService` | `POST /api/reservations` |

Inventory create/edit supports legacy and editorial references. Routes remain
outside the primary navigation. `ShopService.updateShop()` is implemented but
no current routed component calls it.

## Reservations

| Route/component | Angular service | Backend operation |
| --- | --- | --- |
| `/shops/:shopId/reservations` - `ShopReservationsComponent` | `ReservationService` | `GET /api/shops/{shopId}/reservations` |
| `/shops/:shopId/reservations` - `ShopReservationsComponent` | `ReservationService` | `PUT /api/shops/{shopId}/reservations/{reservationId}/status` |
| `/reservations` - `MyReservationsComponent` | `ReservationService` | `GET /api/reservations/my` |
| `/reservations` - `MyReservationsComponent` | `ReservationService` | `PUT /api/reservations/{reservationId}/cancel` |
| `/reservations/:reservationId` - `ReservationDetailComponent` | `ReservationService` | `GET /api/reservations/{reservationId}` |
| `/reservations/:reservationId` - `ReservationDetailComponent` | `ReservationService` | `PUT /api/reservations/{reservationId}/cancel` |
| `/reservations/:reservationId` - `ReservationDetailComponent` | `AuthService` | `GET /api/users/me` (conditional) |

All rows in this section are `LEGACY_FUTURE`.

## Redirects and fallback

## MVP3 Admin Editorial

`EditorialAdminService` powers all editorial routes for `ADMIN` or
`EDITORIAL_ADMIN`. Publishers, franchises,
series, items and editions use their existing search/get/create/update API
operations; creators and credits also use DELETE; relationships use
GET/POST/PUT/DELETE by source item; master-product-links use search/get/create/
update, verify, reject, backfill and editorial legacy bridge lookup. No endpoint
or backend contract was created for these screens.

`/admin/editorial/data-quality` - `AdminDataQualityComponent` uses
`EditorialAdminService.getEditorialDataQualityReport()` and
`GET /api/catalog/admin/data-quality/report` as a read-only editorial report.

| Route | Component | Backend relationship | Status |
| --- | --- | --- | --- |
| `/` | Redirect | No direct API call | `REDIRECT` |
| `/dashboard` | Redirect | No direct API call | `REDIRECT` |
| `/recommendations` | Redirect | No direct API call | `REDIRECT` |
| `/**` | Fallback redirect | No direct API call | `REDIRECT` |
