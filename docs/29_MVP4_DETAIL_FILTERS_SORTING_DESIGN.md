# MVP4 Final Collection Detail, Filters and Sorting Design

Fecha: 2026-07-31. Este documento cierra las decisiones de EPIC 44G-A. No
implementa API, persistencia, migraciones, rutas ni interfaz.

## 1. Resumen ejecutivo

El detalle final de una coleccion reutilizara `/collections/:collectionId` y
mantendra las tarjetas existentes, sus acciones de propietario y la lectura
publica segura. La lista admitira busqueda, filtros combinables y orden
determinista. El progreso por serie se integrara como resumen owner-only sin
duplicar el calculo canonico de EPIC 44F ni lanzar una peticion por cada serie.

## 2. Estado auditado

- `GET /api/collections/{collectionId}` incluye todos los items ordenados por
  id y `GET /api/collections/{collectionId}/items` vuelve a cargar la misma
  lista. El frontend usa finalmente la segunda respuesta.
- El detalle no tiene busqueda, filtros ni selector de orden. El contador usa
  la longitud de la lista cargada y las tarjetas se muestran en orden de id.
- Se muestran identidad editorial, legacy o manual, estado, condicion, edition,
  publisher, unidad, fecha y notas segun el DTO sanitizado por backend.
- Las acciones de alta, edicion y borrado se muestran solo al propietario. Una
  coleccion PUBLIC es legible por terceros; PRIVATE responde como no encontrada.
- Los enlaces de progreso se deduplican a partir de las series presentes en la
  lista. No muestran cifras y no descubren series que no tengan una referencia
  participante en la coleccion.
- El progreso canonico de 44F ya calcula OWNED, WANTED y MISSING por serie, es
  owner-only y no persiste MISSING calculado.

## 3. Objetivo de producto

El usuario debe poder consultar una biblioteca grande sin perder el contexto de
la coleccion: localizar una entrada, aislar estados o tipos de referencia,
ordenar el resultado y abrir el progreso de sus series. Un lector publico debe
obtener la misma navegacion sobre campos publicos, pero nunca progreso personal,
notas ni fecha de adquisicion.

## 4. Composicion del detalle

La ruta existente se conserva. El detalle tendra cuatro zonas:

1. cabecera y metadatos de la coleccion;
2. resumen de entradas persistidas de la coleccion completa;
3. filtros y orden de la lista;
4. tarjetas de items y, solo para el propietario, resumen de progreso por serie.

El total de la coleccion y los recuentos por estado nunca cambian al filtrar. La
UI mostrara aparte `resultCount` cuando haya criterios activos. MISSING calculado
solo aparece en progreso; un MISSING persistido sigue identificado como legacy y
si se filtra por estado aparece como la fila real que es.

## 5. Contrato de listado de items

Se ampliara de forma aditiva:

`GET /api/collections/{collectionId}/items`

Parametros opcionales:

| Parametro | Valores | Regla |
| --- | --- | --- |
| `q` | texto, maximo 100 | Coincidencia parcial, sin distinguir mayusculas, sobre titulo visible, serie, edition, publisher y metadata legacy/manual visible |
| `status` | valor de `CollectionItemStatus` | Estado persistido exacto; repetible para OR |
| `referenceKind` | valor calculado actual | Exacto; repetible para OR |
| `seriesId` | id positivo | Serie editorial exacta; manual y legacy sin bridge no coinciden |
| `sort` | enum cerrado | Un solo criterio; valor invalido responde 400 |

Valores de `sort`:

- `CATALOG_ORDER`: predeterminado; titulo de serie, `sortOrder` del catalog item,
  titulo visible e id. Las entradas sin serie quedan despues y usan titulo e id.
- `TITLE_ASC`: titulo visible e id.
- `TITLE_DESC`: titulo visible descendente e id.
- `STATUS_ASC`: orden funcional OWNED, WANTED, MISSING, DUPLICATED, SELLABLE,
  TRADABLE; despues `CATALOG_ORDER`.
- `NEWEST_ENTRY`: id descendente como aproximacion estable al alta; no promete
  una fecha que el contrato actual no expone.

Los filtros se combinan con AND entre grupos y OR dentro de parametros
repetidos. Valores vacios se ignoran despues de normalizar; ids o enums invalidos
responden 400. La respuesta sigue siendo `CollectionItemResponse[]`, sin romper
clientes existentes. No se introduce paginacion en MVP4: primero se medira el
volumen real y se evita cambiar el contrato a `Page` en esta fase.

## 6. Busqueda, identidad y compatibilidad

El titulo visible mantiene la precedencia actual: catalog item, manual y master
product. `referenceKind` sigue siendo calculado y no se persiste. La busqueda y
el orden no realizan backfill, no crean bridges y no reinterpretan coincidencias
manuales por titulo. DIRECT_CATALOG, VERIFIED_BRIDGE, LEGACY_UNRESOLVED, MANUAL
e INVALID_REFERENCE siguen siendo distinguibles.

Las comparaciones textuales deben ser deterministas y coherentes con PostgreSQL.
La implementacion inicial puede resolver filtros y orden en servicio sobre una
consulta acotada a la coleccion, siempre evitando nuevas consultas por item. No
se anadiran indices ni una migracion sin evidencia de rendimiento.

## 7. Resumen de items

El frontend derivara del snapshot completo de `GET /api/collections/{id}`:

- total de entradas persistidas;
- recuentos OWNED y WANTED;
- recuento de otros estados persistidos;
- aviso separado si existen filas MISSING legacy.

