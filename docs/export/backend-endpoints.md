# Backend REST endpoints

Source of truth reviewed: Spring MVC controllers, `SecurityConfig`, request and
response DTOs, and application-service authorization. This inventory contains
82 application endpoints. Framework-generated Swagger/OpenAPI paths are public
but are not counted as application endpoints.

Authentication is stateless JWT. `PROTECTED` requires a valid Bearer token;
`PUBLIC_OR_OWNER` permits anonymous reads only when the resource is public.
Unless a role is named, authorization is based on authentication and resource
ownership or membership.

## Health

## Editorial data quality

| Method and path | Access | Request | Response | Status | Purpose |
| --- | --- | --- | --- | --- | --- |
| `GET /api/catalog/admin/data-quality/report` | `ADMIN` | Optional `scope`; `limit` 1..200 | `EditorialDataQualityReportResponse` | `MVP3_ADMIN_PARTIAL` | Read-only exact duplicate report; no fix, merge or deletion. |

| Method and path | Access | Request | Response | Status | Purpose |
| --- | --- | --- | --- | --- | --- |
| `GET /api/health` | Public | None | `HealthResponse` | `TECHNICAL` | Backend liveness and service identity. |

## Auth

Controller: `AuthController`.

| Method and path | Access | Request | Response | Status | Main errors |
| --- | --- | --- | --- | --- | --- |
| `POST /api/auth/register` | Public | `RegisterRequest` | `AuthResponse` | `MVP1_VISIBLE` | 400 validation; 409 duplicate email. |
| `POST /api/auth/login` | Public | `LoginRequest` | `AuthResponse` | `MVP1_VISIBLE` | 400 validation; 401 invalid credentials. |

Both responses include public user data, roles, access token and refresh token;
they never expose `password_hash`.

## Users/Profile

| Method and path | Controller | Access | Response | Status | Purpose |
| --- | --- | --- | --- | --- | --- |
| `GET /api/users/me` | `UserController` | Authenticated user | `UserMeResponse` | `MVP1_VISIBLE` | Current profile and global roles. |

## Catalog

| Method and path | Controller | Access/permission | Request or query | Response | Status |
| --- | --- | --- | --- | --- | --- |
| `GET /api/product-categories` | `ProductCategoryController` | Public | None | `List<ProductCategoryResponse>` | `MVP1_VISIBLE` |
| `GET /api/master-products` | `MasterProductController` | Public | `categoryCode`, `name`, `franchise`, `collectionName`, `language`, `status` | `List<MasterProductResponse>` | `MVP1_VISIBLE` |
| `GET /api/master-products/{id}` | `MasterProductController` | Public | Path `id` | `MasterProductResponse` | `MVP1_VISIBLE` |
| `POST /api/master-products` | `MasterProductController` | `ADMIN` or `SHOP_OWNER` | `CreateMasterProductRequest` | `MasterProductResponse` | `MVP1_VISIBLE` |
| `PUT /api/master-products/{id}` | `MasterProductController` | `ADMIN` or `SHOP_OWNER` | `UpdateMasterProductRequest` | `MasterProductResponse` | `MVP1_VISIBLE` |

Writes validate categories and logical duplicates. Expected errors include 400
for validation, 403 for insufficient authority, 404 for missing resources and
409 for duplicate ISBN, EAN or logical identity.

## Editorial catalog foundations

Status: `MVP2_FOUNDATION`. All list operations return a stable `PageResponse`
and expose only `ACTIVE`, non-deleted records by default. An authenticated
`ADMIN or EDITORIAL_ADMIN` may filter by `recordStatus` and read non-public detail. Writes require
`ADMIN or EDITORIAL_ADMIN`; `SHOP_OWNER` retains only its legacy `/api/master-products` permission.

