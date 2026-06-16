# Testing y calidad

## Objetivo

El proyecto debe tener tests desde el inicio. Codex no debe crear solo código funcional sin pruebas.

## Backend

Herramientas:

- JUnit.
- Mockito.
- Spring Boot Test.
- Testcontainers.
- PostgreSQL real en tests de integración.

## Tipos de tests

### Unitarios

Para:

- Servicios de aplicación.
- Reglas de negocio.
- Validaciones.
- Mappers si contienen lógica.

### Integración

Para:

- Controladores REST.
- Repositorios.
- Seguridad.
- Migraciones Liquibase.
- Acceso a PostgreSQL con Testcontainers.

## Cobertura mínima recomendada

MVP:

- Auth: login, registro, refresh, errores.
- Users: roles acumulables.
- Shops: aislamiento por tienda.
- Catalog: creación, duplicados, sugerencias.
- Inventory: stock, estados, permisos.
- Collections: estados y visibilidad.
- Reservations: flujo completo.
- Recommendations: productos faltantes disponibles.

## Frontend

Cuando se cree frontend:

- Tests de servicios Angular.
- Tests de componentes críticos.
- Tests de guards.
- Tests de formularios.

## Calidad

- SonarQube configurado.
- GitHub Actions debe ejecutar tests.
- No permitir código muerto evidente.
- No permitir duplicación innecesaria.
- No permitir secretos en repositorio.

## Criterio de aceptación

Una tarea no está completa si:

- No compila.
- No tiene tests mínimos.
- No está documentada.
- Rompe CI.
- No actualiza `docs/12_TASK_LOG.md`.
