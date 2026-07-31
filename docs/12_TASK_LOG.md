# Registro de avance

## 2026-07-13 - EPIC 44D-FIX2 - Validacion final e invalidacion de busquedas obsoletas

- Protegidas busquedas editorial y legacy con identificadores vigentes; cambios
  de modo invalidan respuestas, errores y loading obsoletos.
- Ejecutado `npm.cmd ci`: 474 paquetes correctos. Ejecutado
  `npm.cmd test -- --watch=false`: 58 archivos y 212 tests correctos.
- Ejecutado `npm.cmd run build`: correcto; warning conocido de bundle inicial
  622.72 kB frente a budget 500 kB. Sin backend, migraciones, rutas,
  endpoints, exports, E2E ni Playwright. Siguiente tarea: EPIC 44E.

## 2026-07-13 - EPIC 44D-FIX - Endurecimiento del selector de item y edicion

- Eliminados formularios anidados; las busquedas usan botones `type="button"`
  y no pueden disparar el POST.
- Anadida invalidacion por identificador de peticion para que respuestas o
  errores obsoletos no modifiquen detalle, edition, `detailLoading` ni error.
- Diferenciados estado inicial de busqueda y sin resultados; anadidas pruebas
  DOM, de propiedad, concurrencia determinista y payloads exactos.
- Ejecutado `npm.cmd test -- --watch=false`: 58 archivos y 210 tests correctos.
- Sin backend, migraciones, rutas/endpoints o exports nuevos, items manuales,
  cambios OWNED/WANTED/MISSING, E2E ni Playwright. Siguiente tarea: EPIC 44E.

## 2026-07-13 - EPIC 44D - Alta desde catalogo y seleccion de edicion

- Implementado flujo catalogo editorial como via principal: busqueda
  item/edition deduplicada por item, detalle publico del item y edicion opcional
  limitada a sus propias ediciones.
- Conservada la compatibilidad legacy como modo secundario explicito. El payload
  editorial envia solo `catalogItemId` y la edition opcional, sin enlazar
  automaticamente `linkedMasterProductId`.
- Anadida comprobacion frontend de propiedad antes de mostrar formulario,
  buscar o enviar POST; los roles administrativos ajenos no la sustituyen.
- Anadido `referenceKind` opcional al modelo frontend y priorizado en la etiqueta
  del detalle. Actualizados mapa frontend-backend y exports, incluyendo la
  resolucion condicional de propietario de EPIC 44C.
- Ejecutados `npm.cmd ci` (474 paquetes), `npm.cmd test -- --watch=false`
  (58 archivos y 205 tests correctos) y `npm.cmd run build` correcto. Persiste
  el warning conocido del bundle inicial: 622.59 kB frente al budget de 500 kB.
- Sin backend, migraciones, rutas o endpoints nuevos, items manuales, cambios
  OWNED/WANTED/MISSING, E2E ni Playwright. Siguiente tarea: EPIC 44E - Items
  manuales y enlace posterior al catalogo.

## 2026-07-13 - EPIC 44C - Flujo frontend de creacion y edicion de colecciones

- Endurecidas las validaciones de nombre, descripcion y visibilidad en crear y
  editar; los nombres se recortan y se rechazan valores compuestos por espacios.
- Anadido control frontend de propiedad para edicion: `ADMIN` y
  `EDITORIAL_ADMIN` ajenos no ven ni pueden enviar el formulario.
- Corregida la semantica de actualizacion: editar envia `""` para borrar
  description o categoryCode, mientras crear conserva `null` para opcionales
  vacios.
- Anadidos estados de carga, guardado, error y categorias, textos ES/EN y
  explicaciones de privacidad. El listado deja de mostrar un contador de items
  no fiable.
- Anadidos tests de create/edit, listado y servicio. Ejecutados `npm.cmd ci`
  (474 paquetes), `npm.cmd test -- --watch=false` (58 archivos y 201 tests
  correctos) y `npm.cmd run build` correcto. Permanece el warning conocido del
  bundle inicial: 620.58 kB frente al budget de 500 kB.
- Sin backend, migraciones, rutas o endpoints nuevos, items manuales, cambios
  OWNED/WANTED/MISSING, E2E ni Playwright. Siguiente tarea: EPIC 44D - Alta
  desde catalogo y seleccion de edicion.

## 2026-07-12 - EPIC 44B-FIX - Cierre de pruebas y documentacion del contrato de collection items

- Anadidos tests de privacidad: el propietario recibe `notes` y `acquiredAt`;
  lectores anonimos, usuarios ajenos, `ADMIN` y `EDITORIAL_ADMIN` reciben la
  respuesta publica sanitizada, sin privilegio por rol.
- Anadidos tests de `referenceKind` para `DIRECT_CATALOG`, `VERIFIED_BRIDGE` y
  `LEGACY_UNRESOLVED`, sin mutacion ni escritura durante lecturas.
  `INVALID_REFERENCE` queda documentado para datos historicos inconsistentes:
  no se crea fixture persistible porque produccion exige una identidad valida.
- Comprobada la serializacion MVC real de respuesta publica sanitizada y de la
  respuesta completa del propietario, incluidas las referencias editoriales.
- Actualizados contrato API, exportaciones backend, backlog, estado y diseno
  MVP4 para reflejar `catalogItemId` canonico, edition opcional, compatibilidad
  `masterProductId` y privacidad por propiedad.
- Ejecutado `backend && .\\mvnw.cmd clean verify`: BUILD SUCCESS, 340 tests,
  0 fallos, 0 errores y 0 omitidos.
- Sin frontend, migraciones, endpoints nuevos, items manuales, cambios de
  OWNED/WANTED/MISSING, E2E ni Playwright. Siguiente tarea: EPIC 44C - Flujo
  frontend de creacion y edicion de colecciones.

## 2026-07-12 - EPIC 44B - Contrato backend de collection items y compatibilidad editorial

- Consolidado `catalogItemId` como identidad editorial directa con edition
  opcional validada; `masterProductId` legacy y referencia dual compatible se
  conservan sin backfill.
- Anadido `referenceKind` calculado y aditivo; bridge VERIFIED conserva su
  clasificacion sin reescribir filas.
- Corregida privacidad backend: notas y fecha de adquisicion solo se devuelven
  al propietario; lecturas PUBLIC quedan sanitizadas.
- Ejecutado `backend && .\\mvnw.cmd clean verify`: BUILD SUCCESS, 335 tests,
  0 fallos, 0 errores y 0 omitidos.
- Sin frontend, migraciones, endpoints nuevos, items manuales, cambios de
  OWNED/WANTED/MISSING, E2E ni Playwright. Siguiente tarea: EPIC 44C - Flujo
  frontend de creacion y edicion de colecciones.

## 2026-07-12 - EPIC 44A - Diseno y auditoria de MVP4 Colecciones finales de usuario

- Auditados backend, frontend, modelo de datos, referencias editoriales y
  compatibilidad legacy de colecciones.
- Decidida identidad canonica `catalogItemId`, edition opcional y
  `masterProductId` mantenido temporalmente para legacy.
- Disenados items manuales, OWNED/WANTED/MISSING, privacidad, migraciones
  futuras, plan 44B-44H y criterios de cierre en `docs/26_MVP4_COLLECTIONS_DESIGN.md`.
- Sin codigo funcional, migraciones, endpoints, rutas, E2E ni Playwright.
  Siguiente tarea: EPIC 44B - Contrato backend de collection items y
  compatibilidad editorial.

## 2026-07-12 - EPIC 43A - Diseno de roles editoriales separados de ADMIN

- Realizado inventario de autorizaciones: 28 operaciones HTTP editoriales,
  defensas de servicio equivalentes, guard compartido de 11 rutas y navegacion.
- Creada matriz de capacidades y evaluadas las alternativas de rol unico,
  editor/reviewer y permisos granulares.
- Decision adoptada: `EDITORIAL_ADMIN` como rol operativo editorial; `ADMIN`
  conserva acceso como superusuario.
- Estudiada la compatibilidad JWT/sesion y definidos los planes de backend,
  frontend y asignacion controlada exclusivamente por ADMIN.
- Sin codigo funcional, migraciones, endpoints, guards nuevos, E2E, Playwright
  ni MVP4. Siguiente tarea: EPIC 43B - Backend role EDITORIAL_ADMIN y
  autorizacion editorial centralizada.

## 2026-07-12 - EPIC 43B - Backend role EDITORIAL_ADMIN y autorizacion editorial centralizada

- Creada la migracion Liquibase 012, que inserta `EDITORIAL_ADMIN` sin asignar
  usuarios y con rollback condicionado a no tener asignaciones.
- Centralizada la autorizacion editorial en `EditorialCatalogSupport` y
  `EditorialAdminRequired`; controladores, servicios y lectura DRAFT/ARCHIVED
  aceptan `ADMIN` o `EDITORIAL_ADMIN`.
- Anadidas pruebas de rol, controlador, servicio y changelog; JWT mantiene los
  codigos de rol como authorities sin prefijo y no recibe claims nuevos.
- Ejecutado `backend && .\\mvnw.cmd clean verify`: BUILD SUCCESS, 335 tests,
  0 fallos, 0 errores y 0 omitidos; la prueba PostgreSQL/Testcontainers no se
  omitio.
- Sin frontend, guards, navegacion, asignacion automatica, endpoints nuevos,
  E2E, Playwright ni MVP4. Siguiente tarea: EPIC 43C - Guard y navegacion
  frontend para EDITORIAL_ADMIN.

## 2026-07-12 - EPIC 43C - Guard y navegacion frontend para EDITORIAL_ADMIN

- Creado `editorialAdminGuard`: permite `ADMIN` y `EDITORIAL_ADMIN`, redirige
  anonimos a login con `returnUrl` y usuarios sin permiso a home.
- Conservado `adminGuard` para ADMIN global; las 11 rutas editoriales y el
  enlace principal ahora usan la capacidad editorial centralizada.
- Anadidos tests de guard, rutas, layout y sesion/roles.
- Ejecutados `npm.cmd ci`, `npm.cmd test -- --watch=false` (57 archivos y 190
  tests correctos) y `npm.cmd run build` correcto. Persiste el warning conocido
  del budget inicial: 619.73 kB frente a 500 kB.
- Sin backend, migraciones, JWT, asignacion automatica, rutas nuevas, E2E,
  Playwright ni MVP4. Siguiente tarea: EPIC 43D - Provisionamiento controlado
  de EDITORIAL_ADMIN y validacion integral.

## 2026-07-12 - EPIC 43D - Provisionamiento controlado de EDITORIAL_ADMIN y validacion integral

- Creados scripts operativos para Status, Grant y Revoke, idempotentes,
  transaccionales y sin credenciales hardcodeadas; rechazan usuarios inactivos
  o eliminados y no alteran otros roles.
- Creado smoke de API que renueva sesion, comprueba `/api/users/me`, HTTP 200
  editorial y HTTP 403 opcional de usuario regular; `-Cleanup` solo revoca una
  concesion creada por la misma ejecucion.
- Validada sintaxis PowerShell. No se pudo ejecutar PostgreSQL/API real porque
  `psql` no esta disponible en el entorno; quedan comandos operativos en
  `scripts/admin/README.md`.
- Sin migraciones, endpoints, pantalla de roles, autoasignacion, JWT, E2E,
  Playwright ni MVP4. Siguiente tarea: EPIC 44A - Diseno y auditoria de MVP4
  Colecciones finales de usuario.

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

## 2026-06-19 - EPIC 23 - Datos de demo y preparacion para capturas/portfolio

- Revisada documentacion requerida antes de modificar: prompt, README raiz, frontend README, task log, decisiones, flujo manual, demo flow, estado MVP, despliegue local, guia E2E, portfolio review, guia UI/UX, guia i18n, endpoints MVP, changelogs Liquibase, Docker Compose, `.env.example` y helpers E2E.
- Creada carpeta `scripts/demo/`.
- Creado `scripts/demo/create-demo-data.ps1` para generar datos locales usando solo la API existente con `Invoke-RestMethod`.
- El script registra usuario tienda y usuario coleccionista, crea tienda, reloguea al shop owner para refrescar `SHOP_OWNER`, crea productos maestros, inventario, coleccion, items, recomendaciones y reserva.
- Anadido parametro `-Suffix` opcional con generacion automatica de sufijo unico si no se informa.
- Anadido parametro `-ApiBaseUrl` y soporte opcional de `-FrontendBaseUrl` para imprimir URLs utiles.
- Anadido resumen local `scripts/demo/.last-demo-data.json` con sufijo, usuarios, IDs y URLs, ignorado por Git.
- Actualizado `.gitignore` para no versionar `.last-demo-data.json`.
- Creado `docs/25_DEMO_DATA.md` con requisitos, ejecucion, datos generados, URLs, limpieza manual de Docker y limitaciones.
- Actualizados `README.md`, `frontend/README.md`, `docs/18_DEMO_FLOW.md`, `docs/19_MVP_STATUS.md`, `docs/22_PORTFOLIO_REVIEW.md` y `docs/23_UI_UX_REDESIGN.md`.
- Actualizado `docs/13_DECISIONS.md` con la decision de usar script de demo por API y no SQL directo ni endpoints especiales.
- Ejecutado parser PowerShell sobre `scripts/demo/create-demo-data.ps1`: correcto.
- Ejecutado `cd infra && docker compose up --build -d`: correcto; backend, PostgreSQL y frontend levantados. El build frontend Docker mantiene el aviso conocido de budget inicial excedido en 15.47 kB.
- Validado `GET http://localhost:8080/api/health`: `UP`.
- Ejecutado `.\scripts\demo\create-demo-data.ps1 -ApiBaseUrl "http://localhost:8080" -Suffix "demo-20260619-test"`: bloqueado por ExecutionPolicy local de PowerShell.
- Ejecutado `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\demo\create-demo-data.ps1 -ApiBaseUrl "http://localhost:8080" -Suffix "demo-20260619-test"`: correcto; creados usuarios demo, tienda, productos maestros, inventario, coleccion, items, recomendacion y reserva.
- Actualizado `docs/25_DEMO_DATA.md` y `README.md` con alternativa `-ExecutionPolicy Bypass` para entornos Windows que bloquean scripts locales.
- Ejecutado `cd frontend && npm.cmd ci`: correcto; se mantienen 7 vulnerabilidades dev/transitivas y aviso de deprecacion de `@angular/animations`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: correcto; 33 archivos de test y 69 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; se mantiene aviso de budget inicial excedido en 15.47 kB.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: correcto; 161 tests correctos, 0 fallos, 0 errores, 0 saltados.
- Ejecutado `cd frontend && npm.cmd run e2e`: primer intento fallo por selectores estrictos duplicados tras UI/i18n (`email` visible dos veces, `New` coincidiendo con `Like new` y selector de idioma duplicado).
- Ajustados solo tests/helpers E2E para acotar selectores sin cambiar logica de producto.
- Reejecutado `cd frontend && npm.cmd run e2e`: correcto; 4 tests Playwright correctos.
- Ejecutado `cd infra && docker compose down` para parar contenedores sin borrar volumen de PostgreSQL.
- No se modifican backend, endpoints, modelos, frontend de producto, base de datos, pagos, chat, feed social, marketplace, OAuth, 2FA, uploads ni IA.

