# CollectoHub local deployment

Esta guia explica como levantar el MVP en local con ejecucion clasica o con
Docker Compose. No describe despliegue cloud ni produccion.

## Requisitos

Opcion clasica:

- Java 25.
- PostgreSQL 17 o compatible.
- Node.js 24.x.
- npm 11.x.

Opcion Docker:

- Docker Desktop o Docker Engine.
- Docker Compose v2.

Comprobar Docker:

```powershell
docker --version
docker compose version
```

## Opcion 1 - Ejecucion local clasica

### Base de datos

Crea una base PostgreSQL local con estos valores:

```text
host: 127.0.0.1
port: 5432
database: collectohub
username: collectohub
password: collectohub_local_password
```

Tambien puedes usar solo PostgreSQL desde Compose:

```powershell
cd infra
Copy-Item .env.example .env
docker compose up postgres
```

### Backend

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Comprobar:

```powershell
curl.exe http://localhost:8080/api/health
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

### Frontend

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

Abrir:

```text
http://localhost:4200
```

## Opcion 2 - Docker Compose MVP

Crear variables locales:

```powershell
Copy-Item infra\.env.example infra\.env
```

Levantar servicios desde `infra`:

```powershell
cd infra
docker compose up --build
```

Servicios levantados:

- `postgres`: PostgreSQL 17 con volumen persistente.
- `backend`: Spring Boot con perfil `docker`.
- `frontend`: build Angular servido por nginx.

URLs:

```text
Backend health: http://localhost:8080/api/health
Swagger UI:     http://localhost:8080/swagger-ui.html
Frontend:       http://localhost:4200
PostgreSQL:     localhost:5432
```

Comandos utiles:

```powershell
cd infra
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
docker compose down
docker compose down -v
```

Si ejecutas Compose desde la raiz del repositorio, usa:

```powershell
Copy-Item infra\.env.example infra\.env
docker compose --env-file infra\.env -f infra\docker-compose.yml up --build
```

## Variables principales

El fichero `infra/.env.example` no contiene secretos reales. Copialo a
`infra/.env` y cambia valores si hace falta.

Variables relevantes:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`
- `BACKEND_PORT`
- `FRONTEND_PORT`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION_MINUTES`
- `JWT_REFRESH_EXPIRATION_DAYS`
- `CORS_ALLOWED_ORIGINS`
- `SHOP_DEFAULT_CURRENCY`
- `SHOP_DEFAULT_RESERVATION_EXPIRATION_HOURS`

`JWT_SECRET` es solo un valor local de ejemplo y debe cambiarse fuera de entornos
locales.

No subas `infra/.env` al repositorio.

## Notas de configuracion

- El backend usa `application-docker.yml` con `POSTGRES_HOST=postgres` dentro de Compose.
- Liquibase se ejecuta automaticamente al arrancar el backend.
- El frontend Docker se sirve con nginx en el puerto interno `80`.
- El frontend mantiene `apiBaseUrl = "http://localhost:8080"` para que el navegador del host llame al backend publicado.
- Spring Boot permite CORS para `http://localhost:4200` y `http://127.0.0.1:4200` por defecto.

## Limitaciones MVP

- No hay despliegue cloud.
- No hay HTTPS local automatizado.
- No hay gestion runtime avanzada de variables Angular; el `apiBaseUrl` queda fijado en build.
- No hay subida de archivos ni almacenamiento persistente de uploads.
- No hay job automatico de expiracion de reservas.
- El volumen de PostgreSQL persiste datos hasta ejecutar `docker compose down -v`.