| Method and path | Controller | Access/permission | Request or query | Response |
| --- | --- | --- | --- | --- |
| `GET /api/catalog/publishers` | `PublisherController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | `q`, `recordStatus`, `page`, `size`, `sort` | `PageResponse<PublisherResponse>` |
| `GET /api/catalog/publishers/{id}` | `PublisherController` | Public ACTIVE or ADMIN or EDITORIAL_ADMIN | Path `id` | `PublisherResponse` |
| `POST /api/catalog/publishers` | `PublisherController` | `ADMIN or EDITORIAL_ADMIN` | `CreatePublisherRequest` | `PublisherResponse` |
| `PUT /api/catalog/publishers/{id}` | `PublisherController` | `ADMIN or EDITORIAL_ADMIN` | `UpdatePublisherRequest` | `PublisherResponse` |
| `GET /api/catalog/franchises` | `CatalogFranchiseController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | `q`, `recordStatus`, `page`, `size`, `sort` | `PageResponse<CatalogFranchiseResponse>` |
| `GET /api/catalog/franchises/{id}` | `CatalogFranchiseController` | Public ACTIVE or ADMIN or EDITORIAL_ADMIN | Path `id` | `CatalogFranchiseResponse` |
| `POST /api/catalog/franchises` | `CatalogFranchiseController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogFranchiseRequest` | `CatalogFranchiseResponse` |
| `PUT /api/catalog/franchises/{id}` | `CatalogFranchiseController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogFranchiseRequest` | `CatalogFranchiseResponse` |
| `GET /api/catalog/series` | `CatalogSeriesController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | `q`, `franchiseId`, `type`, `publicationStatus`, `publisherId`, `language`, `country`, `recordStatus`, pagination | `PageResponse<CatalogSeriesResponse>` |
| `GET /api/catalog/series/{id}` | `CatalogSeriesController` | Public ACTIVE or ADMIN or EDITORIAL_ADMIN | Path `id` | `CatalogSeriesResponse` |
| `POST /api/catalog/series` | `CatalogSeriesController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogSeriesRequest` | `CatalogSeriesResponse` |
| `PUT /api/catalog/series/{id}` | `CatalogSeriesController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogSeriesRequest` | `CatalogSeriesResponse` |
| `GET /api/catalog/series/{seriesId}/items` | `CatalogItemController` | Public ACTIVE chain; ADMIN or EDITORIAL_ADMIN status filter | Item filters and pagination | `PageResponse<CatalogItemResponse>` |
| `GET /api/catalog/items/{id}` | `CatalogItemController` | Public ACTIVE chain or ADMIN or EDITORIAL_ADMIN | Path `id` | `CatalogItemResponse` |
| `POST /api/catalog/series/{seriesId}/items` | `CatalogItemController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogItemRequest` | `CatalogItemResponse` |
| `PUT /api/catalog/items/{id}` | `CatalogItemController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogItemRequest` | `CatalogItemResponse` |
| `GET /api/catalog/items/{itemId}/editions` | `CatalogItemEditionController` | Public ACTIVE chain; ADMIN or EDITORIAL_ADMIN status filter | Edition filters and pagination | `PageResponse<CatalogItemEditionResponse>` |
| `GET /api/catalog/editions/{id}` | `CatalogItemEditionController` | Public ACTIVE chain or ADMIN or EDITORIAL_ADMIN | Path `id` | `CatalogItemEditionResponse` |
| `POST /api/catalog/items/{itemId}/editions` | `CatalogItemEditionController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogItemEditionRequest` | `CatalogItemEditionResponse` |
| `PUT /api/catalog/editions/{id}` | `CatalogItemEditionController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogItemEditionRequest` | `CatalogItemEditionResponse` |
| `GET /api/catalog/master-product-links` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | Bridge filters and pagination | `PageResponse<MasterProductCatalogLinkResponse>` |
| `GET /api/catalog/master-product-links/{id}` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | Path `id` | `MasterProductCatalogLinkResponse` |
| `POST /api/catalog/master-product-links` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | `CreateMasterProductCatalogLinkRequest` | `MasterProductCatalogLinkResponse` |
| `PUT /api/catalog/master-product-links/{id}` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateMasterProductCatalogLinkRequest` | `MasterProductCatalogLinkResponse` |
| `PUT /api/catalog/master-product-links/{id}/verify` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | Path `id` | `MasterProductCatalogLinkResponse` |
| `PUT /api/catalog/master-product-links/{id}/reject` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | Path `id` | `MasterProductCatalogLinkResponse` |
| `POST /api/catalog/master-product-links/backfill` | `MasterProductCatalogLinkController` | `ADMIN or EDITORIAL_ADMIN` | None | `BackfillMasterProductCatalogLinksResponse` |
| `GET /api/catalog/editorial/search` | `EditorialCatalogFacadeController` | Public ACTIVE; link results require `ADMIN or EDITORIAL_ADMIN` | Editorial filters and pagination | `PageResponse<EditorialCatalogSearchItemResponse>` |
| `GET /api/catalog/editorial/series/{seriesId}/detail` | `EditorialCatalogFacadeController` | Public ACTIVE chain | Path `seriesId` | `EditorialCatalogSeriesDetailResponse` |
| `GET /api/catalog/editorial/items/{itemId}/detail` | `EditorialCatalogFacadeController` | Public ACTIVE chain | Path `itemId` | `EditorialCatalogItemDetailResponse` with editions, creators and relationships |
| `GET /api/catalog/editorial/editions/{editionId}/detail` | `EditorialCatalogFacadeController` | Public ACTIVE chain | Path `editionId` | `EditorialCatalogEditionDetailResponse` |
| `GET /api/catalog/editorial/master-products/{masterProductId}/link` | `EditorialCatalogFacadeController` | `ADMIN or EDITORIAL_ADMIN` | Path `masterProductId` | `EditorialLegacyBridgeResponse` |
| `GET /api/catalog/creators` | `CreatorController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | Creator filters and pagination | `PageResponse<CreatorResponse>` |
| `GET /api/catalog/creators/{id}` | `CreatorController` | Public ACTIVE or ADMIN or EDITORIAL_ADMIN | Path `id`; optional `recordStatus` | `CreatorResponse` |
| `POST /api/catalog/creators` | `CreatorController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCreatorRequest` | `CreatorResponse` |
| `PUT /api/catalog/creators/{id}` | `CreatorController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCreatorRequest` | `CreatorResponse` |
| `DELETE /api/catalog/creators/{id}` | `CreatorController` | `ADMIN or EDITORIAL_ADMIN` | Path `id` | No content |
| `GET /api/catalog/items/{itemId}/creators` | `CatalogItemCreatorController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | Path `itemId`; optional `recordStatus` | `List<CatalogItemCreatorResponse>` |
| `POST /api/catalog/items/{itemId}/creators` | `CatalogItemCreatorController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogItemCreatorRequest` | `CatalogItemCreatorResponse` |
| `PUT /api/catalog/items/{itemId}/creators/{creditId}` | `CatalogItemCreatorController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogItemCreatorRequest` | `CatalogItemCreatorResponse` |
| `DELETE /api/catalog/items/{itemId}/creators/{creditId}` | `CatalogItemCreatorController` | `ADMIN or EDITORIAL_ADMIN` | Path `itemId`, `creditId` | No content |
| `GET /api/catalog/items/{itemId}/relationships` | `CatalogItemRelationshipController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | Path `itemId`; optional `recordStatus` | `List<CatalogItemRelationshipResponse>` |
| `POST /api/catalog/items/{itemId}/relationships` | `CatalogItemRelationshipController` | `ADMIN or EDITORIAL_ADMIN` | `CreateCatalogItemRelationshipRequest` | `CatalogItemRelationshipResponse` |
| `GET /api/catalog/items/{itemId}/relationships/{relationshipId}` | `CatalogItemRelationshipController` | Public ACTIVE; ADMIN or EDITORIAL_ADMIN status filter | Path `itemId`, `relationshipId`; optional `recordStatus` | `CatalogItemRelationshipResponse` |
| `PUT /api/catalog/items/{itemId}/relationships/{relationshipId}` | `CatalogItemRelationshipController` | `ADMIN or EDITORIAL_ADMIN` | `UpdateCatalogItemRelationshipRequest` | `CatalogItemRelationshipResponse` |
| `DELETE /api/catalog/items/{itemId}/relationships/{relationshipId}` | `CatalogItemRelationshipController` | `ADMIN or EDITORIAL_ADMIN` | Path `itemId`, `relationshipId` | No content |

Writes may return 400 for validation/lifecycle rules, 401 without a token, 403
without `ADMIN or EDITORIAL_ADMIN`, 404 for missing dependencies, and 409 for duplicate identity.
Public creator and relationship reads expose only ACTIVE, non-deleted records
with public item chains. The editorial item detail aggregates active editions,
public creator credits and public relationships without adding separate
frontend-facing facade endpoints.

## Collections

Controller: `CollectionController`.

`catalogItemId` is the canonical editorial reference and
`catalogItemEditionId` is optional. `masterProductId` remains supported for
legacy compatibility. `CollectionItemResponse.referenceKind` is calculated as
`DIRECT_CATALOG`, `VERIFIED_BRIDGE` or `LEGACY_UNRESOLVED`; it is not stored.
Public readers receive editorial references and status, but `notes` and
`acquiredAt` are included only for the collection owner. Manual items are not
supported yet.

| Method and path | Access/permission | Request | Response | Status |
| --- | --- | --- | --- | --- |
| `POST /api/collections` | Authenticated user | `CreateCollectionRequest` | `CollectionResponse` | `MVP1_VISIBLE` |
| `GET /api/collections/my` | Owner; query `visibility`, `categoryCode` | None | `List<CollectionResponse>` | `MVP1_VISIBLE` |
| `GET /api/collections/{collectionId}` | Public collection or owner | None | `CollectionResponse`; item personal fields owner-only | `MVP1_VISIBLE` |
| `PUT /api/collections/{collectionId}` | Owner | `UpdateCollectionRequest` | `CollectionResponse` | `MVP1_VISIBLE` |
| `DELETE /api/collections/{collectionId}` | Owner | None | No content | `MVP1_VISIBLE` |
| `POST /api/collections/{collectionId}/items` | Owner | Manual metadata, canonical `catalogItemId`, optional edition, or legacy `masterProductId` | `CollectionItemResponse` | `MVP1_VISIBLE` |
| `GET /api/collections/{collectionId}/items` | Public collection or owner; optional `q`, repeated `status`, repeated `referenceKind`, `seriesId`, `sort` | None | `List<CollectionItemResponse>`; personal fields owner-only | `MVP1_VISIBLE` |
| `PUT /api/collections/{collectionId}/items/{itemId}` | Owner | Manual metadata or canonical editorial/legacy reference | `CollectionItemResponse` | `MVP1_VISIBLE` |
| `PUT /api/collections/{collectionId}/items/{itemId}/catalog-reference` | Owner | `LinkManualCollectionItemRequest` | `CollectionItemResponse` | `MVP1_VISIBLE` |
| `GET /api/collections/{collectionId}/series/{seriesId}/progress` | Authenticated collection owner | None | Calculated `CollectionSeriesProgressResponse`, without private item data | `MVP1_VISIBLE` |
| `GET /api/collections/{collectionId}/series-progress` | Authenticated collection owner | None | Aggregated `List<CollectionSeriesProgressSummaryResponse>` for participating series, without private item data | `MVP4_VISIBLE` |
| `DELETE /api/collections/{collectionId}/items/{itemId}` | Owner | None | No content | `MVP1_VISIBLE` |

Delete operations are logical deletes. Private resources are not exposed to
other users; normal failures are 400 validation, 401 unauthenticated, 403
ownership violations and 404 missing or unreadable resources.

## Wanted/Recommendations

Controller: `RecommendationController`. Both operations are protected and use
the authenticated user's `MISSING` and `WANTED` collection items.

| Method and path | Query filters | Response | Status |
| --- | --- | --- | --- |
| `GET /api/recommendations/my` | `categoryCode`, `maxPrice`, `currency`, `physicalCondition`, `shopId` | `UserRecommendationResponse` | `MVP1_VISIBLE` |
| `GET /api/recommendations/my/summary` | Same filters | `UserRecommendationSummaryResponse` | `MVP1_VISIBLE` |

Recommendations are calculated; there is no recommendation table.

## Shops

Controller: `ShopController`. These operations are implemented but belong to a
later product phase.

| Method and path | Access/permission | Request | Response | Status |
| --- | --- | --- | --- | --- |
| `POST /api/shops` | Authenticated user | `CreateShopRequest` | `ShopResponse` | `LEGACY_FUTURE` |
| `GET /api/shops/my` | Active shop member | None | `List<ShopResponse>` | `LEGACY_FUTURE` |
| `GET /api/shops/{shopId}` | Public | None | `ShopResponse` | `LEGACY_FUTURE` |
| `GET /api/shops/{shopId}/members` | Shop `OWNER` or `MANAGER` | None | `List<ShopMemberResponse>` | `MVP5_PARTIAL` |
| `POST /api/shops/{shopId}/members` | Shop `OWNER` | `AddShopMemberRequest` | `ShopMemberResponse` | `MVP5_PARTIAL` |
| `PUT /api/shops/{shopId}` | Shop `OWNER` or `MANAGER` | `UpdateShopRequest` | `ShopResponse` | `LEGACY_FUTURE` |

Creating a shop also creates an internal `OWNER` membership and grants the
global `SHOP_OWNER` role if absent.

## Inventory

| Method and path | Controller | Access/permission | Request/query | Response | Status |
| --- | --- | --- | --- | --- | --- |
| `POST /api/shops/{shopId}/products` | `ShopInventoryController` | `OWNER` or `MANAGER` | `CreateShopProductRequest` | `ShopProductResponse` | `LEGACY_FUTURE` |
| `PUT /api/shops/{shopId}/products/{shopProductId}` | `ShopInventoryController` | `OWNER` or `MANAGER` | `UpdateShopProductRequest` | `ShopProductResponse` | `LEGACY_FUTURE` |
| `GET /api/shops/{shopId}/products/my` | `ShopInventoryController` | Active shop member | None | `List<ShopProductResponse>` | `LEGACY_FUTURE` |
| `GET /api/shops/{shopId}/products` | `ShopInventoryController` | Public | Product/category/name/franchise/collection/condition/status filters | `List<ShopProductResponse>` | `LEGACY_FUTURE` |
| `GET /api/shop-products/{shopProductId}` | `PublicShopProductController` | Public visible entry | None | `ShopProductResponse` | `LEGACY_FUTURE` |

## Reservations

Controller: `ReservationController`. Reservations do not process payment or
guarantee stock locking.

| Method and path | Access/permission | Request/query | Response | Status |
| --- | --- | --- | --- | --- |
| `POST /api/reservations` | Authenticated user | `CreateReservationRequest` | `ReservationResponse` | `LEGACY_FUTURE` |
| `GET /api/reservations/my` | Reservation owner | `status`, `shopId` | `List<ReservationResponse>` | `LEGACY_FUTURE` |
| `GET /api/reservations/{reservationId}` | Owner or shop `OWNER`/`MANAGER` | None | `ReservationResponse` | `LEGACY_FUTURE` |
| `GET /api/shops/{shopId}/reservations` | Shop `OWNER` or `MANAGER` | `status`, `userId`, `shopProductId` | `List<ReservationResponse>` | `LEGACY_FUTURE` |
| `PUT /api/shops/{shopId}/reservations/{reservationId}/status` | Shop `OWNER` or `MANAGER` | `UpdateReservationStatusRequest` | `ReservationResponse` | `LEGACY_FUTURE` |
| `PUT /api/reservations/{reservationId}/cancel` | Reservation owner | None | `ReservationResponse` | `LEGACY_FUTURE` |

## Security and generated routes

`SecurityConfig` also permits `/v3/api-docs/**`, `/swagger-ui/**` and
`/swagger-ui.html`. Those routes are supplied by Springdoc, not declared by a
CollectoHub controller, and are intentionally excluded from the CSV count.
All protected failures use the shared `ErrorResponse` envelope.
