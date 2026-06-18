# CollectoHub Frontend

SPA Angular del MVP de CollectoHub. Consume el backend real en
`http://localhost:8080` y cubre auth, dashboard, tiendas, catalogo, inventario,
colecciones, recomendaciones y reservas.

## Stack

- Angular 21.
- TypeScript.
- Angular Material.
- Routing standalone.
- Formularios reactivos.
- HTTP Client con interceptor JWT.
- SCSS.
- i18n ligero propio ES/EN.
- Playwright para E2E locales.

## Requisitos

- Node.js 24.x.
- npm 11.x.
- Backend local en `http://localhost:8080`.

En Windows PowerShell, si `npm` falla por politica de ejecucion de scripts, usa
`npm.cmd`:

```powershell
npm.cmd ci
npm.cmd test -- --watch=false
npm.cmd run build
```

## Instalacion Y Comandos

```powershell
cd frontend
npm ci
npm test -- --watch=false
npm run build
npm start
```

Abrir:

```text
http://localhost:4200
```

El workflow de CI ejecuta:

```powershell
npm ci
npm test -- --watch=false
npm run build
```

## Backend Local

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

## E2E Playwright

Los tests end-to-end viven en:

```text
frontend/e2e/
```

Instalar Chromium:

```powershell
cd frontend
npm run e2e:install
```

Ejecutar E2E contra `http://localhost:4200` y API `http://localhost:8080`:

```powershell
cd frontend
npm run e2e
```

Modo headed y UI:

```powershell
cd frontend
npm run e2e:headed
npm run e2e:ui
```

Variables opcionales:

```powershell
$env:E2E_BASE_URL="http://localhost:4200"
$env:E2E_API_BASE_URL="http://localhost:8080"
npm run e2e
```

Los E2E no se ejecutan todavia en CI porque requieren backend, frontend y base de
datos levantados previamente. Guia completa: `../docs/21_E2E_TESTING.md`.

## Rutas MVP

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
- `/` redirige a dashboard/login.
- `**` redirige de forma controlada a dashboard/login.

## Modulos MVP Frontend

- Autenticacion: login, registro, sesion local y dashboard autenticado.
- Tiendas: listado de tiendas propias, creacion de tienda y detalle publico.
- Catalogo maestro: listado/busqueda publica, detalle publico y creacion protegida por rol.
- Inventario de tienda: listado interno, alta, edicion y detalle publico de producto de tienda.
- Colecciones personales: listado propio, creacion, detalle, edicion, borrado e items.
- Recomendaciones: resumen, filtros y listado de productos de tienda que coinciden con items `MISSING` o `WANTED`.
- Reservas: creacion desde producto de tienda, listado propio, detalle y gestion de reservas de tienda.

## UI/UX MVP

- Layout tipo producto social con sidebar desktop, navegacion inferior movil y panel contextual derecho.
- Sistema visual dark-soft con variables SCSS, tarjetas de radio maximo 8px, chips, badges, tokens visuales y estados vacios.
- Login, registro y dashboard redisenados como primeras pantallas de producto, manteniendo formularios reactivos y guards.
- Listados y formularios MVP usan una jerarquia visual comun para tiendas, catalogo, inventario, colecciones, recomendaciones y reservas.
- Los `data-testid` de Playwright se mantienen para no fragilizar los E2E.

Guia de la fase: `../docs/23_UI_UX_REDESIGN.md`.

## Internacionalizacion

- Idiomas soportados: `es` y `en`.
- Selector visible en layout principal, login y registro.
- Idioma persistido en `localStorage` con la clave `collectohub.language`.
- Registro sincroniza `preferredInterfaceLanguage` con el idioma activo.
- Enums visibles se traducen sin cambiar los valores enviados al backend.
- Los datos dinamicos de usuarios, tiendas, productos, colecciones y API no se traducen.

Guia de la fase: `../docs/24_I18N_FRONTEND.md`.

## Endpoints Consumidos

Los servicios frontend usan los endpoints MVP documentados en
`../docs/16_MVP_API_ENDPOINTS.md`, incluyendo:

- Auth y usuario: `/api/auth/register`, `/api/auth/login`, `/api/users/me`.
- Tiendas: `/api/shops`, `/api/shops/my`, `/api/shops/{id}`.
- Catalogo: `/api/product-categories`, `/api/master-products`.
- Inventario: `/api/shops/{shopId}/products`, `/api/shop-products/{shopProductId}`.
- Colecciones: `/api/collections`, `/api/collections/my`, `/api/collections/{collectionId}/items`.
- Recomendaciones: `/api/recommendations/my`, `/api/recommendations/my/summary`.
- Reservas: `/api/reservations`, `/api/reservations/my`, `/api/shops/{shopId}/reservations`.

## Sesion

En el MVP se usa `localStorage` para guardar `accessToken`, `refreshToken` y
datos publicos del usuario autenticado. No se muestran tokens en pantalla ni se
registran en logs.

Si el usuario crea su primera tienda, el backend le asigna `SHOP_OWNER` en base
de datos. El JWT actual no cambia; el usuario debe volver a iniciar sesion o usar
un futuro refresh token para ver los roles actualizados.

## Demo

Flujo recomendado desde UI:

```text
../docs/18_DEMO_FLOW.md
```
