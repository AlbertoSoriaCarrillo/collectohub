# Database schema

This document describes the PostgreSQL application schema created by the seven
Liquibase SQL migrations currently included by
`db/changelog/db.changelog-master.yaml`. It contains 19 application tables.
Liquibase runtime tables are intentionally excluded.

## Domain summary

| Domain | Tables | Product status |
| --- | --- | --- |
| Identity & Access | `users`, `roles`, `user_roles` | MVP 1 visible |
| Catalog Knowledge Base | `product_categories`, `master_products`, `product_suggestions`, `publishers`, `catalog_franchises`, `catalog_series`, `catalog_items`, `catalog_item_editions`, `master_product_catalog_links` | MVP 1 catalog plus MVP 2 foundations |
| User Collections | `collections`, `collection_items` | MVP 1 visible |
| Shops & Inventory | `shops`, `shop_members`, `shop_products` | Implemented legacy/future base |
| Matching | No table | Calculated from collections and inventory |
| Commerce | `reservations` | Implemented legacy/future base |
| Technical/Audit | `refresh_tokens` and audit columns | Authentication support |

## Shared audit and soft delete

Every table except the pure join table `user_roles` contains this audit set:

| Column | Type | Required | Meaning |
| --- | --- | --- | --- |
| `created_at` | `TIMESTAMPTZ` | Yes | Creation time; defaults to `CURRENT_TIMESTAMP`. |
| `created_by` | `BIGINT` | No | Actor identifier; not declared as a foreign key. |
| `updated_at` | `TIMESTAMPTZ` | No | Last update time. |
| `updated_by` | `BIGINT` | No | Last update actor; not declared as a foreign key. |
| `deleted_at` | `TIMESTAMPTZ` | No | Logical deletion time. |
| `deleted_by` | `BIGINT` | No | Logical deletion actor; not declared as a foreign key. |

The MVP 2 foundation tables make `created_by` mandatory because their write
endpoints require an authenticated ADMIN. Earlier tables retain their original
nullable audit actor definition.

Repositories normally query `deleted_at IS NULL`. A populated `deleted_at`
therefore hides a row without physically deleting it. The schema does not add
foreign keys for audit actor IDs, so historic audit data is not coupled to the
lifecycle of a user row.

## Identity & Access

### users

- Domain: Identity & Access
- Status: `MVP1_VISIBLE`

Registered identities and the profile fields used by authentication and the
MVP profile screen.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `email` | `VARCHAR(320)` | Yes | Unique login identifier; application comparisons are case-insensitive. |
| `password_hash` | `VARCHAR(255)` | Yes | Encoded password, never returned by the API. |
| `display_name` | `VARCHAR(120)` | Yes | Public display name. |
| `preferred_interface_language` | `VARCHAR(10)` | Yes | Interface preference, default `es`. |
| `status` | `VARCHAR(30)` | Yes | User lifecycle state, default `ACTIVE`. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

Constraints: PK `id`; unique `uk_users_email(email)`. Referenced by global
roles, refresh tokens, shops, memberships, suggestions, collections and
reservations.

### roles

- Domain: Identity & Access
- Status: `MVP1_VISIBLE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `code` | `VARCHAR(50)` | Yes | Unique authority used by Spring Security. |
| `name` | `VARCHAR(100)` | Yes | Human-readable role name. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

Seeded codes: `ADMIN`, `USER`, `SHOP_OWNER`, `CONTENT_CREATOR`. Constraint:
`uk_roles_code(code)`.

### user_roles

- Domain: Identity & Access
- Status: `MVP1_VISIBLE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `user_id` | `BIGINT` | Yes | PK part and FK to `users.id`. |
| `role_id` | `BIGINT` | Yes | PK part and FK to `roles.id`. |

The composite primary key `(user_id, role_id)` prevents duplicate global-role
assignments. This join table has no audit or soft-delete columns.

### refresh_tokens

