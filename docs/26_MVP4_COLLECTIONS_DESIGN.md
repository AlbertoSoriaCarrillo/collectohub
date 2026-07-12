# MVP4 Collections Design

Fecha: 2026-07-12. Este documento audita el estado real y disena MVP4; no
introduce cambios funcionales, migraciones, endpoints, rutas ni datos.

## 1. Resumen ejecutivo

MVP1 ya entrega colecciones personales funcionales y MVP2 anadio referencias
editoriales duales. MVP4 debe convertir el catalogo editorial en la experiencia
principal de altas catalogadas sin romper las colecciones que solo conocen
`masterProductId`. La prioridad es definir identidad, flujos personales y
compatibilidad antes de ampliar UI o schema.

## 2. Estado actual

Las colecciones son propiedad de un usuario, tienen nombre, descripcion,
visibilidad PUBLIC/PRIVATE y categoria legacy opcional. Las operaciones de
create, update, soft delete, lista propia, detalle publico/propietario y CRUD
de items existen. La lista propia filtra por visibilidad/categoria y ordena por
id; no hay paginacion, orden configurable, estadisticas ni progreso.

## 3. Inventario backend

| Area | Backend | Frontend | Tests | Estado | Limitacion |
| --- | --- | --- | --- | --- | --- |
| Crear/editar/eliminar coleccion | CRUD propietario, soft delete | Formularios reales | MVC y servicio | IMPLEMENTED | Sin imagen ni tipo rico |
| Lista propia/detalle | Lista propia, detalle PUBLIC o propietario | Lista, detalle y acciones owner | MVC/UI | IMPLEMENTED | Sin pagina, sort ni resumen |
| Visibilidad | PUBLIC/PRIVATE y 404 para privada ajena | Selector | Seguridad | IMPLEMENTED | Campos privados no se separan |
| Imagen de coleccion | No existe | No existe | No | MISSING | Sin files |
| Alta/edicion/borrado item | CRUD propietario, soft delete | Formularios reales | MVC, servicio y UI | IMPLEMENTED | Sin manual real ni copias |
| Datos personales | Estado, condicion, unidad, limite, notas, fecha | Formulario y detalle | Servicio/UI | IMPLEMENTED | Notas expuestas en detalle publico |
| Catalogo editorial | Item/edition validos y bridge verificado | Busqueda editorial basica | Servicio/UI | PARTIAL | Seleccion de edition depende del resultado de busqueda |
| Legacy | `masterProductId` y bridge gradual | Selector legacy | Servicio/UI | LEGACY | Continua como via principal paralela |
| Wanted/buscados | Estados y recomendaciones tienda | Pantalla `/wanted` de recomendaciones | Servicio/UI | PARTIAL | No es lista de coleccion ni faltante calculado |
| Filtros/orden/paginacion | Solo lista propia por visibility/category | Filtro equivalente | MVC/UI | PARTIAL | Items sin filtros ni orden |
| Estadisticas/progreso | No existe | No existe | No | MISSING | Sin owned/wanted/missing agregado |

## 4. Inventario frontend

`MyCollections`, create, edit, detalle, create/edit item y `/wanted` consumen
la API real. El detalle detecta propietario y muestra referencia legacy,
bridge verificado o editorial manual. Las pantallas de item ofrecen modo
LEGACY/EDITORIAL, pero no una experiencia de seleccion item -> editions ni un
modo manual. `/wanted` muestra recomendaciones comerciales derivadas de estados
WANTED/MISSING, no una lista canonica ni progreso de serie.

## 5. Modelo de datos actual

| Campo | Finalidad y uso | Riesgo/deuda |
| --- | --- | --- |
| `collections.user_id` | Propietario; controla escritura y lectura privada | Correcto, sin comparticion |
| `name`, `description`, `visibility`, `category_id` | Metadatos y filtros legacy | No hay tipo, imagen ni privacidad por campo |
| `master_product_id` | Identidad MVP1 y compatibilidad | No es la identidad editorial comun |
| `catalog_item_id` | Referencia editorial principal disponible | Aun no es obligatoria para nuevas altas |
| `catalog_item_edition_id` | Variante concreta; FK y requiere item | Debe seleccionarse ergonomicamente |
| `editorial_reference_source` | LEGACY, VERIFIED_BRIDGE, MANUAL_EDITORIAL | No distingue MANUAL de item no catalogado |
| `collection_status` | OWNED, WANTED, MISSING, DUPLICATED, SELLABLE, TRADABLE | Semanticas mezcladas y no hay calculo de missing |
| condicion, unidad, limite, notas, fecha | Datos personales por entrada | Notas/fecha se devuelven a lectura publica |
| auditoria y `deleted_at` | Soft delete y trazabilidad | Sin politica de retencion/recuperacion |

