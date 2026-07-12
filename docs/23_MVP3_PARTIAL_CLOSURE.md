# MVP3 partial closure - Admin Editorial

Fecha: 2026-07-12. Este cierre cubre EPIC 40A-40G, EPIC 41A y las decisiones
posteriores de calidad/autorizacion; MVP3 no queda cerrado como producto final.

## Resumen ejecutivo

El primer bloque de administracion editorial esta implementado, auditado y
validado localmente. Permite a `ADMIN` mantener datos editoriales y reconciliar
el catalogo legacy, usando endpoints ya existentes sin migraciones nuevas.
EPIC 43A ha documentado, sin cambios funcionales, la evolucion hacia
`EDITORIAL_ADMIN` para aplicar minimo privilegio.

## Pantallas disponibles

| Ruta | Estado | Funcionalidad principal | Limitaciones |
| --- | --- | --- | --- |
| `/admin/editorial` | Implementada | Shell protegido y navegacion. | Sin dashboard agregado. |
| `/admin/editorial/publishers` | Implementada | Buscar, crear y editar publishers. | Sin acciones masivas. |
| `/admin/editorial/franchises` | Implementada | Buscar, crear y editar franchises. | Sin acciones masivas. |
| `/admin/editorial/series` | Implementada | Mantenimiento de series. | Sin importacion. |
| `/admin/editorial/items` | Implementada | Items por serie. | Requiere contexto de serie. |
| `/admin/editorial/editions` | Implementada | Editions por item. | Requiere contexto de item. |
| `/admin/editorial/creators` | Implementada | CRUD de creators. | Sin aliases. |
| `/admin/editorial/credits` | Implementada | Creditos por item. | Requiere contexto de item. |
| `/admin/editorial/relationships` | Implementada | Relaciones entre items. | Sin grafo visual. |
| `/admin/editorial/master-product-links` | Implementada | Enlaces legacy, verify/reject y backfill. | Sin reconciliacion automatica avanzada. |

## Endpoints, demo y validacion

Las pantallas reutilizan `GET/POST/PUT` de publishers, franchises, series,
items, editions, creators, credits, relationships y `master-product-links`; el
detalle completo esta en `docs/16_MVP_API_ENDPOINTS.md` y `docs/export/`.
`scripts/demo/create-editorial-admin-demo-data.ps1` prepara datos demo por API.

Se validaron frontend `npm.cmd ci`, tests (55 archivos, 174 tests) y build. El
backend quedo verde en EPIC 40G (322 tests, 0 fallos, 2 omitidos). Docker y la
ejecucion real del script siguen pendientes por daemon/backend local no
disponibles durante esas validaciones.

## Auditoria UX y limitaciones

EPIC 41A movio confirmaciones hardcodeadas a i18n y valido navegacion solo
ADMIN. Auditoria: `docs/22_MVP3_ADMIN_UX_AUDIT.md`.

| Area | Limitacion | Impacto | Siguiente accion |
| --- | --- | --- | --- |
| E2E admin | No hay recorrido real automatizado. | Riesgo de integracion UI/API. | EPIC 41C. |
| Datos demo | No se ejecuto contra backend ADMIN local. | Validacion parcial. | EPIC 41C. |
| Docker | Daemon no disponible. | Sin smoke compose reciente. | Reintentar localmente. |
| Datos reales | No hay carga productiva. | Catalogo de demostracion. | Calidad de datos posterior. |
| Roles | Solo `ADMIN` editorial en codigo actual. | Exceso de privilegio para editores. | EPIC 43B/43C implementaran el diseno `EDITORIAL_ADMIN`. |
| Calidad | EPIC 42A documenta calidad basica y warnings; faltan reglas backend fuertes, datos reales y E2E futuro. | Duplicados complejos posibles. | EPIC 42B. |

EPIC 42B anade hardening selectivo para creators y confirma las protecciones ya
existentes de credits, relationships y master links. No constituye cierre total
de calidad: siguen pendientes reglas complejas, datos reales y E2E futuro.

## Decision recomendada

EPIC 43A ha cerrado el diseno de roles editoriales separados de `ADMIN`; no
inicia MVP4 ni modifica seguridad. Siguiente EPIC: **EPIC 43B - Backend role
EDITORIAL_ADMIN y autorizacion editorial centralizada**, seguido de EPIC 43C
para guard y navegacion editorial.
