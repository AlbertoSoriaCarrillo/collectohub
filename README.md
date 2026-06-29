# CollectoHub

CollectoHub es una plataforma en evolucion para coleccionistas, tiendas y
creadores de contenido, apoyada en un catalogo comun de objetos coleccionables.

## Foco Actual

**MVP 1: gestion de colecciones personales usando un catalogo comun.**

El recorrido visible ayuda a una persona coleccionista a explorar libros,
comics y manga, crear colecciones, marcar lo que tiene o busca y revisar sus
faltantes. Tiendas, inventario y reservas se conservan como base tecnica y rutas
legacy/futuras, fuera del flujo principal.

## Vision A Largo Plazo

**Plataforma para coleccionistas, tiendas y creadores de contenido, con catalogo
comun, matching oferta/demanda y funciones sociales.**

La vision larga incluye una biblioteca/wiki de objetos coleccionables, social,
matching con stock de tiendas, comercio y herramientas para creadores. Estas
capacidades se desarrollan por fases y no deben interpretarse como un
marketplace, pagos o tiendas ya listos para usuario final.

## Estado Del Proyecto

- MVP 1 cerrado como base tecnica/producto el 2026-06-29; checklist completo en
  `docs/07_MVP1_ACCEPTANCE_CHECKLIST.md`.
- MVP funcional de backend y frontend.
- Docker Compose local con PostgreSQL, backend y frontend validado.
- Tests backend, tests frontend y E2E Playwright implementados.
- CI en GitHub Actions para documentacion, backend, frontend unit tests y build.
- E2E fuera de CI por decision tecnica para evitar un pipeline mas lento y fragil.

## Stack Tecnologico

| Area | Tecnologias |
| --- | --- |
| Backend | Java 25, Spring Boot 4.1.x, Spring Security, Spring Data JPA |
| Seguridad | JWT, refresh token basico, roles globales e internos de tienda |
| Base de datos | PostgreSQL, Liquibase |
| Frontend | Angular 21, TypeScript, Angular Material, SCSS |
| Infra local | Docker Compose, nginx para frontend Docker |
| Testing | JUnit, Mockito, Testcontainers, Vitest/Angular Test, Playwright |
| CI | GitHub Actions |

## Funcionalidades Implementadas

- Home publica orientada a coleccionistas.
- Registro y login con email/password.
- Perfil autenticado con datos basicos de usuario.
- Catalogo maestro publico de obras reutilizables.
- Colecciones personales con items `OWNED`, `WANTED`, `MISSING`, etc.
- Vista `Buscados` para revisar elementos deseados o faltantes.
- i18n frontend ES/EN y layout responsive.
- Backend con tiendas, inventario y reservas conservados como base tecnica/futura.
- Swagger/OpenAPI en `http://localhost:8080/swagger-ui.html`.
- Tests automatizados backend, frontend y E2E locales.

## Arquitectura Resumida

El backend es un monolito modular Spring Boot. Cada area funcional vive en su
modulo de paquete: `auth`, `users`, `shops`, `catalog`, `inventory`,
`collections`, `recommendations`, `reservations`, `shared` y `config`.

La base de datos es unica y PostgreSQL. El aislamiento entre tiendas y usuarios
se resuelve con reglas de dominio, roles internos de tienda y comprobaciones de
pertenencia en servicios backend. Las migraciones se gestionan con Liquibase.

El frontend es una SPA Angular standalone con Angular Material. Consume la API
real del backend, usa guards para rutas protegidas e interceptor HTTP para JWT.
La navegacion principal actual prioriza `Home`, `Catalogo`, `Colecciones`,
`Buscados` y `Perfil`; las rutas de tiendas/inventario/reservas siguen
disponibles manualmente para no romper el trabajo tecnico previo.

## URLs Locales

| Servicio | URL |
| --- | --- |
| Frontend | `http://localhost:4200` |
| Backend | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| PostgreSQL | `localhost:5432` |

## Arranque Con Docker Compose

Requisitos: Docker Desktop o Docker Engine con Docker Compose.

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

Parar servicios:

```powershell
cd infra
docker compose down
```

Limpiar tambien el volumen de PostgreSQL:

```powershell
cd infra
docker compose down -v
```

## Arranque Clasico

Backend:

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Frontend:

```powershell
cd frontend
npm ci
npm test -- --watch=false
npm run build
npm start
```

Si PowerShell bloquea `npm.ps1`, usa `npm.cmd`:

```powershell
cd frontend
npm.cmd ci
npm.cmd test -- --watch=false
npm.cmd run build
npm.cmd start
```

## Tests

Backend:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Frontend:

```powershell
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

E2E Playwright, con backend y frontend ya levantados:

```powershell
cd frontend
npm run e2e:install
npm run e2e
```

Modo headed o UI:

```powershell
cd frontend
npm run e2e:headed
npm run e2e:ui
```

## Datos de demo local

Con backend y base de datos levantados, puedes generar datos locales de demo
usando la API real. El script sigue creando tambien datos legacy de tienda,
inventario y reserva, pero las URLs principales impresas priorizan Home,
Catalogo, Colecciones, Buscados y Perfil:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -Suffix "demo001"
```

Si omites `-Suffix`, el script genera uno automaticamente. Guia completa:
`docs/25_DEMO_DATA.md`.

Si PowerShell bloquea scripts locales:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\demo\create-demo-data.ps1
```

## Demo / Screenshots

No hay capturas reales versionadas todavia. Las futuras imagenes de portfolio se
pueden guardar en:

```text
docs/assets/screenshots/
```

Capturas recomendadas:

| Vista | Captura sugerida |
| --- | --- |
| Home | Propuesta de valor coleccionista |
| Login | Formulario de acceso |
| Catalogo | Busqueda y ficha de obra |
| Colecciones | Biblioteca personal y detalle |
| Buscados | Elementos `WANTED`/`MISSING` |
| Perfil | Datos basicos y roles |
| Swagger | OpenAPI con JWT |
| Playwright E2E | Ejecucion local correcta |

## Documentacion Principal

- Vision completa: `docs/00_PRODUCT_VISION.md`.
- Roadmap por fases: `docs/01_ROADMAP.md`.
- Alcance MVP 1: `docs/02_MVP1_SCOPE.md`.
- Dominios de producto: `docs/03_PRODUCT_DOMAINS.md`.
- Modelo conceptual de catalogo: `docs/04_CATALOG_CONCEPT_MODEL.md`.
- Matching usuario-tienda: `docs/05_USER_SHOP_MATCHING.md`.
- Social y creadores: `docs/06_SOCIAL_AND_CREATORS.md`.
- Checklist de cierre MVP 1: `docs/07_MVP1_ACCEPTANCE_CHECKLIST.md`.
- Backlog posterior: `docs/08_NEXT_BACKLOG.md`.
- Endpoints MVP: `docs/16_MVP_API_ENDPOINTS.md`.
- Flujo manual por API: `docs/17_MANUAL_TESTING_FLOW.md`.
- Demo desde UI: `docs/18_DEMO_FLOW.md`.
- Estado MVP: `docs/19_MVP_STATUS.md`.
- Despliegue local: `docs/20_DEPLOYMENT_LOCAL.md`.
- E2E Playwright: `docs/21_E2E_TESTING.md`.
- Revision portfolio/entrevista: `docs/22_PORTFOLIO_REVIEW.md`.
- Reenfoque producto libros/comics/manga: `docs/26_PRODUCT_REFOCUS_BOOKS.md`.
- Navegacion publica y header: `docs/27_PUBLIC_NAVIGATION_UX.md`.
- Datos de demo local: `docs/25_DEMO_DATA.md`.

## Documentacion tecnica exportable

La estructura real de base de datos, los endpoints backend, las rutas frontend
y el mapa pantalla-servicio-endpoint estan disponibles en `docs/export/` en
formatos Markdown, Mermaid y CSV descargables.

## Limitaciones Actuales

- No hay pagos ni bloqueo transaccional de stock.
- Las reservas no caducan mediante job automatico.
- Las tiendas, inventario y reservas estan implementadas en backend/UI legacy,
  pero no forman parte del recorrido principal actual.
- No hay chat, feed social ni marketplace avanzado.
- No hay OAuth, 2FA, email ni notificaciones.
- No hay subida real de imagenes o archivos.
- La sesion frontend usa `localStorage`, aceptado solo como simplificacion MVP.
- Los E2E requieren entorno local levantado y no se ejecutan en CI.
- La configuracion Docker local no equivale a despliegue productivo.

## Proximas Fases Posibles

- Consolidar el catalogo editorial con franquicias, colecciones de catalogo,
  items, autores, editoriales, relaciones e imagenes.
- Validar despues social basico y, en fases separadas, matching con tiendas,
  comercio real y herramientas para creadores.
- Ejecutar E2E en CI con un job separado y estable.
- Revisar vulnerabilidades npm transitivas y estrategia de actualizacion.
- Endurecer seguridad para produccion: cookies/refresh, CSRF segun estrategia,
  secretos gestionados y rotacion.
- Gestion real de imagenes y almacenamiento externo.
- Panel administrativo global.
- Internacionalizacion avanzada y PWA completa.

El orden estrategico completo se mantiene en `docs/01_ROADMAP.md`.

## Nota

La entrega actual de CollectoHub es un MVP/portfolio. Su objetivo es demostrar
arquitectura, criterio tecnico y un flujo de producto completo en local, no
operar todavia la plataforma productiva descrita en la vision larga.