- Domain: Technical/Audit
- Status: `TECHNICAL`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `user_id` | `BIGINT` | Yes | FK to `users.id`. |
| `token_hash` | `VARCHAR(128)` | Yes | Unique hash; raw refresh tokens are not persisted. |
| `expires_at` | `TIMESTAMPTZ` | Yes | Expiry instant. |
| `revoked_at` | `TIMESTAMPTZ` | No | Explicit revocation instant. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

Indexes: unique `uk_refresh_tokens_token_hash`,
`idx_refresh_tokens_user_id`, `idx_refresh_tokens_expires_at`.

## Catalog Knowledge Base

### product_categories

- Domain: Catalog Knowledge Base
- Status: `MVP1_VISIBLE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `code` | `VARCHAR(80)` | Yes | Unique stable category code. |
| `name` | `VARCHAR(120)` | Yes | Display name. |
| `parent_id` | `BIGINT` | No | Self-referencing FK for a category hierarchy. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

Seeded codes: `MANGA_COMIC`, `TRADING_CARD`, `FIGURE`, `VIDEOGAME`,
`MERCHANDISING`, `MOVIE_SERIES`. Constraints: `uk_product_categories_code` and
`fk_product_categories_parent`.

### publishers

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Editorial publisher identity used by series and future item editions.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `name` | `VARCHAR(160)` | Yes | Publisher display name. |
| `country` | `VARCHAR(2)` | No | Two-letter country code. |
| `record_status` | `VARCHAR(30)` | Yes | `DRAFT`, `ACTIVE` or `ARCHIVED`; default `DRAFT`. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required; update and soft-delete fields are optional. |

Check: `chk_publishers_record_status`. Indexes: `idx_publishers_name` and
`idx_publishers_record_status`.

### catalog_franchises

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Optional universe or intellectual-property grouping above catalog series.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `name` | `VARCHAR(160)` | Yes | Public franchise name. |
| `slug` | `VARCHAR(160)` | Yes | Lowercase URL identifier. |
| `description` | `TEXT` | No | Editorial description. |
| `record_status` | `VARCHAR(30)` | Yes | `DRAFT`, `ACTIVE` or `ARCHIVED`; default `DRAFT`. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required; update and soft-delete fields are optional. |

Check: `chk_catalog_franchises_record_status`. Indexes:
`idx_catalog_franchises_name`, `idx_catalog_franchises_slug`,
`idx_catalog_franchises_record_status`; partial unique index
`uk_catalog_franchises_slug_active` applies while `deleted_at IS NULL`.

### catalog_series

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Editorial series, line or single-item grouping. This type is intentionally
separate from `product_categories`.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `franchise_id` | `BIGINT` | No | FK to `catalog_franchises.id`. |
| `primary_publisher_id` | `BIGINT` | No | FK to `publishers.id`. |
| `title` | `VARCHAR(240)` | Yes | Main series title. |
| `original_title` | `VARCHAR(240)` | No | Original-language title. |
| `type` | `VARCHAR(30)` | Yes | `BOOK`, `COMIC` or `MANGA`. |
| `publication_status` | `VARCHAR(30)` | Yes | `ONGOING`, `COMPLETED`, `CANCELLED`, `HIATUS` or `UNKNOWN`. |
| `description` | `TEXT` | No | Editorial description. |
| `origin_country` | `VARCHAR(2)` | No | Country of origin. |
| `original_language` | `VARCHAR(10)` | No | Original language code. |
| `start_year` | `INTEGER` | No | First year, constrained to 1000-3000. |
| `end_year` | `INTEGER` | No | Final year, constrained to 1000-3000 and not before start. |
| `record_status` | `VARCHAR(30)` | Yes | `DRAFT`, `ACTIVE` or `ARCHIVED`; default `DRAFT`. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required; update and soft-delete fields are optional. |

FKs: `fk_catalog_series_franchise`,
`fk_catalog_series_primary_publisher`. Six check constraints validate type,
publication status, record status and years. Indexes cover both FKs, title,
type, publication status and record status.

### catalog_items

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Collectable editorial identity inside a series, independent from any concrete
publication edition.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `series_id` | `BIGINT` | Yes | FK to `catalog_series.id`. |
| `title` | `VARCHAR(240)` | Yes | Main item title. |
| `original_title` | `VARCHAR(240)` | No | Original-language title. |
| `sequence_label` | `VARCHAR(50)` | No | Flexible volume/issue label. |
| `sort_order` | `NUMERIC(10,3)` | No | Non-negative ordering value. |
| `description` | `TEXT` | No | Editorial description. |
| `first_publication_date` | `DATE` | No | Earliest known publication date. |
| `first_publication_year` | `INTEGER` | No | Year constrained to 1000-3000. |
| `original_language` | `VARCHAR(10)` | No | Original language code. |
| `origin_country` | `VARCHAR(2)` | No | Country of origin. |
| `record_status` | `VARCHAR(30)` | Yes | `DRAFT`, `ACTIVE` or `ARCHIVED`; default `DRAFT`. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required. |

FK: `fk_catalog_items_series`. Checks enforce non-negative `sort_order`, a
reasonable publication year and valid record status. Seven indexes cover the
FK, public status, title, ordering, year, language and country. Logical
duplicate detection for series, title and sequence label is handled by the
service to avoid an overly aggressive database constraint.

### catalog_item_editions

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Concrete publication of a catalog item, including identifiers, format,
language and optional publisher.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `catalog_item_id` | `BIGINT` | Yes | FK to `catalog_items.id`. |
| `publisher_id` | `BIGINT` | No | FK to `publishers.id`. |
| `isbn` | `VARCHAR(32)` | No | Normalized ISBN without spaces or hyphens. |
| `ean` | `VARCHAR(32)` | No | Normalized EAN without spaces or hyphens. |
| `format` | `VARCHAR(40)` | Yes | `HARDCOVER`, `PAPERBACK`, `SOFTCOVER`, `DIGITAL`, `OMNIBUS`, `BOX_SET`, `SINGLE_ISSUE` or `OTHER`. |
| `edition_name` | `VARCHAR(240)` | No | Commercial edition name. |
| `publication_date` | `DATE` | No | Edition publication date. |
| `publication_year` | `INTEGER` | No | Year constrained to 1000-3000. |
| `language` | `VARCHAR(10)` | No | Edition language code. |
| `country` | `VARCHAR(2)` | No | Publication market/country. |
| `page_count` | `INTEGER` | No | Positive page count. |
| `cover_image_url` | `VARCHAR(1000)` | No | Temporary external HTTP(S) cover URL. |
| `record_status` | `VARCHAR(30)` | Yes | `DRAFT`, `ACTIVE` or `ARCHIVED`; default `DRAFT`. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required. |

FKs: `fk_catalog_item_editions_item`,
`fk_catalog_item_editions_publisher`. Checks validate format, positive page
count, publication year and record status. Nine indexes cover both FKs, status,
ISBN, EAN, format, language, country and year. Partial unique indexes
`uk_catalog_item_editions_isbn_active` and
`uk_catalog_item_editions_ean_active` apply to non-null identifiers while
`deleted_at IS NULL`.

### master_product_catalog_links

- Domain: Catalog Knowledge Base
- Status: `MVP2_FOUNDATION`

Audited reconciliation bridge from legacy master products to editorial items
and optional concrete editions. Existing consumers do not read this table yet.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `master_product_id` | `BIGINT` | Yes | FK to `master_products.id`. |
| `catalog_item_id` | `BIGINT` | Yes | FK to `catalog_items.id`. |
| `catalog_item_edition_id` | `BIGINT` | No | Optional FK to `catalog_item_editions.id`; service validates item ownership. |
| `link_status` | `VARCHAR(30)` | Yes | `PROPOSED`, `VERIFIED` or `REJECTED`. |
| `link_source` | `VARCHAR(40)` | Yes | Manual, identifier, title or backfill evidence. |
| `confidence_score` | `NUMERIC(5,4)` | No | Confidence constrained to 0-1. |
| `match_reason` | `TEXT` | No | Evidence used for the proposal. |
| `review_note` | `TEXT` | No | Administrative reconciliation note. |
| audit set | shared columns | Mixed | `created_at` and `created_by` are required. |

Three FKs connect the bridge without changing linked tables. Checks validate
status, source and confidence. Six lookup indexes cover FKs and reconciliation
filters; partial unique index `uk_master_product_catalog_links_verified_master`
allows only one non-deleted `VERIFIED` link per master product.

### master_products

- Domain: Catalog Knowledge Base
- Status: `MVP1_VISIBLE`

Reusable catalog identity shared by user collections and shop inventory.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `name` | `VARCHAR(240)` | Yes | Product/work name. |
| `description` | `TEXT` | No | Free description. |
| `category_id` | `BIGINT` | Yes | FK to `product_categories.id`. |
| `franchise` | `VARCHAR(160)` | No | Franchise or universe. |
| `collection_name` | `VARCHAR(160)` | No | Catalog grouping name. |
| `volume_number` | `VARCHAR(50)` | No | Volume/issue identifier. |
| `publisher` | `VARCHAR(160)` | No | Publisher or brand. |
| `isbn` | `VARCHAR(20)` | No | ISBN used in duplicate detection. |
| `ean` | `VARCHAR(20)` | No | EAN used in duplicate detection. |
| `release_date` | `DATE` | No | Product release date. |
| `edition_start_date` | `DATE` | No | Edition validity start. |
| `edition_end_date` | `DATE` | No | Edition validity end. |
| `product_language` | `VARCHAR(10)` | No | Product language code. |
| `is_limited_edition` | `BOOLEAN` | Yes | Limited-edition flag, default `FALSE`. |
| `publication_countries` | `JSONB` | Yes | Country-code array, default `[]`. |
| `cover_image_url` | `VARCHAR(2048)` | No | External cover URL. |
| `status` | `VARCHAR(30)` | Yes | Lifecycle state, default `ACTIVE`. |
| `attributes` | `JSONB` | Yes | Flexible metadata object, default `{}`. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FK: `fk_master_products_category`. Explicit indexes:
`idx_master_products_isbn`, `idx_master_products_ean`,
`idx_master_products_name`, `idx_master_products_franchise`.
`limitedEditionTotalUnits` from the API is stored as a key inside `attributes`;
there is no column with that name.

### product_suggestions

- Domain: Catalog Knowledge Base
- Status: `LEGACY_FUTURE`

The migration prepared a catalog-review workflow, but the current code has no
JPA entity, repository or REST endpoint for it.

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `suggested_by_user_id` | `BIGINT` | Yes | FK to proposing `users.id`. |
| `name` | `VARCHAR(240)` | Yes | Proposed product name. |
| `data` | `JSONB` | Yes | Flexible proposal data, default `{}`. |
| `status` | `VARCHAR(30)` | Yes | Review state, default `PENDING`. |
| `reviewed_by_user_id` | `BIGINT` | No | FK to reviewing `users.id`. |
| `review_comment` | `TEXT` | No | Reviewer notes. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_product_suggestions_suggested_by_user` and
`fk_product_suggestions_reviewed_by_user`.

