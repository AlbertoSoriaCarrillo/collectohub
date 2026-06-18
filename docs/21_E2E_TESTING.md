# CollectoHub E2E testing

Esta guia explica la primera capa de tests end-to-end del MVP con Playwright.
Los E2E validan flujos reales desde la UI Angular contra el backend y la base de
datos reales levantados en local.

## Que son los E2E

Los tests end-to-end abren un navegador real y ejecutan acciones similares a las
de una persona: registro, login, creacion de tienda, catalogo, inventario,
colecciones, recomendaciones y reservas.

No sustituyen los tests unitarios ni de backend. Cubren integracion visible entre
frontend, backend y base de datos.

## Ubicacion

Los E2E estan dentro de:

```text
frontend/e2e/
```

Estructura:

```text
frontend/e2e/helpers/
frontend/e2e/specs/
```

## Requisitos

- Backend levantado en `http://localhost:8080`.
- Frontend levantado en `http://localhost:4200`.
- Base de datos PostgreSQL disponible.
- Dependencias frontend instaladas con `npm ci`.
- Chromium instalado mediante Playwright.

## Levantar entorno con Docker Compose

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

Comprobar:

```powershell
curl.exe http://localhost:8080/api/health
```

Abrir:

```text
http://localhost:4200
```

## Levantar entorno clasico

Terminal 1:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Terminal 2:

```powershell
cd frontend
npm start
```

## Instalar navegadores Playwright

Desde `frontend`:

```powershell
npm run e2e:install
```

Esta fase instala solo Chromium.

Si PowerShell bloquea `npm.ps1`, usa:

```powershell
npm.cmd run e2e:install
```

## Ejecutar E2E

Desde `frontend`:

```powershell
npm run e2e
```

Alternativa con `npm.cmd`:

```powershell
npm.cmd run e2e
```

Variables opcionales:

```powershell
$env:E2E_BASE_URL="http://localhost:4200"
$env:E2E_API_BASE_URL="http://localhost:8080"
npm run e2e
```

## Ejecutar en modo headed

```powershell
npm run e2e:headed
```

Alternativa:

```powershell
npm.cmd run e2e:headed
```

## Abrir Playwright UI

```powershell
npm run e2e:ui
```

Alternativa:

```powershell
npm.cmd run e2e:ui
```

## Tests actuales

- `01-smoke.spec.ts`: comprueba shell frontend, toolbar, login/dashboard y health del backend.
- `02-auth-dashboard.spec.ts`: registra usuario, valida dashboard, logout y login.
- `03-mvp-flow.spec.ts`: valida flujo MVP principal desde UI con datos unicos.

## Datos de prueba

Los tests generan datos unicos con timestamp y sufijo aleatorio:

```text
e2e-<suffix>@collectohub.local
E2E Shop <suffix>
E2E Manga Volume <suffix>
E2E Collection <suffix>
```

No se crean endpoints especiales ni reset automatico de base de datos.

Para limpiar estado local con Docker:

```powershell
cd infra
docker compose down -v
docker compose up --build
```

## Interpretar errores

- Si falla health: el backend no esta levantado o `E2E_API_BASE_URL` apunta mal.
- Si falla al abrir `/login` o `/register`: el frontend no esta levantado o `E2E_BASE_URL` apunta mal.
- Si falla una seleccion de producto/categoria: revisa que Liquibase haya cargado categorias y que el backend este usando la base correcta.
- Si falla reserva/recomendaciones: revisa que el flujo haya creado inventario visible y un item `MISSING`.

Playwright guarda screenshots solo en fallo y trazas cuando hay fallo o retry.

## Limitaciones conocidas

- Los E2E no se ejecutan en GitHub Actions todavia.
- No hay reset automatico de base de datos.
- No se arranca backend/frontend automaticamente desde Playwright.
- Los tests usan Chromium como primer navegador soportado.
- La suite esta pensada como smoke E2E local, no como matriz completa cross-browser.
