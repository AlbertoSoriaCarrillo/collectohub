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

## 2026-06-17 - EPIC 9 - Reservas MVP

- Creado modulo backend `reservations` con capas `api`, `application`, `domain`, `dto` e `infrastructure`.
- Creada entidad JPA `Reservation` y enum `ReservationStatus` mapeando la tabla existente `reservations`.
- Creado repositorio `ReservationRepository`.
- Creados DTOs `CreateReservationRequest`, `UpdateReservationStatusRequest` y `ReservationResponse`.
- Implementado `POST /api/reservations` protegido por JWT para crear reservas `PENDING` sobre productos de tienda reservables.
- Implementado `GET /api/reservations/my` con filtros opcionales `status` y `shopId`.
- Implementado `GET /api/reservations/{reservationId}` con acceso para el usuario propietario o miembros `OWNER`/`MANAGER` de la tienda.
- Implementado `GET /api/shops/{shopId}/reservations` para miembros `OWNER`/`MANAGER`, con filtros `status`, `userId` y `shopProductId`.
- Implementado `PUT /api/shops/{shopId}/reservations/{reservationId}/status` con transiciones validas de tienda.
- Implementado `PUT /api/reservations/{reservationId}/cancel` para cancelar reservas propias `PENDING` o `ACCEPTED`.
- Anadidas reglas de reservabilidad: producto de tienda existente, no eliminado, visible, `AVAILABLE`, con stock mayor que cero, tienda activa y producto maestro activo.
- Anadida validacion de stock insuficiente, transiciones invalidas, producto no reservable y aislamiento por usuario/tienda con respuestas `400`, `403`, `404` y `409`.
- Se mantiene el stock sin reduccion automatica en MVP.
- Anadidos tests unitarios y MVC para creacion, errores de reservabilidad, stock, consultas de usuario/tienda, permisos, transiciones, cancelacion y conversion HTTP de errores.
- Ejecutado `.\mvnw.cmd test`.
- Resultado: build correcto; 161 tests totales, 159 ejecutados correctamente y 2 saltados por no estar Docker instalado.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado.
- Resultado: build correcto; 161 tests totales, 159 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-18 - EPIC 11 - Frontend Angular base

- Revisada documentacion requerida antes de modificar codigo: prompt, README, specs, seguridad, testing, CI, task log, decisiones, endpoints MVP y flujo manual.
- Creado proyecto Angular standalone en `frontend` con routing, SCSS, TypeScript y scripts `npm start`, `npm test` y `npm run build`.
- Verificado que Angular 22.0.2 no puede ejecutarse con Node.js local `24.14.0`; se usa Angular CLI/Angular 21.2.x por compatibilidad con Node 24 local y CI.
- Instalado Angular Material/CDK 21.2.x y configurado tema oscuro inicial.
- Creada configuracion local `frontend/src/environments/environment.ts` con `apiBaseUrl = "http://localhost:8080"`.
- Creados modelos frontend `AuthResponse`, `LoginRequest`, `RegisterRequest`, `UserMeResponse` y `ErrorResponse`.
- Creado `TokenStorageService` para persistir tokens y datos publicos de usuario en `localStorage`.
- Creado `AuthService` con `register`, `login`, `logout`, `getMe`, comprobacion de token y roles.
- Creado `authInterceptor` para anadir `Authorization: Bearer <token>` y limpiar sesion ante `401` fuera de endpoints de auth.
- Creado `authGuard` para proteger `/dashboard` y redirigir a `/login`.
- Creado `ErrorMessageService` para convertir `ErrorResponse` del backend en mensajes mostrables.
- Implementadas rutas `/login`, `/register`, `/dashboard`, `/` y redireccion controlada `**`.
- Implementado layout principal con toolbar, navegacion basica y estado autenticado/no autenticado.
- Implementadas pantallas iniciales de login, registro y dashboard conectado a `GET /api/users/me`.
- Anadidas tarjetas placeholder en dashboard para Mis tiendas, Catalogo, Inventario, Mis colecciones, Recomendaciones y Reservas.
- Anadidos tests minimos: app arranca, validaciones de login/registro, `AuthService.login`, interceptor Bearer, guard sin/con token y dashboard con servicio mock.
- Creado `frontend/README.md` con instalacion, arranque, backend local y comandos.
- Ejecutado `npm run build`.
- Intentado `npm test -- --watch=false` dentro del sandbox; fallo por bloqueo de lectura de directorios superiores del entorno.
- Ejecutado `npm test -- --watch=false` con permisos autorizados.
- Ejecutado `npm ci` para validar la instalacion reproducible usada por CI.
- Ejecutados de nuevo `npm test -- --watch=false` y `npm run build`.
- Ejecutado `npm audit --omit=dev`.
- Resultado: build correcto; tests correctos con 7 archivos y 9 tests; auditoria de dependencias de produccion con 0 vulnerabilidades.
- Observacion: `npm install`/`npm ci` informan 5 vulnerabilidades totales en dependencias de desarrollo/transitivas y la deprecacion de `@angular/animations`; no afectan a dependencias de produccion segun `npm audit --omit=dev`.

