# CollectoHub Frontend

Aplicacion Angular base para el MVP de CollectoHub.

## Stack

- Angular 21.
- TypeScript.
- Angular Material.
- Routing standalone.
- Formularios reactivos.
- HTTP Client con interceptor JWT.
- SCSS.

## Requisitos

- Node.js 24.x.
- npm 11.x.
- Backend local en `http://localhost:8080`.

En Windows PowerShell, si `npm` falla por politica de ejecucion de scripts,
usa `npm.cmd`:

```powershell
& "C:\Program Files\nodejs\npm.cmd" install
```

## Instalacion

```bash
npm install
```

Para CI o una instalacion limpia desde `package-lock.json`:

```bash
npm ci
```

## Backend local

El frontend usa:

```text
src/environments/environment.ts
apiBaseUrl = "http://localhost:8080"
```

Arranca el backend en otra terminal:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Demo local

Con backend y frontend levantados, abre:

```text
http://localhost:4200
```

El flujo recomendado para una demo completa esta en:

```text
../docs/18_DEMO_FLOW.md
```

## Docker

El frontend tiene un Dockerfile multi-stage:

- build con Node.js 24 y `npm ci`.
- publicacion del build Angular con nginx.
- puerto de contenedor `80`, mapeado por Compose a `http://localhost:4200`.

Desde la raiz del repositorio:

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

Limitacion MVP: `src/environments/environment.ts` mantiene `apiBaseUrl` en
`http://localhost:8080`. En Docker local el navegador del host accede a ese
backend publicado y Spring permite CORS para `localhost:4200`.

## Comandos

```bash
npm start
npm test
npm run build
npm run e2e:install
npm run e2e
npm run e2e:headed
npm run e2e:ui
```

El workflow de CI ejecuta:

```bash
npm ci
npm test -- --watch=false
npm run build
```

Los E2E Playwright no se ejecutan todavia en CI. Requieren backend, frontend y
base de datos levantados previamente.

## E2E Playwright

Los tests end-to-end viven en:

```text
e2e/
```

Instalar Chromium:

```powershell
npm run e2e:install
```

Ejecutar E2E contra `http://localhost:4200` y API `http://localhost:8080`:

```powershell
npm run e2e
```

Variables opcionales:

```powershell
$env:E2E_BASE_URL="http://localhost:4200"
$env:E2E_API_BASE_URL="http://localhost:8080"
npm run e2e
```

Guia completa:

```text
../docs/21_E2E_TESTING.md
```

## Rutas iniciales

- `/login`
- `/register`
- `/dashboard` protegida.
- `/shops` protegida.
- `/shops/new` protegida.
- `/shops/:id` publica.
- `/shops/:shopId/inventory` protegida.
- `/shops/:shopId/inventory/new` protegida.
- `/shops/:shopId/inventory/:shopProductId/edit` protegida.
- `/shops/:shopId/reservations` protegida.
- `/shop-products/:shopProductId` publica.
- `/collections` protegida.
- `/collections/new` protegida.
- `/collections/:collectionId` publica/protegida segun visibilidad backend.
- `/collections/:collectionId/edit` protegida.
- `/collections/:collectionId/items/new` protegida.
- `/collections/:collectionId/items/:itemId/edit` protegida.
- `/recommendations` protegida.
- `/reservations` protegida.
- `/reservations/:reservationId` protegida.
- `/catalog` publica.
- `/catalog/new` protegida; la pantalla solo permite crear a `SHOP_OWNER` o `ADMIN`.
- `/catalog/:id` publica.
- `/`
- `**` redirige de forma controlada a dashboard/login.

## Modulos MVP frontend

- Autenticacion: login, registro, sesion local y dashboard autenticado.
- Tiendas: listado de tiendas propias, creacion de tienda y detalle publico.
- Catalogo maestro: listado/busqueda publica, detalle publico y creacion protegida por rol.
- Inventario de tienda: listado interno, alta, edicion y detalle publico de producto de tienda.
- Colecciones personales: listado propio, creacion, detalle, edicion, borrado e items de coleccion.
- Recomendaciones: resumen, filtros y listado de productos de tienda que coinciden con items `MISSING` o `WANTED`.
- Reservas: creacion desde producto de tienda, listado propio, detalle y gestion de reservas de tienda.

Los servicios frontend usan los endpoints MVP documentados:

- `GET /api/shops/my`
- `POST /api/shops`
- `GET /api/shops/{id}`
- `GET /api/product-categories`
- `GET /api/master-products`
- `POST /api/master-products`
- `GET /api/master-products/{id}`
- `GET /api/shops/{shopId}/products/my`
- `POST /api/shops/{shopId}/products`
- `PUT /api/shops/{shopId}/products/{shopProductId}`
- `GET /api/shops/{shopId}/products`
- `GET /api/shop-products/{shopProductId}`
- `GET /api/collections/my`
- `POST /api/collections`
- `GET /api/collections/{collectionId}`
- `PUT /api/collections/{collectionId}`
- `DELETE /api/collections/{collectionId}`
- `GET /api/collections/{collectionId}/items`
- `POST /api/collections/{collectionId}/items`
- `PUT /api/collections/{collectionId}/items/{itemId}`
- `DELETE /api/collections/{collectionId}/items/{itemId}`
- `GET /api/recommendations/my`
- `GET /api/recommendations/my/summary`
- `POST /api/reservations`
- `GET /api/reservations/my`
- `GET /api/reservations/{reservationId}`
- `GET /api/shops/{shopId}/reservations`
- `PUT /api/shops/{shopId}/reservations/{reservationId}/status`
- `PUT /api/reservations/{reservationId}/cancel`

## Sesion

En el MVP se usa `localStorage` para guardar `accessToken`, `refreshToken` y
datos publicos del usuario autenticado. No se muestran tokens en pantalla ni se
registran en logs.

Si el usuario crea su primera tienda, el backend le asigna `SHOP_OWNER` en base
de datos. El JWT actual no cambia; el usuario debe volver a iniciar sesion o usar
un futuro refresh token para ver los roles actualizados.
