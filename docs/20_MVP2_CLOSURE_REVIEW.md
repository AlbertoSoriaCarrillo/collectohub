# CollectoHub MVP 2 closure review

Fecha de revision: 2026-07-09.

## Resultado ejecutivo

Estado recomendado: `MVP2_CLOSED_WITH_LIMITATIONS`.

MVP 2 cumple su objetivo principal: crear una biblioteca comun editorial capaz
de representar series, items, ediciones, publishers, creators, relaciones entre
items, compatibilidad legacy y adopcion gradual por colecciones, inventario y
recomendaciones.

La recomendacion es cerrar MVP 2 como base tecnica y producto editorial de
lectura/adopcion gradual, documentando sus limitaciones. La siguiente fase no
debe implementar social, tiendas, marketplace ni pagos directamente. Debe ser:

```text
EPIC 39 - Cierre MVP2 editorial y preparacion de MVP3 Admin Editorial
```

EPIC 39 debe ser una fase corta de cierre, estabilizacion documental y
preparacion del siguiente bloque administrativo, no una implementacion funcional
del admin editorial completo.

## Capacidades auditadas

| Area | Estado | Evidencia documental | Riesgo | Siguiente accion |
| --- | --- | --- | --- | --- |
| catalog foundation | Implementado | EPIC 31 en `docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md`; `publishers`, `catalog_franchises`, `catalog_series` en `docs/export/database-schema.md` | Documentos conceptuales antiguos pueden seguir hablando del catalogo editorial como futuro | Mantener documentacion estrategica alineada con el estado real |
| catalog items/editions | Implementado | EPIC 32; tablas `catalog_items` y `catalog_item_editions`; endpoints `/api/catalog/items` y `/api/catalog/editions` | Calidad de datos depende de carga/revision editorial | Preparar admin editorial en MVP3 |
| master product bridge | Implementado | EPIC 33; `master_product_catalog_links`; fachada legacy documentada | Enlaces `PROPOSED` no deben tratarse como verdad editorial | Mantener verificacion ADMIN y no romper `/api/master-products` |
| editorial facade | Implementado | EPIC 34; `/api/catalog/editorial/search` y detalles de serie/item/edicion | La fachada publica no sustituye aun todos los consumidores legacy | Seguir usando adopcion gradual |
| editorial frontend | Implementado | EPIC 35; rutas `/catalog/editorial`, serie, item y edicion en `docs/export/frontend-routes.md` | No existe frontend admin editorial | Preparar MVP3 Admin Editorial |
| collection editorial references | Implementado | EPIC 36; `collection_items` admite `catalog_item_id` y `catalog_item_edition_id` | Convivencia legacy/editorial puede requerir limpieza futura | Mantener compatibilidad y medir adopcion editorial |
| inventory editorial references | Implementado | EPIC 37; `shop_products` admite referencias editoriales | Las rutas de tienda siguen fuera del recorrido principal | No adelantar MVP4 sin decision de producto |
| recommendation matching | Implementado basico | EPIC 37; prioridad `EDITION_EXACT`, `ITEM_EXACT`, `LEGACY_MASTER_PRODUCT` | Sin alertas, ranking avanzado, distancia ni preferencias | Reservar mejoras para MVP4 Matching |
| creators | Implementado acotado | EPIC 38; `creators` y `catalog_item_creators`; detalle editorial muestra creditos | Sin frontend admin, moderacion ni creators de serie | Llevar gestion visual a MVP3 Admin Editorial |
| item relationships | Implementado acotado | EPIC 38; `catalog_item_relationships`; detalle editorial muestra relaciones | Sin grafo avanzado ni relaciones entre series/ediciones | Mantener alcance por item hasta casos reales |
| exports/docs | Implementado | `docs/export/README.md`: 11 migraciones, 22 tablas, 81 endpoints, 32 rutas, 61 relaciones | Exports manuales pueden quedar obsoletos | Actualizarlos en cada EPIC funcional |
| security | Implementado para MVP | API editorial nueva restringe escrituras a `ADMIN`; lecturas publicas solo `ACTIVE` | Sin moderacion avanzada, rate limiting ni historial wiki | Reforzar antes de abrir colaboracion masiva |
| tests | Implementado | `docs/12_TASK_LOG.md`: backend 322 tests; frontend 43 archivos y 119 tests en EPIC 38E | E2E fuera de CI; Testcontainers puede saltarse sin Docker | Mantener validacion local y valorar job E2E separado |
| docker/local validation | Implementado | EPIC 38E valida Docker Compose, PostgreSQL healthy y `/api/health` | Docker local no equivale a despliegue productivo | No presentar como produccion |