## 2026-06-17 - EPIC 10 - Endurecimiento y revision backend MVP

- Revisados controladores de `auth`, `users`, `shops`, `catalog`, `inventory`, `collections`, `recommendations` y `reservations`.
- Revisada configuracion de seguridad: endpoints publicos, endpoints protegidos por JWT, `@PreAuthorize` en catalogo maestro y reglas de pertenencia por `shop_members`.
- Revisada gestion homogenea de errores mediante `ErrorResponse` y `GlobalExceptionHandler` para `400`, `401`, `403`, `404` y `409`.
- Revisado que las respuestas de usuario/autenticacion no exponen `passwordHash` ni hashes de refresh token.
- Revisado aislamiento logico de tiendas, inventario, colecciones privadas, recomendaciones propias y reservas por usuario/tienda.
- Revisado `.github/workflows/ci.yml`: Java 25, `chmod +x mvnw`, build con `./mvnw clean verify`, sin Maven global y frontend omitido si no existe `frontend/package.json`.
- Anadido esquema OpenAPI `bearer-jwt` para facilitar pruebas de endpoints protegidos desde Swagger UI.
- Creado `docs/16_MVP_API_ENDPOINTS.md` con matriz de endpoints MVP, permisos, bodies, respuestas y errores principales.
- Creado `docs/17_MANUAL_TESTING_FLOW.md` con flujo manual PowerShell completo usando `Invoke-RestMethod`.
- Documentado que, tras crear la primera tienda, el cliente debe obtener un token nuevo para que el JWT incluya `SHOP_OWNER`.
- Intentado `.\mvnw.cmd clean verify` sin permisos de red; fallo al resolver dependencias en Maven Central por bloqueo del sandbox.
- Ejecutado `.\mvnw.cmd clean verify` con acceso autorizado.
- Resultado: build correcto; 161 tests totales, 159 ejecutados correctamente y 2 saltados por no estar Docker instalado.

## 2026-06-18 - EPIC 12 - Frontend tiendas y catalogo

- Revisada documentacion requerida antes de modificar codigo: prompt, README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Creados modelos frontend `ShopResponse`, `ShopMemberResponse`, `CreateShopRequest`, `UpdateShopRequest`, `ProductCategoryResponse`, `MasterProductResponse`, `CreateMasterProductRequest`, `UpdateMasterProductRequest` y filtros de busqueda de catalogo.
- Creados servicios `ShopService` y `CatalogService` conectados a endpoints MVP de tiendas y catalogo.
- Anadidas rutas `/shops`, `/shops/new`, `/shops/:id`, `/catalog`, `/catalog/new` y `/catalog/:id`; protegidas con `authGuard` las rutas privadas solicitadas.
- Actualizada navegacion principal y dashboard para enlazar `Mis tiendas` y `Catalogo`, manteniendo el resto de modulos futuros como placeholders.
- Implementadas pantallas de tiendas: listado de tiendas propias, creacion de tienda y detalle publico con deteccion de pertenencia autenticada.
- Implementadas pantallas de catalogo: listado/busqueda publica de productos maestros, creacion protegida por rol `SHOP_OWNER`/`ADMIN` y detalle publico.
- Anadido bloqueo UI para impedir que un usuario autenticado sin `SHOP_OWNER` o `ADMIN` cree productos maestros.
- Mejorado `ErrorMessageService` con mensajes fallback para `400`, `401`, `403`, `404` y `409`.
- Revisado `frontend/tsconfig.app.json`; `tsc -p tsconfig.app.json --noEmit` pasaba sin errores y se normalizo la configuracion a la forma estandar de Angular CLI con `files: ["src/main.ts"]`.
- Actualizado `frontend/README.md` con rutas, servicios y modulos frontend MVP.
- Anadidos tests de servicios, rutas protegidas, listado de tiendas, validacion de tienda, listado de catalogo, validacion de producto maestro y bloqueo UI por rol.
- Intentado `npm test -- --watch=false` dentro del sandbox; fallo por bloqueo de lectura de directorios superiores del entorno.
- Ejecutado `npm ci` con permisos autorizados.
- Ejecutado `npm test -- --watch=false` con permisos autorizados.
- Ejecutado `npm run build` con permisos autorizados.
- Resultado: tests correctos con 14 archivos y 20 tests; build correcto.
- Observacion: `npm ci` informa 5 vulnerabilidades totales en dependencias de desarrollo/transitivas y la deprecacion de `@angular/animations`; no se cambian versiones en esta fase.