## 2026-06-23 - EPIC 24 - Reenfoque producto a colecciones de libros/comics/manga

- Revisada la documentacion y frontend indicados en el prompt de la fase: prompt, README raiz, frontend README, task log, decisiones, endpoints, estado MVP, demo flow, portfolio, UI/UX, i18n, demo data, rutas Angular, layout, auth, catalogo, colecciones, recomendaciones/buscados, perfil y E2E.
- Reenfocado el recorrido principal de frontend a Home, Catalogo, Colecciones, Buscados y Perfil.
- Creado `HomeComponent` como primera pantalla publica.
- Creado `ProfileComponent` protegido con datos seguros de `GET /api/users/me` y acciones futuras deshabilitadas para editar perfil, password y avatar.
- Actualizadas rutas Angular: `/` y `/dashboard` redirigen a `/home`; `/wanted` carga la pantalla de buscados; `/recommendations` redirige a `/wanted`; `**` redirige a `/home`.
- Ocultadas tiendas, inventario y reservas de la navegacion principal, home y CTAs primarios sin borrar rutas legacy/futuras ni backend.
- Ajustados login y registro para copy de coleccionista, confirmacion de password y redireccion a `/collections`.
- Ajustadas pantallas de catalogo, colecciones y buscados para lenguaje de obras/biblioteca y para eliminar el filtro manual `shopId` de buscados.
- Actualizados enlaces internos visibles de colecciones/reservas legacy para apuntar a `/wanted` en lugar de `/recommendations`.
- Actualizados diccionarios ES/EN con dominios Home y Perfil, copy de biblioteca/obras/buscados y limpieza de claves de layout antiguas no usadas.
- Saneado `DashboardComponent` aunque la ruta ya redirige, evitando CTAs primarios a tiendas/reservas en ese componente huerfano.
- Actualizados tests unitarios de rutas, registro, dashboard, buscados, Home y Perfil.
- Actualizados E2E para validar el flujo principal de coleccionista: home, auth, colecciones, catalogo, buscados, perfil e i18n.
- Actualizado `scripts/demo/create-demo-data.ps1` para imprimir primero URLs principales (`home`, `login`, `catalog`, `collections`, `collectionDetail`, `wanted`, `profile`) y etiquetar URLs de tienda/reserva como legacy.
- Creado `docs/26_PRODUCT_REFOCUS_BOOKS.md` con contexto, decision de producto, conclusiones por rol, rutas principales, rutas legacy/futuras, demo recomendada y fuera de alcance.
- Actualizados `README.md`, `frontend/README.md`, `docs/18_DEMO_FLOW.md`, `docs/19_MVP_STATUS.md`, `docs/22_PORTFOLIO_REVIEW.md`, `docs/23_UI_UX_REDESIGN.md`, `docs/24_I18N_FRONTEND.md`, `docs/25_DEMO_DATA.md` y `docs/13_DECISIONS.md`.
- Ejecutado `cd frontend && npm.cmd ci`: correcto; se mantienen 7 vulnerabilidades dev/transitivas y aviso de deprecacion de `@angular/animations`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: correcto; 35 archivos de test y 73 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; se mantiene aviso de budget inicial excedido, ahora por 8.29 kB.
- Ejecutado parser PowerShell sobre `scripts/demo/create-demo-data.ps1`: correcto.
- Intentado `cd infra && docker compose up --build -d`: bloqueado porque Docker Desktop/daemon no esta iniciado (`dockerDesktopLinuxEngine` no disponible).
- No se ejecutan E2E en esta sesion por Docker no disponible; los tests quedan preparados para ejecutarse cuando Docker Desktop este iniciado.
- No se modifica backend, base de datos, endpoints, pagos, chat, marketplace, OAuth, 2FA, uploads ni app movil.

## 2026-06-23 - EPIC 25A - Header global, navegacion publica e idioma

- Revisada la documentacion y frontend indicados en el prompt de la fase: prompt, README raiz, frontend README, task log, decisiones, estado MVP, UI/UX, i18n, reenfoque de producto, rutas Angular, layout principal, auth, home y E2E.
- Implementado header global persistente con marca, navegacion principal, selector de idioma, enlace de login para visitantes y menu de usuario autenticado.
- Simplificada la sidebar desktop para que contenga solo navegacion principal, sin login, registro, selector de idioma, datos de sesion ni CTAs de tienda.
- Eliminado `Register` de la navegacion global y de los CTAs publicos persistentes; `/register` permanece accesible desde la pantalla de login y por URL directa.
- Convertido `LanguageSelectorComponent` en selector compacto con menu Angular Material y `data-testid` estables: `language-selector`, `language-es` y `language-en`.
- Anadido menu de usuario autenticado con Angular Material y `data-testid` estables: `user-menu-button`, `user-menu-profile` y `user-menu-logout`.
- Simplificadas las pantallas de login y registro a cards centradas sin panel introductorio decorativo, token visual ni selector interno de idioma.
- Mantenido en registro el campo `preferredInterfaceLanguage` sincronizado con el idioma activo sin duplicar el selector global.
- Actualizada home para que la CTA secundaria publica lleve a login, no a registro.
- Actualizada navegacion movil para conservar accesos principales y mover perfil/logout al menu de usuario del header.
- Actualizados diccionarios ES/EN, README raiz, README frontend y documentos de estado/UI/i18n/reenfoque.
- Creado `docs/27_PUBLIC_NAVIGATION_UX.md` con la estructura oficial de navegacion publica, header, sidebar limpia, registro desde login, selector unico de idioma, avatar/menu y criterios de test.
- Anadidos tests unitarios para el selector de idioma, layout publico/autenticado y CTA anonima de home.
- Actualizados tests E2E y helpers Playwright para usar el nuevo header, selector de idioma y menu de usuario.
- Ejecutado `cd frontend && npm.cmd ci`: correcto; se mantienen 7 vulnerabilidades dev/transitivas y aviso de deprecacion de `@angular/animations`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: correcto; 37 archivos de test y 77 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; se mantiene aviso de budget inicial excedido, ahora por 92.26 kB.
- Ejecutado `cd infra && docker compose down`: correcto.
- Ejecutado `cd infra && docker compose up --build -d`: correcto; PostgreSQL queda healthy y backend/frontend arrancan.
- Validado `GET http://localhost:8080/api/health`: primer intento durante calentamiento con conexion cerrada; segundo intento correcto con `UP`.
- Intentado ejecutar `cd frontend && npm.cmd run e2e`: no se ejecuta porque la solicitud de permisos elevados para Playwright fue rechazada por limite de uso del entorno. La suite queda actualizada y preparada para ejecutarse cuando haya permisos disponibles.
- No se modifica backend, base de datos, endpoints, modelos, pagos, chat, marketplace, OAuth, 2FA, uploads ni app movil.

## 2026-06-29 - EPIC 27 - Vision completa, dominios y roadmap

- Revisados prompt, README raiz, README frontend, task log, decisiones,
  endpoints MVP, demo, estado MVP, portfolio, UI/UX, i18n, datos demo y
  reenfoque de producto.
- Revisados los modulos backend `auth`, `users`, `catalog`, `collections`,
  `recommendations`, `shops`, `inventory` y `reservations`, junto con los
  controladores y contratos existentes.
- Revisadas las rutas frontend actuales para separar recorrido principal y
  rutas legacy/futuras.
- Creado `docs/00_PRODUCT_VISION.md` con la vision completa y los pilares de
  coleccionistas, catalogo, tiendas y comunidad/creadores.
- Creado `docs/01_ROADMAP.md` con seis fases desde nucleo coleccionista hasta
  creadores y comunidades.
- Creado `docs/02_MVP1_SCOPE.md` con limites explicitos de MVP 1.
- Creado `docs/03_PRODUCT_DOMAINS.md` con dominios y estado implementado,
  parcial o futuro.
- Creado `docs/04_CATALOG_CONCEPT_MODEL.md` con la cadena franquicia,
  coleccion de catalogo, item de catalogo, coleccion personal e item personal.
- Creado `docs/05_USER_SHOP_MATCHING.md` con el estado actual y la evolucion
  futura de matching oferta/demanda.
- Creado `docs/06_SOCIAL_AND_CREATORS.md` con la separacion entre social basico
  y herramientas posteriores para creadores.
- Actualizados README, estado MVP, portfolio y documento de reenfoque para
  distinguir foco actual y vision larga.
- Marcado `docs/10_ROADMAP.md` como roadmap tecnico historico y
  `docs/01_ROADMAP.md` como fuente vigente del roadmap estrategico de producto.
- Actualizada la decision estrategica de desarrollar la vision amplia por fases
  sin convertir MVP 1 en un marketplace.
- No se modifica codigo funcional, backend, frontend, rutas, base de datos,
  migraciones, endpoints, tests ni dependencias.
- Ejecutado `git status`: solo aparecen README y documentos de EPIC 27.
- Ejecutado `git diff --check`: correcto; solo avisos locales LF/CRLF de Git en
  Windows, sin errores de whitespace.
- Validada la presencia de la documentacion minima exigida por CI y de los siete
  documentos estrategicos nuevos.
- No se ejecutan suites backend/frontend al ser una EPIC exclusivamente
  documental sin cambios de codigo, configuracion ni contratos.

## 2026-06-29 - EPIC 28 - Cierre y auditoria de MVP 1

- Auditados producto visible, layout, navegacion, auth, Home, Catalogo,
  Colecciones, Buscados, Perfil, rutas, guards, i18n, tests, E2E, demo data,
  Docker y documentacion estrategica/operativa.
- Confirmado que Home, header, sidebar y bottom nav no promocionan tiendas,
  inventario ni reservas.
- Confirmado que Register solo se enlaza desde Login y que `confirmPassword` no
  se envia al backend.
- Limitados los estados seleccionables de items a `OWNED`, `WANTED`, `MISSING`
  y `DUPLICATED`; `SELLABLE` y `TRADABLE` siguen soportados por contrato para
  compatibilidad legacy.
- Simplificada `/wanted` para filtrar solo por categoria y mostrar coincidencias
  sin precio, condicion ni enlace a `/shop-products`.
- Cambiado el subtitulo global de biblioteca social a biblioteca personal en
  ES/EN.
- Anadido test de paridad de claves de traduccion ES/EN.
- Anadidos tests de estados MVP 1, ausencia de rutas legacy en layout y ausencia
  de CTA comercial en Buscados.
- Actualizado E2E smoke para impedir enlaces principales de tiendas,
  inventario o reservas.
- Corregidos selectores E2E de Buscados para exigir el `h1` exacto y evitar
  colision con el texto del estado vacio en ingles.
- Separada la salida de `create-demo-data.ps1` en `MVP 1 URLs` y
  `Legacy/future URLs`; parser PowerShell correcto.
- Detectado que el healthcheck frontend resolvia `localhost` por IPv6 mientras
  nginx escuchaba IPv4; corregido a `http://127.0.0.1/health`.
- Creado `docs/07_MVP1_ACCEPTANCE_CHECKLIST.md` con criterios funcionales, UI,
  i18n, seguridad, demo, Docker, tests, fuera de alcance y paso a MVP 2.
- Creado `docs/08_NEXT_BACKLOG.md` con gates de portfolio y backlog separado
  para MVP 2 a MVP 6.
- Actualizados README, README frontend, roadmap, alcance, dominios, decisiones,
  demo, estado MVP, portfolio, i18n, demo data y reenfoque.
- Ejecutado `cd frontend && npm.cmd ci`: correcto; 7 vulnerabilidades
  transitivas/dev (3 low, 4 high) y deprecacion de `@angular/animations`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 38 archivos y 80
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; warning de budget
  inicial, 592.30 kB frente a 500 kB.
- Ejecutado `cd backend && .\mvnw.cmd clean verify` con Docker activo: 161
  tests correctos, 0 fallos, 0 errores, 0 saltados.
- Ejecutados `docker compose down` y `docker compose up --build -d`; PostgreSQL,
  backend y frontend quedan healthy.
- Validado backend `/api/health` con `UP` y frontend `/health` con `OK`.
- Ejecutado `cd frontend && npm.cmd run e2e`: primer cierre detecto selector
  estricto ambiguo en ingles; corregido y reejecutado con 4 E2E correctos.
- No se modifican backend, base de datos, migraciones, endpoints ni contratos.
- MVP 1 queda cerrado como base tecnica/producto, no como sistema productivo ni
  como implementacion de la vision larga.

## 2026-06-29 - EPIC 29 - Documentacion tecnica exportable

- Revisados README, vision, roadmap, alcance, dominios, modelo de catalogo,
  matching, social, estado MVP, API, portfolio, demo y reenfoque de producto.
- Contrastados los cuatro changelogs Liquibase, entidades JPA, repositorios,
  DTOs, diez controladores, reglas de Spring Security, rutas Angular, servicios
  y llamadas HTTP de los componentes enrutados.
