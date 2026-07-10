# MVP3 Admin Editorial UX audit

Fecha: 2026-07-10.

Esta revision cubre el primer bloque administrativo de MVP3. No equivale a una
auditoria WCAG ni declara MVP3 cerrado como producto final.

| Pantalla | Problema detectado | Severidad | Accion realizada | Pendiente |
| --- | --- | --- | --- | --- |
| `/admin/editorial` | Ningun enlace roto; las secciones implementadas ya tienen destino. | LOW | Validado shell y enlace de reconciliacion. | Revisar UX visual con datos reales en EPIC 41B/posterior. |
| `/admin/editorial/publishers` | Sin hallazgo funcional. | LOW | Validado estado vacio, carga, error y formulario existentes. | Unificar literal de paginacion en una futura tarea de copy. |
| `/admin/editorial/franchises` | Sin hallazgo funcional. | LOW | Validado flujo CRUD existente. | Unificar literal de paginacion en una futura tarea de copy. |
| `/admin/editorial/series` | Sin hallazgo funcional. | LOW | Validado flujo CRUD existente. | Unificar literal de paginacion en una futura tarea de copy. |
| `/admin/editorial/items` | Sin hallazgo funcional. | LOW | Validado contexto obligatorio, estados y validaciones. | Unificar literal de paginacion en una futura tarea de copy. |
| `/admin/editorial/editions` | Sin hallazgo funcional. | LOW | Validado contexto obligatorio, estados y validaciones. | Unificar literal de paginacion en una futura tarea de copy. |
| `/admin/editorial/creators` | Confirmacion de borrado hardcodeada en ingles. | MEDIUM | Movida a i18n ES/EN. | Ninguno funcional. |
| `/admin/editorial/credits` | Confirmacion de borrado hardcodeada en ingles. | MEDIUM | Movida a i18n ES/EN. | Ninguno funcional. |
| `/admin/editorial/relationships` | Confirmacion de borrado hardcodeada en ingles. | MEDIUM | Movida a i18n ES/EN. | Ninguno funcional. |
| `/admin/editorial/master-product-links` | Confirmaciones de verify/reject/backfill hardcodeadas en ingles. | MEDIUM | Conectadas a las claves i18n existentes. | Ninguno funcional. |

## Resultado

- Las diez rutas administrativas existentes permanecen protegidas por
  `authGuard` y `adminGuard`.
- El enlace ADMIN del layout se muestra solamente a usuarios autenticados con
  rol `ADMIN`; los tests cubren anonimo, usuario normal y ADMIN.
- Los formularios revisados ya aplican contextos requeridos y minimos para IDs,
  ordenes y `confidenceScore` donde corresponde.
- No se encontraron enlaces rotos, nuevas rutas necesarias ni cambios de
  backend durante la auditoria.