## User Collections

### collections

- Domain: User Collections
- Status: `MVP1_VISIBLE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `user_id` | `BIGINT` | Yes | FK to owner `users.id`. |
| `name` | `VARCHAR(160)` | Yes | Collection name. |
| `description` | `TEXT` | No | Owner description. |
| `visibility` | `VARCHAR(20)` | Yes | `PUBLIC` or `PRIVATE`; default `PRIVATE`. |
| `category_id` | `BIGINT` | No | Optional FK to `product_categories.id`. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_collections_user`, `fk_collections_category`. Index:
`idx_collections_user_id`.

### collection_items

- Domain: User Collections
- Status: `MVP1_VISIBLE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `collection_id` | `BIGINT` | Yes | FK to `collections.id`. |
| `master_product_id` | `BIGINT` | Yes | FK to `master_products.id`. |
| `collection_status` | `VARCHAR(30)` | Yes | Collector state such as `OWNED`, `WANTED` or `MISSING`. |
| `physical_condition` | `VARCHAR(30)` | No | Optional copy condition. |
| `unit_number` | `VARCHAR(50)` | No | Number for a limited copy. |
| `total_limited_units` | `INTEGER` | No | Positive total edition size. |
| `notes` | `TEXT` | No | User notes. |
| `acquired_at` | `DATE` | No | Acquisition date. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_collection_items_collection`,
`fk_collection_items_master_product`. Indexes:
`idx_collection_items_collection_id`,
`idx_collection_items_master_product_id`. Check:
`total_limited_units IS NULL OR total_limited_units > 0`.