## 2026-06-18 - EPIC 13 - Frontend inventario de tienda

- Revisada documentacion requerida antes de modificar codigo: prompt, README, frontend README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Creados modelos frontend `ShopProductResponse`, `CreateShopProductRequest`, `UpdateShopProductRequest`, `ShopProductSearchFilters`, `ShopProductCommercialStatus` y `PhysicalCondition`.
- Creado `InventoryService` con llamadas a `POST /api/shops/{shopId}/products`, `PUT /api/shops/{shopId}/products/{shopProductId}`, `GET /api/shops/{shopId}/products/my`, `GET /api/shops/{shopId}/products` y `GET /api/shop-products/{shopProductId}`.
- Anadidas rutas `/shops/:shopId/inventory`, `/shops/:shopId/inventory/new`, `/shops/:shopId/inventory/:shopProductId/edit` y `/shop-products/:shopProductId`; protegidas con `authGuard` las rutas internas de tienda.
- Implementada pantalla interna de inventario de tienda con listado, empty state, precio, stock, estado comercial, condicion fisica, visibilidad, notas y acciones de editar/ver publico.
- Implementada pantalla para anadir producto al inventario con busqueda de productos maestros mediante `CatalogService.searchMasterProducts`, seleccion de producto y formulario reactivo.
- Implementada pantalla de edicion de producto de inventario cargando el inventario interno y validando pertenencia por `shopId`.
- Implementada pantalla publica de producto de tienda con datos del producto, tienda asociada, precio, stock, condicion, estado y placeholder de reserva futura.
- Integrado inventario en detalle de tienda mostrando productos publicos y boton `Gestionar inventario` para miembros `OWNER` o `MANAGER`.
- Anadido placeholder `Buscar en tiendas proximamente` en detalle de catalogo.
- Actualizado dashboard para que la tarjeta Inventario navegue a `/shops` al no existir tienda por defecto en el MVP.
- Actualizado `frontend/README.md` con rutas, modulo y endpoints de inventario.
- Anadidos tests de `InventoryService`, rutas protegidas, pantalla de inventario vacia y con productos, alta, edicion y detalle publico de producto de tienda.
- Ejecutado `npx.cmd tsc -p tsconfig.app.json --noEmit`.
- Ejecutado `npm ci` con permisos autorizados.
- Ejecutado `npm test -- --watch=false` con permisos autorizados.
- Ejecutado `npm run build` con permisos autorizados.
- Resultado: tests correctos con 19 archivos y 29 tests; build correcto.
- Observacion: `npm ci` mantiene 5 vulnerabilidades totales en dependencias de desarrollo/transitivas y deprecacion de `@angular/animations`; no se cambian versiones en esta fase.

## 2026-06-18 - EPIC 14 - Frontend colecciones de usuario

- Revisada documentacion requerida antes de modificar codigo: prompt, README, frontend README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Creados modelos frontend `CollectionResponse`, `CollectionItemResponse`, `CreateCollectionRequest`, `UpdateCollectionRequest`, `CreateCollectionItemRequest`, `UpdateCollectionItemRequest`, `CollectionSearchFilters`, `CollectionVisibility` y `CollectionItemStatus`.
- Reutilizado `PhysicalCondition` desde los modelos frontend de inventario.
- Creado `CollectionService` con llamadas a colecciones e items: crear, listar propias, detalle, actualizar, borrar, anadir item, listar items, actualizar item y borrar item.
- Anadidas rutas `/collections`, `/collections/new`, `/collections/:collectionId`, `/collections/:collectionId/edit`, `/collections/:collectionId/items/new` y `/collections/:collectionId/items/:itemId/edit`.
- Protegidas con `authGuard` las rutas propias/de modificacion; el detalle queda publico/protegido segun visibilidad backend.
- Implementada pantalla `Mis colecciones` con filtros por visibilidad y categoria, listado, empty state, acciones de detalle, edicion y borrado con confirmacion.
- Implementadas pantallas de crear y editar coleccion con formularios reactivos y categorias del catalogo.
- Implementada pantalla de detalle de coleccion con carga de items, vista publica/privada y acciones de propietario cuando el usuario autenticado coincide con `userId`.
- Implementadas pantallas de anadir y editar item de coleccion con busqueda de productos maestros, estados de coleccion, condicion fisica opcional, unidad limitada, notas y fecha de adquisicion.
- Integrado dashboard para que `Mis colecciones` navegue a `/collections`.
- Integrado detalle de producto maestro con accion `Anadir a coleccion` hacia `/collections`.
- Actualizado `frontend/README.md` con rutas, modulo y endpoints de colecciones.
- Anadidos tests de `CollectionService`, rutas protegidas, listado de colecciones vacio/con datos, creacion, detalle con items, alta de item y edicion de item.
- Ejecutado `npx.cmd tsc -p tsconfig.app.json --noEmit`.
- Ejecutado `npx.cmd tsc -p tsconfig.spec.json --noEmit`.
- Intentado ejecutar `npm ci` con permisos autorizados; el entorno rechazo la ejecucion elevada por limite de uso de la sesion en ese momento.
- La validacion completa de frontend se retoma y completa durante EPIC 15.

