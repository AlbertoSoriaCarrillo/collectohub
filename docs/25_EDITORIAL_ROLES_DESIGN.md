# Editorial roles design

Fecha: 2026-07-12. Esta EPIC es de auditoria y diseno: no introduce roles,
migraciones, endpoints, anotaciones de seguridad, JWT, guards ni navegacion.

## 1. Resumen

El mantenimiento del catalogo editorial no debe requerir el rol global `ADMIN`.
La recomendacion es crear mas adelante `EDITORIAL_ADMIN` como rol operativo
editorial, manteniendo `ADMIN` como superusuario. La implementacion queda fuera
de esta EPIC y se planifica en EPIC 43B y 43C.

## 2. Estado actual

Los roles seed actuales son `ADMIN`, `USER`, `SHOP_OWNER` y
`CONTENT_CREATOR`. `User` mantiene una relacion many-to-many con `Role`.
`AuthenticatedUser.from(User)` convierte los codigos de rol en
`SimpleGrantedAuthority` sin prefijo, por lo que las reglas actuales usan
`hasAuthority('ADMIN')`.

El admin editorial usa `adminGuard`, que solo acepta `ADMIN`, en 11 rutas
`/admin/editorial...`; el enlace de navegacion tambien comprueba solo ese rol.
En backend hay 28 operaciones HTTP editoriales protegidas, con comprobaciones
defensivas equivalentes en los servicios. Los listados y detalles publicos
mantienen la lectura de registros `ACTIVE` no eliminados.

## 3. Problema de seguridad

`ADMIN` tambien representa administracion global. Concederlo a quien mantiene
datos editoriales vulnera minimo privilegio y amplia el alcance a capacidades
ajenas al catalogo. `CONTENT_CREATOR`, `SHOP_OWNER` y `USER` no expresan la
responsabilidad editorial administrativa y no deben reutilizarse para ello.

## 4. Inventario de comprobaciones ADMIN

Se inventariaron 28 operaciones HTTP editoriales, 28 comprobaciones defensivas
de servicio, un guard compartido para 11 rutas y una comprobacion de
navegacion. Todas las filas editoriales propuestas aceptaran `ADMIN` y
`EDITORIAL_ADMIN` tras EPIC 43B/43C; `ADMIN` conserva acceso en todas.

| Archivo / componente | Endpoint o ruta | Operacion actual | Comprobacion actual | Clasificacion | Rol futuro |
| --- | --- | --- | --- | --- | --- |
| `PublisherController` / `PublisherService` | `POST`, `PUT /api/catalog/publishers` | alta, edicion | `hasAuthority('ADMIN')` y `ensureAdmin` | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogFranchiseController` / servicio | `POST`, `PUT /api/catalog/franchises` | alta, edicion | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogSeriesController` / servicio | `POST`, `PUT /api/catalog/series` | alta, edicion | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogItemController` / servicio | `POST`, `PUT /api/catalog/series/{id}/items` | alta, edicion | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogItemEditionController` / servicio | `POST`, `PUT /api/catalog/items/{id}/editions` | alta, edicion | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CreatorController` / `CreatorService` | `POST`, `PUT`, `DELETE /api/catalog/creators` | CRUD, soft delete | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogItemCreatorController` / servicio | `POST`, `PUT`, `DELETE /api/catalog/items/{id}/creators` | creditos | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `CatalogItemRelationshipController` / servicio | `POST`, `PUT`, `DELETE /api/catalog/items/{id}/relationships` | relaciones | igual | editorial | ADMIN o EDITORIAL_ADMIN |
| `MasterProductCatalogLinkController` / servicio | 7 operaciones `/api/catalog/master-product-links` | listar, detalle, crear, editar, verify, reject, backfill | anotacion de clase y `ensureAdmin` | editorial | ADMIN o EDITORIAL_ADMIN |
| `EditorialCatalogFacadeController` / servicio | `GET /api/catalog/editorial/master-products/{id}/link` | lectura de enlace interno | `hasAuthority('ADMIN')` y defensa | editorial | ADMIN o EDITORIAL_ADMIN |
| `EditorialDataQualityController` / servicio | `GET /api/catalog/admin/data-quality/report` | reporte de calidad | anotacion de clase y defensa | editorial | ADMIN o EDITORIAL_ADMIN |
| `EditorialCatalogSupport` | filtros `recordStatus`, DRAFT/ARCHIVED | lectura interna | `isAdmin` | editorial | ADMIN o EDITORIAL_ADMIN |
| `MasterProductController` / `CatalogService` | `POST`, `PUT /api/master-products` | catalogo legacy de tienda | `ADMIN` o `SHOP_OWNER` | tiendas/legacy, fuera de cambio | sin cambio |
| `adminGuard`, rutas y `MainLayout` | 11 rutas y enlace admin | acceso y visibilidad | `hasRole('ADMIN')` | editorial frontend | ADMIN o EDITORIAL_ADMIN |

