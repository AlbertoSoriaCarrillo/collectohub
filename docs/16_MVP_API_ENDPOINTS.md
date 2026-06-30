# CollectoHub MVP API endpoints

Estado: contrato backend MVP consumido por el frontend Angular y validado en el flujo local.

Esta guia documenta el contrato real expuesto por el backend actual. Todos los
errores controlados usan el envelope `ErrorResponse`:

La exportacion completa y filtrable de los 47 endpoints actuales esta en
`docs/export/backend-endpoints.md` y `docs/export/backend-endpoints.csv`.

```json
{
  "timestamp": "2026-06-17T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/example",
  "details": {
    "field": "message"
  }
}
```

## Reglas generales

- Autenticacion: `Authorization: Bearer <accessToken>` para endpoints protegidos.
- Roles globales: `ADMIN`, `USER`, `SHOP_OWNER`, `CONTENT_CREATOR`.
- Roles internos de tienda: `OWNER`, `MANAGER`, `STAFF`.
- Los permisos de tienda se resuelven con `shop_members`, no solo con rol global.
- Las colecciones privadas ajenas devuelven `404` en lectura para no revelar su existencia.
- Los DTOs publicos no exponen `passwordHash`, hashes de refresh token ni datos internos de seguridad.
- Swagger UI esta disponible en `/swagger-ui.html` y `/swagger-ui/**`; OpenAPI en `/v3/api-docs/**`.

## Codigos HTTP principales

| Codigo | Uso |
| --- | --- |
| 200 | Lectura o actualizacion correcta |
| 201 | Recurso creado |
| 204 | Borrado logico correcto sin cuerpo |
| 400 | Validacion de request o filtro invalido |
| 401 | Falta token o credenciales invalidas |
| 403 | Token valido sin permiso suficiente |
| 404 | Recurso no encontrado o recurso privado ajeno oculto |
| 409 | Conflicto de negocio, duplicado o transicion invalida |

## Salud

| Metodo | Path | Acceso | Body | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/health` | Publico | No | `status`, `service` | - |

## Autenticacion y usuario

| Metodo | Path | Acceso | Body | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | Publico | `email`, `password`, `displayName`, `preferredInterfaceLanguage` opcional (`es`, `en`) | `id`, `email`, `displayName`, `preferredInterfaceLanguage`, `roles`, `accessToken`, `refreshToken` | `400`, `409` |
| POST | `/api/auth/login` | Publico | `email`, `password` | `id`, `email`, `displayName`, `preferredInterfaceLanguage`, `roles`, `accessToken`, `refreshToken` | `400`, `401` |
| GET | `/api/users/me` | Protegido | No | `id`, `email`, `displayName`, `preferredInterfaceLanguage`, `roles` | `401` |

Notas:

- El registro asigna `USER`.
- Al crear una tienda se asigna `SHOP_OWNER` en base de datos si falta. Para que el JWT contenga ese rol, el cliente debe obtener un token nuevo mediante login o futuro refresh.

## Tiendas

| Metodo | Path | Acceso | Permiso | Body | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/shops` | Protegido | Usuario autenticado | `name`, `description`, `contactEmail`, `contactPhone`, `country`, `currency`, `defaultReservationExpirationHours`, `logoUrl` | `ShopResponse` con `currentUserMembership` OWNER | `400`, `401` |
| GET | `/api/shops/my` | Protegido | Miembro de tiendas propias | No | Lista de `ShopResponse` | `401` |
| GET | `/api/shops/{shopId}` | Publico | Datos publicos basicos | No | `ShopResponse` sin datos internos sensibles | `404` |
| PUT | `/api/shops/{shopId}` | Protegido | `shop_members` OWNER o MANAGER | Campos de `UpdateShopRequest` | `ShopResponse` | `400`, `401`, `403`, `404` |

Valores por defecto:

- `currency`: `EUR` si no se informa.
- `defaultReservationExpirationHours`: 48 si no se informa.

## Catalogo maestro

