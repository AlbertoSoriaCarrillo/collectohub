# Registro de avance

## 2026-06-16

Repositorio inicializado con documentación base.

Siguiente paso: crear el backend en la carpeta backend.

## 2026-06-16 - EPIC 1 - Inicializacion backend

- Creado proyecto Maven Spring Boot en `backend`.
- Configurado Java 25 y Spring Boot 4.1.0.
- Creada la estructura modular base bajo `backend/src/main/java/com/collectohub`.
- Configuradas dependencias iniciales de Web, Security, JPA, PostgreSQL, Liquibase, Validation, OpenAPI, JUnit, Mockito y Testcontainers para PostgreSQL.
- Configurados perfiles `local` y `test` usando variables compatibles con `infra/.env.example`.
- Configurado Liquibase con changelog inicial preparado para futuras migraciones.
- Creada clase principal `CollectohubApplication`.
- Creado endpoint tecnico temporal `GET /api/health`.
- Anadidos tests minimos de arranque de contexto, endpoint `/api/health` y contexto con PostgreSQL via Testcontainers.
- Ejecutado `clean verify` con Maven temporal, porque `mvn` no esta instalado en el PATH del entorno.
- Resultado: build correcto; 2 tests ejecutados correctamente y 1 test de Testcontainers saltado por no estar Docker instalado.

## 2026-06-16 - EPIC 2 - Base de datos

- Separada la configuracion comun, `local` y `test` en `application.yml`, `application-local.yml` y `application-test.yml`.
- Configurado el perfil `local` para PostgreSQL en `jdbc:postgresql://127.0.0.1:5432/collectohub` usando variables compatibles con `infra/.env.example`.
- Configurado Liquibase para ejecutarse automaticamente al arrancar la aplicacion.
- Creado changelog inicial versionado con las tablas MVP: `users`, `roles`, `user_roles`, `shops`, `shop_members`, `product_categories`, `master_products`, `product_suggestions`, `shop_products`, `collections`, `collection_items` y `reservations`.
- Anadidos campos de auditoria y borrado logico en las tablas principales.
- Anadidos constraints basicos, claves foraneas, indices minimos recomendados y datos iniciales de roles/categorias.
- Creado test de parseo de changelog Liquibase sin Docker.
- Creado test de integracion con PostgreSQL y Testcontainers para validar tablas, indices, constraints y datos semilla cuando Docker este disponible.
- Anadida documentacion local en `docs/15_LOCAL_DATABASE_SETUP.md`.
- Intentado `mvn clean verify`; no esta instalado en el PATH y no existe `mvnw`.
- Ejecutado `clean verify` con Maven temporal.
- Resultado: build correcto; 5 tests totales, 3 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-16 - Maven Wrapper backend

- Anadido Maven Wrapper dentro de `backend`.
- Configurado Maven Wrapper 3.3.4 en modo `only-script`.
- Fijada la distribucion de Apache Maven 3.9.11.
- Verificado `.\mvnw.cmd -v` en Windows PowerShell.
- Verificado `.\mvnw.cmd clean verify`; resultado correcto con 5 tests totales, 3 ejecutados correctamente y 2 saltados por no estar Docker instalado.
- Verificado `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local`; la aplicacion arranco correctamente y se detuvo despues de confirmar el arranque.

## 2026-06-16 - Correccion Liquibase local

- Revisada la configuracion `application.yml` y `application-local.yml`: `spring.liquibase.enabled=true` y el changelog apunta a `classpath:/db/changelog/db.changelog-master.yaml`.
- Confirmado que el changelog principal existe en `backend/src/main/resources/db/changelog/db.changelog-master.yaml` e incluye los cambios `001-create-mvp-schema.sql` y `002-seed-reference-data.sql`.
- Detectada la causa: el proyecto tenia `liquibase-core`, pero faltaba el modulo `spring-boot-liquibase` necesario para activar la autoconfiguracion de Liquibase en Spring Boot 4.
- Anadida la dependencia `org.springframework.boot:spring-boot-liquibase`.
- Anadido logging de Liquibase en perfil `local`.
- Anadido test `LiquibaseAutoConfigurationClasspathTest` para detectar si la autoconfiguracion de Liquibase desaparece del classpath.
- Ejecutado `.\mvnw.cmd clean verify`; resultado correcto con 6 tests totales, 4 ejecutados correctamente y 2 saltados por no estar Docker instalado.
- Ejecutado `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`; Liquibase creo `public.databasechangelog`, ejecuto los 3 changesets y completo correctamente la actualizacion.