- Creada `docs/export/` con referencia en Markdown y CSV para base de datos,
  endpoints backend, rutas frontend y mapa frontend-backend, mas diagrama
  Mermaid y README de uso/actualizacion.
- Documentadas 13 tablas de aplicacion, 15 indices Liquibase explicitos y las
  relaciones PK/FK/UNIQUE, auditoria y borrado logico reales.
- Documentados 35 endpoints REST de aplicacion, excluyendo rutas generadas por
  Swagger/OpenAPI del conteo.
- Documentadas 28 rutas hijas navegables, redirecciones y fallback; el wrapper
  `MainLayoutComponent` no se cuenta como destino independiente.
- Documentadas 53 relaciones pantalla-backend: 48 llamadas HTTP y 5 rutas sin
  llamada directa.
- Clasificados elementos como `MVP1_VISIBLE`, `LEGACY_FUTURE`, `TECHNICAL` o
  `REDIRECT` segun el recorrido de producto vigente.
- Validados los CSV con `Import-Csv`: 13 tablas, 35 endpoints, 28 rutas y 53
  relaciones; 48 relaciones corresponden a llamadas HTTP reales.
- Cruzados automaticamente los CSV contra los 13 `CREATE TABLE`, 15
  `CREATE INDEX`, 35 mappings de controladores y 28 registros de rutas; no hay
  tablas, endpoints ni rutas mapeadas ausentes o duplicadas.
- Validado el Mermaid con cabecera `erDiagram` y 13 bloques de entidad, y
  comprobados los diez archivos requeridos sin whitespace final.
- Ejecutado `git status`: el alcance contiene solo README y documentacion.
- No se ejecutan suites backend/frontend porque no hay cambios funcionales,
  de configuracion, contratos, dependencias ni tests.
- No se modifica backend, frontend funcional, base de datos, migraciones,
  endpoints, rutas, componentes, seguridad, tests ni dependencias.

## 2026-06-29 - EPIC 30 - Diseno tecnico del catalogo editorial MVP 2

- Revisados vision, roadmap, alcance, dominios, modelo conceptual, matching,
  social, checklist, backlog, decisiones, API, estado, portfolio, reenfoque y
  documentacion exportable.
- Auditados changelogs Liquibase y modulos backend `catalog`, `collections`,
  `recommendations` e `inventory`, junto con DTOs, entidades, repositorios y
  consumidores frontend.
- Confirmado que `master_products` mezcla identidad de item y edicion, y que su
  ID aparece como contrato transversal en 25 archivos backend y 36 frontend.
- Creado `docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md` con conclusiones de producto,
  dominio, backend, frontend, datos/migracion y QA.
- Recomendado un modelo nuevo de publishers, franchises, series, items y
  editions con `master_product_catalog_links` como puente compatible y auditado.
- Definido item obligatorio y edicion opcional para colecciones futuras; el
  inventario debe converger a una edicion concreta.
- Disenados endpoints `/api/catalog/**`, rutas frontend futuras, permisos,
  estrategia de backfill, riesgos, criterios de salida y EPICs 31 a 38.
- Creators se difiere hasta estabilizar el nucleo editorial; relaciones entre
  obras requieren casos validados. No se implementan en EPIC 30.
- Actualizados README, roadmap, dominios, modelo conceptual, backlog, decisiones
  y estado MVP para reflejar que MVP 2 esta disenado pero no implementado.
- Validado el documento principal: 20 secciones numeradas, seis conclusiones por
  perspectiva, nueve entidades candidatas analizadas, 24 operaciones API
  futuras y seis rutas frontend propuestas.
- Comprobados los enlaces locales, la secuencia EPIC 31-38, los terminos
  obligatorios y la ausencia de whitespace final.
- Ejecutados `git status` y `git diff --check`: solo cambian README y
  documentacion; no hay errores de whitespace aparte de avisos locales LF/CRLF.
- No se ejecutan suites backend/frontend porque no hay cambios funcionales ni de
  configuracion.
- No se modifica codigo, frontend funcional, base de datos, Liquibase,
  endpoints, rutas, tests, demo data ni documentacion exportable.

## 2026-06-30 - EPIC 31 - Fundamentos editoriales de MVP 2

- Revisados el diseno editorial aprobado, alcance MVP 1, backlog, decisiones,
  contratos API, documentacion exportable, modulo `catalog`, seguridad,
  auditoria JPA, Liquibase y tests existentes.
- Creada la migracion Liquibase
  `005-create-editorial-catalog-foundations.sql` e incluida en el changelog
  master sin modificar las migraciones previas.
- Creadas las tablas aditivas `publishers`, `catalog_franchises` y
  `catalog_series` con auditoria, borrado logico, checks, dos claves foraneas y
  12 indices explicitos; el esquema suma 16 tablas y 27 indices Liquibase.
- Creadas las entidades `Publisher`, `CatalogFranchise` y `CatalogSeries`, y
  los enums `CatalogRecordStatus`, `CatalogSeriesType` y
  `CatalogPublicationStatus`.
- Creados tres repositorios JPA, nueve DTOs de request/response, un DTO
  generico de paginacion, tres servicios de aplicacion y excepciones de
  dominio controladas.
- Expuestos 12 endpoints paginados bajo `/api/catalog`: lectura publica de
  registros `ACTIVE` no eliminados y escritura exclusiva para `ADMIN`.
- Anadidos filtros editoriales, validacion de slugs, estados, dependencias y
  anos, deteccion de duplicados y proteccion del ciclo de vida de dependencias
  activas.
- Actualizada Spring Security para permitir solo los nuevos `GET` de forma
  publica; `POST` y `PUT` siguen autenticados y protegidos adicionalmente con
  `@PreAuthorize("hasAuthority('ADMIN')")`.
- Documentados los endpoints con OpenAPI y respuestas 400, 401, 403, 404 y
  409.
- Anadidos tests unitarios de los tres servicios, tests web de visibilidad,
  filtros, validacion y autorizacion, y ampliados los tests de Liquibase y de
  arranque de contexto.
- Ejecutado `cd backend && .\mvnw.cmd -DskipTests compile`: correcto.
- Ejecutado `cd backend && .\mvnw.cmd test`: 198 tests correctos, 0 fallos,
  0 errores y 2 saltados por disponibilidad de Docker en ese punto de control.
  Despues se anadieron tres casos web adicionales de autorizacion; el
  `clean verify` final no pudo reintentarse porque el entorno alcanzo su limite
  temporal de ejecucion con permisos elevados.
- Los tests y el build frontend no pudieron completarse en el sandbox por
  errores de acceso a los archivos de Angular; el reintento con permisos
  elevados fue bloqueado por el mismo limite temporal del entorno.
- Docker no se ejecuto: el daemon no estaba disponible y el entorno tampoco
  permitio leer su configuracion local.
- Actualizados README, diseno MVP 2, backlog, decisiones, referencia API,
  estado MVP y exportaciones de esquema/endpoints; validados 16 registros de
  tablas, 47 endpoints, 16 entidades Mermaid y la marca `MVP2_FOUNDATION` en
  las tres tablas y los 12 endpoints nuevos.
- `git diff --check` no detecta errores de whitespace; solo muestra los avisos
  locales esperados de conversion LF/CRLF.
- Se mantienen intactos `master_products`, `/api/master-products`,
  `collection_items`, `shop_products`, recomendaciones, rutas y UI frontend.
- La siguiente tarea recomendada es EPIC 32: `catalog_items`, una vez ejecutada
  la validacion global pendiente en un entorno sin las restricciones actuales.

## 2026-06-30 - EPIC 31B - Validacion global de fundamentos editoriales

- Estado inicial limpio en `main`, sincronizado con `origin/main`; EPIC 31
  estaba versionada en `5fb4a99`.
- Confirmado mediante el diff de EPIC 31 que la implementacion fue aditiva y
  no modifico funcionalmente `master_products`, `collection_items`,
  `shop_products`, recomendaciones, reservas, rutas ni UI frontend.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 201
  tests, 0 fallos, 0 errores y 2 saltados por disponibilidad de Docker en los
  tests de integracion condicionados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados;
  persiste la deprecacion conocida de `@angular/animations` y avisos de scripts
  de dependencias sujetos a aprobacion de npm.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 38 archivos y 80
  tests correctos. El primer intento fallo por permisos del sandbox sobre
  archivos SCSS/spec; el reintento fuera del sandbox confirmo que no habia un
  fallo del proyecto.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; permanece el warning
  conocido del bundle inicial, 592.30 kB frente al budget de 500 kB.
- Iniciado Docker Desktop 29.5.3 y ejecutados `docker compose down` y
  `docker compose up --build -d` sin eliminar volumenes. PostgreSQL, backend y
  frontend arrancaron correctamente y sus healthchecks quedaron sanos.
- Validado `GET /api/health`: HTTP 200 con estado `UP`.
- Validados sin autenticacion `GET /api/catalog/publishers`,
  `GET /api/catalog/franchises` y `GET /api/catalog/series`: HTTP 200 con
  paginas vacias validas en la base local.
- Validado Swagger UI: redireccion correcta desde `/swagger-ui.html` y HTTP
  200 final. OpenAPI 3.1 expone 33 paths e incluye los grupos `Editorial
  publishers`, `Editorial franchises` y `Editorial series`.
- Ejecutado `cd frontend && npm.cmd run e2e`: 4 pruebas Playwright correctas
  sobre Chromium (smoke, i18n, autenticacion/colecciones y flujo MVP 1).
- Ejecutado `docker compose down` al finalizar, sin `-v`; contenedores y red
  quedan detenidos y los datos se conservan.
- No fue necesaria ninguna correccion funcional. Se actualizan unicamente
  README y documentacion de estado para cerrar la validacion y situar EPIC 32
  como siguiente paso.
- No se implementan `catalog_items`, `catalog_item_editions`, creators, puente,
  backfill ni UI editorial; no se inicia EPIC 32.

## 2026-06-30 - EPIC 32 - Catalog items y catalog item editions

- Implementada la segunda capa del catalogo editorial con las tablas aditivas
  `catalog_items` y `catalog_item_editions`.
- Creada la migracion Liquibase
  `006-create-editorial-catalog-items-and-editions.sql` e incluida en
  `db.changelog-master.yaml`.
- `catalog_items` modela la identidad coleccionable dentro de una serie
  editorial.
- `catalog_item_editions` modela ediciones concretas de un item, con publisher
  opcional, ISBN, EAN, formato, idioma, pais, fecha/ano de publicacion y paginas.
- Anadidas constraints, FKs, checks, indices y unicidad parcial para ISBN/EAN
  entre filas no eliminadas.
- Creadas las entidades `CatalogItem` y `CatalogItemEdition`.
- Creado el enum `CatalogItemEditionFormat`.
- Creados los repositorios `CatalogItemRepository` y
  `CatalogItemEditionRepository`.
- Creados DTOs de request/response para items y editions.
- Creados los servicios `CatalogItemService` y `CatalogItemEditionService`.
- Expuestos 8 endpoints bajo `/api/catalog`:
  - `GET /api/catalog/series/{seriesId}/items`
  - `GET /api/catalog/items/{id}`
  - `POST /api/catalog/series/{seriesId}/items`
  - `PUT /api/catalog/items/{id}`
  - `GET /api/catalog/items/{itemId}/editions`
  - `GET /api/catalog/editions/{id}`
  - `POST /api/catalog/items/{itemId}/editions`
  - `PUT /api/catalog/editions/{id}`
- Los `GET` son publicos para cadenas `ACTIVE` no eliminadas y permiten lectura
  ampliada a `ADMIN`.
- Las escrituras `POST` y `PUT` siguen limitadas a `ADMIN`.
- Se normalizan ISBN/EAN en servicio eliminando espacios y guiones y pasando a
  mayusculas.
- Una edition `ACTIVE` requiere item y serie activos, y publisher activo si se
  informa.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: 237 tests, 0 fallos,
  0 errores y 2 saltados mientras Docker no estaba disponible.
- Ejecutado despues el test `LiquibaseMigrationIntegrationTest` con Docker
  Desktop 29.5.3 y PostgreSQL 17: 1 test correcto, 0 fallos, 0 errores y
  0 saltados; Liquibase 006, las 18 tablas, 43 indices y constraints quedan
  validados sobre PostgreSQL real.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 38 archivos y 80
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; permanece el warning
  conocido del bundle inicial de 592.30 kB frente al budget de 500 kB.
- Ejecutado `docker compose up --build -d`: imagenes backend/frontend
  construidas y PostgreSQL, backend y frontend iniciados correctamente.
- Se mantiene intacto `master_products`.
- Se mantiene intacto `collection_items`.
- Se mantiene intacto `shop_products`.
- Se mantienen intactas recomendaciones, reservas, rutas y UI frontend.
- No se crea `master_product_catalog_links`.
- No se implementa backfill ni frontend editorial.
- No se inicia EPIC 33.
- Siguiente tarea recomendada: EPIC 33, puente con `master_products`, backfill y
  reconciliacion, tras cerrar esta sincronizacion documental.

## 2026-06-30 - EPIC 33 - Puente master_products con catalogo editorial

- Implementado el puente aditivo `master_product_catalog_links` entre
  `master_products` y el catalogo editorial.
- Creada la migracion Liquibase
  `007-create-master-product-catalog-links.sql` e incluida en
  `db.changelog-master.yaml`.
- La tabla enlaza `master_products` con `catalog_items` y opcionalmente con
  `catalog_item_editions`.
- Anadidos estados de enlace `PROPOSED`, `VERIFIED` y `REJECTED`.
- Anadidos origenes de enlace `MANUAL`, `ISBN`, `EAN`, `TITLE`,
  `TITLE_AND_VOLUME`, `TITLE_AND_PUBLISHER` y `BACKFILL`.
- Anadidos checks, FKs, indices y un indice unico parcial para impedir mas de un
  enlace `VERIFIED` activo por `master_product_id`.
- Creada la entidad `MasterProductCatalogLink`.
- Creados los enums `MasterProductCatalogLinkStatus` y
  `MasterProductCatalogLinkSource`.
