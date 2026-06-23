# CollectoHub

CollectoHub es un MVP de red social/catalogo para gestionar colecciones de
libros, comics y manga.

El foco actual de producto esta en ayudar a una persona coleccionista a explorar
un catalogo, crear colecciones personales, marcar lo que tiene o busca y revisar
su perfil. El backend conserva los modulos de tiendas, inventario y reservas ya
implementados, pero el frontend principal los deja como rutas legacy/futuras para
mantener el MVP centrado y coherente.

## Estado Del Proyecto

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

- Endpoints MVP: `docs/16_MVP_API_ENDPOINTS.md`.
- Flujo manual por API: `docs/17_MANUAL_TESTING_FLOW.md`.
- Demo desde UI: `docs/18_DEMO_FLOW.md`.
- Estado MVP: `docs/19_MVP_STATUS.md`.
- Despliegue local: `docs/20_DEPLOYMENT_LOCAL.md`.
- E2E Playwright: `docs/21_E2E_TESTING.md`.
- Revision portfolio/entrevista: `docs/22_PORTFOLIO_REVIEW.md`.
- Reenfoque producto libros/comics/manga: `docs/26_PRODUCT_REFOCUS_BOOKS.md`.
- Datos de demo local: `docs/25_DEMO_DATA.md`.

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

- Ejecutar E2E en CI con un job separado y estable.
- Revisar vulnerabilidades npm transitivas y estrategia de actualizacion.
- Endurecer seguridad para produccion: cookies/refresh, CSRF segun estrategia,
  secretos gestionados y rotacion.
- Gestion real de imagenes y almacenamiento externo.
- Pagos y reservas con bloqueo de stock.
- Panel administrativo global.
- Internacionalizacion avanzada y PWA completa.

## Nota

CollectoHub es un MVP/portfolio. El objetivo actual es demostrar arquitectura,
criterio tecnico y un flujo de producto completo en local, no operar una
plataforma productiva real.