## 2026-06-18 - EPIC 15 - Frontend recomendaciones

- Revisada documentacion requerida antes de modificar codigo: prompt, README, frontend README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Creados modelos frontend `RecommendedShopProductResponse`, `RecommendationReasonResponse`, `UserRecommendationResponse`, `UserRecommendationSummaryResponse` y `RecommendationFilters`.
- Creado `RecommendationService` con llamadas a `GET /api/recommendations/my` y `GET /api/recommendations/my/summary`, incluyendo filtros `categoryCode`, `maxPrice`, `currency`, `physicalCondition` y `shopId`.
- Anadida ruta protegida `/recommendations`.
- Implementada pantalla de recomendaciones con resumen, filtros, categorias desde `CatalogService.getCategories()`, estados vacios diferenciados y cards enlazadas a producto de tienda, producto maestro y tienda.
- Integrado enlace `Recomendaciones` en navegacion autenticada, dashboard y detalle de coleccion propia.
- Actualizado `frontend/README.md` con ruta, modulo y endpoints de recomendaciones.
- Anadidos tests de `RecommendationService`, pantalla de recomendaciones, filtros, ruta protegida y enlace de dashboard.
- Ejecutado `npx.cmd tsc -p tsconfig.app.json --noEmit`.
- Ejecutado `npx.cmd tsc -p tsconfig.spec.json --noEmit`.
- Intentado `npm test -- --watch=false` y `npm run build` dentro del sandbox; ambos fallan por `Acceso denegado` al resolver archivos SCSS/specs desde directorios superiores.
- Ejecutado `npm ci` con permisos autorizados.
- Ejecutado `npm test -- --watch=false` con permisos autorizados.
- Ejecutado `npm run build` con permisos autorizados.
- Reejecutado `npm test -- --watch=false` con permisos autorizados tras `npm ci`.
- Resultado: tests correctos con 27 archivos y 47 tests; build correcto.
- Observacion: `npm ci` mantiene 5 vulnerabilidades totales en dependencias de desarrollo/transitivas y deprecacion de `@angular/animations`; no se cambian versiones en esta fase.

## 2026-06-18 - EPIC 16 - Frontend reservas MVP

- Revisada documentacion requerida antes de modificar codigo: prompt, README, frontend README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Creados modelos frontend `ReservationResponse`, `CreateReservationRequest`, `UpdateReservationStatusRequest`, `ReservationSearchFilters`, `ShopReservationSearchFilters` y `ReservationStatus`.
- Creado `ReservationService` con llamadas a `POST /api/reservations`, `GET /api/reservations/my`, `GET /api/reservations/{reservationId}`, `GET /api/shops/{shopId}/reservations`, `PUT /api/shops/{shopId}/reservations/{reservationId}/status` y `PUT /api/reservations/{reservationId}/cancel`.
- Anadidas rutas protegidas `/reservations`, `/reservations/:reservationId` y `/shops/:shopId/reservations`.
- Implementada creacion de reservas desde `/shop-products/:shopProductId`, con formulario para usuarios autenticados y enlace a login para usuarios anonimos.
- Implementada pantalla `Mis reservas` con filtros por estado y `shopId`, listado, empty state, detalle y cancelacion para estados `PENDING` o `ACCEPTED`.
- Implementada pantalla de detalle de reserva con datos de producto, tienda, usuario, mensajes y fechas; permite cancelar reservas propias en estados validos.
- Implementada pantalla de reservas de tienda con filtros `status`, `userId` y `shopProductId`, respuesta de tienda y transiciones `PENDING -> ACCEPTED/REJECTED` y `ACCEPTED -> COMPLETED/CANCELLED`.
- Integrado dashboard para que `Reservas` navegue a `/reservations`.
- Integrado layout autenticado con enlace `Mis reservas`.
- Integradas recomendaciones con accion `Reservar` hacia el producto de tienda.
- Integrado detalle de tienda con accion `Gestionar reservas` para miembros `OWNER` o `MANAGER`.
- Actualizado `frontend/README.md` con rutas, modulo y endpoints de reservas.
- Actualizado `docs/13_DECISIONS.md` con decisiones de filtros numericos temporales y detalle comun de reserva.
- Anadidos tests de `ReservationService`, rutas protegidas, `Mis reservas`, detalle de reserva, reservas de tienda, acciones por estado, creacion de reserva desde producto de tienda y enlace de dashboard.
- Ejecutado `npx.cmd tsc -p tsconfig.app.json --noEmit`.
- Ejecutado `npx.cmd tsc -p tsconfig.spec.json --noEmit`.
- Ejecutado `npm ci` con permisos autorizados.
- Ejecutado `npm test -- --watch=false` con permisos autorizados.
- Ejecutado `npm run build` con permisos autorizados.
- Resultado: tests correctos con 31 archivos y 60 tests; build correcto.
- Observacion: `npm ci` mantiene 5 vulnerabilidades totales en dependencias de desarrollo/transitivas y deprecacion de `@angular/animations`; no se cambian versiones en esta fase.