La migracion 008 permite `master_product_id` o `catalog_item_id`, exige item
cuando hay edition y realiza backfill solo desde bridges VERIFIED activos. El
servicio rechaza edition de otro item, referencias inactivas y contradicciones
con bridge verificado. No permite una entrada sin identidad catalogada.

## 6. Problemas y deuda tecnica

- EPIC 44B ya sanitiza `notes` y `acquiredAt` para lectores no propietarios;
  quedan pendientes flujos frontend que comuniquen esa privacidad.
- Una alta editorial puede incluir master product compatible, pero la UI actual
  limpia esa relacion al cambiar a editorial y no expresa el bridge con claridad.
- No hay item manual, cantidad, copias, alias local ni enlace posterior.
- `MISSING` se usa como estado persistido y como entrada para recomendaciones,
  no como ausencia calculada de una serie.
- No hay validacion de categoria de coleccion frente a taxonomia editorial.
- No se bloquean duplicados, pero tampoco existe una regla de copias explicita.

## 7. Identidad canonica

Se adopta la opcion A: para nuevas altas catalogadas, `catalogItemId` sera la
identidad principal; `catalogItemEditionId` sera opcional y siempre pertenecera
al item. `masterProductId` se conserva para legacy y compatibilidad gradual,
incluido bridge VERIFIED. Los datos editoriales permanecen en catalogo y los
datos personales en `collection_items`. La opcion B prolonga duplicacion legacy;
la opcion C hibrida sin regla mantiene ambiguedades.

## 8. Items manuales

MVP4 debe ofrecer una via manual controlada, marcada `MANUAL`, para no bloquear
al usuario. Debe conservar titulo minimo y metadatos personales separados de
catalogo, permitir enlace posterior a un catalog item y nunca crear
automaticamente publishers, series, items o editions. Requiere una evolucion de
schema/contrato: el check actual exige master product o catalog item.

## 9. Duplicados y copias

Una entrada representa inicialmente una pieza coleccionable. MVP4 no bloqueara
duplicados automaticamente: dos editions pueden ser intencionales y varias
copias fisicas requieren una decision explicita. La recomendacion es mantener
entradas separadas durante MVP4; evaluar `quantity` solo despues de validar
casos de copia, condicion y unidad. Duplicado en colecciones distintas siempre
es valido. OWNED y WANTED del mismo item deben poder coexistir solo con una
explicacion de edition/copia o con una futura regla de consolidacion.

## 10. OWNED, WANTED y MISSING

Se conserva una unica fuente de verdad: `collection_items.collection_status`.
`WANTED` es una intencion explicita y puede seleccionar edition. `OWNED` es una
entrada poseida con sus datos personales. Pasar WANTED a OWNED debe actualizar
la misma entrada y conservar notas/contexto, no crear un duplicado por defecto.
`MISSING` sera un resultado calculado futuro: item ACTIVE de una serie elegida
que no aparece como OWNED en la coleccion; no sustituye ni duplica WANTED.
El estado persistido MISSING existente se mantiene por compatibilidad hasta una
migracion y transicion explicitamente diseniadas.

## 11. Flujos de usuario

1. Crear coleccion: tipo/categoria, nombre, descripcion, visibilidad, imagen
   futura y llamada a anadir el primer item.
2. Alta catalogada: buscar, elegir item, edition opcional, OWNED/WANTED y datos
   personales; guardar sin duplicar datos editoriales.
3. Alta manual: confirmar ausencia, introducir minimo y enlazar despues.
4. WANTED a OWNED: actualizar estado, edition/adquisicion y conservar notas.
5. Consulta: resumen, filtros, orden, progreso, owned/wanted/missing y acciones
   de propietario, sin exponer datos privados.

## 12. Seguridad y privacidad

El propietario mantiene CRUD; terceros no modifican; colecciones PUBLIC son
legibles y PRIVATE responde como no encontrada a terceros. MVP4 debe crear DTOs
publicos separados o filtrado de campos: notas, valor estimado y fecha de
adquisicion son privados por defecto. Solo referencias editoriales ACTIVE son
visibles al usuario normal; DRAFT/ARCHIVED y borrados no se exponen.

## 13. Contratos API futuros

