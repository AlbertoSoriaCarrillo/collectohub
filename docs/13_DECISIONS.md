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

## 2026-06-17 - Reservas MVP

- Decision: crear reservas sin reducir automaticamente `shop_products.stock_quantity`.
- Motivo: en el MVP la reserva es una solicitud sin pago ni bloqueo transaccional de stock; reducir stock queda para una fase posterior con reglas de concurrencia y expiracion mas completas.
- Decision: calcular `expires_at` al crear la reserva usando `shops.default_reservation_expiration_hours`.
- Motivo: respeta la configuracion de reserva por tienda ya modelada desde la fase de tiendas.
- Decision: no implementar job automatico para pasar reservas a `EXPIRED`.
- Motivo: la expiracion automatica esta documentada como proceso futuro y no forma parte del alcance solicitado para esta fase.
- Decision: permitir que la tienda gestione estados mediante `PUT /api/shops/{shopId}/reservations/{reservationId}/status` y el usuario cancele mediante `PUT /api/reservations/{reservationId}/cancel`.
- Motivo: son los endpoints solicitados para esta fase y separan permisos de tienda de acciones del usuario propietario.

## 2026-06-17 - Endurecimiento backend MVP

- Decision: declarar en OpenAPI el esquema de seguridad `bearer-jwt` sin aplicar un requisito global a todas las operaciones.
- Motivo: Swagger UI puede autorizar llamadas protegidas sin marcar incorrectamente como protegidos los endpoints publicos del MVP.
- Decision: mantener el JWT como token stateless que no cambia al asignar `SHOP_OWNER` durante la creacion de la primera tienda.
- Motivo: evita introducir rotacion automatica de token en esta fase; el cliente debe obtener un token nuevo mediante login o futuro refresh para recibir los roles actualizados.

## 2026-06-18 - Frontend Angular base

- Decision: usar Angular CLI/Angular 21.2.x para el frontend inicial, con Angular Material 21.2.x.
- Motivo: Angular 22.0.2 ya esta publicado, pero requiere Node.js `24.15.0` o superior; el entorno local actual usa Node.js `24.14.0` y Angular 21.2.x es compatible con Node 24 y el CI configurado.
- Decision: usar `localStorage` para persistir `accessToken`, `refreshToken` y datos publicos del usuario durante el MVP frontend.
- Motivo: permite mantener la sesion tras recargar la pagina y simplifica la base inicial; no se registran tokens en logs ni se muestran en pantalla. Antes de produccion se revisara la estrategia junto con refresh/logout completo.

## 2026-06-18 - Frontend tiendas y catalogo

- Decision: mantener la autorizacion real de creacion de productos maestros en backend y anadir bloqueo UX en frontend para usuarios sin `SHOP_OWNER` o `ADMIN`.
- Motivo: el frontend mejora la experiencia, pero la seguridad efectiva sigue residiendo en Spring Security.
- Decision: capturar `master_products.attributes` en el formulario frontend como objeto JSON textual opcional, enviando `{}` cuando queda vacio.
- Motivo: respeta el modelo flexible JSONB del backend sin inventar campos especificos fuera del alcance MVP.
- Decision: normalizar `frontend/tsconfig.app.json` a la forma estandar Angular CLI con `files: ["src/main.ts"]` e `include: ["src/**/*.d.ts"]`.
- Motivo: `tsc -p tsconfig.app.json --noEmit` ya pasaba, pero esta configuracion evita que el proyecto app trate todos los `.ts` como entradas raiz y reduce avisos del editor.

## 2026-06-18 - Frontend inventario de tienda

- Decision: mostrar productos publicos de tienda dentro de `/shops/:id` y reservar `/shop-products/:shopProductId` para el detalle publico de cada producto.
- Motivo: evita anadir una ruta publica extra de inventario de tienda no solicitada y mantiene el detalle de tienda como punto natural de descubrimiento.
- Decision: llevar la tarjeta `Inventario` del dashboard a `/shops`.
- Motivo: en MVP no existe seleccion de tienda por defecto; el usuario debe elegir que tienda gestionar antes de entrar al inventario.
- Decision: cargar la pantalla de edicion desde `GET /api/shops/{shopId}/products/my` y filtrar por `shopProductId`.
- Motivo: el contrato MVP no expone un endpoint interno de detalle por `shopId`; la lista interna garantiza permisos de miembro y permite validar pertenencia a la tienda de la ruta.

