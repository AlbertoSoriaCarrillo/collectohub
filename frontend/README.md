# CollectoHub Frontend

SPA Angular del MVP de CollectoHub. Consume el backend real en
`http://localhost:8080` y presenta el producto como una red social/catalogo para
gestionar colecciones de libros, comics y manga.

La navegacion principal se centra en Home, Catalogo, Colecciones, Buscados y
Perfil. El acceso de registro no se muestra como CTA global: se llega a
`/register` desde el enlace inferior de login. Las pantallas de tiendas,
inventario y reservas siguen existiendo como rutas legacy/futuras para no romper
el backend ni los flujos ya implementados, pero no se promocionan en el
recorrido principal.

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

- `/home` publica.
- `/login`
- `/register`, accesible desde login o por URL manual.
- `/collections` protegida.
- `/collections/new` protegida.
- `/collections/:collectionId` publica/protegida segun visibilidad backend.
- `/collections/:collectionId/edit` protegida.
- `/collections/:collectionId/items/new` protegida.
- `/collections/:collectionId/items/:itemId/edit` protegida.
- `/wanted` protegida.
- `/profile` protegida.
- `/catalog` publica.
- `/catalog/new` protegida; la pantalla solo permite crear a `SHOP_OWNER` o `ADMIN`.
- `/catalog/:id` publica.
- `/` redirige a `/home`.
- `/dashboard` redirige a `/home`.
- `/recommendations` redirige a `/wanted`.
- `**` redirige de forma controlada a `/home`.

Rutas legacy/futuras conservadas:

- `/shops`, `/shops/new`, `/shops/:id`.
- `/shops/:shopId/inventory`, `/shops/:shopId/inventory/new`, `/shops/:shopId/inventory/:shopProductId/edit`.
- `/shops/:shopId/reservations`.
- `/shop-products/:shopProductId`.
- `/reservations`, `/reservations/:reservationId`.

## Modulos MVP Frontend

- Home publica de producto.
- Autenticacion: login, registro y sesion local.
- Perfil: datos basicos del usuario autenticado y acciones futuras deshabilitadas.
- Catalogo maestro: listado/busqueda publica, detalle publico y creacion protegida por rol.
- Colecciones personales: listado propio, creacion, detalle, edicion, borrado e items.
- Buscados: resumen, filtros y listado de elementos `MISSING` o `WANTED`.
- Legacy/futuro: tiendas, inventario y reservas permanecen accesibles por URL manual.

## UI/UX MVP

- Layout tipo producto social con header global, sidebar desktop solo de navegacion, navegacion inferior movil y contenido central.
- Sistema visual dark-soft con variables SCSS, tarjetas de radio maximo 8px, chips, badges, tokens visuales y estados vacios.
- Login, registro, home, catalogo, colecciones, buscados y perfil como primeras pantallas de producto.
- Listados y formularios MVP usan una jerarquia visual comun para catalogo, colecciones y buscados.
- El header muestra idioma y login para visitante; al iniciar sesion muestra avatar/menu con Perfil y Cerrar sesion.
- Los `data-testid` de Playwright se mantienen para no fragilizar los E2E.

Guia de la fase: `../docs/23_UI_UX_REDESIGN.md`.

## Internacionalizacion

- Idiomas soportados: `es` y `en`.
- Selector principal visible una sola vez en el header global.
- Idioma persistido en `localStorage` con la clave `collectohub.language`.
- Registro mantiene el campo `preferredInterfaceLanguage` como dato enviado al backend y lo sincroniza con el idioma activo.
- Enums visibles se traducen sin cambiar los valores enviados al backend.
- Los datos dinamicos de usuarios, tiendas, productos, colecciones y API no se traducen.

Guia de la fase: `../docs/24_I18N_FRONTEND.md`.

## Endpoints Consumidos

Los servicios frontend usan los endpoints MVP documentados en
`../docs/16_MVP_API_ENDPOINTS.md`, incluyendo:

- Auth y usuario: `/api/auth/register`, `/api/auth/login`, `/api/users/me`.
- Catalogo: `/api/product-categories`, `/api/master-products`.
- Colecciones: `/api/collections`, `/api/collections/my`, `/api/collections/{collectionId}/items`.
- Recomendaciones: `/api/recommendations/my`, `/api/recommendations/my/summary`.

Tambien existen servicios para endpoints legacy/futuros de tiendas, inventario y
reservas porque el backend los conserva.

## Sesion

En el MVP se usa `localStorage` para guardar `accessToken`, `refreshToken` y
datos publicos del usuario autenticado. No se muestran tokens en pantalla ni se
registran en logs.

Si el usuario crea su primera tienda, el backend le asigna `SHOP_OWNER` en base
de datos. El JWT actual no cambia; el usuario debe volver a iniciar sesion o usar
un futuro refresh token para ver los roles actualizados.

## Datos De Demo Local

Desde la raiz del repositorio, con backend y base de datos levantados:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -Suffix "demo001"
```

El script genera datos por API real para capturas y demo visual. Guia completa:
`../docs/25_DEMO_DATA.md`.

## Demo

Flujo recomendado desde UI:

```text
../docs/18_DEMO_FLOW.md
```
