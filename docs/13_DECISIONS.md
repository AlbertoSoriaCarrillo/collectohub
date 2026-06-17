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

## 2026-06-16 - Autenticacion JWT

- Decision: usar `org.springframework.security:spring-security-oauth2-jose` para firmar y validar JWT HS256.
- Motivo: evita implementar JWT manualmente y mantiene la seguridad dentro del stack de Spring Security.
- Decision: exigir `JWT_SECRET` de al menos 32 bytes y actualizar el valor local de ejemplo.
- Motivo: HS256 requiere una clave suficiente; el valor por defecto es solo para desarrollo local y debe cambiarse fuera de local.
- Decision: normalizar emails a minusculas durante registro y login.
- Motivo: evita duplicados logicos por diferencias de mayusculas aunque PostgreSQL trate `email` como texto sensible a mayusculas.

## 2026-06-16 - Refresh tokens

- Decision: crear tabla `refresh_tokens` y almacenar solo hash SHA-256 del refresh token opaco.
- Motivo: prepara refresh/logout futuros sin guardar tokens reutilizables en claro en base de datos.

## 2026-06-16 - Tiendas MVP

- Decision: hacer nullable `shops.country` mediante Liquibase.
- Motivo: la API de esta fase define el pais de tienda como opcional inicialmente y no hay un pais por defecto universal seguro.
- Decision: usar `EUR` y 48 horas como valores por defecto configurables para tiendas.
- Motivo: coincide con el MVP de reservas sin pago y permite cambiar defaults por entorno mediante variables.
- Decision: asignar automaticamente el rol global `SHOP_OWNER` cuando un usuario crea una tienda y todavia no tiene ese rol.
- Motivo: los roles globales son acumulables; `SHOP_OWNER` identifica capacidad global de gestionar tiendas, mientras que `shop_members.OWNER` define permisos dentro de una tienda concreta.

## 2026-06-17 - Catalogo maestro MVP

- Decision: mantener los endpoints de esta fase como `/api/product-categories` y `/api/master-products`.
- Motivo: son los endpoints solicitados para la fase de catalogo maestro y quedan aislados dentro del modulo backend `catalog`.
- Decision: almacenar `limitedEditionTotalUnits` dentro de `master_products.attributes` con la clave `limitedEditionTotalUnits`.
- Motivo: la tabla MVP `master_products` no define una columna especifica para ese dato y ya existe `attributes JSONB` para atributos flexibles; las unidades concretas de tienda o coleccion siguen perteneciendo a tablas futuras como `shop_products` y `collection_items`.
- Decision: limitar la creacion y actualizacion de productos maestros a usuarios con rol global `ADMIN` o `SHOP_OWNER`.
- Motivo: el catalogo maestro es reutilizable por tiendas, colecciones e inventario; en el MVP solo perfiles con capacidad de administracion de plataforma o tienda pueden modificarlo.
- Decision: aplicar deteccion de duplicados solo sobre productos activos y no eliminados.
- Motivo: evita bloquear futuras recreaciones de datos retirados por borrado logico y mantiene la deteccion alineada con el listado publico del catalogo.

## 2026-06-17 - Seguridad por roles e inventario MVP

- Decision: habilitar `@EnableMethodSecurity` y aplicar `@PreAuthorize` en endpoints con permisos globales de catalogo maestro.
- Motivo: centraliza la autorizacion global en Spring Security sin mezclarla con reglas de pertenencia a tienda.
- Decision: usar authorities sin prefijo `ROLE_` para que las expresiones sean `hasAnyAuthority('ADMIN', 'SHOP_OWNER')`.
- Motivo: alinea las expresiones de seguridad con los codigos de roles globales almacenados en base de datos y expuestos en JWT.
- Decision: mantener los permisos de inventario por tienda en `InventoryService`, usando `shop_members` y roles internos `OWNER` o `MANAGER`.
- Motivo: la autorizacion de inventario depende del `shopId` de la ruta y de la pertenencia activa a una tienda concreta, no solo del rol global del usuario.
- Decision: usar en inventario MVP los estados comerciales `AVAILABLE`, `RESERVED`, `SOLD` y `HIDDEN`, y las condiciones fisicas `NEW`, `LIKE_NEW`, `GOOD`, `ACCEPTABLE` y `DAMAGED`.
- Motivo: son los estados solicitados para esta fase; otros estados descritos en la especificacion de base de datos quedan como ampliacion futura para no aumentar el alcance del MVP.
- Decision: considerar producto de tienda publicable solo cuando no esta eliminado, `visible=true` y `commercial_status=AVAILABLE`.
- Motivo: evita exponer productos ocultos, reservados o vendidos en endpoints publicos.

## 2026-06-17 - Colecciones MVP

- Decision: usar los estados de item de coleccion `OWNED`, `WANTED`, `MISSING`, `DUPLICATED`, `SELLABLE` y `TRADABLE`.
- Motivo: son los estados solicitados para esta fase de colecciones personales; otros estados descritos en la especificacion de base de datos quedan para ampliaciones futuras.
- Decision: reutilizar `PhysicalCondition` del modulo de inventario en `collection_items`.
- Motivo: evita duplicar vocabulario de condicion fisica y mantiene consistencia entre inventario de tienda y colecciones personales.
- Decision: devolver `404 Not Found` al intentar leer una coleccion privada ajena o sus items.
- Motivo: evita revelar la existencia de colecciones privadas; las operaciones de modificacion ajenas devuelven `403 Forbidden`.
- Decision: aplicar borrado logico en colecciones e items mediante `deleted_at` y `deleted_by`.
- Motivo: respeta el modelo de auditoria ya creado en las tablas MVP y evita borrado fisico de datos de usuario.

## 2026-06-17 - Recomendaciones simples MVP

- Decision: implementar los endpoints de esta fase como `GET /api/recommendations/my` y `GET /api/recommendations/my/summary`.
- Motivo: son los endpoints solicitados para recomendaciones simples y quedan aislados dentro del modulo backend `recommendations`.
- Decision: ordenar recomendaciones primero por items `MISSING`, despues `WANTED`, despues por nombre de producto y despues por precio ascendente.
- Motivo: un producto marcado como faltante representa una necesidad mas directa dentro de una coleccion que un producto deseado.
- Decision: si `maxPrice` se informa sin `currency`, filtrar comparando el importe numerico contra la moneda propia de cada producto, sin conversion de divisa.
- Motivo: el MVP no incluye conversion de divisas ni fuentes de tipo de cambio; la regla sigue siendo explicable y predecible.
- Decision: deduplicar por `shopProductId` y devolver una sola coincidencia principal por producto recomendado.
- Motivo: evita respuestas repetidas cuando el usuario tiene el mismo producto maestro en varias colecciones. Si hay varias coincidencias, se prioriza `MISSING` sobre `WANTED`.

## 2026-06-16 - Edad recomendada

- Decisión: plataforma recomendada para mayores de 18 años.
- Motivo: en fases futuras habrá contenido publicado por usuarios y tiendas difícil de controlar completamente.