## 2026-06-18 - Frontend colecciones de usuario

- Decision: mostrar acciones de propietario en el detalle de coleccion solo cuando el usuario autenticado coincide con `CollectionResponse.userId`.
- Motivo: el contrato MVP no expone un flag `owner`; comparar con `/api/users/me` evita ofrecer acciones de modificacion a lectores de colecciones publicas.
- Decision: mantener la adicion a coleccion desde catalogo como navegacion a `/collections` sin modal avanzado.
- Motivo: cumple el alcance de EPIC 14 sin introducir un flujo cruzado complejo entre catalogo y colecciones.

## 2026-06-18 - Frontend recomendaciones

- Decision: mantener `shopId` como filtro numerico manual en `/recommendations`.
- Motivo: el contrato MVP ya acepta `shopId`, pero no existe todavia un selector dedicado de tiendas para recomendaciones; evita introducir endpoints o flujos nuevos.
- Decision: no aplicar filtro de moneda por defecto en la pantalla de recomendaciones.
- Motivo: filtrar inicialmente por `EUR` ocultaria productos recomendables en otras monedas; la moneda se envia solo cuando el usuario la informa.

## 2026-06-18 - Frontend reservas MVP

- Decision: mantener `shopId`, `userId` y `shopProductId` como filtros numericos manuales en las pantallas de reservas.
- Motivo: los endpoints MVP ya aceptan esos filtros, pero no existen todavia selectores dedicados para usuarios, tiendas o productos de tienda; evita introducir endpoints nuevos.
- Decision: usar `/reservations/{reservationId}` como detalle comun de reserva y reservar `/shops/{shopId}/reservations` para acciones de gestion de tienda.
- Motivo: el backend ya aplica permisos de lectura por propietario o gestor de tienda; separar la mutacion de tienda evita mostrar acciones administrativas fuera del panel de tienda.

## 2026-06-18 - Empaquetado Docker local MVP

- Decision: preparar Docker Compose local con PostgreSQL, backend y frontend, sin anadir despliegue cloud ni Kubernetes.
- Motivo: permite levantar el MVP completo en local manteniendo el alcance de empaquetado solicitado.
- Decision: usar Dockerfile multi-stage para backend con Maven Wrapper y Eclipse Temurin Java 25.
- Motivo: respeta el stack Java 25/Spring Boot 4.1.x y no exige Maven global dentro de la imagen.
- Decision: servir el frontend Angular con nginx, mapeando el puerto del host `4200` al puerto interno `80`.
- Motivo: permite probar el build estatico del MVP sin introducir un servidor Node en runtime.
- Decision: mantener `apiBaseUrl` del frontend en `http://localhost:8080` para Docker local y permitir CORS configurable en backend.
- Motivo: desde el navegador del host `localhost:8080` apunta al backend publicado por Compose; evita introducir runtime config Angular en esta fase.

## 2026-06-18 - Playwright E2E MVP

- Decision: ubicar los tests E2E en `frontend/e2e/`.
- Motivo: la UI Angular es quien ejecuta los flujos de navegador, Playwright queda cerca del `package.json` frontend y se evita crear otro paquete Node en la raiz del monorepo.
- Decision: no ejecutar Playwright en GitHub Actions durante esta fase.
- Motivo: la suite requiere backend, frontend y base de datos levantados; se deja para una fase posterior para no hacer el pipeline mas lento o fragil.
- Decision: usar Chromium como unico navegador E2E inicial.
- Motivo: reduce coste de instalacion y mantiene una primera capa smoke mantenible.
- Decision: dejar `App` como host de `router-outlet` y mantener `MainLayoutComponent` solo como layout de rutas.
- Motivo: evita renderizar dos barras/layouts al mismo tiempo y estabiliza la UI real que validan los E2E.

## 2026-06-18 - Portfolio y presentacion GitHub

- Decision: convertir `README.md` en la entrada principal de portfolio del proyecto.
- Motivo: una persona externa debe entender rapidamente proposito, estado, stack, ejecucion, tests, limitaciones y siguientes pasos sin leer toda la carpeta `docs/`.
- Decision: preparar `docs/assets/screenshots/` para capturas reales futuras sin versionar imagenes ficticias.
- Motivo: evita presentar evidencias visuales inventadas y deja una ubicacion estable para material de demo.
- Decision: mantener Playwright fuera de CI durante la limpieza tecnica.
- Motivo: esta fase es documental y de presentacion; anadir E2E al pipeline requiere disenar un job con servicios levantados y merece una fase propia.

