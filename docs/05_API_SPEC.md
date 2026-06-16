# Especificación de API

## Convenciones

- Prefijo: `/api`.
- Versionado futuro: `/api/v1` si se decide estabilizar API pública.
- JSON como formato principal.
- Swagger/OpenAPI obligatorio.
- Paginación en listados.
- Filtros mediante query params.
- No exponer entidades JPA directamente.

## Formato de error común

```json
{
  "timestamp": "2026-06-16T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/...",
  "details": []
}
```

## Auth

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

## Usuario actual

```http
GET /api/users/me
PUT /api/users/me
GET /api/users/me/roles
```

## Tiendas

```http
POST /api/shops
GET /api/shops/{shopId}
PUT /api/shops/{shopId}
GET /api/shops/my
```

## Miembros de tienda

```http
GET /api/shops/{shopId}/members
POST /api/shops/{shopId}/members
PUT /api/shops/{shopId}/members/{memberId}
DELETE /api/shops/{shopId}/members/{memberId}
```

En MVP puede quedar parcialmente implementado, pero el modelo debe existir.

## Catálogo maestro

```http
POST /api/catalog/products
GET /api/catalog/products
GET /api/catalog/products/{productId}
PUT /api/catalog/products/{productId}
DELETE /api/catalog/products/{productId}
GET /api/catalog/products/duplicates
```

Reglas:

- ADMIN y SHOP_OWNER pueden crear productos.
- USER puede sugerir productos, no crearlos directamente.

## Sugerencias de producto

```http
POST /api/catalog/product-suggestions
GET /api/catalog/product-suggestions/my
GET /api/admin/product-suggestions
POST /api/admin/product-suggestions/{suggestionId}/approve
POST /api/admin/product-suggestions/{suggestionId}/reject
POST /api/admin/product-suggestions/{suggestionId}/merge
```

## Inventario de tienda

```http
POST /api/shops/{shopId}/products
GET /api/shops/{shopId}/products
GET /api/shops/{shopId}/products/{shopProductId}
PUT /api/shops/{shopId}/products/{shopProductId}
DELETE /api/shops/{shopId}/products/{shopProductId}
```

## Colecciones

```http
POST /api/collections
GET /api/collections/my
GET /api/collections/{collectionId}
PUT /api/collections/{collectionId}
DELETE /api/collections/{collectionId}
```

## Productos en colección

```http
POST /api/collections/{collectionId}/items
GET /api/collections/{collectionId}/items
PUT /api/collections/{collectionId}/items/{itemId}
DELETE /api/collections/{collectionId}/items/{itemId}
```

## Reservas

```http
POST /api/reservations
GET /api/reservations/my
GET /api/shops/{shopId}/reservations
POST /api/shops/{shopId}/reservations/{reservationId}/accept
POST /api/shops/{shopId}/reservations/{reservationId}/reject
POST /api/reservations/{reservationId}/cancel
POST /api/shops/{shopId}/reservations/{reservationId}/complete
```

## Recomendaciones básicas

```http
GET /api/recommendations/missing-products
GET /api/recommendations/wishlist-available
GET /api/recommendations/available-shop-products
```

## Archivos

```http
POST /api/files/product-cover
POST /api/files/shop-logo
```

MVP: solo producto y tienda.

Fase futura: imagen de perfil, imagen de colección, fotos, vídeos y audios en publicaciones/comentarios.

## Seguridad de API

- Endpoints públicos: catálogo visible y tiendas públicas si aplica.
- Endpoints privados: colecciones, reservas, panel de tienda.
- ADMIN: administración global.
- SHOP_OWNER: solo sus tiendas.
- USER: solo sus datos y colecciones.

## Paginación

Formato recomendado:

```http
GET /api/catalog/products?page=0&size=20&sort=name,asc
```

Respuesta:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```
