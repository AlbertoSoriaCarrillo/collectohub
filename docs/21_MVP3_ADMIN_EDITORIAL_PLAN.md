# CollectoHub MVP 3 admin editorial plan

Fecha de revision: 2026-07-10.

## 1. Objetivo de MVP3

MVP 3 debe convertir el catalogo editorial ya implementado en una base
mantenible mediante administracion visual, carga real de datos y control basico
de calidad.

El objetivo no es ampliar el producto hacia social o comercio, sino permitir que
publishers, franquicias, series, items, ediciones, creators, creditos,
relaciones y enlaces legacy puedan gestionarse sin depender de llamadas manuales
por API.

## 2. Por que MVP3 va antes de social/tiendas/marketplace

MVP 2 creo la biblioteca editorial comun y la adopcion gradual por colecciones,
inventario y matching. Sin herramientas de administracion, la calidad del
catalogo depende de datos manuales y de conocimiento tecnico.

No conviene escalar a social, tiendas, marketplace o pagos con datos editoriales
dificiles de mantener. MVP 3 debe asegurar navegacion admin, permisos, revision
de estados, deteccion basica de duplicados y reconciliacion antes de exponer
flujos de mayor riesgo.

## 3. Alcance funcional

- Admin de publishers.
- Admin de franquicias.
- Admin de series.
- Admin de items.
- Admin de ediciones.
- Admin de creators.
- Admin de creditos de creators por item.
- Admin de relaciones entre items.
- Reconciliacion visual `master_products` <-> catalogo editorial.
- Revision de estados `DRAFT`, `ACTIVE` y `ARCHIVED`.
- Busqueda y filtros admin.
- Validaciones visuales antes de enviar cambios.
- Gestion de duplicados basica.
- Datos demo editoriales mantenibles.

## 4. Fuera de alcance

- Social.
- Posts, likes, comentarios y feed.
- Tiendas como flujo principal.
- Marketplace.
- Pagos.
- Pedidos.
- Envios.
- Movil.
- Wiki colaborativa publica.
- Importadores masivos abiertos.
- IA.
- Scraping.
- Moderacion avanzada comunitaria.
- Pantalla de grafo avanzada.

MVP 3 puede incluir moderacion editorial basica de registros y estados, pero no
moderacion social/comunitaria.

## 5. Roles y permisos

- `ADMIN` puede crear, editar, archivar, verificar y rechazar contenido
  editorial segun los endpoints existentes.
- Usuarios normales no pueden acceder al admin editorial.
- `SHOP_OWNER` conserva permisos legacy sobre `/api/master-products`, pero no
  obtiene escritura editorial nueva.
- La lectura publica sigue limitada a registros `ACTIVE` y no eliminados.
- `DRAFT` y `ARCHIVED` solo deben mostrarse en pantallas admin.
- El enlace de navegacion admin debe ocultarse si el usuario no tiene `ADMIN`.

No se implementan cambios de permisos en esta fase documental.

## 6. Pantallas admin necesarias

Rutas implementadas y protegidas para el primer bloque de MVP3:

```text
/admin/editorial
/admin/editorial/publishers
/admin/editorial/franchises
/admin/editorial/series
/admin/editorial/items
/admin/editorial/editions
/admin/editorial/creators
/admin/editorial/credits
/admin/editorial/relationships
/admin/editorial/master-product-links
```

Estas rutas estan protegidas por autenticacion y rol `ADMIN`. El shell admin se
mantiene separado del recorrido publico para no contaminar `Home`, `Catalogo`,
`Colecciones`, `Buscados` y `Perfil`.

## 7. Endpoints existentes reutilizables

Los endpoints ADMIN ya documentados permiten empezar MVP 3 sin nuevas
migraciones:

- `GET/POST/PUT /api/catalog/publishers`.
- `GET/POST/PUT /api/catalog/franchises`.
- `GET/POST/PUT /api/catalog/series`.
- `GET/POST/PUT /api/catalog/series/{seriesId}/items`.
- `GET/POST/PUT /api/catalog/items/{itemId}/editions`.
- `GET/POST/PUT/DELETE /api/catalog/creators`.
- `GET/POST/PUT/DELETE /api/catalog/items/{itemId}/creators`.
- `GET/POST/PUT/DELETE /api/catalog/items/{itemId}/relationships`.
- `GET/POST/PUT /api/catalog/master-product-links`.
- `PUT /api/catalog/master-product-links/{id}/verify`.
- `PUT /api/catalog/master-product-links/{id}/reject`.
- `POST /api/catalog/master-product-links/backfill`.
- `GET /api/catalog/editorial/master-products/{masterProductId}/link`.

Tambien son reutilizables las lecturas publicas de fachada editorial para
previsualizar resultados `ACTIVE`.

## 8. Endpoints o capacidades que podrian faltar

- Dashboard admin agregado con conteos por entidad y estado.
- Busqueda global admin que incluya `DRAFT` y `ARCHIVED` de varias entidades en
  una sola respuesta.
- Endpoints de bulk action controlados para archivar/publicar varios registros.
- Informe de duplicados o candidatos ambiguos mas amigable para UI.
- Historial de cambios o auditoria legible para editores.
- Gestion de aliases/traducciones de titulos si se decide entrar en ese alcance.

Estas capacidades deben validarse durante las EPICs funcionales. No se crean en
EPIC 39C.

## 9. Orden recomendado de EPICs

1. EPIC 40A - Admin editorial shell y navegacion protegida. Completada el
   2026-07-10.
2. EPIC 40B - Admin publishers/franchises/series. Completada el 2026-07-10.
3. EPIC 40C - Admin items/editions. Completada el 2026-07-10.
4. EPIC 40D - Admin creators y creditos. Completada el 2026-07-10.
5. EPIC 40E - Admin relationships. Completada el 2026-07-10.
6. EPIC 40F - Admin master product links/reconciliation. Completada el 2026-07-10.
7. EPIC 40G - Datos demo editoriales y validacion integral MVP3 parcial.
   Completada el 2026-07-10.

El orden prioriza primero el shell admin, despues entidades base, luego
items/editions, creators, relaciones, reconciliacion y finalmente demo/validacion.

## 10. Criterios de entrada

- MVP 2 cerrado como `MVP2_CLOSED_WITH_LIMITATIONS`.
- Roadmap alineado con MVP 3 Admin editorial y carga real de datos.
- Endpoints editoriales ADMIN documentados.
- Rutas publicas MVP 1 y MVP 2 estables.
- Decisiones de permiso basicas documentadas: escritura editorial nueva solo
  `ADMIN`.
- No hay cambios pendientes sin commitear antes de iniciar EPIC 40A.

## 11. Criterios de salida

MVP 3 podra considerarse parcialmente cerrado cuando:

- exista shell admin protegido por `ADMIN`;
- existan pantallas admin para entidades base, items, ediciones, creators,
  creditos, relaciones y enlaces legacy;
- se puedan revisar `DRAFT`, `ACTIVE` y `ARCHIVED`;
- se puedan verificar/rechazar enlaces `master_products`;
- existan validaciones visuales y errores comprensibles;
- existan datos demo editoriales mantenibles;
- backend/frontend/build y validaciones locales pasen segun el alcance funcional;
- el recorrido publico de MVP 1 y MVP 2 no tenga regresiones.

## 12. Riesgos

- Mezclar admin editorial con navegacion publica y confundir al usuario normal.
- Exponer acciones admin a usuarios sin `ADMIN`.
- Implementar CRUDs antes de validar shell, permisos y layout.
- Duplicar logica de validacion sin respetar errores backend.
- Abrir carga masiva o colaboracion publica sin moderacion suficiente.
- Romper compatibilidad legacy de `/api/master-products`.

## 13. Primera EPIC funcional recomendada

Primera EPIC funcional:

```text
EPIC 40A - Admin editorial shell y navegacion protegida
```

Alcance propuesto:

- Crear ruta protegida `/admin/editorial`.
- Crear layout/shell admin.
- Mostrar dashboard admin simple.
- Proteger acceso por `ADMIN`.
- Ocultar enlace si el usuario no es `ADMIN`.
- Sin formularios CRUD todavia.
- Sin tocar backend salvo tests/guards si hiciera falta.