- Creado el repositorio `MasterProductCatalogLinkRepository`.
- Creados DTOs de request/response para enlaces y respuesta de backfill.
- Creado `MasterProductCatalogLinkService` para busqueda, detalle, creacion,
  actualizacion, verificacion y rechazo.
- Creado `MasterProductCatalogBackfillService` para propuestas idempotentes.
- El backfill crea enlaces `PROPOSED` y nunca verifica automaticamente.
- El backfill propone enlaces por ISBN/EAN normalizados y por coincidencias
  textuales simples cuando hay un unico candidato.
- Expuestos endpoints ADMIN bajo `/api/catalog/master-product-links`:
  - `GET /api/catalog/master-product-links`
  - `GET /api/catalog/master-product-links/{id}`
  - `POST /api/catalog/master-product-links`
  - `PUT /api/catalog/master-product-links/{id}`
  - `PUT /api/catalog/master-product-links/{id}/verify`
  - `PUT /api/catalog/master-product-links/{id}/reject`
  - `POST /api/catalog/master-product-links/backfill`
- Los endpoints del puente son exclusivamente ADMIN.
- Se mantiene intacto `master_products`.
- Se mantiene intacto `collection_items`.
- Se mantiene intacto `shop_products`.
- Se mantienen intactas recomendaciones y reservas.
- No se modifica frontend ni se crea UI de reconciliacion.
- No se activan consumidores legacy sobre el puente.
- No se inicia EPIC 34.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 250
  tests, 0 fallos, 0 errores y 2 saltados por disponibilidad de Docker en los
  tests de integracion condicionados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 38 archivos y 80
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; permanece el warning
  conocido del bundle inicial de 592.30 kB frente al budget de 500 kB.
- Iniciado Docker Desktop 29.5.3 y ejecutados `docker compose down`,
  `docker compose up --build -d`, `docker compose ps` y `docker compose down`
  sin eliminar volumenes. PostgreSQL, backend y frontend arrancaron
  correctamente.
- Validado `GET /api/health`: HTTP 200 con estado `UP`.
- Validado Swagger UI: HTTP 200. OpenAPI incluye el tag `Editorial catalog
  bridge` y las 7 operaciones del puente.
- Sin credenciales ADMIN locales disponibles, no se ejecutaron operaciones
  autenticadas del puente. `GET /api/catalog/master-product-links` y
  `POST /api/catalog/master-product-links/backfill` devolvieron HTTP 401 sin
  token, confirmando su proteccion.
- Confirmada la documentacion exportable: 7 migraciones, 19 tablas de
  aplicacion, 62 endpoints y los 7 endpoints del puente.
- Confirmado que EPIC 33 esta completada, EPIC 34 es la siguiente tarea y MVP 2
  no se presenta como terminado.

## 2026-06-30 - EPIC 34 - API editorial y fachada legacy

- Creada la fachada de lectura `EditorialCatalogFacadeService` y el controller
  `EditorialCatalogFacadeController` bajo `/api/catalog/editorial`.
- Expuestos 5 endpoints nuevos:
  - `GET /api/catalog/editorial/search`
  - `GET /api/catalog/editorial/series/{seriesId}/detail`
  - `GET /api/catalog/editorial/items/{itemId}/detail`
  - `GET /api/catalog/editorial/editions/{editionId}/detail`
  - `GET /api/catalog/editorial/master-products/{masterProductId}/link`
- La busqueda combina series, items y ediciones en un
  `PageResponse<EditorialCatalogSearchItemResponse>` y admite filtros y orden
  validados.
- Creados DTOs agregados de busqueda, contexto, detalle de serie, item y
  edicion, y respuesta del puente legacy.
- Las lecturas publicas solo exponen cadenas completas `ACTIVE` y no
  eliminadas.
- Los resultados `MASTER_PRODUCT_LINK` y la consulta legacy requieren `ADMIN`.
- La consulta legacy prioriza el enlace `VERIFIED`; si no existe, devuelve al
  ADMIN la propuesta mas reciente. No se exponen propuestas al publico.
- Anadidos 13 tests de servicio y seguridad para busqueda, detalle, estados,
  filtros, paginacion y permisos del puente.
- Ejecutado localmente `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`,
  263 tests, 0 fallos, 0 errores y 2 saltados. Los 2 tests saltados
  corresponden a integraciones condicionadas por disponibilidad de Docker/Testcontainers
  durante la fase Maven.
- Ejecutado localmente `cd frontend && npm.cmd ci`: correcto, 474 paquetes
  instalados y 475 auditados. Se mantienen avisos conocidos de
  `@angular/animations`, scripts pendientes de aprobacion y `npm audit`
  reporta 7 vulnerabilidades, 3 bajas y 4 altas, a revisar en una tarea futura
  de mantenimiento sin aplicar `npm audit fix --force` automaticamente.
- Ejecutado localmente `cd frontend && npm.cmd test -- --watch=false`: 38
  archivos de test correctos y 80 tests correctos.
- Ejecutado localmente `cd frontend && npm.cmd run build`: correcto. Permanece
  el warning conocido del bundle inicial de 592.30 kB frente al budget de 500 kB.
- Ejecutado localmente `cd infra && docker compose down`. El primer intento fallo
  porque Docker Desktop no estaba iniciado; tras arrancarlo, el comando funciono.
- Ejecutado localmente `docker compose up --build -d`: imagenes backend/frontend
  construidas, red creada, PostgreSQL healthy y contenedores backend/frontend
  iniciados correctamente.
- Validado localmente `curl.exe http://localhost:8080/api/health`: HTTP 200 con
  respuesta `{"status":"UP","service":"collectohub-backend"}`.
- Ejecutado localmente `docker compose down`: contenedores frontend, backend,
  PostgreSQL y red eliminados correctamente, sin usar `-v` y sin borrar volumenes.
- Actualizados contrato API, decisiones, estado, backlog, README y exportables;
  el inventario queda alineado en 67 endpoints, 5 de la fachada.
- Se mantiene intacto `/api/master-products` y `MasterProductResponse`.
- Se mantienen intactos `collection_items`, `shop_products`, recomendaciones y
  reservas.
- No se crea frontend editorial ni se inicia EPIC 35.
- Siguiente tarea recomendada: EPIC 35, frontend editorial sobre la nueva
  fachada de lectura.

## 2026-07-01 - EPIC 35 - Frontend editorial

- Creado el modelo TypeScript `editorial-catalog.model.ts` con respuestas de
  busqueda, serie, item, edicion, puente legacy, paginacion y filtros.
- Creado `EditorialCatalogService` para consumir los 5 endpoints de la fachada
  bajo `/api/catalog/editorial`.
- El metodo ADMIN `getMasterProductLink` queda modelado para uso futuro, pero no
  se expone desde la UI publica.
- Anadidas 4 rutas publicas lazy-loaded:
  - `/catalog/editorial`
  - `/catalog/editorial/series/:seriesId`
  - `/catalog/editorial/items/:itemId`
  - `/catalog/editorial/editions/:editionId`
- Creada la busqueda editorial con query, tipo BOOK/COMIC/MANGA, tipo de
  resultado SERIES/ITEM/EDITION, estados de carga/error/vacio y paginacion.
- Creados detalles publicos de serie, item y edicion con navegacion jerarquica,
  metadatos editoriales y cubiertas cuando estan disponibles.
- Anadido acceso visible `Catalogo editorial` en header, sidebar y navegacion
  movil, manteniendo `/catalog` como catalogo legacy independiente.
- Actualizado i18n ES/EN con textos y enums editoriales.
- Anadidos tests de servicio HTTP, busqueda, estado vacio, resultados,
  navegacion por tipo, detalles, rutas e i18n.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 89 tests correctos.
- Ejecutado `npm.cmd run build`: correcto; permanece el warning conocido del
  bundle inicial, 597.68 kB frente al budget de 500 kB.
- Validada visualmente la busqueda editorial en escritorio y movil con
  Playwright sobre backend/frontend locales, sin cortes ni solapamientos.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 263 tests,
  0 fallos, 0 errores y 0 saltados.
- Ejecutados `docker compose down` y `docker compose up --build -d`; PostgreSQL,
  backend y frontend quedaron `healthy`.
- Validados `GET /api/health` con estado `UP` y `/catalog/editorial` con HTTP
  200 sobre el stack Docker.
- Ejecutado `docker compose down` al finalizar, sin `-v` ni borrado de volumenes.
- Actualizados backlog, decisiones, estado, README y exportables: 32 rutas
  Angular y 57 relaciones frontend-backend.
- No se modifica backend, migraciones, `/api/master-products`,
  `collection_items`, `shop_products`, recomendaciones ni reservas.
- No se crean colecciones editoriales, inventario por edicion, matching ni UI
  ADMIN de reconciliacion. No se inicia EPIC 36.
- Siguiente tarea recomendada: EPIC 36, referencias editoriales graduales en
  colecciones.

## 2026-07-01 - EPIC 36 - Referencias editoriales en colecciones

- Creada la migracion Liquibase
  `008-add-editorial-references-to-collection-items.sql` y registrada en el
  changelog master.
- `collection_items` incorpora `catalog_item_id`, `catalog_item_edition_id` y
  `editorial_reference_source`; `master_product_id` pasa a nullable con checks,
  FKs e indices defensivos.
- Anadido backfill idempotente para items activos usando exclusivamente enlaces
  `VERIFIED` activos, sin cambiar producto master ni estado de coleccion.
- Ampliados entidad, DTOs y servicio para referencias legacy, puente verificado
  y editorial manual. Las contradicciones con puente verificado devuelven 409.
- Las recomendaciones ignoran defensivamente items editoriales puros; no se
  implementa matching editorial.
- Actualizados create/edit/detail de colecciones con selector legacy/editorial,
  busqueda de items/ediciones, rechazo de series y fallback visual legacy.
- Anadidas traducciones ES/EN y tests backend/frontend para los contratos y
  comportamientos nuevos.
- Ejecutado localmente el 2026-07-07
  `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 271 tests,
  0 fallos, 0 errores y 2 saltados. Los saltos corresponden a integraciones
  condicionadas por Docker/Testcontainers porque Docker Desktop no estaba
  iniciado.
- Ejecutado `npx.cmd tsc -p tsconfig.spec.json --noEmit`: correcto.
- Ejecutado localmente el 2026-07-07 `cd frontend && npm.cmd ci`: correcto,
  474 paquetes instalados y 475 auditados. Se mantienen los avisos conocidos de
  `@angular/animations`, scripts pendientes de aprobacion y 7 vulnerabilidades
  npm, 3 bajas y 4 altas, sin aplicar `npm audit fix --force`.
- Ejecutado localmente `npm.cmd test -- --watch=false`: 43 archivos de test y
  94 tests correctos.
- Ejecutado localmente `npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 598.92 kB frente al budget de 500 kB.
- Ejecutado `docker compose down` y `docker compose up --build -d`: imagenes
  backend/frontend construidas, PostgreSQL y frontend healthy, y backend
  iniciado. El primer health se consulto mientras Spring seguia en `starting`;
  la repeticion y el `docker compose down` final quedaron bloqueados al agotarse
  la ventana de acciones ampliadas del entorno. No se uso `-v` ni se borraron
  volumenes.
- Revisado el 2026-07-07: Docker Desktop no estaba iniciado y no habia un daemon
  accesible; no se repitio el stack. La validacion de build Docker anterior se
  conserva como evidencia, sin presentar el health final como completado.
- Se mantienen 19 tablas, 67 endpoints y 32 rutas Angular; el mapa
  frontend-backend pasa a 59 relaciones.
- No se modifican `shop_products`, inventario ni reservas. No se implementa
  inventario por edicion, matching editorial ni EPIC 37.
- Siguiente tarea recomendada: EPIC 37, inventario y matching editorial.

## 2026-07-07 - EPIC 36B - Cierre de referencias editoriales en colecciones

- Corregida la edicion de items para que los modos legacy y editorial sean
  selecciones funcionales y no solo un cambio visual.
- El modo legacy permite buscar productos master, precarga el
  `masterProductId` actual, exige una seleccion y envia los IDs editoriales a
  `null` al guardar.
- El modo editorial conserva la referencia existente si no se selecciona otra,
  permite cambiar a item o edicion y sigue rechazando resultados `SERIES`.
- Corregida en backend la semantica de update detectada durante la validacion:
  una seleccion legacy explicita limpia la referencia editorial y una seleccion
  de item editorial aplica exactamente la edicion enviada, incluido `null`.
- Anadidas traducciones ES/EN y ampliados los tests del editor y de
  `CollectionService`.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados; se mantienen 7 vulnerabilidades conocidas, 3 bajas y 4 altas,
  sin ejecutar `npm audit fix --force`.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 100 tests correctos.
- Ejecutado `npm.cmd run build`: correcto; permanece el warning conocido del
  bundle inicial de 599.35 kB frente al budget de 500 kB.
- Ejecutado `cd backend && .\mvnw.cmd clean verify` por el ajuste real de
  servicio: `BUILD SUCCESS`, 273 tests, 0 fallos, 0 errores y 0 saltados.
- Ejecutado `docker compose down`, `docker compose up --build -d`, health y
  `docker compose down`: PostgreSQL, backend y frontend healthy; `/api/health`
  respondio `{"status":"UP","service":"collectohub-backend"}`. No se uso
  `-v` ni se borraron volumenes.
- No se crean migraciones, endpoints, inventario editorial, matching editorial
  ni cambios en `shop_products`, recomendaciones o reservas. No se inicia
  EPIC 37.

## 2026-07-07 - EPIC 37A - Backend de inventario editorial

- Creada y registrada la migracion Liquibase
  `009-add-editorial-references-to-shop-products.sql`.
- `shop_products` incorpora referencias opcionales a item y edicion editorial,
  su origen (`LEGACY`, `VERIFIED_BRIDGE` o `MANUAL_EDITORIAL`), FKs, checks e
  indices defensivos; `master_product_id` pasa a nullable.
- Anadido backfill idempotente exclusivamente desde enlaces `VERIFIED` activos,
  sin aceptar enlaces propuestos o rechazados.
