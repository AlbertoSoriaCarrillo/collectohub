# Prompt inicial para Codex

Usa este repositorio como monorepo para desarrollar CollectoHub.

CollectoHub es el nombre provisional de una plataforma para gestionar colecciones personales, conectar usuarios con tiendas especializadas y evolucionar hacia una red social/marketplace de coleccionistas.

## Regla principal

No intentes construir toda la plataforma a la vez.

Primero desarrolla el MVP de backend:

1. Autenticación email/password con JWT + refresh token.
2. Usuarios con roles acumulables.
3. Tiendas con propietario y miembros.
4. Aislamiento lógico por tienda usando una única base de datos PostgreSQL.
5. Catálogo maestro de productos.
6. Inventario de tienda.
7. Colecciones personales.
8. Reservas sin pago.
9. Recomendaciones básicas.
10. Tests unitarios e integración.
11. Swagger/OpenAPI.
12. Docker Compose.
13. GitHub Actions.

## Stack objetivo

Backend:

- Java 25.
- Spring Boot 4.x.
- Spring Security.
- JWT + refresh token.
- Spring Data JPA.
- PostgreSQL.
- Liquibase.
- Maven.
- JUnit + Mockito.
- Testcontainers.
- Swagger/OpenAPI.
- SonarQube.

Frontend, fase posterior:

- Angular.
- TypeScript.
- Angular Material.
- PWA.
- Web responsive.
- Multiidioma castellano e inglés desde el inicio.

## Arquitectura

Usa monolito modular, no microservicios.

Estructura recomendada del backend:

```text
backend/src/main/java/com/collectohub
 ├── auth
 ├── users
 ├── shops
 ├── catalog
 ├── inventory
 ├── collections
 ├── reservations
 ├── recommendations
 ├── files
 ├── i18n
 ├── audit
 ├── shared
 └── config
```

## Reglas de Codex

- Lee primero todos los documentos de `docs/`.
- Respeta la diferencia entre MVP y evolutivos.
- No añadas pagos, chat, feed social ni app móvil en el MVP.
- No inventes decisiones arquitectónicas que contradigan `docs/13_DECISIONS.md`.
- Actualiza siempre `docs/12_TASK_LOG.md` cuando completes una tarea.
- Si una tarea requiere una decisión no documentada, anótala en `docs/13_DECISIONS.md` antes de implementarla.
- Cada módulo debe incluir tests.
- Cada endpoint debe documentarse con Swagger/OpenAPI.
- Usa DTOs. No expongas entidades JPA directamente.
- Aplica borrado lógico y auditoría en entidades principales.
- Evita dependencias innecesarias.

## Orden de trabajo obligatorio

1. Crear proyecto backend base.
2. Configurar PostgreSQL + Liquibase.
3. Implementar seguridad y usuarios.
4. Implementar tiendas y multitenancy lógico.
5. Implementar catálogo maestro.
6. Implementar inventario de tienda.
7. Implementar colecciones personales.
8. Implementar reservas.
9. Implementar recomendaciones básicas.
10. Implementar tests.
11. Implementar CI/CD.
12. Empezar frontend Angular cuando el backend esté estable.