| Metodo | Path | Acceso | Permiso | Body/Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/product-categories` | Publico | - | No | Categorias iniciales | - |
| GET | `/api/master-products` | Publico | - | Filtros `categoryCode`, `name`, `franchise`, `collectionName`, `language`, `status` | Lista de productos activos no eliminados | `400` si filtro invalido |
| GET | `/api/master-products/{id}` | Publico | - | No | `MasterProductResponse` | `404` |
| POST | `/api/master-products` | Protegido | Rol global `ADMIN` o `SHOP_OWNER` | `name`, `description`, `categoryCode`, `franchise`, `collectionName`, `volumeNumber`, `publisher`, `isbn`, `ean`, `releaseDate`, `editionStartDate`, `editionEndDate`, `language`, `limitedEdition`, `limitedEditionTotalUnits`, `publicationCountries`, `coverImageUrl`, `attributes` | `MasterProductResponse` | `400`, `401`, `403`, `404`, `409` |
| PUT | `/api/master-products/{id}` | Protegido | Rol global `ADMIN` o `SHOP_OWNER` | Campos de `UpdateMasterProductRequest` | `MasterProductResponse` | `400`, `401`, `403`, `404`, `409` |

Categorias iniciales:

- `MANGA_COMIC`
- `TRADING_CARD`
- `FIGURE`
- `VIDEOGAME`
- `MERCHANDISING`
- `MOVIE_SERIES`

Deteccion de duplicados:

- Mismo `isbn` activo si se informa.
- Mismo `ean` activo si se informa.
- Misma combinacion normalizada aproximada de `name`, `franchise`, `volumeNumber` y `language`.

## Catalogo editorial MVP 2 - Fundamentos

Los listados usan `PageResponse` con `content`, `page`, `size`,
`totalElements`, `totalPages`, `first` y `last`. Por defecto solo exponen
registros `ACTIVE` no eliminados. `recordStatus` es un filtro exclusivo de
`ADMIN`; la escritura editorial nueva no admite `SHOP_OWNER`.

| Metodo | Path | Acceso | Permiso | Body/Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/catalog/publishers` | Publico | ACTIVE; ADMIN puede filtrar estado | `q`, `recordStatus`, `page`, `size`, `sort` | `PageResponse<PublisherResponse>` | `400`, `403` |
| GET | `/api/catalog/publishers/{id}` | Publico/ADMIN | ACTIVE publico; cualquier no eliminado para ADMIN | No | `PublisherResponse` | `404` |
| POST | `/api/catalog/publishers` | Protegido | `ADMIN` | `CreatePublisherRequest` | `PublisherResponse` | `400`, `401`, `403`, `409` |
| PUT | `/api/catalog/publishers/{id}` | Protegido | `ADMIN` | `UpdatePublisherRequest` | `PublisherResponse` | `400`, `401`, `403`, `404`, `409` |
| GET | `/api/catalog/franchises` | Publico | ACTIVE; ADMIN puede filtrar estado | `q`, `recordStatus`, `page`, `size`, `sort` | `PageResponse<CatalogFranchiseResponse>` | `400`, `403` |
| GET | `/api/catalog/franchises/{id}` | Publico/ADMIN | ACTIVE publico; cualquier no eliminado para ADMIN | No | `CatalogFranchiseResponse` | `404` |
| POST | `/api/catalog/franchises` | Protegido | `ADMIN` | `CreateCatalogFranchiseRequest` | `CatalogFranchiseResponse` | `400`, `401`, `403`, `409` |
| PUT | `/api/catalog/franchises/{id}` | Protegido | `ADMIN` | `UpdateCatalogFranchiseRequest` | `CatalogFranchiseResponse` | `400`, `401`, `403`, `404`, `409` |
| GET | `/api/catalog/series` | Publico | ACTIVE; ADMIN puede filtrar estado | `q`, `franchiseId`, `type`, `publicationStatus`, `publisherId`, `language`, `country`, `recordStatus`, paginacion | `PageResponse<CatalogSeriesResponse>` | `400`, `403` |
| GET | `/api/catalog/series/{id}` | Publico/ADMIN | ACTIVE publico; cualquier no eliminado para ADMIN | No | `CatalogSeriesResponse` | `404` |
| POST | `/api/catalog/series` | Protegido | `ADMIN` | `CreateCatalogSeriesRequest` | `CatalogSeriesResponse` | `400`, `401`, `403`, `404`, `409` |
| PUT | `/api/catalog/series/{id}` | Protegido | `ADMIN` | `UpdateCatalogSeriesRequest` | `CatalogSeriesResponse` | `400`, `401`, `403`, `404`, `409` |

Enums iniciales:

- `recordStatus`: `DRAFT`, `ACTIVE`, `ARCHIVED`.
- `type`: `BOOK`, `COMIC`, `MANGA`.
- `publicationStatus`: `ONGOING`, `COMPLETED`, `CANCELLED`, `HIATUS`, `UNKNOWN`.

Estos endpoints no modifican ni sustituyen `/api/master-products`.

## Inventario de tienda

