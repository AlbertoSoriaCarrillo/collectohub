# DevOps y CI/CD

## Objetivo

Desde el inicio debe existir una base local reproducible y una integración continua básica.

## Docker Compose

Debe existir `infra/docker-compose.yml`.

Servicios mínimos MVP:

- PostgreSQL.
- Backend cuando exista.

Servicios opcionales futuros:

- pgAdmin.
- MinIO.
- SonarQube.
- Frontend.
- OpenSearch.

## Variables de entorno

No subir secretos.

Usar `.env.example` para documentar variables.

Variables previstas:

```text
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
JWT_SECRET
JWT_ACCESS_EXPIRATION
JWT_REFRESH_EXPIRATION
FILES_STORAGE_PATH
```

## GitHub Actions

Debe existir workflow inicial.

Cuando backend exista:

- Instalar Java.
- Ejecutar Maven.
- Ejecutar tests.
- Ejecutar análisis si Sonar está configurado.

Cuando frontend exista:

- Instalar Node.
- Ejecutar lint.
- Ejecutar tests.
- Ejecutar build.

## SonarQube

Configurar cuando exista backend mínimo.

## Ramas

Recomendación:

- `main`: estable.
- `develop`: integración.
- `feature/...`: tareas.

En MVP puede trabajarse directamente con PRs hacia `main` si el repositorio está privado y solo trabaja una persona, pero debe mantenerse orden.

## Commits

Formato recomendado:

```text
feat: add catalog module
fix: validate reservation ownership
docs: update roadmap
chore: configure docker compose
test: add auth integration tests
```

## Entornos

- Local.
- Test.
- Futuro staging.
- Futuro producción.