## 2026-06-18 - EPIC 17 - Validacion integral MVP y preparacion de demo

- Revisada documentacion requerida antes de modificar documentacion: prompt, README raiz, frontend README, specs de producto/arquitectura/API/seguridad/testing, task log, decisiones, endpoints MVP y flujo manual.
- Revisado `.github/workflows/ci.yml`: mantiene backend con Java 25, `chmod +x mvnw`, `./mvnw clean verify`, y frontend con Node 24, `npm ci`, `npm test -- --watch=false` y `npm run build`.
- Ejecutado `.\mvnw.cmd clean verify` en backend con permisos autorizados tras bloqueo de red del sandbox.
- Resultado backend: build correcto; 161 tests totales, 159 correctos y 2 saltados por falta de Docker/Testcontainers.
- Ejecutado `npm ci` en frontend con permisos autorizados.
- Ejecutado `npm test -- --watch=false` en frontend con permisos autorizados.
- Resultado frontend tests: 31 archivos de test y 60 tests correctos.
- Ejecutado `npm run build` en frontend con permisos autorizados.
- Resultado frontend build: correcto.
- Validado arranque backend local con `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"` y `GET /api/health` correcto.
- Validado arranque frontend local con `npm start`; Angular compila y responde `200 OK` en `http://127.0.0.1:4200/`.
- Ejecutado smoke test por API del flujo MVP completo contra backend local: registro, login, creacion de tienda, relogin con `SHOP_OWNER`, producto maestro, inventario visible, coleccion con item `MISSING`, recomendaciones, reserva completada y reserva cancelada.
- Revisado que no hay `.env` reales versionados; solo existe `infra/.env.example`.
- Revisado que la UI no contiene `console.log` de tokens y que el README frontend documenta el uso temporal de `localStorage`.
- Creado `docs/18_DEMO_FLOW.md` con pasos de demo desde la UI y datos de ejemplo locales.
- Creado `docs/19_MVP_STATUS.md` con estado implementado, no implementado, limitaciones, decisiones vigentes, comandos, CI y siguientes pasos.
- Actualizado `README.md` con instrucciones de ejecucion local, validacion y enlaces de demo.
- Actualizado `frontend/README.md` con enlace al flujo de demo local.
- No se modifica logica de negocio ni se amplia el alcance MVP en esta fase.

## 2026-06-18 - EPIC 18 - Preparacion de despliegue local y empaquetado MVP

- Revisada documentacion requerida antes de modificar codigo/documentacion: prompt, README raiz, frontend README, task log, decisiones, demo flow, estado MVP, CI, `infra/docker-compose.yml` e `infra/.env.example`.
- Revisada infraestructura actual: `infra/docker-compose.yml` solo levantaba PostgreSQL; faltaban backend y frontend dockerizados.
- Creado `backend/Dockerfile` multi-stage con Eclipse Temurin Java 25, Maven Wrapper, jar Spring Boot y healthcheck sobre `/api/health`.
- Creado `backend/.dockerignore`.
- Anadido perfil `docker` en `backend/src/main/resources/application-docker.yml`, conectando por defecto a `jdbc:postgresql://postgres:5432/collectohub`.
- Anadida configuracion CORS local en backend mediante `CORS_ALLOWED_ORIGINS`, por defecto `http://localhost:4200,http://127.0.0.1:4200`.
- Creado `frontend/Dockerfile` multi-stage con Node.js 24 para build Angular y nginx para servir estaticos.
- Creado `frontend/.dockerignore` y `frontend/nginx.conf` con fallback SPA, healthcheck `/health` y proxy preparado para `/api`.
- Actualizado `infra/docker-compose.yml` para levantar PostgreSQL, backend y frontend en red interna `collectohub-network`, con volumen de PostgreSQL y puertos `5432`, `8080` y `4200`.
- Actualizado `infra/.env.example` con `FRONTEND_PORT`, `CORS_ALLOWED_ORIGINS` y aviso de valores locales de ejemplo.
- Actualizado `README.md` con opciones de ejecucion clasica y Docker Compose, URLs y comandos utiles.
- Actualizado `frontend/README.md` con seccion Docker y limitacion de `apiBaseUrl`.
- Actualizado `docs/18_DEMO_FLOW.md` con alternativa de arranque mediante Docker Compose.
- Actualizado `docs/19_MVP_STATUS.md` con estado de empaquetado local y limitaciones.
- Creado `docs/20_DEPLOYMENT_LOCAL.md` con pasos de ejecucion clasica y Docker Compose.
- Actualizado `docs/13_DECISIONS.md` con decisiones de empaquetado Docker local.
- Revisado `.github/workflows/ci.yml`; se mantiene CI sin despliegue y sin anadir build Docker para no alargar el pipeline en esta fase.
- Ejecutado `.\mvnw.cmd clean verify` en backend. Primer intento dentro del sandbox fallo por bloqueo de red Maven Central; reejecutado con permisos autorizados correctamente.
- Resultado backend: build correcto; 161 tests totales, 159 correctos y 2 saltados por falta de Docker/Testcontainers.
- Ejecutado `npm ci`, `npm test -- --watch=false` y `npm run build` en frontend con permisos autorizados.
- Resultado frontend: `npm ci` correcto con 5 vulnerabilidades transitivas/dev ya conocidas; 31 archivos de test y 60 tests correctos; build correcto.
- La validacion completa de Docker Compose queda retomada y completada en fases posteriores con PostgreSQL, backend y frontend levantados localmente.
- Revisado que no hay `.env` reales versionados; solo existe `infra/.env.example`.
- No se implementa despliegue cloud ni funcionalidades nuevas fuera del empaquetado local.