## 2026-06-18 - Rediseño UI/UX social MVP

- Decision: mantener Angular Material + SCSS y no introducir Tailwind ni otra libreria de UI.
- Motivo: respeta el stack ya documentado, evita churn de dependencias y permite mejorar la experiencia con el sistema existente.
- Decision: aplicar un layout social propio con sidebar desktop, bottom navigation movil, contenido central y panel contextual derecho.
- Motivo: acerca el producto a una experiencia social/marketplace reconocible sin copiar marcas ni ampliar funcionalidades fuera del MVP.
- Decision: mantener todos los `data-testid` existentes en componentes y flujos E2E.
- Motivo: el rediseño debe ser visual y de experiencia, no una ruptura de automatizacion ni de contratos de prueba.
- Decision: usar iconografia de Angular Material Icons solo en el layout principal.
- Motivo: aporta lectura visual a la navegacion sin obligar a redisenar cada componente ni cambiar la logica funcional.

## 2026-06-18 - Internacionalizacion frontend ES/EN

- Decision: implementar una capa i18n ligera propia para Angular en `frontend/src/app/core/i18n/`, sin anadir librerias externas.
- Motivo: el MVP solo necesita ES/EN, textos estaticos, cambio en caliente, persistencia local, fallback e interpolacion simple; una dependencia completa anadiria complejidad innecesaria.
- Decision: persistir el idioma activo en `localStorage` con la clave `collectohub.language`.
- Motivo: mantiene la preferencia entre recargas sin modificar backend ni contratos de usuario.
- Decision: si no hay idioma guardado, usar `en` solo cuando el navegador este en ingles; si el valor guardado es invalido, volver a `es`.
- Motivo: respeta el idioma por defecto del producto y evita que valores corruptos en almacenamiento local generen estados no soportados.
- Decision: traducir solo representaciones visibles de enums y textos de UI, no datos dinamicos de backend/usuario.
- Motivo: nombres, emails, descripciones, mensajes, IDs, precios y codigos tecnicos son datos de dominio y no deben alterarse en la capa visual.

## 2026-06-19 - Datos de demo locales

- Decision: crear `scripts/demo/create-demo-data.ps1` como generador local de datos demo usando solo endpoints API existentes.
- Motivo: permite preparar capturas y demos repetibles sin introducir SQL directo, endpoints de seed/reset ni cambios de reglas de negocio.
- Decision: guardar el resumen de la ultima ejecucion en `scripts/demo/.last-demo-data.json` e ignorarlo en Git.
- Motivo: los IDs y usuarios generados ayudan a navegar la demo local, pero son datos desechables del entorno de cada persona.
- Decision: usar sufijos unicos en emails, productos e IDs externos de demo.
- Motivo: evita conflictos con las restricciones de unicidad y la deteccion de duplicados del catalogo maestro al repetir demos.

## 2026-06-23 - Reenfoque producto libros/comics/manga

- Decision: reenfocar el recorrido principal visible de CollectoHub a red social/catalogo para gestionar colecciones de libros, comics y manga.
- Motivo: el MVP se entiende mejor como experiencia de coleccionista centrada en catalogo, colecciones, buscados y perfil.
- Decision: conservar backend, modelos, migraciones y rutas existentes de tiendas, inventario y reservas como base tecnica/futura.
- Motivo: no se debe borrar trabajo funcional ya validado ni cambiar contratos backend fuera del alcance.
- Decision: ocultar tiendas, inventario y reservas de la navegacion principal, home, dashboard y CTAs primarios del frontend.
- Motivo: evita que el usuario perciba el producto como marketplace/reservas en esta fase.
- Decision: redirigir `/dashboard` a `/home` y `/recommendations` a `/wanted`.
- Motivo: mantiene compatibilidad de rutas sin promocionar el flujo antiguo.
- Decision: mover login, selector de idioma y sesion de usuario al header global; la sidebar queda solo para navegacion principal.
- Motivo: evita duplicidades visuales, deja el acceso de usuario en una posicion esperada y mejora la lectura publica del producto.
- Decision: no mostrar `Register` en header, sidebar ni CTA global; `/register` queda accesible desde login o por URL manual.
- Motivo: el registro debe ser un paso secundario desde la intencion de iniciar sesion, no una accion persistente en toda la interfaz.
- Decision: usar un avatar/menu Material para Perfil y Cerrar sesion sin implementar subida real de avatar.
- Motivo: prepara el patron de usuario autenticado sin ampliar backend ni perfil.