Estos numeros no sustituyen el progreso editorial. Tras altas, ediciones o
borrados, el detalle recarga el snapshot y la lista para no conservar contadores
obsoletos. Un fallo de la lista no borra el resumen ya cargado y ofrece reintento.

## 8. Resumen de progreso por series

Se anadira un endpoint owner-only:

`GET /api/collections/{collectionId}/series-progress`

La respuesta sera una lista de `CollectionSeriesProgressSummaryResponse` con:

```json
{
  "seriesId": 2,
  "seriesTitle": "Example",
  "totalCatalogItems": 10,
  "ownedItems": 4,
  "wantedItems": 2,
  "missingItems": 4,
  "completionPercentage": 40
}
```

Incluye una fila por serie ACTIVE alcanzada mediante una referencia participante
DIRECT_CATALOG o VERIFIED_BRIDGE de la coleccion. Reutiliza exactamente las
reglas de precedencia, visibilidad, posesion y redondeo de 44F. Se ordena por
titulo de serie e id. La consulta/servicio debe calcularlo en lote y evitar N+1;
el detalle completo de una serie continua en el endpoint y ruta existentes.

No se muestra a lectores publicos y ADMIN o EDITORIAL_ADMIN ajenos no sustituyen
propiedad. No contiene ids de entradas, edition, notas, condicion ni fechas.

## 9. Comportamiento frontend

- Los filtros se aplican con boton explicito y se pueden limpiar en una accion.
- El estado de filtros y sort se refleja en query params de la ruta para poder
  recargar o compartir la vista; no se incluyen valores por defecto.
- Cambiar idioma no altera valores de enum ni el orden recibido del backend.
- Loading inicial, error de metadatos, error de lista y error de progreso son
  estados independientes. Un fallo de progreso no oculta la coleccion.
- Un resultado filtrado vacio se distingue de una coleccion sin items.
- Tras borrar, se recargan lista y resumen; no se resta manualmente un contador
  cuya clasificacion pueda haber cambiado.
- Los controles y tarjetas mantienen teclado, labels accesibles, foco visible y
  disposicion responsive. Se conservan `data-testid` existentes.

## 10. Seguridad y privacidad

El backend aplica propiedad/visibilidad antes de filtrar. Los parametros nunca
permiten inferir una coleccion PRIVATE. La respuesta publica conserva la
sanitizacion actual de `notes` y `acquiredAt`; busqueda y orden no pueden usar
esos campos porque revelarian informacion por inclusion o posicion. Condicion y
unidad mantienen su visibilidad contractual actual y no se amplian en 44G.

El resumen de progreso es estrictamente owner-only. No se cachean respuestas de
un propietario para otro usuario y los mensajes de error siguen el contrato
actual de 400/403/404 sin revelar existencia privada.

## 11. Estrategia de implementacion

1. **EPIC 44G-B - Backend de listado final y resumen de progreso.** Parametros,
   enum de sort, filtrado/orden, endpoint agregado, reutilizacion del calculo de
   44F y tests de servicio/MVC/privacidad.
2. **EPIC 44G-C - Detalle final frontend.** Resumen, filtros, query params,
   orden, progreso agregado, estados independientes, i18n y tests Angular.
3. **EPIC 44G-D - Regresion y cierre.** Casos legacy/manual/publicos, exports si
   los contratos cambiaron, documentacion y validacion integral sin E2E.

## 12. Estrategia de tests

- Backend: combinacion de filtros, parametros repetidos, normalizacion de `q`,
  cada sort y desempates, soft delete, referencias manual/legacy/bridge/directa,
  lista publica sanitizada, PRIVATE oculta y enums/ids invalidos.
- Progreso agregado: precedencia OWNED/WANTED/MISSING, estados de posesion,
  varias editions, serie archivada, coleccion sin series, orden estable,
  owner-only y ausencia de datos personales.
- Frontend: hidratacion desde query params, aplicar/limpiar, resultado vacio,
  recuentos no afectados por filtros, retry independiente, render publico sin
  progreso, acciones owner-only y navegacion al progreso detallado.
- Regresion: crear, editar, enlazar y borrar items; transicion WANTED a OWNED;
  privacidad de notas/fecha; `/wanted` sin cambios.

## 13. Criterios de aceptacion

- Contrato aditivo y compatible con clientes sin parametros.
- Filtros combinables y orden total determinista.
- Resumen de coleccion distingue filas persistidas de MISSING calculado.
- Progreso agregado reutiliza 44F, evita N+1 y solo lo ve el propietario.
- Lectura publica no filtra ni ordena por datos privados.
- Ninguna migracion, dependencia, nueva ruta Angular, cambio de `/wanted`, E2E
  o Playwright forma parte de 44G-B/44G-C salvo nueva decision documentada.

## 14. Fuera de alcance

Paginacion, busqueda global entre colecciones, filtros por precio o tiendas,
imagenes nuevas, cantidad/copias, tags, favoritos, rareza, valor, compartir
filtros privados, marketplace, alertas, social, cambios de recomendaciones y
eliminacion o migracion automatica de MISSING legacy.

## 15. Decisiones cerradas

1. Se conserva la ruta de detalle y el DTO de item.
2. El listado existente recibe filtros y sort opcionales; no nace otro endpoint.
3. `CATALOG_ORDER` es el orden predeterminado y todos los sorts desempatan por id.
4. No hay paginacion ni migracion en MVP4.
5. El resumen de items usa filas persistidas; MISSING calculado vive en progreso.
6. El progreso por series se agrega en backend y es owner-only.
7. `/wanted` permanece fuera de 44G.

## Estado de implementacion

44G-A diseno cerrado. La siguiente implementacion es EPIC 44G-B.