- Ampliados entidad, DTOs y servicio de inventario para crear y actualizar
  referencias legacy, puentes verificados, items y ediciones editoriales.
- Las referencias contradictorias con un puente verificado devuelven 409 y las
  combinaciones invalidas devuelven 400.
- Las lecturas publicas admiten inventario editorial puro y conservan los
  filtros legacy asociados a `master_products`.
- Anadidos tests de servicio, parseo de changelog y migracion para los nuevos
  contratos, reglas, constraints e indices.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 286 tests,
  0 fallos, 0 errores y 0 saltados.
- No se modifica frontend, colecciones, reservas ni recomendaciones; no se
  implementa matching editorial ni se inicia la siguiente fase.
- Siguiente tarea recomendada: completar la siguiente sub-EPIC de inventario y
  matching editorial conforme al backlog aprobado.

## 2026-07-07 - EPIC 37B - Frontend inventario editorial

- Actualizados los modelos frontend de inventario para admitir referencias
  legacy, items y ediciones editoriales, con campos master nullable.
- Create y edit de inventario incorporan selector de referencia
  legacy/editorial, busqueda especifica, rechazo de series y conservacion de la
  referencia editorial actual durante la edicion.
- Lista, detalle de inventario y resumen de tienda muestran datos editoriales
  preferentes con fallback completo para productos legacy.
- Ampliados i18n ES/EN y tests de modelos, payloads, create/edit, lista y detalle.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen 7 vulnerabilidades conocidas, 3 bajas y 4 altas,
  sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 111 tests correctos.
- Ejecutado `npm.cmd run build`: correcto. Permanece el warning conocido del
  bundle inicial de 600.57 kB frente al budget de 500 kB.
- No se modifica backend ni migraciones; no se implementa matching editorial,
  recomendaciones editoriales, reservas editoriales ni colecciones.
- Siguiente tarea recomendada: EPIC 37C.

## 2026-07-07 - EPIC 37C - Matching editorial en recomendaciones

- Anadido matching por edicion exacta (`EDITION_EXACT`) y por item editorial
  (`ITEM_EXACT`), manteniendo el fallback legacy por `masterProduct`
  (`LEGACY_MASTER_PRODUCT`).
- La deduplicacion por producto de tienda conserva la mejor coincidencia por
  tipo de match, estado MISSING/WANTED, nombre de coleccion e ID de item.
- Los repositorios de colecciones e inventario usan `left join` para admitir
  referencias editoriales puras sin exigir `masterProduct`.
- Ampliado el DTO de recomendaciones con referencias y metadatos editoriales,
  `matchType` y fallbacks para nombre, franquicia, serie, volumen y cubierta.
- Los filtros comerciales existentes se mantienen para legacy y editorial.
  `categoryCode` sigue aplicandose solo cuando existe categoria master legacy,
  sin inventar equivalencias con tipos de serie editorial.
- Ampliados los tests backend para matching editorial puro, prioridades,
  deduplicacion, fallbacks, filtros y summary sin categoria legacy.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 292 tests,
  0 fallos, 0 errores y 0 saltados.
- No se modifica frontend ni migraciones; no se tocan reservas y no se
  implementan marketplace, pagos, alertas ni ranking avanzado.
- Siguiente tarea recomendada: EPIC 37D.

## 2026-07-07 - EPIC 37D - Cierre documental y validacion integral

- Actualizados README, backlog, diseno MVP 2, decisiones, estado MVP, contrato
  API y exportables tecnicos para cerrar EPIC 37 y situar EPIC 38 como siguiente
  tarea.
- Los exports reflejan 9 migraciones Liquibase, 19 tablas de aplicacion, 67
  endpoints, 32 rutas Angular y 61 relaciones frontend-backend.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 292 tests,
  0 fallos, 0 errores y 0 saltados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen 7 vulnerabilidades conocidas, 3 bajas y 4 altas,
  sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 111 tests correctos.
- Ejecutado `npm.cmd run build`: correcto. Permanece el warning conocido del
  bundle inicial de 600.57 kB frente al budget de 500 kB.
- Ejecutado `docker compose down` y `docker compose up --build -d`: imagenes
  backend/frontend construidas, PostgreSQL healthy y servicios iniciados.
- Validado `curl.exe http://localhost:8080/api/health`: respuesta
  `{"status":"UP","service":"collectohub-backend"}`.
- Ejecutado `docker compose down` al finalizar, sin `-v` ni borrado de volumenes.
- No se modifica backend funcional ni frontend funcional; no se crean
  migraciones, endpoints o rutas y no se inicia EPIC 38.
- EPIC 37 queda cerrada. Siguiente tarea recomendada: EPIC 38, creators y
  relaciones priorizadas.

## 2026-07-08 - EPIC 38A - Backend creators editoriales

- Creada y registrada la migracion Liquibase 010 con las tablas `creators` y
  `catalog_item_creators`, FKs, checks, indices y unicidades parciales.
- Anadidos dominio, DTOs, repositorios y servicios para creators y creditos por
  item con roles AUTHOR, WRITER, ARTIST, ILLUSTRATOR, TRANSLATOR, EDITOR y OTHER.
- Creados 9 endpoints bajo `/api/catalog`: 5 para creators y 4 para creditos de
  item. Las lecturas son publicas para contenido ACTIVE y las escrituras
  requieren ADMIN.
- Implementadas normalizacion de slug/pais, validacion de anos, deteccion de
  duplicados, orden de creditos y borrado logico.
- La fachada de detalle editorial no se amplia en esta fase; la integracion de
  creators se reserva para EPIC 38B para mantener aislado el contrato agregado.
- Anadidos tests de servicios, seguridad, contexto, parseo y migracion.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 306 tests,
  0 fallos, 0 errores y 0 saltados.
- No se modifica frontend; no se implementan relaciones entre obras,
  `catalog_relationships`, creators de serie, reservas, marketplace ni pagos.
- Siguiente tarea recomendada: EPIC 38B.

## 2026-07-08 - EPIC 38B - Creators en detalle editorial

- Ampliado `GET /api/catalog/editorial/items/{itemId}/detail` con la lista
  `creators`, usando solo creditos activos y creators publicos ACTIVE, en el
  orden estable definido por creditOrder, nombre e ID.
- Anadido DTO agregado de creditos y mantenido el detalle sin creators como
  lista vacia, sin cambiar endpoints existentes.
- El frontend de detalle de item muestra la seccion Creditos solo cuando hay
  datos, con nombre, rol traducido y etiqueta opcional.
- Anadidos roles de creator en i18n ES/EN y tests para contenido presente,
  vacio y respuestas antiguas sin el campo `creators`.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 308 tests,
  0 fallos, 0 errores y 0 saltados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados; se mantienen 7 vulnerabilidades conocidas, 3 bajas y 4 altas,
  sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 114 tests correctos.
- Ejecutado `npm.cmd run build`: correcto. Permanece el warning conocido del
  bundle inicial de 600.90 kB frente al budget de 500 kB.
- No se crean migraciones, tablas, endpoints o rutas; no se implementan
  relaciones entre obras, reservas, marketplace, pagos ni social.
- Siguiente tarea recomendada: EPIC 38C.

## 2026-07-08 - EPIC 38C - Backend relaciones editoriales

- Creada y registrada la migracion Liquibase 011 con la tabla
  `catalog_item_relationships`, FKs entre items, checks, indices y unicidad
  parcial para relaciones activas no eliminadas.
- Implementados los tipos ADAPTATION, REMAKE, REPRINT, SAME_WORK, SPIN_OFF,
  PREQUEL, SEQUEL y RELATED, con estado editorial, orden y borrado logico.
- Creados 5 endpoints bajo `/api/catalog/items/{itemId}/relationships` para
  listar, consultar, crear, actualizar y eliminar relaciones. GET es publico
  para relaciones ACTIVE entre items publicos; las escrituras requieren ADMIN.
- El listado combina relaciones entrantes y salientes, devuelve direccion
  INCOMING/OUTGOING y mantiene orden estable por prioridad, titulo e ID.
- Anadidos tests de servicio, seguridad API, contexto, parseo Liquibase y
  migracion PostgreSQL para tabla, FKs, checks, indices y unicidad parcial.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 320 tests,
  0 fallos, 0 errores y 0 saltados.
- No se modifica frontend ni se crean rutas Angular. La integracion visual y
  con la fachada editorial queda pendiente para EPIC 38D.
- No se implementan reservas, marketplace, pagos, social ni relaciones entre
  series o ediciones.
- Siguiente tarea recomendada: EPIC 38D.

## 2026-07-08 - EPIC 38D - Relaciones en detalle editorial

- Ampliado `GET /api/catalog/editorial/items/{itemId}/detail` con
  `relationships`, reutilizando el servicio publico de relaciones para devolver
  solo relaciones ACTIVE/no eliminadas entre items publicos.
- El detalle editorial mantiene creators y editions y devuelve `relationships`
  como lista vacia cuando no existen relaciones publicas.
- El frontend de detalle de item muestra relaciones despues de creditos y antes
  de ediciones, con tipo, direccion, item relacionado, serie relacionada,
  descripcion opcional y enlace al item relacionado correcto.
- Anadidos modelos TypeScript para tipos/direcciones de relationships e i18n
  ES/EN para tipos y direcciones.
- Anadidos/actualizados tests backend de fachada y tests frontend del detalle de
  item, incluyendo relationships presentes, vacios, undefined, tipos,
  direcciones y enlaces OUTGOING/INCOMING.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 322
  tests, 0 fallos, 0 errores y 2 saltados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados. Se
  mantienen avisos conocidos de `@angular/animations` y scripts pendientes de
  aprobacion, sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `npm.cmd test -- --watch=false`: 43 archivos y 119 tests correctos.
- Ejecutado `npm.cmd run build`: correcto. Permanece el warning conocido del
  bundle inicial de 601.47 kB frente al budget de 500 kB.
- No se crean migraciones, tablas, endpoints ni rutas Angular nuevas.
- No se implementan pantalla de grafo, pantalla admin de relaciones,
  marketplace, pagos, reservas ni social.
- Siguiente tarea recomendada: EPIC 38E.

## 2026-07-08 - EPIC 38E - Cierre documental y validacion integral

- Actualizada la documentacion de estado para reflejar que EPIC 38 queda
  cerrada: creators editoriales, creditos por item, relaciones entre items y
  detalle editorial agregado estan implementados.
- Actualizados los exports documentales con el inventario actual: 11
  migraciones Liquibase, 22 tablas de aplicacion, 81 endpoints backend, 32 rutas
  Angular y 61 relaciones frontend-backend documentadas.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 322
  tests, 0 fallos, 0 errores y 2 saltados.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados. Se
  mantienen avisos conocidos de `@angular/animations` y scripts pendientes de
  aprobacion, sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 43 archivos y 119
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 601.47 kB frente al budget de 500 kB.
- Ejecutado `cd infra && docker compose down`: el primer intento fallo porque
  Docker Desktop no estaba iniciado; tras arrancarlo, el comando funciono.
- Ejecutado `cd infra && docker compose up --build -d`: imagenes backend y
  frontend construidas, red creada, PostgreSQL healthy y contenedores backend y
  frontend iniciados correctamente.
- Validado `curl.exe http://localhost:8080/api/health`: HTTP 200 con respuesta
  `{"status":"UP","service":"collectohub-backend"}`.
- Ejecutado `cd infra && docker compose down`: contenedores frontend, backend y
  PostgreSQL eliminados junto con la red, sin usar `-v` y sin borrar volumenes.
- No se modifica backend funcional, frontend funcional, migraciones, endpoints
  ni rutas Angular. No se inicia EPIC 39.
- Siguiente tarea recomendada: definir EPIC 39 tras revisar el alcance restante
  de MVP 2; candidata: cierre de adopcion gradual del catalogo editorial.

## 2026-07-09 - EPIC 39A - Auditoria de cierre MVP2

- Creado `docs/20_MVP2_CLOSURE_REVIEW.md` con auditoria de capacidades,
  evidencias, riesgos, deuda tecnica/documental, limitaciones y propuesta de
  siguiente fase.
- Estado recomendado de MVP 2: `MVP2_CLOSED_WITH_LIMITATIONS`.
- Siguiente EPIC propuesta: EPIC 39 - Cierre MVP2 editorial y preparacion de
  MVP3 Admin Editorial.
- Actualizados README, roadmap, dominios, modelo conceptual, backlog, estado
  MVP y decisiones para alinear la documentacion con EPIC 31 a EPIC 38.
- Validacion documental: `git diff --check` ejecutado sin errores.
- No se modifica backend funcional, frontend funcional, migraciones, endpoints
  ni rutas Angular. No se inicia EPIC 39 funcional.

## 2026-07-09 - EPIC 39B - Alineacion roadmap producto

- Roadmap de producto alineado a una secuencia de 10 MVPs tras el cierre de
  MVP 2.
- MVP 3 redefinido como Admin editorial y carga real de datos.
- Social basico movido a MVP 6 para evitar saltar a social/comercio antes de
  tener gestion editorial mantenible.
- Backlog actualizado con EPIC 39 como cierre formal MVP2 y preparacion de MVP3
  Admin Editorial.
- Estado MVP actualizado para indicar MVP 2 cerrado con limitaciones y siguiente
  gran bloque MVP 3 Admin editorial.
- Closure review actualizado para dejar claro que MVP 3 sera Admin editorial y
  Social basico queda en fase posterior.
- Validacion documental: `git diff --check` ejecutado sin errores.
- No se modifica backend funcional, frontend funcional, migraciones, endpoints
  ni rutas Angular.

## 2026-07-09 - EPIC 39C - Plan de entrada MVP3 Admin Editorial

- Creado `docs/21_MVP3_ADMIN_EDITORIAL_PLAN.md`.
- Alcance MVP3 definido: admin editorial, carga real de datos, calidad,
  busqueda/filtros admin, validaciones visuales, duplicados basicos,
  reconciliacion y datos demo editoriales.
- Fuera de alcance definido: social, tiendas como flujo principal, marketplace,
  pagos, movil, wiki publica, importadores masivos abiertos, IA, scraping,
  moderacion comunitaria avanzada y grafo avanzado.
