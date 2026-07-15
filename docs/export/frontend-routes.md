# Frontend routes

Source of truth: `frontend/src/app/app.routes.ts` and the lazy-loaded feature
components. The 32 child routes below are all wrapped by
`MainLayoutComponent`; the wrapper route is not independently navigable and is
therefore not counted.

`authGuard` redirects unauthenticated users to `/login` and preserves the
requested URL. Some public routes still rely on backend resource-level rules,
notably private collection reads.

## Public

| Path | Component | Navigation | Status | Description |
| --- | --- | --- | --- | --- |
| `/home` | `HomeComponent` | Primary | `MVP1_VISIBLE` | Public collector-focused home. |
| `/login` | `LoginComponent` | Anonymous header | `MVP1_VISIBLE` | Sign in. |
| `/register` | `RegisterComponent` | Login link/direct | `MVP1_VISIBLE` | Account creation; not a global CTA. |
| `/catalog` | `CatalogListComponent` | Primary | `MVP1_VISIBLE` | Public catalog search. |
| `/catalog/:id` | `MasterProductDetailComponent` | Contextual | `MVP1_VISIBLE` | Public catalog detail. |
| `/catalog/editorial` | `EditorialSearchComponent` | Primary | `MVP2_VISIBLE` | Public editorial search. |
| `/catalog/editorial/series/:seriesId` | `EditorialSeriesDetailComponent` | Contextual | `MVP2_VISIBLE` | Series with active items and editions. |
| `/catalog/editorial/items/:itemId` | `EditorialItemDetailComponent` | Contextual | `MVP2_VISIBLE` | Item with active editions, creator credits and item relationships. |
| `/catalog/editorial/editions/:editionId` | `EditorialEditionDetailComponent` | Contextual | `MVP2_VISIBLE` | Concrete edition and editorial context. |
| `/collections/:collectionId` | `CollectionDetailComponent` | Contextual | `MVP1_VISIBLE` | Public collection or private owner view, enforced by backend. |

## Authenticated MVP 1

| Path | Component | Guard | Navigation | Description |
| --- | --- | --- | --- | --- |
| `/collections` | `MyCollectionsComponent` | `authGuard` | Primary | Own collection list and filters. |
| `/collections/new` | `CollectionCreateComponent` | `authGuard` | Contextual | New collection form. |
| `/collections/:collectionId/edit` | `CollectionEditComponent` | `authGuard` | Contextual | Edit an owned collection. |
| `/collections/:collectionId/items/new` | `CollectionItemCreateComponent` | `authGuard` | Contextual | Search and add catalog item. |
| `/collections/:collectionId/items/:itemId/edit` | `CollectionItemEditComponent` | `authGuard` | Contextual | Edit item state and metadata. |
| `/collections/:collectionId/series/:seriesId/progress` | `CollectionSeriesProgressComponent` | `authGuard` | Contextual | Owner-only calculated series progress. |
| `/wanted` | `RecommendationsComponent` | `authGuard` | Primary | Wanted/missing items and matches. |
| `/profile` | `ProfileComponent` | `authGuard` | User menu | Current user's basic profile. |
| `/catalog/new` | `MasterProductCreateComponent` | `authGuard` plus UI/backend role check | Contextual role action | Catalog write for `ADMIN` or `SHOP_OWNER`. |

## Redirects

## MVP3 Admin Editorial

All routes below require `authGuard` and `editorialAdminGuard`. They are exposed
to `ADMIN` or `EDITORIAL_ADMIN`; `adminGuard` remains reserved for future global
administration.

| Path | Component | Navigation | Status | Description |
| --- | --- | --- | --- | --- |
| `/admin/editorial` | `AdminEditorialShellComponent` | Header/admin shell | `MVP3_ADMIN_PARTIAL` | Editorial admin navigation. |
| `/admin/editorial/publishers` | `AdminPublishersComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Publisher maintenance. |
| `/admin/editorial/franchises` | `AdminFranchisesComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Franchise maintenance. |
| `/admin/editorial/series` | `AdminSeriesComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Series maintenance. |
| `/admin/editorial/items` | `AdminItemsComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Item maintenance. |
| `/admin/editorial/editions` | `AdminEditionsComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Edition maintenance. |
| `/admin/editorial/creators` | `AdminCreatorsComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Creator maintenance. |
| `/admin/editorial/credits` | `AdminCreditsComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Item credits. |
| `/admin/editorial/relationships` | `AdminRelationshipsComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Item relationships. |
| `/admin/editorial/master-product-links` | `AdminMasterProductLinksComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Legacy reconciliation. |
| `/admin/editorial/data-quality` | `AdminDataQualityComponent` | Shell | `MVP3_ADMIN_PARTIAL` | Read-only duplicate quality report. |

| Path | Destination | Reason |
| --- | --- | --- |
| `/` | `/home` | Default application entry. |
| `/dashboard` | `/home` | Former dashboard compatibility. |
| `/recommendations` | `/wanted` | Renamed collector-facing screen. |

All three have status `REDIRECT` and make no API call.

## Legacy/Future

These routes remain functional and addressable manually, but the main header,
sidebar and mobile navigation do not promote them.

| Path | Component | Access | Module |
| --- | --- | --- | --- |
| `/shops` | `MyShopsComponent` | `authGuard` | Shops |
| `/shops/new` | `ShopCreateComponent` | `authGuard` | Shops |
| `/shops/:shopId/inventory` | `ShopInventoryComponent` | `authGuard` | Inventory |
| `/shops/:shopId/inventory/new` | `ShopProductCreateComponent` | `authGuard` | Inventory |
| `/shops/:shopId/inventory/:shopProductId/edit` | `ShopProductEditComponent` | `authGuard` | Inventory |
| `/shops/:shopId/reservations` | `ShopReservationsComponent` | `authGuard` | Reservations |
| `/shops/:id` | `ShopDetailComponent` | Public | Shops |
| `/shop-products/:shopProductId` | `ShopProductDetailComponent` | Public; reservation action requires auth | Inventory |
| `/reservations` | `MyReservationsComponent` | `authGuard` | Reservations |
| `/reservations/:reservationId` | `ReservationDetailComponent` | `authGuard` | Reservations |

## Fallback

`/**` redirects unknown URLs to `/home`. It has no component and is counted as
a `REDIRECT` fallback route.

## Navigation values

EPIC 44F-C adds the authenticated, lazy-loaded series progress route. The total
is now 33.

- `PRIMARY`: visible in the main collector navigation.
- `HEADER`: visible in the global anonymous header.
- `USER_MENU`: visible in the authenticated avatar menu.
- `LOGIN_ONLY`: linked only from the Login screen.
- `CONTEXTUAL`: reached from an action or item link inside another screen.
- `CONTEXTUAL_ROLE`: contextual and only useful with a required global role.
- `MANUAL_ONLY`: addressable legacy/future route omitted from main navigation.
