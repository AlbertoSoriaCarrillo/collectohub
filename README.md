# CollectoHub

CollectoHub es el nombre provisional de una plataforma para gestionar colecciones personales, conectar usuarios con tiendas especializadas y evolucionar hacia una red social/marketplace para coleccionistas.

## Estado del repositorio

Este repositorio está en fase inicial. La primera etapa no debe intentar construir toda la red social completa, sino validar el núcleo del producto:

1. Usuarios y autenticación.
2. Tiendas y propietarios de tienda.
3. Catálogo maestro de productos.
4. Inventario de tiendas.
5. Colecciones personales.
6. Reservas sin pago.
7. Recomendaciones básicas entre productos buscados y stock disponible.

## Stack objetivo

- Backend: Java 25, Spring Boot 4.x, Spring Security, JWT + refresh token, Spring Data JPA, PostgreSQL, Liquibase, Maven.
- Frontend: Angular, TypeScript, Angular Material, PWA y diseño responsive.
- Calidad: JUnit, Mockito, Testcontainers, SonarQube, GitHub Actions.
- Infraestructura local: Docker Compose.

## Ejecucion local

### Backend

Requisitos:

- Java 25.
- PostgreSQL local con la base de datos `collectohub`.
- Usuario `collectohub` y password `collectohub_local_password`.

Comandos:

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

El backend escucha en `http://localhost:8080` y expone `GET /api/health`.

### Frontend

Requisitos:

- Node.js 24.x.
- npm 11.x.
- Backend local en `http://localhost:8080`.

Comandos:

```powershell
cd frontend
npm ci
npm test -- --watch=false
npm run build
npm start
```

El frontend escucha en `http://localhost:4200` y consume la API local del backend.

## Validacion y demo

- Endpoints MVP: `docs/16_MVP_API_ENDPOINTS.md`.
- Flujo manual por API: `docs/17_MANUAL_TESTING_FLOW.md`.
- Flujo de demo desde la UI: `docs/18_DEMO_FLOW.md`.
- Estado MVP, limitaciones y pasos siguientes: `docs/19_MVP_STATUS.md`.

## Orden de desarrollo

El desarrollo debe empezar por el backend. Después se desarrollará el frontend Angular consumiendo la API real.

La documentación principal para Codex está en la carpeta `docs/` y en `PROMPT_FOR_CODEX.md`.

## Nota

El nombre CollectoHub es provisional y no representa necesariamente el nombre comercial definitivo.