- Orden de EPICs MVP3 definido: EPIC 40A a EPIC 40G.
- EPIC 40A propuesta como primera fase funcional: Admin editorial shell y
  navegacion protegida.
- Backlog actualizado, estado MVP actualizado y decisiones actualizadas.
- Validacion documental: `git diff --check` ejecutado sin errores.
- No se modifica backend funcional, frontend funcional, migraciones, endpoints
  ni rutas Angular.

## 2026-07-10 - EPIC 40A - Admin editorial shell y navegacion protegida

- Creado guard funcional `adminGuard` como `CanActivateFn`, reutilizando
  `AuthService`, redirigiendo usuarios anonimos a `/login` con `returnUrl` y
  usuarios autenticados sin `ADMIN` a `/home`.
- Creada ruta lazy `/admin/editorial` protegida por `[authGuard, adminGuard]`.
- Creado shell standalone de admin editorial con secciones futuras de
  publishers, franquicias, series, items, ediciones, creators, creditos,
  relaciones y reconciliacion legacy.
- Anadido enlace de navegacion admin en header, menu de usuario y sidebar solo
  para usuarios con rol `ADMIN`.
- Anadidas traducciones ES/EN para la navegacion y el shell admin.
- Anadidos tests de guard, shell, navegacion visible solo para `ADMIN` y claves
  i18n.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen avisos conocidos de `@angular/animations`,
  scripts pendientes de aprobacion y 7 vulnerabilidades npm, sin ejecutar
  `npm audit fix` ni `npm audit fix --force`.
- Primer intento de `npm.cmd test -- --watch=false`: fallo por expectativa del
  test nuevo en ES mientras el entorno de test renderizaba EN. Ajustado el spec
  del shell sin cambiar logica funcional.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 45 archivos y 125
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 604.55 kB frente al budget de 500 kB.
- No se modifica backend, infra, migraciones, endpoints, CRUDs, formularios,
  servicios admin, reservas, tiendas, inventario, recomendaciones, colecciones,
  social ni marketplace.
- No se inicia EPIC 40B.
- Siguiente tarea recomendada: EPIC 40B - Admin publishers/franchises/series.

## 2026-07-10 - EPIC 40B - Admin publishers/franchises/series

- Creados modelos frontend admin para `CatalogRecordStatus`, `CatalogSeriesType`,
  `CatalogPublicationStatus`, `PageResponse`, publishers, franchises y series.
- Creado `EditorialAdminService` con busqueda, detalle, create y update para
  publishers, franchises y series, usando `/api/catalog`.
- Creadas rutas admin protegidas por `[authGuard, adminGuard]`:
  `/admin/editorial/publishers`, `/admin/editorial/franchises` y
  `/admin/editorial/series`.
- Actualizado el shell admin para enlazar publishers, franchises y series; el
  resto de secciones sigue sin CRUD funcional.
- Creadas pantallas admin de publishers, franchises y series con busqueda,
  filtro por `recordStatus`, listado, create, update y cambio de estado
  `DRAFT` / `ACTIVE` / `ARCHIVED`.
- Anadidas validaciones visuales: nombre/titulo obligatorios, pais de 2 letras,
  slug de franchise, IDs numericos simples para series y rango de anos
  `endYear >= startYear`.
- Anadidas traducciones ES/EN para acciones, campos, estados, tipos, estados de
  publicacion y mensajes.
- Anadidos/actualizados tests de servicio admin, rutas, shell, i18n y pantallas
  admin de publishers/franchises/series.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen avisos conocidos de `@angular/animations`,
  scripts pendientes de aprobacion y 7 vulnerabilidades npm, sin ejecutar
  `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 49 archivos y 142
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 607.47 kB frente al budget de 500 kB.
- No se modifica backend funcional, backend tests, migraciones, infra,
  endpoints ni docs/export.
- No se implementan items/editions, creators, credits, relationships,
  reconciliacion master-product-links, social, tiendas, marketplace, pagos ni
  movil.
- Siguiente tarea recomendada: EPIC 40C - Admin items/editions.

## 2026-07-10 - EPIC 40C - Admin items/editions

- Ampliados modelos frontend admin con `CatalogItemEditionFormat`, items,
  editions y parametros de busqueda para items/editions.
- Ampliado `EditorialAdminService` con busqueda, detalle, create y update para
  items y editions, usando endpoints existentes bajo `/api/catalog`.
- Creadas rutas admin protegidas por `[authGuard, adminGuard]`:
  `/admin/editorial/items` y `/admin/editorial/editions`.
- Actualizado el shell admin para enlazar items y editions; creators, credits,
  relationships y reconciliation siguen sin CRUD funcional.
- Creada pantalla admin de items con `seriesId` numerico obligatorio, busqueda,
  filtros, listado, create, update y cambio de estado `DRAFT` / `ACTIVE` /
  `ARCHIVED`.
- Creada pantalla admin de editions con `itemId` numerico obligatorio, filtros,
  listado, create, update, formatos editoriales y cambio de estado.
- Anadidas validaciones visuales para pais de 2 letras, anos 1000-3000,
  `sortOrder` no negativo, `pageCount` positivo y `coverImageUrl` http/https.
- Anadidas traducciones ES/EN para items, editions, campos, formatos y mensajes.
- Anadidos/actualizados tests de servicio admin, rutas, shell, i18n y pantallas
  admin de items/editions.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen avisos conocidos de `@angular/animations`,
  scripts pendientes de aprobacion y 7 vulnerabilidades npm, sin ejecutar
  `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 51 archivos y 152
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 609.54 kB frente al budget de 500 kB.
- No se modifica backend funcional, backend tests, migraciones, infra,
  endpoints ni docs/export.
- No se implementan creators, credits, relationships, reconciliacion
  master-product-links, social, tiendas, marketplace, pagos ni movil.
- Siguiente tarea recomendada: EPIC 40D - Admin creators y creditos.

## 2026-07-10 - EPIC 40D - Admin creators y creditos

- Ampliados modelos frontend admin con `CreatorCreditRole`, creators, creditos
  por item y parametros de busqueda para creators.
- Ampliado `EditorialAdminService` con busqueda, detalle, create, update y
  delete para creators, y listado, create, update y delete de creditos por item,
  usando endpoints existentes bajo `/api/catalog`.
- Creadas rutas admin protegidas por `[authGuard, adminGuard]`:
  `/admin/editorial/creators` y `/admin/editorial/credits`.
- Actualizado el shell admin para enlazar creators y creditos; relationships y
  reconciliation siguen sin CRUD funcional.
- Creada pantalla admin de creators con busqueda, filtro por `recordStatus`,
  listado, create, update, delete y cambio de estado `DRAFT` / `ACTIVE` /
  `ARCHIVED`.
- Creada pantalla admin de creditos por item con contexto `catalogItemId`,
  listado, create, update y delete de roles `AUTHOR`, `WRITER`, `ARTIST`,
  `ILLUSTRATOR`, `TRANSLATOR`, `EDITOR` y `OTHER`.
- Anadidas validaciones visuales para nombre obligatorio, slug opcional,
  pais de 2 letras, anos de vida no negativos, `deathYear >= birthYear`,
  `creatorId` obligatorio y `creditOrder` positivo.
- Anadidas traducciones ES/EN para creators, creditos, roles, campos y mensajes.
- Anadidos/actualizados tests de servicio admin, rutas, shell, i18n y pantallas
  admin de creators/creditos.
- Primer intento de `cd frontend && npm.cmd test -- --watch=false` dentro del
  sandbox: fallo por `Acceso denegado` al resolver ficheros SCSS y specs.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados. Se
  mantienen avisos conocidos de `@angular/animations` y scripts pendientes de
  aprobacion, sin ejecutar `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false` fuera del sandbox:
  53 archivos y 162 tests correctos.
- Ejecutado `cd frontend && npm.cmd run build` fuera del sandbox: correcto.
  Permanece el warning conocido del bundle inicial de 611.63 kB frente al
  budget de 500 kB.
- No se modifica backend funcional, backend tests, migraciones, infra,
  endpoints ni docs/export.
- No se implementan relationships, reconciliacion master-product-links, imports,
  social, tiendas, marketplace, pagos ni movil.
- Siguiente tarea recomendada: EPIC 40E - Admin relationships.

## 2026-07-10 - EPIC 40E - Admin editorial relationships

- Anadidos modelos frontend admin para `EditorialAdminRelationshipType`,
  `EditorialAdminRelationshipDirection`, response y requests de relaciones
  editoriales entre items.
- Ampliado `EditorialAdminService` con `getItemRelationships`,
  `createItemRelationship`, `updateItemRelationship` y
  `deleteItemRelationship`, usando endpoints existentes bajo
  `/api/catalog/items/{itemId}/relationships`.
- Creada ruta admin protegida por `[authGuard, adminGuard]`:
  `/admin/editorial/relationships`.
- Actualizado el shell admin para enlazar relationships; reconciliation sigue
  sin CRUD funcional.
- Creada pantalla admin relationships con busqueda/seleccion de item origen y
  destino, listado de relaciones por item origen, filtro por estado, create,
  update, delete, loading state, empty state y errores backend.
- Anadidas validaciones visuales para source obligatorio, target obligatorio,
  `relationshipOrder > 0`, tipo requerido, estado requerido y aviso de que
  source y target deben ser distintos.
- Anadidas traducciones ES/EN para relationships, tipos, direcciones, campos y
  mensajes.
- Anadidos/actualizados tests de servicio admin, rutas, shell, i18n y pantalla
  admin relationships.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Se mantienen avisos conocidos de `@angular/animations`,
  scripts pendientes de aprobacion y 7 vulnerabilidades npm, sin ejecutar
  `npm audit fix` ni `npm audit fix --force`.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 54 archivos y 168
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto. Permanece el warning
  conocido del bundle inicial de 613.64 kB frente al budget de 500 kB.
- No se modifica backend funcional, backend tests, migraciones, infra,
  endpoints ni docs/export.
- No se implementan endpoints nuevos, grafo avanzado, relaciones automaticas
  inversas, relaciones entre series, relaciones entre ediciones, reconciliacion
  legacy, master product links admin, social, tiendas, marketplace, pagos ni
  movil.
- Siguiente tarea recomendada: EPIC 40F - Admin master product links/reconciliation.

## 2026-07-10 - EPIC 40F - Admin master product links/reconciliation

- Creada la ruta protegida `/admin/editorial/master-product-links`.
- Creada la pantalla admin de enlaces entre `master_products` y catalogo editorial.
- Anadidos modelos, servicio frontend, bridge lookup, verify/reject y backfill
  con confirmacion explicita.
- Anadidos filtros por master product, item, edition, estado y fuente; create y
  update con validaciones visuales de IDs positivos y confianza entre 0 y 1.
- Anadido i18n ES/EN y tests frontend de servicio, ruta, shell y componente.
- Ejecutados `npm.cmd ci`, `npm.cmd test -- --watch=false` y `npm.cmd run build`.
- Sin backend funcional, migraciones ni endpoints nuevos.
- Sin importadores abiertos, IA, scraping ni reconciliacion automatica avanzada.
- Siguiente tarea recomendada: EPIC 40G - Datos demo editoriales y validacion
  integral MVP3 parcial.

## 2026-07-10 - EPIC 40G - Datos demo editoriales y validacion integral MVP3 parcial

- Creado `scripts/demo/create-editorial-admin-demo-data.ps1`, con parametros
  para API, cuenta ADMIN, sufijo y `MasterProductId` opcional. Usa solamente
  endpoints existentes, reintenta conflictos 409 por busqueda y no persiste
  tokens ni credenciales.
- El script cubre publisher, franchise, series, dos items, edition, creator,
  creator credit, relationship y master product link opcional. Guarda un
  resumen local ignorado en `.last-editorial-admin-demo-data.json`.
- Validada sintaxis y flujo seguro mediante `-WhatIf`; no se ejecuto contra la
  API porque `localhost:8080` no estaba levantado y no habia una cuenta ADMIN
  local disponible durante esta validacion.
- Documentadas las diez rutas ADMIN de MVP3, de `/admin/editorial` a
  `/admin/editorial/master-product-links`.
- Ejecutado `cd frontend && npm.cmd ci`: correcto, 474 paquetes instalados y
  475 auditados. Persisten los avisos conocidos de scripts pendientes,
  `@angular/animations` y 7 vulnerabilidades npm, sin aplicar fixes automaticos.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 55 archivos y 174
  tests correctos.
- Ejecutado `cd frontend && npm.cmd run build`: correcto; persiste el warning
  conocido del bundle inicial de 617.05 kB frente al budget de 500 kB.