## 2026-06-29 - Vision completa y desarrollo por fases

- Decision: mantener una vision de producto amplia con coleccionistas,
  catalogo, tiendas y creadores, pero desarrollar por fases empezando por el
  nucleo coleccionista/catalogo.
- Motivo: la vision completa es ambiciosa y requiere separar validacion de
  usuario, catalogo, social, tiendas, matching y comercio para evitar un MVP
  inabarcable.
- Decision: considerar libros, comics y manga como el alcance editorial inicial
  de MVP 1, no como el limite definitivo del catalogo.
- Motivo: permite validar la experiencia con un dominio comprensible y ampliar
  despues a audiovisual, cartas, merchandising, figuras, videojuegos, consolas
  y otras ramas sin desordenar el foco actual.
- Decision: clasificar tiendas, inventario, recomendaciones y reservas actuales
  como base tecnica parcial para fases posteriores, no como marketplace o
  comercio listo para usuario final.
- Motivo: conserva el trabajo validado sin prometer matching, pedidos, pagos,
  almacen ni operacion comercial que todavia no existen.

## 2026-06-29 - Cierre de MVP 1

- Decision: cerrar MVP 1 como base tecnica/producto con Home, Catalogo,
  Colecciones, Buscados, Perfil e i18n como recorrido principal.
- Motivo: el alcance tiene navegacion coherente, tests completos, Docker healthy
  y E2E del flujo real; las capacidades futuras estan separadas en roadmap y
  backlog.
- Decision: limitar los estados seleccionables en la UI principal a `OWNED`,
  `WANTED`, `MISSING` y `DUPLICATED`, manteniendo `SELLABLE` y `TRADABLE` en el
  contrato/modelo para compatibilidad.
- Motivo: evita lenguaje de venta/intercambio dentro del nucleo coleccionista
  sin romper backend ni datos existentes.
- Decision: mantener el calculo backend de recomendaciones contra stock, pero
  retirar de `/wanted` filtros comerciales y enlaces a producto de tienda.
- Motivo: Buscados debe representar faltantes, deseados y coincidencias de MVP 1,
  no funcionar como entrada visible a reservas o marketplace.

## 2026-06-29 - Exportacion tecnica versionada

- Decision: mantener en `docs/export/` una referencia manual y versionada del
  esquema Liquibase, endpoints Spring, rutas Angular y relaciones
  frontend-backend en Markdown, Mermaid y CSV.
- Motivo: los formatos son descargables y revisables desde GitHub sin introducir
  generadores complejos ni cambiar codigo funcional; Swagger sigue siendo la
  referencia interactiva de API.
- Decision: clasificar cada elemento como `MVP1_VISIBLE`, `LEGACY_FUTURE`,
  `TECHNICAL` o `REDIRECT` segun su papel en el recorrido cerrado de MVP 1, no
  solo segun si existe implementacion tecnica.
- Motivo: evita presentar tiendas, inventario y reservas conservados como si
  formaran parte de la experiencia principal actual.

## 2026-06-29 - Estrategia de catalogo editorial MVP 2

- Decision: construir el catalogo editorial mediante publishers, franchises,
  series, items y editions nuevos, manteniendo `master_products` como fachada de
  compatibilidad durante la transicion.
- Motivo: `master_products` mezcla obra y edicion, pero sus IDs sostienen
  colecciones, inventario, recomendaciones, reservas y frontend; una sustitucion
  directa tendria un riesgo transversal innecesario.
- Decision: usar una tabla puente `master_product_catalog_links` con item
  obligatorio, edicion opcional y estado de verificacion, en lugar de anadir
  multiples FKs opcionales a `master_products`.
- Motivo: permite backfill idempotente, reconciliacion auditada y adopcion por
  dominios sin afirmar automaticamente que textos legacy son identidades
  editoriales correctas.
- Decision: permitir que una coleccion futura apunte a item y opcionalmente a
  edicion; el inventario futuro debe apuntar a una edicion concreta.
- Motivo: una persona puede buscar una obra sin conocer la edicion, mientras una
  tienda vende una publicacion fisica identificable.
- Decision: restringir inicialmente las escrituras de la API editorial nueva a
  `ADMIN`; conservar los permisos actuales de `/api/master-products` por
  compatibilidad.
