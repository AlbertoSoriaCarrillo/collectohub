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

## 2026-06-16 - EPIC 3 - Autenticacion y usuarios

- Creadas entidades JPA `User`, `Role` y `RefreshToken`.
- Mapeada la relacion many-to-many entre `users` y `roles` mediante `user_roles`.
- Creados repositorios `UserRepository`, `RoleRepository` y `RefreshTokenRepository`.
- Creados DTOs `RegisterRequest`, `LoginRequest`, `AuthResponse` y `UserMeResponse`.
- Implementado `POST /api/auth/register` con validacion, normalizacion de email, comprobacion de duplicados, BCrypt y rol `USER` por defecto.
- Implementado `POST /api/auth/login` con validacion de credenciales y generacion de access token y refresh token.
- Implementado `GET /api/users/me` protegido por JWT.
- Configurada seguridad stateless con endpoints publicos `/api/health`, `/api/auth/register`, `/api/auth/login`, `/v3/api-docs/**`, `/swagger-ui/**` y `/swagger-ui.html`.
- Anadida generacion y validacion JWT con `spring-security-oauth2-jose` y firma HS256.
- Anadida tabla `refresh_tokens` por Liquibase para almacenar hashes de refresh tokens.
- Actualizado `infra/.env.example` con un `JWT_SECRET` local de longitud compatible con HS256.
- Corregido `backend/mvnw.cmd` para evitar fallo de PowerShell al resolver `.m2` cuando no es enlace simbolico.
- Anadidos tests unitarios y MVC para registro, email duplicado, login correcto, login incorrecto, `/api/users/me` sin token, `/api/users/me` con token y `/api/health` publico.
- Ejecutado `.\mvnw.cmd clean verify`.
- Resultado: build correcto; 16 tests totales, 14 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-16 - EPIC 4 - Tiendas y miembros de tienda

- Creadas entidades JPA `Shop` y `ShopMember`, con enums de estado y rol interno.
- Mapeadas las tablas existentes `shops` y `shop_members`.
- Creados repositorios `ShopRepository` y `ShopMemberRepository`.
- Creados DTOs `CreateShopRequest`, `UpdateShopRequest`, `ShopResponse` y `ShopMemberResponse`.
- Implementado `POST /api/shops` protegido por JWT para crear tiendas asociadas al usuario autenticado.
- Implementada creacion automatica de `shop_members` con rol interno `OWNER` al crear una tienda.
- Implementado `GET /api/shops/my` protegido por JWT para listar tiendas asociadas al usuario autenticado.
- Implementado `GET /api/shops/{shopId}` como consulta publica de datos basicos de tienda.
- Implementado `PUT /api/shops/{shopId}` protegido por JWT y limitado a miembros `OWNER` o `MANAGER`.
- Anadido aislamiento logico de modificacion mediante `shop_members`.
- Anadidas validaciones de nombre obligatorio, email de contacto, pais opcional, moneda y horas de expiracion de reserva.
- Anadidas propiedades `SHOP_DEFAULT_CURRENCY` y `SHOP_DEFAULT_RESERVATION_EXPIRATION_HOURS`.
- Anadida migracion Liquibase `004-alter-shops-country-nullable.sql` para hacer `shops.country` opcional.
- Anadidos tests unitarios y MVC para creacion, seguridad sin token, miembro OWNER automatico, listado de tiendas propias, bloqueo de tienda ajena, actualizacion por propietario y validaciones.
- Ejecutado `.\mvnw.cmd clean verify`.
- Resultado: build correcto; 27 tests totales, 25 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - Ajuste de rol global SHOP_OWNER

- Modificada la creacion de tienda para asignar el rol global `SHOP_OWNER` al usuario si todavia no lo tiene.
- Mantenida la creacion automatica del `shop_member` con rol interno `OWNER`.
- Anadida comprobacion para no duplicar roles globales si el usuario ya tiene `SHOP_OWNER`.
- Anadidos tests para primera tienda, usuario que ya tiene `SHOP_OWNER` y continuidad de creacion de `shop_member OWNER`.
- Actualizada la decision previa de EPIC 4 sobre rol global `SHOP_OWNER`.
- Ejecutado `.\mvnw.cmd clean verify`.
- Resultado: build correcto; 29 tests totales, 27 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - EPIC 5 - Catalogo maestro de productos