- Ejecutado `cd backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 322
  tests, 0 fallos, 0 errores y 2 omitidos.
- Docker no pudo validarse: Docker Desktop no exponia el daemon
  `dockerDesktopLinuxEngine`; no se ejecutaron `up` ni operaciones sobre
  volumenes.
- Sin backend funcional, migraciones, endpoints nuevos ni docs/export.
- Sin social, tiendas, marketplace, pagos ni movil.
- MVP3 Admin Editorial queda validado parcialmente; siguiente tarea recomendada:
  EPIC 41A - Auditoria UX/Admin editorial y bugs de usabilidad.

## 2026-07-10 - EPIC 41A - Auditoria UX/Admin editorial y bugs de usabilidad

- Creada la auditoria `docs/22_MVP3_ADMIN_UX_AUDIT.md` para las diez pantallas
  admin de MVP3.
- Corregidas confirmaciones hardcodeadas de creators, credits, relationships y
  master product links; ahora usan i18n ES/EN.
- Revisados shell, layout, rutas protegidas, visibilidad solo ADMIN, estados
  vacios/carga/error y validaciones existentes.
- Actualizados tests de traducciones; los tests de layout ya cubren anonimo,
  usuario normal y ADMIN.
- Ejecutados frontend `npm.cmd ci`, `npm.cmd test -- --watch=false` y
  `npm.cmd run build`. El backend no se reejecuto en esta auditoria sin cambios
  funcionales; conserva la validacion verde documentada en EPIC 40G.
- Docker no pudo validarse si Docker Desktop no expone el daemon local.
- Sin backend funcional, migraciones, endpoints nuevos, rutas nuevas ni MVP4.
- Siguiente tarea recomendada: EPIC 41B - Cierre documental/exportable MVP3
  parcial.

## 2026-07-10 - EPIC 41B - Cierre documental/exportable MVP3 parcial

- Creado `docs/23_MVP3_PARTIAL_CLOSURE.md`; estado MVP, plan y backlog
  alineados con EPIC 40A-40G y 41A completadas.
- Actualizados exports de README, rutas y mapa frontend-backend para MVP3 ADMIN.
- El snapshot de base de datos permanece sin cambios: no hay migraciones nuevas.
- Ejecutado `git diff --check`; sin backend funcional, endpoints, rutas nuevas
  ni MVP4. Siguiente tarea: EPIC 41C - E2E basico Admin Editorial y datos demo.

## 2026-07-10 - EPIC 42A - Calidad de datos editorial y reglas anti-duplicados

- Creado `docs/24_EDITORIAL_DATA_QUALITY.md` con reglas de normalizacion,
  riesgos y checks manuales para cada entidad editorial.
- Anadidas claves i18n ES/EN para warnings no bloqueantes de posibles
  duplicados. No se fuerza deteccion automatica sobre resultados paginados o
  contextuales incompletos para evitar avisos fragiles.
- El frontend ya normaliza textos opcionales, pais y datos de formulario donde
  corresponde; no se cambian contratos ni reglas backend.
- Sin backend funcional, migraciones, endpoints, rutas, E2E, Playwright ni MVP4.
- Siguiente tarea recomendada: EPIC 42B - Hardening admin editorial y
  validaciones backend selectivas.

## 2026-07-10 - EPIC 42B - Hardening admin editorial y validaciones backend selectivas

- Auditadas validaciones existentes: credits, relationships y master product
  links ya cubrian duplicados activos, update con exclusion y conflictos 409.
- Anadida unicidad de nombre de creator sin distincion de mayusculas en create
  y update, junto a test unitario de conflicto.
- Ejecutado `backend && .\mvnw.cmd clean verify`: `BUILD SUCCESS`, 323 tests,
  0 fallos, 0 errores y 0 omitidos.
- Pendientes sin forzar: reglas de publishers/franchises/series/items/editions,
  fuzzy matching, aliases, metricas y constraints/migraciones futuras.
- Sin migraciones, endpoints, rutas, E2E, Playwright ni MVP4. Siguiente tarea:
  EPIC 42C - Calidad editorial avanzada y reporte de duplicados.

## 2026-07-10 - EPIC 42C - Calidad editorial avanzada y reporte de duplicados

- Anadido endpoint ADMIN de solo lectura `/api/catalog/admin/data-quality/report`
  con `scope` y limite acotado a 200, sin auto-fix, merge ni escritura.
- Anadidos DTOs, servicio backend y pantalla `/admin/editorial/data-quality`.
- Checks iniciales: nombres de creators duplicados y multiples enlaces VERIFIED
  por master product. Los checks restantes quedan para ampliacion posterior.
- Compilacion backend correcta con tests omitidos y build frontend correcto; sin
  migraciones, E2E, Playwright ni MVP4.

## 2026-07-10 - EPIC 42C-FIX - Completar reporte de calidad editorial

- Corregido el label i18n de scope para usar `scopeLabel`; mantenidas las claves
  `scope.*` para las opciones.
- Anadidos tests frontend de ruta protegida, llamada GET con scope/limit e i18n.
- El reporte mantiene checks exactos de creators y enlaces VERIFIED; publishers,
  franchises, series, items y editions quedan pendientes de queries agregadas
  dedicadas para no cargar tablas completas de forma fragil.
- Ejecutado `npm.cmd test -- --watch=false`: 55 archivos y 175 tests correctos.
- Sin migraciones, escritura, auto-fix, merge, borrado, E2E, Playwright ni MVP4.

## 2026-07-12 - EPIC 42C-FIX2 - Cierre tecnico del reporte de calidad editorial

- Sustituidos escaneos globales por queries agregadas con `GROUP BY`, `HAVING`,
  exclusion de borrados y limite por check.
- Implementados 13 checks: publisher; franchise name/slug; series; item
  title/sequence; edition ISBN/EAN/name; creator name/slug; master link
  VERIFIED/exacto.
- Anadidos tests backend de scopes, limite, informe vacio e invalido, y tests
  frontend de pantalla, ruta, servicio, shell e i18n.
- Actualizados API docs y exports de endpoint, ruta y mapa frontend-backend.
- Backend `clean verify` y frontend tests correctos; sin migraciones,
  escritura, auto-fix, merge, borrado, E2E, Playwright ni MVP4.

## 2026-07-13 - EPIC 44E-A1 - Esquema y contrato base de items manuales

- Anadida la migracion Liquibase 013 con `manual_title`,
  `manual_description` y `manual_type` en `collection_items`, sin backfill ni
  indices manuales.
- El constraint `chk_collection_items_reference` conserva la identidad
  referenciada legacy/editorial y anade la identidad manual exclusiva; se
  prohíben referencias mezcladas, title vacio o solo espacios, metadata manual
  sin title y source `MANUAL` sin title. Se mantiene
  `chk_collection_items_edition_requires_item`.
- El rollback falla explicitamente si existe source `MANUAL` o cualquiera de
  los campos manuales. Sin datos manuales, restaura los constraints previos y
  elimina las columnas nuevas.
- Anadido `MANUAL` a `CollectionEditorialReferenceSource` y a
  `CollectionItemReferenceKind`; `referenceKind` sigue calculado y no se
  persiste. Anadidos `CollectionItem.createManual` e `isManual`.
- Ampliados los DTOs de create/update y la respuesta publica con los campos
  manuales. `manualTitle`, `manualDescription` y `manualType` se muestran en
  lecturas publicas; `notes` y `acquiredAt` siguen siendo privados.
- Anadidas pruebas de entidad, respuesta y migracion real con
  PostgreSQL/Testcontainers, incluyendo los constraints validos e invalidos.
- Ejecutados tests dirigidos: 61 tests, 0 fallos, 0 errores y 0 omitidos.
  Ejecutado `cd backend && .\mvnw.cmd test`: `BUILD SUCCESS`, 346 tests,
  0 fallos, 0 errores y 0 omitidos. PostgreSQL/Testcontainers se ejecuto.
- La API todavia no permite altas manuales: queda pendiente EPIC 44E-A2 para
  creacion/edicion backend y EPIC 44E-A3 para enlace posterior al catalogo.
  Sin frontend, endpoints, exports, E2E ni Playwright.

## 2026-07-13 - EPIC 44E-A2 - Creacion y edicion backend de items manuales

- Activada la alta manual mediante el POST existente de collection items y la
  edicion mediante el PUT existente, sin rutas ni endpoints nuevos.
- La creacion normaliza title, description y type; title es obligatorio y los
  valores opcionales vacios se guardan como `null`. La ruta manual no consulta
  master products, catalog items, editions ni bridges.
- Se rechazan mezclas entre metadata manual y referencias legacy/editoriales.
  Los items manuales no pueden enlazarse por el PUT generico; ese enlace queda
  reservado para EPIC 44E-A3.
- Anadido `updateManualMetadata` en dominio. Conserva referencias y datos
  personales; el PUT puede modificar estos ultimos sin perder identidad manual.
- Mantenidas propiedad estricta y compatibilidad legacy, editorial y VERIFIED
  BRIDGE. Las entradas referenciadas rechazan metadata manual.
- Anadidas pruebas de dominio, servicio y MVC. Tests dirigidos: 61 correctos,
  sin fallos, errores u omitidos. Ejecutados `mvnw.cmd test` y
  `mvnw.cmd clean verify`: `BUILD SUCCESS`, 354 tests, 0 fallos, 0 errores y
  0 omitidos. Sin migraciones, frontend, exports, E2E ni Playwright. Siguiente
  tarea: EPIC 44E-A3.

## 2026-07-13 - EPIC 44E-A3 - Enlace posterior de item manual al catálogo

- Añadido el PUT propietario `catalog-reference` para transformar una identidad
  manual en una referencia editorial pública, con edición opcional.
- El enlace limpia la metadata manual, conserva los datos personales y usa
  `MANUAL_EDITORIAL` con `DIRECT_CATALOG`; no consulta legacy ni bridges.
- Reintentos exactos son idempotentes y no actualizan auditoría. Referencias
  incompatibles se traducen al conflicto existente.
- Añadidas pruebas de dominio, servicio y MVC. Sin migraciones, frontend, E2E
  ni Playwright. Siguiente tarea: EPIC 44E-B.

## 2026-07-13 - EPIC 44E-A3-FIX - Cierre de pruebas, documentación y exports

- Ampliada la cobertura MVC del enlace con 401 sin token, 400 sin catálogo,
  409 por identidad incompatible y 404 por item inexistente, preservando el
  formato de error existente.
- Tests dirigidos: `BUILD SUCCESS`, 67 tests, 0 fallos, 0 errores y 0 omitidos.
- Sin migraciones, frontend, E2E ni Playwright. Siguiente tarea: EPIC 44E-B.

## 2026-07-13 - EPIC 44E-B1 - Frontend de alta y visualización de items manuales

- Actualizados los modelos TypeScript, el alta manual con payload aislado y la
  visualización pública de identidad manual en detalle de colección.
- Añadidos modo MANUAL, normalización, i18n ES/EN y fixtures compatibles.
- Ejecutados tests Angular y build correctamente; se mantiene el warning de
  bundle inicial de 623.35 kB frente al budget de 500 kB. Sin backend, rutas,
  dependencias, E2E ni Playwright. Siguiente tarea: EPIC 44E-B2.

## 2026-07-13 - EPIC 44E-B1-FIX - Cierre del alta y visualización manual

- Añadidos mensajes de validación de longitud, prueba HTTP del payload manual
  y visualización de unitNumber/acquiredAt compartida por tarjetas manuales.
- Tests y build Angular correctos; persiste el warning de bundle inicial de
  623.78 kB frente al budget de 500 kB. Sin backend, rutas, dependencias, E2E
  ni Playwright. Siguiente tarea: EPIC 44E-B2.

## 2026-07-13 - EPIC 44E-B2A - Edición de items manuales y contrato de enlace

- La edición reconoce items manuales, permite actualizar su metadata y conserva
  datos personales sin enviar referencias legacy/editoriales.
- Añadido el contrato y servicio TypeScript de `catalog-reference`, sin selector
  ni llamada visual todavía. Tests y build Angular correctos; permanece el
  warning de bundle de 623.78 kB. Siguiente tarea: EPIC 44E-B2B.

## 2026-07-13 - EPIC 44E-B2A-FIX - Cierre de edición frontend de items manuales

- El PUT manual conserva la semántica backend: description y type vacíos se
  envían como `""` para solicitar su borrado. El payload manual no incluye
  `masterProductId`, `catalogItemId` ni `catalogItemEditionId`; los flujos
  legacy/editorial tampoco envían metadata manual residual.
- La ruta de edición resuelve la propiedad mediante `collection.userId` y el
  usuario autenticado antes de cargar items, renderizar el formulario o enviar
  cambios. Los roles administrativos no sustituyen la propiedad.
- La detección manual acepta tanto `referenceKind: MANUAL` como
  `editorialReferenceSource: MANUAL`; se añadieron mensajes DOM, validaciones y
  cobertura de payload, acceso directo y referencia manual fuente-única.
- Añadidas pruebas HTTP de `catalog-reference` con y sin edición, sin selector
  visual ni llamada desde la pantalla.
- Ejecutado `cd frontend && npm.cmd test -- --watch=false`: 58 archivos y 218
  tests correctos. Ejecutado `npm.cmd run build`: correcto, con el warning
  conocido del bundle inicial de 624.34 kB frente al budget de 500 kB. Sin
  backend, rutas nuevas, dependencias, exports, E2E ni Playwright. Siguiente
  tarea: EPIC 44E-B2B.

## 2026-07-13 - EPIC 44E-B2B - Selector y enlace frontend de item manual al catalogo

- Anadida una accion separada para enlazar solo items manuales del propietario.
  El panel usa busqueda editorial publica, deduplicacion por item, carga de
  detalle con edicion opcional y confirmacion explicita antes del PUT existente.
- El enlace no usa el PUT generico, bloquea cambios sin guardar, conserva datos
  personales y limpia la identidad manual tras una respuesta correcta.
- Los estados y request IDs del panel son independientes del formulario de
  edicion, con cancelacion mediante `takeUntilDestroyed`.
- Ejecutado `npm.cmd test -- --watch=false`: 58 archivos y 218 tests correctos.
  Ejecutado `npm.cmd run build`: correcto, con warning conocido de bundle
  inicial de 625.71 kB frente al budget de 500 kB. Siguiente tarea: EPIC 44F.

## 2026-07-13 - EPIC 44E-B2B-FIX - Cierre del enlace frontend manual

- Las busquedas nuevas invalidan la seleccion, detalle y edicion anteriores;
  tambien invalidan cualquier detalle pendiente. El panel no se puede cerrar ni
  reabrir durante el PUT de enlace.
- La confirmacion incluye la advertencia de sustitucion de identidad y la
  conservacion de datos personales. El selector de edicion muestra publisher,
  ISBN/EAN y ano solo cuando existen.
- Ejecutados frontend tests y build: 58 archivos, 218 tests correctos; warning
  conocido de bundle inicial de 625.71 kB frente al budget de 500 kB. Sin
  backend, rutas, dependencias, exports, E2E ni Playwright. Siguiente: EPIC 44F.

## 2026-07-13 - EPIC 44E-B2B-TEST-FIX - Cobertura integral del enlace manual

- Endurecidas las pruebas HTTP de `catalog-reference` para verificar la
  respuesta editorial, metadata manual nula y preservacion de datos personales.
- Anadidas pruebas del panel manual para busqueda normalizada y deduplicada,
  invalidacion de respuesta obsoleta al cerrar y payload exacto sin edicion;
  tambien verifica que no se usa el PUT generico.
- Ejecutados tests y build Angular correctamente. El build mantiene el warning
  conocido de bundle inicial de 625.71 kB frente al budget de 500 kB. Sin
  backend, rutas, dependencias, exports, E2E ni Playwright. Siguiente: EPIC 44F.

## 2026-07-15 - EPIC 44E-B2B-TEST-FIX2 - Cierre final de cobertura manual

- Anadidas pruebas de alta manual con normalizacion de titulo, opcionales nulos,
  referencias aisladas y rechazo de solo espacios; ampliada la cobertura de
  identidad manual y etiqueta de referencia en el detalle.
- La nueva prueba de alta detecto una navegacion no espiada en el router de
  test; se corrigio el doble de navegacion, sin cambios de produccion.
- Tests y build Angular correctos, con el warning conocido de 625.71 kB frente
  al budget de 500 kB. Sin backend, rutas, dependencias, exports, E2E ni
  Playwright. Siguiente tarea: EPIC 44F.

## 2026-07-15 - EPIC 44F-A - Diseño de OWNED, WANTED y faltantes calculados

- Cerrado el contrato de progreso owner-only por coleccion y serie, estados de
  posesion, precedencia, legacy MISSING y transicion WANTED a OWNED mediante
  el PUT existente sobre la misma entrada.
- Sin backend, frontend, migraciones, endpoints, rutas, dependencias, exports,
  E2E ni Playwright. Siguiente tarea: EPIC 44F-B.

## 2026-07-15 - EPIC 44F-B - Backend de OWNED, WANTED y faltantes calculados

- Anadido endpoint owner-only de progreso por coleccion y serie, DTOs
  calculados y clasificacion OWNED/WANTED/MISSING sin persistir faltantes.
- Los estados de posesion ganan sobre WANTED; MISSING legacy genera warning sin
  participar en listas. Las altas y asignaciones explicitas de MISSING devuelven
  400, mientras que una fila legacy puede transicionar mediante el PUT existente.
- Ejecutado `mvnw.cmd test`: BUILD SUCCESS, 360 tests, 0 fallos, 0 errores y 3
  omitidos. Sin frontend, recomendaciones, migraciones, dependencias, E2E ni
  Playwright. Siguiente tarea: EPIC 44F-C.

## 2026-07-15 - EPIC 44F-B-FIX - Cierre de backend de progreso

- Auditada la seguridad owner-only, precedencia OWNED/WANTED/MISSING y el
  bloqueo de nuevas escrituras `MISSING`; reformateado el servicio sin cambiar
  su contrato.
- Anadida cobertura unitaria del progreso calculado, incluida la precedencia,
  warning de `MISSING` legacy, listas de IDs y ausencia de persistencia.
- Actualizados los exports de endpoints con el recurso de progreso calculado.
- Ejecutado `mvnw.cmd test`: BUILD SUCCESS, 363 tests, 0 fallos, 0 errores y 3
  omitidos por las integraciones condicionadas a Docker/Testcontainers.
- Sin frontend, recomendaciones, migraciones, dependencias, E2E ni Playwright.
  Siguiente tarea: EPIC 44F-C.

## 2026-07-15 - EPIC 44F-B-FIX2 - Cobertura completa del progreso backend

- `CollectionProgressServiceTest` pasa de 3 a 26 tests: propiedad y existencia,
  estados de posesion, precedencia, `MISSING` legacy, agregacion, IDs, ediciones,
  orden determinista, porcentaje y ausencia de persistencia.
- Anadidos 12 tests a `CollectionServiceTest` para bloquear altas y updates
  `MISSING`, permitir la edicion de filas legacy y cubrir las transiciones a
  `WANTED`/`OWNED` sin crear otra fila ni perder identidad o datos personales.
- Anadidos 12 tests MVC para respuesta y privacidad del progreso, delegacion,
  seguridad 401/403 por usuario y roles ajenos, errores 404 y mapeo 400 de
  escrituras `MISSING`.
- Creado 1 test PostgreSQL/Testcontainers de la query de progreso para referencias
  directas y `VERIFIED_BRIDGE`, relaciones fetch, filtros, orden y ausencia de
  duplicados. Fue omitido porque Docker no estaba disponible en el entorno.
- Tests dirigidos de servicio y MVC: 112 tests, 0 fallos y 0 errores. Regresion
  completa con `mvnw.cmd test`: BUILD SUCCESS, 411 tests, 0 fallos, 0 errores y
  4 omitidos por integraciones condicionadas a Docker/Testcontainers.
- No se encontraron defectos funcionales ni se modifico produccion. Sin frontend,
  recomendaciones, migraciones, dependencias, exports, E2E ni Playwright.
  Siguiente tarea: EPIC 44F-C.

## 2026-07-15 - EPIC 44F-C - Frontend de progreso y faltantes calculados

- Anadidos modelos de progreso por serie, consumo HTTP owner-only, ruta lazy
  protegida y pantalla con resumen, barra, grupos OWNED/WANTED/MISSING y aviso
  para MISSING legacy.
- La transicion simple WANTED a OWNED usa el PUT existente con payload minimo y
  recarga el progreso canonico; multiples entradas enlazan a su edicion.
- El detalle de coleccion ofrece enlaces deduplicados por serie solo al propietario.
  Alta y edicion separan estados legacy de estados escribibles: MISSING no se
  ofrece en altas y permanece legible, deshabilitado, en filas legacy.
- Anadido i18n ES/EN, tests de servicio y componente, rutas y exports frontend.
- Ejecutado `npm.cmd ci`: 474 paquetes instalados y 475 auditados; se mantienen
  7 vulnerabilidades conocidas (3 bajas y 4 altas), sin ejecutar `npm audit fix`.
  Ejecutados `npm.cmd test -- --watch=false`: 59 archivos y 229 tests correctos.
  Ejecutado `npm.cmd run build`: correcto, warning conocido de bundle inicial de
  629.22 kB frente al budget de 500 kB. Sin backend, migraciones, dependencias,
  cambios en `/wanted`, E2E ni Playwright. Siguiente tarea: EPIC 44F-D.

## 2026-07-15 - EPIC 44F-C-FIX - Cierre del frontend de progreso

- Separados el error del PUT y el de la recarga canonica: el fallo de escritura
  se muestra en pantalla y permite reintentar, mientras que un PUT correcto con
  recarga fallida conserva el progreso anterior y muestra un aviso especifico.
- Anadido estado pendiente de recarga, bloqueo de transiciones repetidas y retry
  que ejecuta solo el GET de progreso hasta recuperar el estado canonico.
- Ampliadas pruebas de progreso para carga, error visible, doble envio, recarga
  posterior, retry, bloqueo y multiples entradas WANTED; cubiertos tambien los
  enlaces de serie, alta sin MISSING y edicion de MISSING legacy.
- Ejecutado `npm.cmd ci`: 474 paquetes instalados y 475 auditados; permanecen 7
  vulnerabilidades conocidas (3 bajas y 4 altas), sin `npm audit fix`.
  Ejecutado `npm.cmd test -- --watch=false`: 59 archivos y 239 tests correctos.
  Ejecutado `npm.cmd run build`: correcto, con warning conocido de bundle inicial
  de 629.27 kB frente al budget de 500 kB.
- Sin backend, recomendaciones, migraciones, dependencias, exports, cambios en
  `/wanted`, E2E ni Playwright. Siguiente tarea: EPIC 44F-D.

## 2026-07-15 - EPIC 44F-D - Cierre de OWNED, WANTED y faltantes calculados

- Corregido el demo legacy: Dragon Quest y Galaxy Dragon son WANTED; Retro Quest
  es OWNED y el script valida que no cree MISSING persistido.
- El demo editorial crea tres items y el nuevo script MVP4 prepara y valida el
  escenario 1 OWNED, 1 WANTED, 1 MISSING calculado y 33%, sin guardar secretos.
- Parser de los tres scripts correcto. `-WhatIf` correcto y sin llamadas HTTP ni
  ficheros locales; la ejecucion real no se realizo porque no habia backend/API,
  PostgreSQL y cuenta ADMIN local disponibles para este cierre.
- Exports auditados: endpoint, ruta y mapa de progreso/transicion ya correctos.
  Creada la revision formal `docs/28_MVP4_44F_CLOSURE_REVIEW.md`.
- Ejecutado `mvnw.cmd clean verify`: BUILD SUCCESS, 411 tests, 0 fallos, 0
  errores y 4 omitidos por integraciones condicionadas a Docker/Testcontainers.
  Ejecutado `npm.cmd ci`: 474 paquetes instalados y 475 auditados; permanecen 7
  vulnerabilidades conocidas (3 bajas y 4 altas). Tests frontend: 59 archivos y
  239 tests correctos; build correcto con warning de bundle inicial de 629.27 kB
  frente al budget de 500 kB. Sin cambios funcionales de backend/frontend,
  migraciones, dependencias, E2E ni Playwright. Siguiente tarea: EPIC 44G-A.

## 2026-07-31 - EPIC 44G-A - Auditoria y diseno del detalle final, filtros y ordenacion

- Auditado el detalle actual, los contratos de coleccion/items, la privacidad,
  el progreso por serie de 44F y su cobertura backend/frontend.
- Creado `docs/29_MVP4_DETAIL_FILTERS_SORTING_DESIGN.md` con el contrato aditivo
  de filtros combinables, orden total determinista y query params del detalle.
- Separados los recuentos de filas persistidas del MISSING calculado y definido
  un resumen agregado owner-only por series que reutiliza 44F y evita N+1.
- Cerradas compatibilidad legacy/manual, lectura publica, sanitizacion, estados
  de error, accesibilidad, estrategia de tests y fuera de alcance.
- Sin cambios funcionales, API, frontend, migraciones, dependencias, exports,
  E2E ni Playwright. Siguiente tarea: EPIC 44G-B.

## 2026-07-31 - EPIC 44G-B - Backend de listado final y resumen de progreso

- Ampliado `GET /api/collections/{collectionId}/items` de forma aditiva con
  `q`, estados y tipos de referencia repetibles, `seriesId` y cinco ordenes
  cerrados; los filtros se combinan sin cambiar `CollectionItemResponse`.
- El orden predeterminado usa serie, orden editorial, titulo e id; todos los
  criterios tienen desempate estable. Entradas manuales y legacy permanecen
  compatibles y los parametros vacios se ignoran tras normalizar.
- Anadido `GET /api/collections/{collectionId}/series-progress`, estrictamente
  owner-only, con una fila por serie ACTIVE participante y el mismo calculo
  OWNED/WANTED/MISSING y porcentaje de 44F, sin ids ni datos personales.
- Las consultas de detalle y progreso precargan las relaciones necesarias; el
  resumen obtiene entradas y catalog items en lote para evitar N+1.
- Tests dirigidos de servicio y MVC: 125 tests correctos. Test PostgreSQL/
  Testcontainers de consultas: 1 test correcto. Ejecutado `mvnw.cmd clean
  verify`: BUILD SUCCESS, 424 tests, 0 fallos, 0 errores y 0 omitidos.
- Sin frontend, migraciones, cambios de dependencias, `pom.xml`, `/wanted`, E2E
  ni Playwright. Siguiente tarea: EPIC 44G-C.

## 2026-07-31 - EPIC 44G-C - Detalle final frontend

- Ampliado el detalle Angular existente con resumen inmutable del snapshot
  completo: total persistido, OWNED, WANTED, otros estados y aviso separado para
  filas MISSING legacy; los filtros no alteran estos recuentos.
- Anadidos busqueda, estados y tipos de referencia repetibles, serie y cinco
  ordenes. Aplicar y limpiar sincronizan query params sin publicar valores por
  defecto; la carga inicial hidrata criterios validos desde la URL.
- Integrado el resumen agregado de progreso solo para el propietario, con
  porcentaje, enlace al detalle de serie y loading/error/retry independiente.
  Un lector publico no solicita el endpoint ni recibe acciones de propietario.
- Separados errores de metadatos, listado, progreso y acciones. Un listado
  filtrado vacio se distingue de una coleccion vacia; borrar recarga snapshot,
  lista y progreso en lugar de ajustar contadores localmente.
- Ejecutado `npm.cmd ci`: 474 paquetes instalados. Ejecutado `npm.cmd test --
  --watch=false`: 59 archivos y 241 tests correctos. Ejecutado `npm.cmd run
  build`: correcto, con warning conocido de bundle inicial de 631.54 kB frente
  al budget de 500 kB.
- Auditoria backend previa: `mvnw.cmd clean verify`, BUILD SUCCESS, 424 tests,
  0 fallos, 0 errores y 0 omitidos. Sin cambios backend, migraciones,
  dependencias, rutas Angular, `/wanted`, E2E ni Playwright. Siguiente tarea:
  EPIC 44G-D.

## 2026-07-31 - EPIC 44G-D - Regresion y cierre

- Auditados los recorridos y tests existentes de referencias directas, bridge,
  legacy y manuales; lectura publica sanitizada; propiedad estricta del progreso;
  transicion WANTED a OWNED; enlace manual y borrado. No fue necesario duplicar
  cobertura ni modificar codigo funcional.
- Sincronizados `docs/16_MVP_API_ENDPOINTS.md` y `docs/export/` con los filtros
  aditivos del listado, el resumen agregado owner-only y su consumo desde el
  detalle Angular.
- Ejecutado `mvnw.cmd clean verify`: BUILD SUCCESS, 424 tests, 0 fallos, 0
  errores y 0 omitidos.
- Ejecutado `npm.cmd ci`: 474 paquetes instalados. Ejecutado `npm.cmd test --
  --watch=false`: 59 archivos y 241 tests correctos. Ejecutado `npm.cmd run
  build`: correcto, con warning conocido de bundle inicial de 631.54 kB frente
  al budget de 500 kB.
- Sin cambios de codigo, migraciones, dependencias, `pom.xml`, manifests npm,
  rutas Angular, `/wanted`, E2E ni Playwright. EPIC 44G cerrada; siguiente tarea:
  EPIC 44H.