- Motivo: la calidad del catalogo comun requiere revision antes de abrir
  contribuciones de tiendas o usuarios.

## 2026-06-30 - Fundamentos editoriales aditivos

- Decision: implementar primero `publishers`, `catalog_franchises` y
  `catalog_series` como fundamentos editoriales aditivos.
- Motivo: separan identidad editorial de `master_products` sin romper
  colecciones, inventario, buscados ni endpoints legacy.
- Decision: usar `TIMESTAMPTZ` y actores `BIGINT` para la auditoria nueva;
  `created_by` es obligatorio porque toda escritura editorial exige un ADMIN
  autenticado.
- Motivo: mantiene consistencia con el modelo JPA y el esquema actual, evitando
  introducir actores textuales incompatibles con el resto del backend.
- Decision: introducir `MVP2_FOUNDATION` en exportables para backend y datos sin
  UI editorial todavia.
- Motivo: distingue una base activa de MVP 2 tanto del recorrido visible de MVP
  1 como de las capacidades legacy/futuras.
- Decision: una serie `ACTIVE` solo puede referenciar publisher/franchise
  `ACTIVE`, y estos no se archivan mientras una serie publica los use.
- Motivo: evita exponer indirectamente registros DRAFT/ARCHIVED en una respuesta
  publica de series.

## 2026-06-30 - Items y ediciones editoriales

- Decision: implementar `catalog_items` y `catalog_item_editions` como segunda
  capa del catalogo editorial.
- Motivo: separa la identidad coleccionable de una obra o volumen de sus
  ediciones concretas y prepara colecciones editoriales, inventario por edicion
  y matching flexible sin romper `master_products`.
- Decision: normalizar ISBN y EAN en servicio eliminando espacios y guiones, y
  aplicar unicidad parcial entre filas no eliminadas sin columnas auxiliares.
- Motivo: mantiene un esquema pequeno, admite entrada humana razonable y evita
  duplicados identificables sin validar checksums ni consultar fuentes externas.
- Decision: una edition `ACTIVE` requiere item, serie y publisher opcional
  `ACTIVE`; no se archivan dependencias mientras tengan descendientes activos.
- Motivo: preserva la cadena publica completa y evita respuestas que revelen
  indirectamente contenido DRAFT o ARCHIVED.

## 2026-06-30 - Puente de catalogo y reconciliacion

- Decision: crear `master_product_catalog_links` como puente aditivo entre
  `master_products` y el catalogo editorial.
- Motivo: permite reconciliar productos legacy con items/editions sin romper
  colecciones, inventario, recomendaciones ni reservas.
- Decision: el backfill crea propuestas `PROPOSED` y nunca verifica
  automaticamente.
- Motivo: los campos legacy pueden ser ambiguos y requieren revision ADMIN
  antes de afectar a futuros consumidores.
- Decision: impedir mas de un enlace `VERIFIED` no eliminado por master product
  mediante regla de servicio e indice parcial PostgreSQL.
- Motivo: una identidad legacy necesita una unica reconciliacion canonica sin
  impedir propuestas alternativas ni conservar rechazos historicos.

## 2026-06-30 - Fachada editorial de lectura

- Decision: exponer una fachada agregada bajo `/api/catalog/editorial` sin
  modificar `/api/master-products` ni sus consumidores.
- Motivo: permite adoptar el catalogo editorial de forma gradual y reversible.
- Decision: limitar las lecturas publicas a cadenas completas `ACTIVE` y no
  eliminadas; los resultados y consultas del puente requieren `ADMIN`.
- Motivo: evita filtrar borradores, dependencias archivadas o reconciliaciones
  internas al recorrido publico.
- Decision: resolver la busqueda combinada mediante una consulta JPA nativa,
  parametrizada y paginada sobre series, items y ediciones.
- Motivo: mantiene una paginacion global estable sin cargar los tres catalogos
  completos en memoria ni introducir una nueva dependencia de busqueda.

## 2026-07-01 - Frontend editorial publico

- Decision: mantener `/catalog` como catalogo legacy y publicar el nuevo flujo
  editorial bajo `/catalog/editorial` con rutas propias de lectura.
- Motivo: permite validar la navegacion editorial sin romper el recorrido MVP 1
  ni cambiar consumidores de `master_products`.
- Decision: exponer en UI solo resultados `SERIES`, `ITEM` y `EDITION`; modelar
  el lookup `MASTER_PRODUCT_LINK` en el servicio sin enlazarlo desde pantallas.
