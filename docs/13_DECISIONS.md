# Decisiones del proyecto

## 2026-06-16 - Nombre provisional

- Decisión: usar CollectoHub como nombre provisional.
- Motivo: permite trabajar con un nombre estable sin decidir todavía marca comercial.

## 2026-06-16 - Arquitectura inicial

- Decisión: monorepo y monolito modular.
- Motivo: reduce complejidad inicial y facilita validar el MVP.

## 2026-06-16 - Backend primero

- Decisión: desarrollar primero el backend.
- Motivo: el backend define seguridad, datos, reglas de negocio y API.

## 2026-06-16 - Base de datos

- Decisión: usar PostgreSQL con una única base de datos.
- Motivo: no se ven beneficios suficientes en separar bases por tienda durante MVP.

## 2026-06-16 - Multitenancy lógico

- Decisión: aislamiento lógico por tienda desde la capa Java y mediante claves como shop_id o tenant_id.
- Motivo: permite separar datos de tiendas sin complejidad de múltiples bases.

## 2026-06-16 - Frontend

- Decisión: Angular con TypeScript y Angular Material.
- Motivo: estructura más cerrada y mantenible para una aplicación grande.

## 2026-06-16 - Reservas

- Decisión: en MVP habrá reservas sin pago.
- Motivo: permite validar la conexión usuario-tienda sin la complejidad legal y técnica de pagos.

## 2026-06-16 - Social y marketplace

- Decisión: dejar chat, feed social, comentarios multimedia, pagos y marketplace completo para fases futuras.
- Motivo: evitar que el MVP sea inabarcable.

## 2026-06-16 - Idiomas

- Decisión: castellano e inglés desde el inicio.
- Motivo: preparar la plataforma para crecimiento internacional.

## 2026-06-16 - Versiones backend iniciales

- Decision: usar Java 25, Spring Boot 4.1.0, springdoc-openapi 3.0.3 y Testcontainers 2.0.5.
- Motivo: Java 25 esta disponible en el entorno y las versiones indicadas estan publicadas en Maven Central. En Testcontainers 2.x se usan los artefactos `testcontainers-*`.

## 2026-06-16 - Base de datos MVP inicial

- Decision: usar claves primarias `BIGINT` autoincrementales para las tablas MVP iniciales.
- Motivo: simplifica el arranque del monolito modular y evita extensiones PostgreSQL adicionales para generar identificadores.
- Decision: usar columnas `JSONB` para `master_products.publication_countries`, `master_products.attributes` y `product_suggestions.data`.
- Motivo: coincide con la especificacion para atributos flexibles sin mover datos esenciales consultados frecuentemente a JSONB.
- Decision: separar configuracion base, `local` y `test` en `application.yml`, `application-local.yml` y `application-test.yml`.
- Motivo: deja mas visible la configuracion local de PostgreSQL y facilita usar Testcontainers en tests.

## 2026-06-16 - Maven Wrapper backend

- Decision: usar Apache Maven 3.9.11 mediante Maven Wrapper 3.3.4 en modo `only-script`.
- Motivo: Maven 3.9.11 ya se verifico con Java 25 y Spring Boot 4.1.0, evita exigir Maven global en Windows y no requiere versionar `maven-wrapper.jar`.

## 2026-06-16 - Autoconfiguracion Liquibase en Spring Boot 4

- Decision: incluir `org.springframework.boot:spring-boot-liquibase` ademas de `org.liquibase:liquibase-core`.
- Motivo: en Spring Boot 4 la autoconfiguracion de Liquibase esta en un modulo separado; `liquibase-core` por si solo no registra `LiquibaseAutoConfiguration`.

## 2026-06-16 - Edad recomendada

- Decisión: plataforma recomendada para mayores de 18 años.
- Motivo: en fases futuras habrá contenido publicado por usuarios y tiendas difícil de controlar completamente.