No se detecta una necesidad de sustituir `ADMIN` globalmente. Los controles
fuera de catalogo, usuarios, seguridad, tiendas o infraestructura permanecen
fuera del alcance de la futura autorizacion editorial.

## 5. Matriz de capacidades

`Si` significa capacidad despues de EPIC 43B/43C. Las lecturas publicas ACTIVE
no cambian; DRAFT y ARCHIVED requieren una capacidad editorial administrativa.

| Area | Anonimo | USER | CONTENT_CREATOR | SHOP_OWNER | EDITORIAL_ADMIN | ADMIN |
| --- | --- | --- | --- | --- | --- | --- |
| Shell y navegacion editorial | No | No | No | No | Si | Si |
| Publishers, franchises y series | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | Gestion | Gestion |
| Items y editions | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | Gestion | Gestion |
| Creators, credits y relationships | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | ACTIVE lectura | Gestion | Gestion |
| Master product links | No | No | No | No | Gestion | Gestion |
| Verify/reject de links y backfill | No | No | No | No | Si | Si |
| Reporte de calidad | No | No | No | No | Si | Si |
| Lectura DRAFT y ARCHIVED | No | No | No | No | Si | Si |
| Creacion, actualizacion, archivado y soft delete | No | No | No | No | Si | Si |

## 6. Alternativas evaluadas

### Opcion A - Un unico rol EDITORIAL_ADMIN

Ventajas: implementacion simple, riesgo bajo, compatible con el modelo actual y
suficiente sin un equipo editorial ni flujo formal de aprobacion. Inconveniente:
no separa edicion y aprobacion. Es la opcion recomendada ahora.

### Opcion B - EDITORIAL_EDITOR y EDITORIAL_REVIEWER

Separaria creacion/modificacion de DRAFT de activacion, archivado y
verify/reject. Aporta control adicional, pero duplica reglas, tests y procesos
operativos sin evidencia de una necesidad actual. Se pospone hasta que exista
equipo editorial real y flujo de aprobacion formal.

### Opcion C - Autoridades por permiso

Permisos como `CATALOG_READ_DRAFT`, `CATALOG_WRITE`, `CATALOG_PUBLISH`,
`CATALOG_RECONCILE` y `CATALOG_QUALITY_READ` ofrecen maxima granularidad, pero
exigen modelo, gestion y auditoria adicionales. Son una evolucion futura, no la
solucion inmediata.

## 7. Decision

El catalogo editorial usara inicialmente `EDITORIAL_ADMIN` como rol operativo.
`ADMIN` conservara acceso como superusuario. No se reutilizaran
`CONTENT_CREATOR`, `SHOP_OWNER` ni `USER`. La division
`EDITORIAL_EDITOR`/`EDITORIAL_REVIEWER` se pospone.

## 8. Compatibilidad JWT y sesiones

El backend genera el claim `roles` al emitir el access token y devuelve los
roles en la respuesta de autenticacion. En cada peticion el filtro JWT valida el
token y recarga `UserDetails`; `AuthenticatedUser` vuelve a derivar
`GrantedAuthority` de los roles actuales de base de datos. El frontend conserva
los roles recibidos en `localStorage` y los usa para guards y navegacion.