## Implementado

- Fundamentos editoriales: publishers, franquicias y series.
- Items y ediciones editoriales.
- Puente `master_products` con enlaces editoriales y backfill/reconciliacion.
- Fachada publica editorial de busqueda y detalle.
- Frontend publico editorial para busqueda, series, items y ediciones.
- Referencias editoriales graduales en colecciones.
- Referencias editoriales graduales en inventario.
- Matching basico por edicion, item y fallback legacy.
- Creators y creditos por item.
- Relaciones dirigidas entre items.
- Documentacion exportable de esquema, endpoints, rutas y mapa frontend-backend.

## Parcialmente implementado

- Administracion editorial: existen endpoints ADMIN, pero no hay frontend admin
  para publishers, franquicias, series, items, ediciones, creators o relaciones.
- Calidad editorial: hay estados, auditoria y constraints, pero no moderacion
  colaborativa, historial wiki, importadores ni flujos de revision avanzados.
- Adopcion por dominios: colecciones, inventario y matching soportan referencias
  editoriales, pero `master_products` sigue siendo contrato compatible.
- Matching: resuelve identidad editorial basica, pero no alertas, distancia,
  ranking avanzado, preferencias finas ni flujo comercial.

## Fuera de MVP 2

- Frontend admin editorial.
- Frontend admin de creators.
- Frontend admin de relaciones.
- Moderacion editorial.
- Edicion colaborativa/wiki.
- Carga masiva real.
- Importadores.
- Pantalla de grafo avanzada.
- Reservas editoriales.
- Marketplace.
- Pagos.
- Alertas.
- Notificaciones.
- Social.
- Movil.

## Riesgos restantes

- Documentacion estrategica antigua puede seguir describiendo el catalogo
  editorial como fase futura si no se mantiene alineada.
- La convivencia legacy/editorial exige disciplina para no romper
  `/api/master-products`, colecciones, inventario ni recomendaciones.
- Abrir edicion editorial a usuarios o tiendas sin moderacion degradaria la
  calidad del catalogo comun.
- Los exports son manuales; deben actualizarse junto a cada cambio funcional.
- Docker Compose local valida integracion, pero no representa despliegue
  productivo ni seguridad operacional.

## Deuda tecnica y documental

- Actualizar documentos estrategicos que aun describan MVP 2 como solo diseno.
- Mantener una politica explicita para cuando congelar escrituras directas en
  `/api/master-products`.
- Definir convenciones finales de slugs, alias/traducciones y cambios de URL.
- Revisar vulnerabilidades npm transitivas/dev en una tarea de mantenimiento.
- Valorar E2E en CI como job separado cuando el entorno sea estable.

## Decision recomendada para EPIC 39

### Objetivo

Cerrar formalmente MVP 2 editorial como `MVP2_CLOSED_WITH_LIMITATIONS` y
preparar MVP3 Admin Editorial sin implementar todavia el panel administrativo.

### Alcance

- Alinear documentos estrategicos con el cierre real de MVP 2.
- Definir criterios de entrada para MVP3 Admin Editorial.
- Priorizar capacidades admin: altas/ediciones visuales, revision de borradores,
  reconciliacion y moderacion basica.
- Revisar datos demo y capturas necesarias para presentar el catalogo editorial.
- Confirmar que los recorridos MVP1 y MVP2 publicos siguen claros.

### Fuera de alcance

- Implementar admin editorial.
- Crear endpoints, migraciones o rutas nuevas.
- Iniciar social, tiendas, marketplace, pagos o movil.
- Crear importadores, carga masiva o wiki colaborativa.

### Riesgos

- Confundir cierre de MVP 2 con producto listo para produccion.
- Abrir contribucion editorial sin controles de calidad.
- Saltar a social o comercio antes de tener una gestion editorial mantenible.

### Orden recomendado

1. Cerrar MVP 2 documentalmente.
2. Preparar criterios y backlog de MVP3 Admin Editorial.
3. Despues iniciar frontend/admin editorial acotado.
4. Mantener social, tiendas, marketplace y pagos como fases posteriores.

### Validaciones necesarias

- `git diff --check`.
- Revision documental de README, roadmap, estado MVP y backlog.
- Sin cambios funcionales en backend/frontend.
- En una EPIC funcional posterior: backend verify, frontend test/build y Docker
  Compose segun el alcance real.