- Creadas entidades JPA `ProductCategory` y `MasterProduct`.
- Mapeadas las tablas existentes `product_categories` y `master_products`, incluyendo campos JSONB para `publication_countries` y `attributes`.
- Creados repositorios `ProductCategoryRepository` y `MasterProductRepository`.
- Creados DTOs `CreateMasterProductRequest`, `UpdateMasterProductRequest`, `MasterProductResponse` y `ProductCategoryResponse`.
- Implementado endpoint publico `GET /api/product-categories`.
- Implementados endpoints publicos `GET /api/master-products` y `GET /api/master-products/{id}`.
- Implementados endpoints protegidos `POST /api/master-products` y `PUT /api/master-products/{id}` para usuarios con rol global `ADMIN` o `SHOP_OWNER`.
- Anadida busqueda basica por `categoryCode`, `name`, `franchise`, `collectionName`, `language` y `status`, limitada a productos activos y no eliminados.
- Anadida deteccion basica de duplicados por ISBN, EAN y combinacion logica normalizada de nombre, franquicia, volumen e idioma.
- Anadidas respuestas controladas para duplicados `409`, producto no encontrado `404`, categoria/filtro invalido `400` y acceso denegado `403`.
- Abiertos en seguridad los GET publicos de categorias y productos maestros, manteniendo protegidos POST/PUT.
- Anadidos tests unitarios y MVC para listado publico, creacion como `SHOP_OWNER`, creacion sin token, bloqueo a `USER`, duplicados 409, actualizacion y validaciones 400.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado a Maven Central.
- Resultado: build correcto; 46 tests totales, 44 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - Ajustes tecnicos e EPIC 6 - Inventario de tienda

- Actualizado `RegisterRequest` para aceptar `preferredInterfaceLanguage` opcional.
- Implementadas reglas de idioma de interfaz: valor por defecto `es`, idiomas permitidos `es` y `en`, y normalizacion a minusculas.
- Anadidos tests para registro sin idioma, registro con `en`, registro con `ES` y rechazo HTTP `400` para idioma no soportado.
- Actualizado `.github/workflows/ci.yml` para ejecutar el backend con `./mvnw clean verify` desde `backend`.
- Habilitada method security con `@EnableMethodSecurity`.
- Anadida autorizacion declarativa con `@PreAuthorize("hasAnyAuthority('ADMIN', 'SHOP_OWNER')")` en endpoints protegidos de catalogo maestro.
- Creadas entidades/enums de inventario `ShopProduct`, `ShopProductCommercialStatus` y `PhysicalCondition`.
- Mapeada la tabla existente `shop_products` con tienda, producto maestro, precio, moneda, stock, estado comercial, condicion fisica, visibilidad, unidad limitada, notas, auditoria y borrado logico.
- Creado `ShopProductRepository` con busquedas por tienda, producto maestro, detalle activo y productos visibles/disponibles.
- Creados DTOs `CreateShopProductRequest`, `UpdateShopProductRequest` y `ShopProductResponse`.
- Implementado `POST /api/shops/{shopId}/products` protegido para miembros `OWNER` o `MANAGER`.
- Implementado `PUT /api/shops/{shopId}/products/{shopProductId}` protegido para miembros `OWNER` o `MANAGER`.
- Implementado `GET /api/shops/{shopId}/products/my` protegido para miembros de la tienda.
- Implementado `GET /api/shops/{shopId}/products` publico, limitado a productos visibles, disponibles y no eliminados.
- Implementado `GET /api/shop-products/{shopProductId}` publico, limitado a productos visibles, disponibles y no eliminados.
- Anadido aislamiento logico por `shopId`: no se permite crear ni actualizar inventario de tiendas ajenas y el producto de tienda debe pertenecer al `shopId` de la ruta.
- Anadidas respuestas controladas `404` para producto de tienda no encontrado y reutilizadas las existentes para tienda/producto maestro no encontrados.
- Anadidos tests unitarios y MVC para creacion como `OWNER` y `MANAGER`, seguridad `401/403`, producto maestro inexistente `404`, validaciones de precio/stock `400`, listado interno, listado publico visible, ocultos no expuestos, actualizacion y bloqueo por `shopId` incorrecto.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado; fallo inicial por `@PreAuthorize` aplicado al servicio de catalogo en tests MVC con mocks proxificados.
- Movida la autorizacion declarativa de catalogo desde el servicio al controlador para proteger el endpoint y mantener tests MVC con mocks limpios.
- Ejecutado de nuevo `.\mvnw.cmd clean verify`.
- Resultado: build correcto; 70 tests totales, 68 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - EPIC 7 - Colecciones de usuario