EPIC 40A debe validar navegacion, permisos, layout y separacion del recorrido
publico antes de anadir pantallas de mantenimiento editorial.

Estado: EPIC 40A completada el 2026-07-10. La ruta `/admin/editorial`, el
shell admin, el guard `ADMIN`, el enlace de navegacion condicionado y las
traducciones ES/EN quedan implementados. No se crean CRUDs ni se inicia EPIC
40B.

Estado: EPIC 40B completada el 2026-07-10. Quedan implementadas las rutas
admin `/admin/editorial/publishers`, `/admin/editorial/franchises` y
`/admin/editorial/series`, el servicio frontend admin, busqueda, filtros,
listado, create/update y cambio de `recordStatus` para entidades base. No se
inician items/editions, creators, relationships ni reconciliacion legacy.

Estado: EPIC 40C completada el 2026-07-10. Quedan implementadas las rutas
admin `/admin/editorial/items` y `/admin/editorial/editions`, modelos y servicio
frontend para items y editions, busqueda/filtros/listado/create/update y cambio
de `recordStatus`. No se inician creators, credits, relationships ni
reconciliacion legacy.

Estado: EPIC 40D completada el 2026-07-10. Quedan implementadas las rutas
admin `/admin/editorial/creators` y `/admin/editorial/credits`, modelos y
servicio frontend para creators y creditos por item, busqueda/filtros/listado,
create/update/delete y validaciones visuales. No se inician relationships,
reconciliacion legacy, imports ni cambios backend.

Estado: EPIC 40E completada el 2026-07-10. Queda implementada la ruta admin
`/admin/editorial/relationships`, modelos y servicio frontend para relaciones
entre items, pantalla admin con seleccion de item origen/destino, filtro por
estado, listado, create/update/delete y validaciones visuales. No se inicia
reconciliacion legacy, grafo avanzado, relaciones automaticas inversas ni
cambios backend.

Estado: EPIC 40F completada el 2026-07-10. Queda implementada la ruta admin
`/admin/editorial/master-product-links`, modelos y servicio frontend para el
bridge legacy, pantalla de reconciliacion con filtros, create/update,
verify/reject, backfill confirmado y consulta de bridge por producto maestro.
No se implementan cambios backend, migraciones, importadores abiertos, IA ni
scraping.

Estado: EPIC 40A-40G completadas el 2026-07-10. MVP3 Admin Editorial queda
validado parcialmente con rutas ADMIN, datos demo mantenibles por API y
validaciones locales. No se considera cerrado como producto final; queda
pendiente decidir el siguiente bloque. La siguiente tarea recomendada es EPIC
41A - Auditoria UX/Admin editorial y bugs de usabilidad.

Estado: EPIC 41A completada el 2026-07-10. La auditoria inicial esta en
`docs/22_MVP3_ADMIN_UX_AUDIT.md`; corrige confirmaciones sin i18n y valida
rutas, navegacion ADMIN, estados y formularios existentes. Siguiente tarea
recomendada: EPIC 41B - Cierre documental/exportable MVP3 parcial.

Estado: EPIC 41B completada el 2026-07-10. EPIC 40A-40G y 41A conforman el
primer bloque parcial implementado; cierre: `docs/23_MVP3_PARTIAL_CLOSURE.md`.
MVP3 no esta cerrado por completo. Siguiente recomendacion: EPIC 41C - E2E
basico Admin Editorial y validacion con datos demo.

Estado: EPIC 42A completada el 2026-07-10. Calidad editorial basica documentada
en `docs/24_EDITORIAL_DATA_QUALITY.md`; no cambia backend ni constraints.
Siguiente recomendacion: EPIC 42B - Hardening admin editorial y validaciones
backend selectivas.

Estado: EPIC 42B completada el 2026-07-10. Hardening selectivo sin schema ni
endpoints nuevos; siguiente recomendacion: EPIC 42C - Calidad editorial avanzada
y reporte de duplicados.