## 2026-06-18 - EPIC 19 - Tests end-to-end basicos con Playwright

- Revisada documentacion requerida antes de modificar codigo: prompt, README raiz, frontend README, task log, decisiones, demo flow, estado MVP, despliegue local, CI, `frontend/package.json`, rutas y componentes/servicios frontend de auth, tiendas, catalogo, inventario, colecciones, recomendaciones y reservas.
- Anadido `@playwright/test` como dependencia de desarrollo del frontend.
- Anadidos scripts frontend `e2e`, `e2e:headed`, `e2e:ui` y `e2e:install`.
- Creado `frontend/playwright.config.ts` con `testDir: ./e2e`, `E2E_BASE_URL`, `E2E_API_BASE_URL`, Chromium, screenshots solo en fallo, traces en fallo y timeout local.
- Creada estructura `frontend/e2e/helpers/` y `frontend/e2e/specs/`.
- Creados helpers E2E para datos unicos, auth, navegacion, selects Material y flujo MVP.
- Creados tests `01-smoke.spec.ts`, `02-auth-dashboard.spec.ts` y `03-mvp-flow.spec.ts`.
- Anadidos `data-testid` minimos en layout, auth, dashboard, tiendas, catalogo, inventario, colecciones, recomendaciones y reservas para estabilizar selectores E2E.
- Corregido el shell frontend para evitar doble layout: `App` ahora hospeda `router-outlet` y `MainLayoutComponent` queda como layout de rutas.
- Actualizado el test unitario de `App` para validar el `router-outlet`.
- Anadido `.gitignore` para artefactos locales de Playwright: `frontend/test-results/` y `frontend/playwright-report/`.
- Creado `docs/21_E2E_TESTING.md` con requisitos, ejecucion Docker/clasica, instalacion de Chromium, comandos, interpretacion de errores y limitaciones.
- Actualizados `README.md`, `frontend/README.md`, `docs/19_MVP_STATUS.md` y `docs/13_DECISIONS.md`.
- No se anade Playwright al CI en esta fase.
- Ejecutado `cd frontend && npm ci` con permisos autorizados.
- Resultado `npm ci`: correcto; se mantienen 5 vulnerabilidades dev/transitivas ya conocidas y deprecacion de `@angular/animations`.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`. Primer intento dentro del sandbox fallo por bloqueo de red Maven Central; reejecutado con permisos autorizados correctamente.
- Resultado backend: build correcto; 161 tests correctos, 0 fallos, 0 errores, 0 saltados.
- Ejecutado `cd frontend && npm test -- --watch=false` con permisos autorizados.
- Resultado frontend unit tests: 31 archivos de test y 60 tests correctos.
- Ejecutado `cd frontend && npm run build` con permisos autorizados.
- Resultado frontend build: correcto.
- Ejecutado `cd frontend && npm run e2e:install`; Chromium, headless shell, FFmpeg y dependencias Playwright descargadas correctamente.
- Ejecutado `cd frontend && npx playwright test --list`; detecta 3 tests en 3 archivos.
- Levantado entorno con `cd infra && docker compose up --build -d`; backend y frontend responden `200` en `/api/health` y `/`.
- Ejecutado `cd frontend && npm run e2e`.
- Resultado Playwright headless final: 3 tests correctos.
- Ejecutado `cd frontend && npm run e2e:headed`.
- Resultado Playwright headed: 3 tests correctos.
- Ejecutado `cd infra && docker compose down` para parar contenedores sin borrar volumen.
- Durante la primera ejecucion E2E se detectaron y corrigieron selectores demasiado amplios, el doble layout de Angular y la interaccion con overlays de Angular Material.
- No se implementan funcionalidades nuevas fuera de la capa E2E y la correccion minima del shell frontend.

## 2026-06-18 - EPIC 20 - Limpieza tecnica y preparacion para portfolio

- Revisada documentacion requerida antes de modificar: prompt, README raiz, frontend README, task log, decisiones, endpoints MVP, flujo manual, demo flow, estado MVP, despliegue local, guia E2E, `.gitignore`, CI, `backend/pom.xml`, `frontend/package.json` e `infra/docker-compose.yml`.
- Reestructurado `README.md` como documento principal de portfolio: descripcion, estado, stack, funcionalidades, arquitectura, URLs, arranque Docker, arranque clasico, tests, E2E, demo/screenshots, limitaciones y siguientes fases.
- Actualizado `frontend/README.md` para alinear comandos PowerShell, `npm.cmd`, Docker, E2E, rutas y endpoints con el README raiz.
- Corregido `docs/16_MVP_API_ENDPOINTS.md` para indicar que el contrato backend ya esta consumido por el frontend Angular.
- Actualizado `docs/19_MVP_STATUS.md` para reflejar Docker Compose validado, Playwright implementado y E2E fuera de CI por decision tecnica.
- Actualizado `docs/20_DEPLOYMENT_LOCAL.md` y `docs/21_E2E_TESTING.md` con alternativas `npm.cmd` para Windows PowerShell.
- Creado `docs/22_PORTFOLIO_REVIEW.md` con resumen de producto, problema, alcance, decisiones, arquitectura, seguridad, base de datos, testing, Docker, CI, valor tecnico y preguntas/respuestas de entrevista.
- Creada carpeta `docs/assets/screenshots/` con `.gitkeep` para capturas reales futuras.
- Revisado `.gitignore` y reforzado para `infra/.env`, reportes Playwright, `test-results/`, `playwright-report/` y temporales.
- Revisado `.github/workflows/ci.yml`; se mantiene CI actual sin Playwright.
- Revisado `backend/pom.xml`, `frontend/package.json` e `infra/docker-compose.yml`; no requieren cambios de version ni estructura.
- Revisadas inconsistencias documentales: se elimino la mencion obsoleta a Docker no validado/no disponible y se sustituyo por el estado actual.
- Revision de secretos: no se detectan tokens reales, claves privadas, emails personales ni `.env` reales versionados; `infra/.env` existe localmente pero no esta versionado y `infra/.env.example` contiene valores locales de ejemplo.
- Ejecutado `cd backend && .\mvnw.cmd clean verify` con permisos autorizados.
- Resultado backend: build correcto; 161 tests correctos, 0 fallos, 0 errores, 0 saltados.
- Ejecutado `cd frontend && npm.cmd ci`.
- Resultado `npm ci`: correcto; npm informa 7 vulnerabilidades dev/transitivas y deprecacion de `@angular/animations`; no se actualizan dependencias en esta fase documental.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`.
- Resultado frontend tests: 31 archivos y 60 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`.
- Resultado frontend build: correcto.
- Ejecutado `cd infra && docker compose up --build -d`; backend y frontend responden `200` en `/api/health` y `/`.
- Ejecutado `cd frontend && npm.cmd run e2e`.
- Resultado Playwright headless: 3 tests correctos.
- Ejecutado `cd infra && docker compose down` para parar contenedores sin borrar volumen.
- No se implementan funcionalidades nuevas de producto, endpoints, pagos, chat, marketplace, OAuth, 2FA, IA, uploads ni despliegue cloud.

## 2026-06-18 - EPIC 21 - Rediseño UI/UX social estilo CollectoHub

- Revisada documentacion requerida antes de modificar codigo: prompt, README raiz, frontend README, estado MVP, guia E2E, portfolio review, task log y decisiones.
- Revisada estructura Angular, rutas, layout principal, auth, dashboard, pantallas de tiendas, catalogo, inventario, colecciones, recomendaciones, reservas y helpers E2E.
- Redisenado `MainLayoutComponent` con shell de producto social: sidebar desktop, panel contextual derecho, cabecera compacta movil y bottom navigation movil.
- Mantenidos `data-testid` existentes para E2E: `app-toolbar`, `app-brand`, `login-link`, `register-link`, `session-label`, `logout-button`, formularios y cards MVP.
- Ampliado sistema visual global en `frontend/src/styles.scss` con variables SCSS dark-soft, tarjetas, chips, botones, formularios, grids, empty states, tokens visuales, estados y responsive.
- Redisenadas pantallas de login y registro con panel introductorio y tarjetas de formulario sin cambiar flujos ni validaciones.
- Redisenado dashboard con hero autenticado, perfil, roles y tarjetas de accion manteniendo rutas MVP.
- Mejoradas plantillas de tiendas, catalogo, inventario, colecciones, recomendaciones y reservas con cabeceras, tokens visuales, estados vacios y jerarquia de cards.
- Mejoradas pantallas de creacion de tienda, producto maestro, coleccion e inventario con cabeceras y formularios consistentes.
- Anadido uso de Angular Material Icons mediante `MatIconModule` en layout y hoja de estilos de iconos Material en `index.html`.
- Actualizado `frontend/README.md` con seccion UI/UX MVP.
- Creado `docs/23_UI_UX_REDESIGN.md` con alcance, principios visuales, componentes redisenados, responsive y validacion esperada.
- Actualizados `docs/13_DECISIONS.md`, `docs/19_MVP_STATUS.md` y `docs/22_PORTFOLIO_REVIEW.md` para reflejar el rediseño.
- Ejecutado `cd frontend && npm.cmd ci`: correcto; se mantiene aviso de deprecacion de `@angular/animations`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false` dentro del sandbox: no completa por bloqueo de permisos del entorno (`Cannot read directory "../../..": Acceso denegado`) durante resolucion de Angular. El intento con permisos elevados no pudo aprobarse por limite de uso del revisor automatico.
- Ejecutado `cd frontend && npm.cmd run build` mediante prefijo autorizado: correcto; build Angular generado en `frontend/dist/collectohub-frontend`.
- E2E Playwright y Docker Compose no se ejecutan en esta sesion porque requieren permisos elevados no disponibles tras el limite del revisor automatico.
- No se modifica backend, base de datos, endpoints, modelos, logica de negocio, pagos, chat, feed social, marketplace, OAuth, 2FA, uploads ni IA.