- Creadas entidades JPA `Collection` y `CollectionItem`.
- Mapeadas las tablas existentes `collections` y `collection_items`.
- Creados enums `CollectionVisibility` y `CollectionItemStatus`.
- Reutilizado `PhysicalCondition` del modulo de inventario para condicion fisica de items de coleccion.
- Creados repositorios `CollectionRepository` y `CollectionItemRepository`.
- Creados DTOs `CreateCollectionRequest`, `UpdateCollectionRequest`, `CollectionResponse`, `CreateCollectionItemRequest`, `UpdateCollectionItemRequest` y `CollectionItemResponse`.
- Implementado `POST /api/collections` protegido por JWT para crear colecciones del usuario autenticado.
- Implementado `GET /api/collections/my` protegido por JWT con filtros opcionales `visibility` y `categoryCode`.
- Implementado `GET /api/collections/{collectionId}` publico/protegido: colecciones publicas sin token y privadas solo para propietario.
- Implementado `PUT /api/collections/{collectionId}` protegido para propietario.
- Implementado `DELETE /api/collections/{collectionId}` con borrado logico para propietario.
- Implementado `POST /api/collections/{collectionId}/items` protegido para propietario.
- Implementado `GET /api/collections/{collectionId}/items` publico/protegido segun visibilidad y propiedad.
- Implementado `PUT /api/collections/{collectionId}/items/{itemId}` protegido para propietario.
- Implementado `DELETE /api/collections/{collectionId}/items/{itemId}` con borrado logico para propietario.
- Anadido aislamiento logico: no se permite modificar colecciones ni items ajenos, y las colecciones privadas ajenas no se exponen en lecturas.
- Anadidas respuestas controladas `404` para coleccion o item no encontrado y reutilizadas las respuestas existentes para producto maestro y categoria.
- Anadidos tests unitarios y MVC para creacion, seguridad `401`, validaciones `400`, listado y filtros, lectura publica/privada, actualizacion, borrado logico, alta/listado/actualizacion/borrado de items, producto maestro inexistente y aislamiento de colecciones ajenas.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado.
- Resultado: build correcto; 107 tests totales, 105 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - Correccion CI Maven Wrapper Linux

- Revisado `.github/workflows/ci.yml`.
- Anadido paso `Make Maven Wrapper executable` en el job `backend` para ejecutar `chmod +x mvnw` desde `backend` antes de lanzar los tests.
- Mantenido el build de backend con `./mvnw clean verify`; no se vuelve a usar `mvn clean verify` directamente.
- Revisado `backend/mvnw` con `git ls-files --eol`; el wrapper Unix ya esta en LF.
- Ejecutado `.\mvnw.cmd clean verify`.
- Resultado: build correcto; 107 tests totales, 105 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-17 - EPIC 8 - Recomendaciones simples

- Creado modulo backend `recommendations` con capas `api`, `application` y `dto`.
- Creados DTOs `RecommendedShopProductResponse`, `RecommendationReasonResponse`, `UserRecommendationResponse` y `UserRecommendationSummaryResponse`.
- Implementado endpoint protegido `GET /api/recommendations/my`.
- Implementado endpoint protegido `GET /api/recommendations/my/summary`.
- Anadida busqueda de items propios de coleccion en estado `MISSING` o `WANTED`.
- Anadida busqueda de productos de tienda recomendables: visibles, `AVAILABLE`, con stock mayor que cero, no eliminados, con tienda activa y producto maestro activo.
- Anadidos filtros opcionales `categoryCode`, `maxPrice`, `currency`, `physicalCondition` y `shopId`.
- Anadida validacion de filtros invalidos con respuesta controlada `400`.
- Anadida deduplicacion por `shopProductId` y seleccion de una coincidencia principal priorizando `MISSING` sobre `WANTED`.
- Anadido orden de recomendaciones por prioridad `MISSING`, nombre de producto y precio ascendente.
- Anadido resumen con conteos de items `MISSING`, items `WANTED`, productos recomendables, tiendas distintas y categorias con coincidencias.
- Anadidos tests unitarios y MVC para seguridad `401`, usuario sin colecciones, coincidencias `MISSING` y `WANTED`, exclusion de productos ocultos/sin stock/reservados/vendidos/ocultos/eliminados, filtros, summary y deduplicacion.
- Ejecutado `.\mvnw.cmd test`.
- Resultado: build correcto; 125 tests totales, 123 ejecutados correctamente y 2 saltados por no estar Docker instalado.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado.
- Resultado: build correcto; 125 tests totales, 123 ejecutados correctamente y 2 saltados por no estar Docker instalado.