Tras un cambio de rol, la API puede reflejarlo al cargar el usuario para la
peticion, pero la UI conserva su snapshot hasta login, refresh o
`GET /api/users/me`. La operacion documentada debera exigir reautenticacion o
refresco de sesion para actualizar tokens y navegacion. Las comprobaciones
editoriales futuras aceptaran ambos roles; los endpoints globales que requieren
`ADMIN` no se tocaran.

## 9. Plan backend

EPIC 43B ha aplicado la auditoria puntual de permisos y:

1. anadir una migracion Liquibase aditiva que inserte `EDITORIAL_ADMIN`, con
   rollback que elimine solo ese rol sin asignaciones activas;
2. centralizar la regla editorial en un helper reutilizable que acepte `ADMIN`
   o `EDITORIAL_ADMIN`;
3. actualizar exclusivamente controladores, servicios y filtros editoriales
   inventariados;
4. mantener `ADMIN` en endpoints globales y en el catalogo legacy de tienda;
5. cubrir autorizacion positiva y 403 por cada rol relevante;
6. no asignar el rol automaticamente a usuarios existentes.

No se realizara un reemplazo textual indiscriminado de `ADMIN`.

## 10. Plan frontend

EPIC 43C creara un `editorialAdminGuard`, o renombrara claramente el guard
actual, para aceptar `ADMIN` o `EDITORIAL_ADMIN`. Mantendra guards globales
separados, centralizara la comprobacion mediante `hasAnyRole`, mostrara el
enlace editorial solo a esos dos roles y actualizara rutas, layout y tests. No
debe duplicarse logica de roles entre componentes.

## 11. Estrategia de asignacion

Inicialmente la asignacion sera controlada exclusivamente por `ADMIN`, sin
interfaz publica ni autoasignacion. Se compararon SQL manual controlado, script
operativo, endpoint ADMIN futuro y pantalla global futura. La primera opcion
operativa recomendada es un procedimiento o script auditado posterior a la
migracion; un endpoint o interfaz requerira una EPIC separada. Registro,
`CONTENT_CREATOR` y `SHOP_OWNER` nunca obtendran este rol automaticamente.

## 12. Riesgos

- Ampliar por error permisos globales al reutilizar el guard editorial.
- Dejar una ruta, filtro DRAFT/ARCHIVED o defensa de servicio solo con ADMIN.
- Mostrar navegacion stale hasta renovar la sesion.
- Eliminar accidentalmente acceso de ADMIN durante la transicion.
- Convertir `CONTENT_CREATOR` en permiso editorial por similitud semantica.

## 13. Estrategia de tests

EPIC 43B debe probar migracion, emision/lectura de autoridades y cada grupo de
endpoints con `ADMIN`, `EDITORIAL_ADMIN` y un rol no autorizado. EPIC 43C debe
probar guard, rutas, visibilidad de navegacion y ausencia de acceso para USER,
CONTENT_CREATOR y SHOP_OWNER. No se anaden E2E o Playwright en esta EPIC.

## 14. Plan de reversion

Si la migracion futura debe revertirse, primero se revocaran asignaciones de
`EDITORIAL_ADMIN`; despues se aplicara el rollback de su seed. La regla
centralizada podra volver temporalmente a ADMIN sin tocar endpoints globales.
No se eliminara ni degradara el rol `ADMIN`.

## 15. EPICs siguientes

1. EPIC 43B - Backend role EDITORIAL_ADMIN y autorizacion editorial centralizada.
   Completada el 2026-07-12: migracion 012, helper y autorizacion backend.
2. EPIC 43C - Guard y navegacion editorial.
3. EPIC futura separada - gestion visual global de roles, solo si es necesaria.

MVP3 continua abierto. Esta decision no inicia MVP4 ni introduce funcionalidades
sociales, marketplace, pagos o permisos colaborativos.