## Shops & Inventory

### shops

- Domain: Shops & Inventory
- Status: `LEGACY_FUTURE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `owner_user_id` | `BIGINT` | Yes | FK to founding `users.id`. |
| `name` | `VARCHAR(160)` | Yes | Shop name. |
| `description` | `TEXT` | No | Public description. |
| `contact_email` | `VARCHAR(320)` | No | Contact email. |
| `contact_phone` | `VARCHAR(40)` | No | Contact phone. |
| `country` | `VARCHAR(2)` | No | Country code; made nullable by changelog 004. |
| `currency` | `VARCHAR(3)` | Yes | Shop currency. |
| `default_reservation_expiration_hours` | `INTEGER` | Yes | Positive expiry, default 48. |
| `logo_url` | `VARCHAR(2048)` | No | External logo URL. |
| `status` | `VARCHAR(30)` | Yes | Lifecycle state, default `ACTIVE`. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FK: `fk_shops_owner_user`. Index: `idx_shops_owner_user_id`. Check:
`default_reservation_expiration_hours > 0`.

### shop_members

- Domain: Shops & Inventory
- Status: `LEGACY_FUTURE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `shop_id` | `BIGINT` | Yes | FK to `shops.id`. |
| `user_id` | `BIGINT` | Yes | FK to `users.id`. |
| `role` | `VARCHAR(30)` | Yes | Internal role: `OWNER`, `MANAGER` or `STAFF`. |
| `status` | `VARCHAR(30)` | Yes | Membership state, default `ACTIVE`. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_shop_members_shop`, `fk_shop_members_user`. Unique constraint:
`uk_shop_members_shop_user(shop_id, user_id)`.

### shop_products

- Domain: Shops & Inventory
- Status: `LEGACY_FUTURE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `shop_id` | `BIGINT` | Yes | FK to `shops.id`. |
| `master_product_id` | `BIGINT` | Yes | FK to `master_products.id`. |
| `price_amount` | `NUMERIC(12,2)` | Yes | Non-negative price. |
| `currency` | `VARCHAR(3)` | Yes | ISO-like currency code. |
| `stock_quantity` | `INTEGER` | Yes | Non-negative stock, default 0. |
| `commercial_status` | `VARCHAR(30)` | Yes | Availability state, default `AVAILABLE`. |
| `physical_condition` | `VARCHAR(30)` | Yes | Copy condition. |
| `visible` | `BOOLEAN` | Yes | Public visibility, default `TRUE`. |
| `unit_number` | `VARCHAR(50)` | No | Limited-copy number. |
| `total_limited_units` | `INTEGER` | No | Positive total edition size. |
| `notes` | `TEXT` | No | Internal/public notes supplied by the shop. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_shop_products_shop`, `fk_shop_products_master_product`. Indexes:
`idx_shop_products_shop_id`, `idx_shop_products_master_product_id`. Checks:
non-negative price and stock; positive limited-unit total when present.

## Commerce

### reservations

- Domain: Commerce
- Status: `LEGACY_FUTURE`

| Column | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | `BIGINT IDENTITY` | Yes | Primary key. |
| `user_id` | `BIGINT` | Yes | FK to reserving `users.id`. |
| `shop_id` | `BIGINT` | Yes | FK to `shops.id`. |
| `shop_product_id` | `BIGINT` | Yes | FK to `shop_products.id`. |
| `quantity` | `INTEGER` | Yes | Positive quantity, default 1. |
| `status` | `VARCHAR(30)` | Yes | Reservation lifecycle state, default `PENDING`. |
| `user_message` | `TEXT` | No | Message from the user. |
| `shop_response` | `TEXT` | No | Response from the shop. |
| `expires_at` | `TIMESTAMPTZ` | No | Reservation expiry. |
| `completed_at` | `TIMESTAMPTZ` | No | Completion time. |
| audit set | shared columns | Mixed | Creation, update and soft-delete metadata. |

FKs: `fk_reservations_user`, `fk_reservations_shop`,
`fk_reservations_shop_product`. Indexes: `idx_reservations_user_id`,
`idx_reservations_shop_id`, `idx_reservations_status`. Check:
`quantity > 0`.

## Index inventory

Liquibase declares 50 explicit indexes, including partial unique franchise,
ISBN and EAN indexes:

| Table | Indexes |
| --- | --- |
| `shops` | `idx_shops_owner_user_id(owner_user_id)` |
| `shop_products` | `idx_shop_products_shop_id(shop_id)`, `idx_shop_products_master_product_id(master_product_id)` |
| `master_products` | `idx_master_products_isbn(isbn)`, `idx_master_products_ean(ean)`, `idx_master_products_name(name)`, `idx_master_products_franchise(franchise)` |
| `collections` | `idx_collections_user_id(user_id)` |
| `collection_items` | `idx_collection_items_collection_id(collection_id)`, `idx_collection_items_master_product_id(master_product_id)` |
| `reservations` | `idx_reservations_user_id(user_id)`, `idx_reservations_shop_id(shop_id)`, `idx_reservations_status(status)` |
| `refresh_tokens` | `idx_refresh_tokens_user_id(user_id)`, `idx_refresh_tokens_expires_at(expires_at)` |
| `publishers` | `idx_publishers_name(lower(name))`, `idx_publishers_record_status(record_status)` |
| `catalog_franchises` | `idx_catalog_franchises_name(lower(name))`, `idx_catalog_franchises_slug(slug)`, `uk_catalog_franchises_slug_active(slug)`, `idx_catalog_franchises_record_status(record_status)` |
| `catalog_series` | `idx_catalog_series_franchise_id(franchise_id)`, `idx_catalog_series_primary_publisher_id(primary_publisher_id)`, `idx_catalog_series_type(type)`, `idx_catalog_series_publication_status(publication_status)`, `idx_catalog_series_record_status(record_status)`, `idx_catalog_series_title(lower(title))` |
| `catalog_items` | `idx_catalog_items_series_id(series_id)`, `idx_catalog_items_record_status(record_status)`, `idx_catalog_items_title(lower(title))`, `idx_catalog_items_sort_order(sort_order)`, `idx_catalog_items_first_publication_year(first_publication_year)`, `idx_catalog_items_original_language(original_language)`, `idx_catalog_items_origin_country(origin_country)` |
| `catalog_item_editions` | `idx_catalog_item_editions_catalog_item_id(catalog_item_id)`, `idx_catalog_item_editions_publisher_id(publisher_id)`, `idx_catalog_item_editions_record_status(record_status)`, `uk_catalog_item_editions_isbn_active(isbn)`, `uk_catalog_item_editions_ean_active(ean)`, `idx_catalog_item_editions_format(format)`, `idx_catalog_item_editions_language(language)`, `idx_catalog_item_editions_country(country)`, `idx_catalog_item_editions_publication_year(publication_year)` |
| `master_product_catalog_links` | `idx_master_product_catalog_links_master_product_id(master_product_id)`, `idx_master_product_catalog_links_catalog_item_id(catalog_item_id)`, `idx_master_product_catalog_links_catalog_item_edition_id(catalog_item_edition_id)`, `idx_master_product_catalog_links_status(link_status)`, `idx_master_product_catalog_links_source(link_source)`, `idx_master_product_catalog_links_confidence(confidence_score)`, `uk_master_product_catalog_links_verified_master(master_product_id)` |

PostgreSQL additionally creates indexes to enforce every primary key and the
unique constraints on user email, role code, category code, shop membership and
refresh-token hash.

## Matching without persistence

There is no `recommendations` table. `RecommendationService` reads non-deleted
`collection_items` in wanted/missing states, then matches their
`master_product_id` values against visible, available `shop_products` with
stock. The result and summary DTOs are calculated per request.