| Metodo | Path | Acceso | Permiso | Body/Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/shops/{shopId}/products` | Protegido | `shop_members` OWNER o MANAGER | `masterProductId`, `priceAmount`, `currency`, `stockQuantity`, `commercialStatus`, `physicalCondition`, `visible`, `unitNumber`, `totalLimitedUnits`, `notes` | `ShopProductResponse` | `400`, `401`, `403`, `404` |
| PUT | `/api/shops/{shopId}/products/{shopProductId}` | Protegido | `shop_members` OWNER o MANAGER | Campos de `UpdateShopProductRequest` | `ShopProductResponse` | `400`, `401`, `403`, `404` |
| GET | `/api/shops/{shopId}/products/my` | Protegido | Miembro activo de tienda | No | Inventario completo no eliminado de la tienda | `401`, `403`, `404` |
| GET | `/api/shops/{shopId}/products` | Publico | Solo productos visibles y disponibles | Filtros `masterProductId`, `categoryCode`, `name`, `franchise`, `collectionName`, `physicalCondition`, `commercialStatus` | Lista publica de `ShopProductResponse` | `400`, `404` |
| GET | `/api/shop-products/{shopProductId}` | Publico | Solo producto visible, disponible y activo | No | `ShopProductResponse` | `404` |

Enums:

- `commercialStatus`: `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`.
- `physicalCondition`: `NEW`, `LIKE_NEW`, `GOOD`, `ACCEPTABLE`, `DAMAGED`.

## Colecciones

| Metodo | Path | Acceso | Permiso | Body/Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/collections` | Protegido | Usuario autenticado | `name`, `description`, `visibility`, `categoryCode` | `CollectionResponse` | `400`, `401`, `404` |
| GET | `/api/collections/my` | Protegido | Propietario autenticado | Filtros `visibility`, `categoryCode` | Lista de colecciones propias | `400`, `401`, `404` |
| GET | `/api/collections/{collectionId}` | Publico/protegido | Publica o propietario | No | `CollectionResponse` con items | `404` |
| PUT | `/api/collections/{collectionId}` | Protegido | Propietario | Campos de `UpdateCollectionRequest` | `CollectionResponse` | `400`, `401`, `403`, `404` |
| DELETE | `/api/collections/{collectionId}` | Protegido | Propietario | No | Sin cuerpo | `401`, `403`, `404` |
| POST | `/api/collections/{collectionId}/items` | Protegido | Propietario | `masterProductId`, `collectionStatus`, `physicalCondition`, `unitNumber`, `totalLimitedUnits`, `notes`, `acquiredAt` | `CollectionItemResponse` | `400`, `401`, `403`, `404` |
| GET | `/api/collections/{collectionId}/items` | Publico/protegido | Coleccion publica o propietario | No | Lista de items | `404` |
| PUT | `/api/collections/{collectionId}/items/{itemId}` | Protegido | Propietario | Campos de `UpdateCollectionItemRequest` | `CollectionItemResponse` | `400`, `401`, `403`, `404` |
| DELETE | `/api/collections/{collectionId}/items/{itemId}` | Protegido | Propietario | No | Sin cuerpo | `401`, `403`, `404` |

Enums:

- `visibility`: `PRIVATE`, `PUBLIC`.
- `collectionStatus`: `OWNED`, `WANTED`, `MISSING`, `DUPLICATED`, `SELLABLE`, `TRADABLE`.

## Recomendaciones

| Metodo | Path | Acceso | Permiso | Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/recommendations/my` | Protegido | Usuario autenticado, solo sus colecciones | `categoryCode`, `maxPrice`, `currency`, `physicalCondition`, `shopId` | `recommendations`, `totalRecommendations` | `400`, `401` |
| GET | `/api/recommendations/my/summary` | Protegido | Usuario autenticado, solo sus colecciones | `categoryCode`, `maxPrice`, `currency`, `physicalCondition`, `shopId` | Conteos de coincidencias | `400`, `401` |

Reglas:

- Solo usa items propios en estado `MISSING` o `WANTED`.
- Solo recomienda productos de tienda activos, visibles, `AVAILABLE` y con stock mayor que cero.
- Deduplica por `shopProductId`.

## Reservas

| Metodo | Path | Acceso | Permiso | Body/Filtros | Respuesta | Errores |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/reservations` | Protegido | Usuario autenticado | `shopProductId`, `quantity`, `userMessage` | `ReservationResponse` | `400`, `401`, `404`, `409` |
| GET | `/api/reservations/my` | Protegido | Propietario autenticado | Filtros `status`, `shopId` | Lista de reservas propias | `400`, `401` |
| GET | `/api/reservations/{reservationId}` | Protegido | Propietario o `shop_members` OWNER/MANAGER de la tienda | No | `ReservationResponse` | `401`, `403`, `404` |
| GET | `/api/shops/{shopId}/reservations` | Protegido | `shop_members` OWNER o MANAGER | Filtros `status`, `userId`, `shopProductId` | Reservas de la tienda | `400`, `401`, `403`, `404` |
| PUT | `/api/shops/{shopId}/reservations/{reservationId}/status` | Protegido | `shop_members` OWNER o MANAGER | `status`, `shopResponse` | `ReservationResponse` | `400`, `401`, `403`, `404`, `409` |
| PUT | `/api/reservations/{reservationId}/cancel` | Protegido | Propietario de la reserva | No | `ReservationResponse` | `401`, `403`, `404`, `409` |

Enums:

- `ReservationStatus`: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `EXPIRED`, `COMPLETED`.

Transiciones MVP:

- Tienda: `PENDING -> ACCEPTED` o `PENDING -> REJECTED`.
- Tienda: `ACCEPTED -> COMPLETED` o `ACCEPTED -> CANCELLED`.
- Usuario: cancelar reserva propia en `PENDING` o `ACCEPTED`.
- No hay job automatico de expiracion en el MVP.
- Crear una reserva no reduce automaticamente `shop_products.stock_quantity`.