| Operacion | Actual | Evolucion MVP4 | Compatibilidad |
| --- | --- | --- | --- |
| Coleccion CRUD | Endpoints actuales | Tipo, imagen y DTO publico/owner | Aditiva |
| Lista/detalle | Lista sin pagina; detalle mixto | filtros, sort, pagina y resumen | Mantener respuesta basica |
| Alta/edicion item | Dual legacy/editorial | `referenceKind`, identidad canonica y manual | Aceptar payload legacy |
| Mover WANTED/OWNED | PUT generico | transicion explicita o PUT semantico | Mantener PUT |
| Buscar para alta | Dos servicios frontend | selector item -> editions | Reutilizar facade publica |
| Missing | No existe | consulta calculada por serie/coleccion | Endpoint nuevo futuro |
| Enlazar manual | No existe | accion controlada de resolucion | Nuevo futuro |

## 14. Compatibilidad legacy

Clasificacion futura: `CATALOG_LINKED` (item/edition), `LEGACY_LINKED` (solo
master product), `MANUAL`, `UNRESOLVED` e `INVALID_REFERENCE`. Los registros
legacy con bridge VERIFIED se enriquecen solo de forma segura; no se fuerza
backfill. Referencias nulas no existen bajo el constraint actual; las
inconsistentes se diagnostican, no se corrigen silenciosamente.

## 15. Migraciones futuras

| Necesidad | Riesgo/orden | Backfill y rollback |
| --- | --- | --- |
| `reference_kind` y campos manuales | Alta: cambia constraint de identidad | Clasificar existentes; rollback conserva legacy |
| privacidad de datos personales | Media: contratos publicos | Defaults privados; DTO antes que ocultacion |
| cantidad/copia | Media: semantica de duplicados | Nulo/1 inicial; rollback deja entradas |
| constraints e indices editoriales | Alta: datos inconsistentes | Auditoria previa, constraint gradual |
| exclusion de missing | Media: calculo futuro | Sin backfill automatico inicialmente |

## 16. Riesgos

- Romper colecciones legacy al imponer catalogo demasiado pronto.
- Exponer datos personales mediante DTOs publicos existentes.
- Confundir wanted explicito con faltante calculado.
- Convertir edition opcional en requisito que bloquee altas validas.
- Crear duplicados por transiciones OWNED/WANTED no idempotentes.

## 17. Estrategia de tests

Cada EPIC funcional debe ampliar tests de servicio, seguridad/MVC y UI para
propiedad, visibilidad, privacidad, referencia item-edition, legacy, manual,
duplicados y transiciones de estado. Los calculos missing requieren fixtures de
serie ordenada. E2E/Playwright siguen pospuestos hasta un recorrido maduro.

## 18. Plan de EPICs

1. EPIC 44B y 44B-FIX - Contrato backend, pruebas y documentacion de
   collection items. Completadas.
2. EPIC 44C - Flujo frontend de creacion y edicion de colecciones. Completada.
3. EPIC 44D - Alta desde catalogo y seleccion de edition.
4. EPIC 44E - Items manuales y enlace posterior al catalogo.
5. EPIC 44F - OWNED, WANTED y faltantes calculados.
6. EPIC 44G - Detalle final, filtros, ordenacion y progreso.
7. EPIC 44H - Datos demo, validacion integral y cierre parcial MVP4.

## 19. Criterios de cierre MVP4

- Crear y mantener una coleccion real con privacidad correcta.
- Alta catalogada con edition opcional y alta manual enlazable.
- Transicion WANTED a OWNED sin perdida de datos ni duplicado accidental.
- Faltantes calculados por serie, filtros, orden y progreso claros.
- Datos privados ausentes de respuestas publicas y legacy compatible.
- Cobertura backend/frontend de reglas; E2E reservado para fase posterior.

## 20. Decisiones abiertas

- `quantity` frente a ejemplares separados.
- Alcance de imagenes y almacenamiento real.
- Tipo de coleccion y compatibilidad con taxonomia editorial.
- Exclusiones manuales de missing y comportamiento de items archivados.
- Momento de retirar el estado MISSING persistido.

## Estado posterior a EPIC 44C

EPIC 44B implementa la identidad editorial directa, la compatibilidad legacy,
`referenceKind` calculado y la sanitizacion de campos personales en backend.
EPIC 44B-FIX anade pruebas de propiedad, roles no propietarios y serializacion
HTTP, y cierra el contrato documental. EPIC 44C cierra el flujo frontend del
contenedor coleccion con validaciones, propiedad, vaciado explicito de campos
opcionales y listado sin contador no fiable. No implementa items manuales,
transiciones de estado, missing calculado ni alta editorial. La siguiente tarea
es EPIC 44D.