## 2026-06-18 - EPIC 22 - Internacionalizacion frontend ES/EN

- Revisada documentacion requerida antes de modificar: prompt, README raiz, frontend README, task log, decisiones, estado MVP, portfolio review y guia UI/UX.
- Implementada capa i18n ligera propia en `frontend/src/app/core/i18n/` sin anadir librerias externas.
- Creados diccionarios `es` y `en` por dominios: common, actions, layout, auth, dashboard, shops, catalog, inventory, collections, recommendations, reservations, validation, errors y enums.
- Implementado `LanguageService` con idioma actual, cambio en caliente, persistencia en `localStorage`, deteccion inicial de navegador ingles, fallback seguro a `es` e interpolacion `{{name}}`.
- Implementado pipe standalone `translate` y selector standalone `LanguageSelectorComponent`.
- Anadido selector de idioma al layout principal, login y registro.
- Sincronizado `preferredInterfaceLanguage` del registro con el idioma activo.
- Migrados textos visibles de layout, auth, dashboard, tiendas, catalogo, inventario, colecciones, recomendaciones y reservas a claves de traduccion.
- Traducidas representaciones visibles de roles, roles de tienda, visibilidad/estado de colecciones, estado comercial, condicion fisica, estado de reservas, estado de tienda y estado de producto maestro.
- Conservados sin traducir datos dinamicos de backend/usuario: emails, nombres, descripciones, notas, mensajes, IDs, precios, monedas, fechas y codigos tecnicos cuando son datos.
- Adaptado `ErrorMessageService` para errores genericos HTTP traducidos manteniendo mensajes especificos del backend.
- Anadidos tests unitarios para `LanguageService` y `TranslatePipe`.
- Actualizados tests existentes que dependian de textos visibles para tolerar ES/EN o validar por href/dato estable.
- Anadidos `data-testid` a campos traducibles usados por E2E.
- Actualizados helpers Playwright para no depender de labels traducibles.
- Anadido E2E `04-i18n.spec.ts` para cambio de idioma en login y persistencia tras recarga.
- Creado `docs/24_I18N_FRONTEND.md` con arquitectura, fallback, enums, errores, testing y textos hardcodeados restantes.
- Actualizados `frontend/README.md`, `docs/13_DECISIONS.md`, `docs/19_MVP_STATUS.md`, `docs/22_PORTFOLIO_REVIEW.md` y `docs/23_UI_UX_REDESIGN.md`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false` dentro del sandbox: no completa por bloqueo de permisos del entorno (`Cannot read directory "../../..": Acceso denegado`) durante resolucion de Angular.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false` con permisos elevados: correcto; 33 archivos de test y 69 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build` con permisos elevados: correcto; Angular genera `frontend/dist/collectohub-frontend` y avisa de budget inicial excedido en 15.47 kB.
- No se modifica backend, base de datos, endpoints, modelos, pagos, chat, feed social, marketplace, OAuth, 2FA, uploads ni IA.