- Motivo: la reconciliacion sigue siendo una capacidad interna exclusiva de
  ADMIN y no debe filtrarse al catalogo publico.
- Decision: clasificar las nuevas rutas y relaciones como `MVP2_VISIBLE`.
- Motivo: distingue el primer recorrido editorial visible de la base backend
  `MVP2_FOUNDATION` y evita presentar MVP 2 completo antes de EPIC 36-38.

## 2026-07-01 - Referencias editoriales en colecciones

- Decision: permitir referencias duales en `collection_items` y hacer
  `master_product_id` nullable, con una constraint que exige producto master o
  item editorial.
- Motivo: habilita items editoriales puros sin romper colecciones legacy.
- Decision: enriquecer automaticamente referencias legacy solo con enlaces
  `VERIFIED` activos; las selecciones editoriales explicitas usan
  `MANUAL_EDITORIAL` y se rechazan con 409 si contradicen un puente verificado.
- Motivo: no se deben filtrar propuestas o rechazos ni crear identidades
  ambiguas en una coleccion personal.
- Decision: en update, la referencia completa se conserva cuando no llega ningun
  ID. Un `masterProductId` explicito selecciona modo legacy y limpia la
  referencia editorial; un `catalogItemId` explicito aplica el item y la edicion
  enviados, incluido `catalogItemEditionId = null`.
- Motivo: mantiene compatibilidad con clientes legacy que actualizan solo estado
  y metadatos.
- Decision: las recomendaciones ignoran defensivamente items editoriales puros
  hasta EPIC 37.
- Motivo: evita un acceso nulo sin adelantar matching editorial.

## 2026-07-07 - Matching editorial gradual en recomendaciones

Las recomendaciones usan una estrategia de prioridad:

1. coincidencia exacta por edicion editorial;
2. coincidencia por item editorial;
3. fallback legacy por master product.

`categoryCode` permanece como filtro legacy mientras no exista una equivalencia
segura entre categorias legacy y tipos editoriales. No se implementan reservas
editoriales, pagos ni marketplace en EPIC 37.

## 2026-07-08 - Creators editoriales asociados a items

Los creditos editoriales se modelan mediante `creators` y
`catalog_item_creators`. Los creditos se asocian a `CatalogItem`, no a series,
para evitar creditos ambiguos de serie. Los roles iniciales son AUTHOR, WRITER,
ARTIST, ILLUSTRATOR, TRANSLATOR, EDITOR y OTHER. La lectura publica solo expone
creators ACTIVE y creditos no eliminados.

## 2026-07-08 - Relaciones editoriales entre items

Las relaciones editoriales se modelan entre `CatalogItem` mediante
`catalog_item_relationships`. No se crean relaciones automaticas inversas ni
relaciones entre series o ediciones. El detalle editorial puede mostrar
relaciones entrantes y salientes con direccion INCOMING/OUTGOING.

## 2026-07-09 - Criterio de cierre MVP2 editorial

MVP2 se considera `MVP2_CLOSED_WITH_LIMITATIONS` porque ya implementa la
biblioteca comun editorial definida para la fase: publishers, franquicias,
series, items, ediciones, creators, relaciones entre items, puente legacy,
fachada publica y adopcion gradual por colecciones, inventario y matching.

Las funcionalidades de administracion editorial, carga masiva, moderacion,
marketplace, pagos, alertas, social y movil quedan fuera de MVP2.

La siguiente fase recomendada es EPIC 39 - Cierre MVP2 editorial y preparacion
de MVP3 Admin Editorial, sin iniciar todavia la implementacion funcional del
admin editorial.

## 2026-07-09 - Reordenacion de MVPs tras cierre MVP2

Tras cerrar MVP2 como `MVP2_CLOSED_WITH_LIMITATIONS`, la siguiente fase pasa a
ser MVP3 Admin editorial y carga real de datos. Social basico se desplaza a una
fase posterior, concretamente MVP 6.

Motivo: el catalogo editorial ya existe tecnicamente, pero necesita herramientas
mantenibles de administracion, calidad de datos y carga real antes de escalar a
social, tiendas, marketplace o pagos.

## 2026-06-16 - Edad recomendada

- Decisión: plataforma recomendada para mayores de 18 años.
- Motivo: en fases futuras habrá contenido publicado por usuarios y tiendas difícil de controlar completamente.
