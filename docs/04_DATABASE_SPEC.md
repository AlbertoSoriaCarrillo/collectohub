# Especificación de base de datos

## Motor

PostgreSQL.

## Migraciones

Liquibase obligatorio.

Todas las tablas deben crearse mediante changelogs versionados.

## Principios

- Una única base de datos.
- Aislamiento lógico por tienda mediante `shop_id` o `tenant_id` cuando aplique.
- Borrado lógico en entidades importantes.
- Auditoría en entidades importantes.
- Índices para búsquedas frecuentes.
- Restricciones únicas donde eviten duplicados.
- JSONB para atributos específicos por categoría.

## Auditoría común

Las tablas principales deben incluir:

```text
created_at
created_by
updated_at
updated_by
deleted_at
deleted_by
```

Si `deleted_at` no es null, el registro está eliminado lógicamente.

## Tablas MVP propuestas

### users

- id
- email
- password_hash
- display_name
- preferred_interface_language
- status
- created_at / updated_at / deleted_at

### roles

- id
- code
- name

Roles iniciales:

- ADMIN
- USER
- SHOP_OWNER
- CONTENT_CREATOR

En MVP se usarán principalmente ADMIN, USER y SHOP_OWNER.

### user_roles

- user_id
- role_id

Los roles son acumulables.

### shops

- id
- owner_user_id
- name
- description
- contact_email
- contact_phone
- country
- currency
- default_reservation_expiration_hours
- logo_url
- status
- created_at / updated_at / deleted_at

### shop_members

- id
- shop_id
- user_id
- role
- status

Roles internos previstos:

- OWNER
- MANAGER
- EMPLOYEE

### product_categories

- id
- code
- name
- parent_id

Categorías iniciales:

- MANGA_COMIC
- TRADING_CARD
- FIGURE
- VIDEOGAME
- MERCHANDISING
- MOVIE_SERIES

### master_products

Producto maestro global.

Campos mínimos:

- id
- name
- description
- category_id
- franchise
- collection_name
- volume_number
- publisher
- isbn
- ean
- release_date
- edition_start_date
- edition_end_date
- product_language
- is_limited_edition
- publication_countries
- cover_image_url
- status
- attributes JSONB
- created_at / updated_at / deleted_at

Notas:

- ISBN/EAN pueden ser null.
- `publication_countries` puede ser JSONB o tabla relacional según implementación.
- `attributes` debe permitir campos propios de cartas, figuras, videojuegos, merchandising, etc.

### product_suggestions

Sugerencias de producto creadas por USER.

- id
- suggested_by_user_id
- name
- data JSONB
- status
- reviewed_by_user_id
- review_comment
- created_at / updated_at

Estados:

- PENDING
- APPROVED
- REJECTED
- MERGED

### shop_products

Producto concreto disponible en una tienda.

- id
- shop_id
- master_product_id
- price_amount
- currency
- stock_quantity
- commercial_status
- physical_condition
- visible
- unit_number
- total_limited_units
- notes
- created_at / updated_at / deleted_at

Estados comerciales:

- AVAILABLE
- OUT_OF_STOCK
- RESERVED
- PREORDER
- HIDDEN

Estados físicos:

- NEW
- LIKE_NEW
- GOOD
- USED
- DAMAGED
- SEALED
- OPENED
- GRADED

### collections

- id
- user_id
- name
- description
- visibility
- category_id
- created_at / updated_at / deleted_at

Visibilidad:

- PUBLIC
- PRIVATE

### collection_items

- id
- collection_id
- master_product_id
- collection_status
- physical_condition
- unit_number
- total_limited_units
- notes
- acquired_at
- created_at / updated_at / deleted_at

Estados de colección:

- OWNED
- MISSING
- WANTED
- RESERVED
- REPEATED
- SELLABLE
- EXCHANGEABLE
- READ
- WATCHED
- PLAYED

### reservations

- id
- user_id
- shop_id
- shop_product_id
- quantity
- status
- user_message
- shop_response
- expires_at
- completed_at
- created_at / updated_at / deleted_at

Estados:

- PENDING
- ACCEPTED
- REJECTED
- CANCELLED
- EXPIRED
- COMPLETED

## Duplicados de catálogo

Detección mínima:

1. Si existe ISBN o EAN, usarlo como criterio principal.
2. Si no existe, usar combinación normalizada:
   - name
   - franchise
   - collection_name
   - volume_number
   - product_language

Si una tienda introduce un producto con datos incompletos y el sistema encuentra coincidencia, debe proponer completar los datos desde el catálogo maestro.

## Índices mínimos

- users.email
- shops.owner_user_id
- shop_products.shop_id
- shop_products.master_product_id
- master_products.isbn
- master_products.ean
- master_products.name
- master_products.franchise
- collections.user_id
- collection_items.collection_id
- collection_items.master_product_id
- reservations.user_id
- reservations.shop_id
- reservations.status

## JSONB

Usar JSONB para atributos específicos por categoría, pero no para datos esenciales que se consulten constantemente.
