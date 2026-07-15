# MVP4 OWNED, WANTED and Calculated Missing Design

Fecha: 2026-07-15. Este documento cierra las decisiones de EPIC 44F-A. No
implementa API, persistencia, migraciones ni interfaz.

## 1. Resumen ejecutivo

MVP4 separa posesion, intencion y progreso calculado. `OWNED` representa una
pieza poseida; `WANTED` una intencion explicita; `MISSING` es un resultado de
progreso por serie, no una nueva fila persistida. Cada catalog item activo de
una serie tiene un solo estado visual: `OWNED`, `WANTED` o `MISSING`.

## 2. Estado auditado

`collection_items.collection_status` conserva `OWNED`, `WANTED`, `MISSING`,
`DUPLICATED`, `SELLABLE` y `TRADABLE`. El PUT existente puede actualizarlo.
No hay progreso canonico ni faltantes por serie. `/wanted` es actualmente un
flujo comercial basado en entradas persistidas.

## 3. Definiciones funcionales

- `OWNED`: entrada persistida de una pieza poseida; admite condicion, unidad,
  limites, notas, fecha y edition opcional.
- `WANTED`: entrada persistida que expresa deseo de obtener un catalog item o
  edition. No es un faltante automatico.
- `MISSING`: estado calculado para un catalog item ACTIVE de la serie que no
  esta poseido ni marcado WANTED. Nunca crea `collection_items`.

## 4. Estados legacy y posesion

Para MVP4 cuentan como posesion `OWNED`, `DUPLICATED`, `SELLABLE` y `TRADABLE`.
Todos siguen escribibles por compatibilidad; los tres ultimos expresan
clasificacion comercial futura, pero una copia existe y completa progreso.
`WANTED` no cuenta como posesion y aparece como necesidad. `MISSING` persistido
es compatibilidad legacy: no se crea en flujos nuevos, no se migra ni borra
silenciosamente, no cuenta como WANTED nuevo y se expone con
`legacyStatusWarning`. Sigue alimentando recomendaciones actuales hasta una
transicion explicita posterior.

## 5. Referencias participantes y precedencia

Participan `DIRECT_CATALOG` por `catalogItemId` y `VERIFIED_BRIDGE` por su item
verificado. `LEGACY_UNRESOLVED`, `MANUAL` e `INVALID_REFERENCE` no completan ni
generan faltantes; el ultimo es diagnosticable. Se ignoran filas y colecciones
soft-deleted, y series/items no ACTIVE. La edition es opcional: varias editions
del mismo item no duplican progreso.

Por catalog item gana posesion sobre `WANTED`, y `WANTED` sobre `MISSING`.
Dos posesiones con editions distintas siguen siendo un item poseido y conservan
todos sus ids. `OWNED + MISSING legacy` es OWNED; `WANTED + MISSING legacy` es
WANTED; `DUPLICATED`, `SELLABLE` y `TRADABLE` ganan sobre WANTED. Una referencia
directa gana sobre bridge; un manual no enlazado no participa aunque coincida
su titulo.

## 6. Transicion WANTED a OWNED

44F-B reutilizara `PUT /api/collections/{collectionId}/items/{itemId}` sobre la
misma entrada: es atomico, idempotente y preserva notas, fecha y edition. No
crea duplicado. La validacion de propietario y los errores actuales 400/403/404
se conservan; 409 queda reservado para invariantes de referencia. Una operacion
dedicada no aporta valor en MVP4.

## 7. Faltantes calculados y contrato API

El alcance inicial es coleccion + serie:

`GET /api/collections/{collectionId}/series/{seriesId}/progress`

DTOs propuestos: `CollectionSeriesProgressResponse` y
`CollectionSeriesProgressItemResponse`. Respuesta:

```json
{
  "collectionId": 1, "seriesId": 2, "seriesTitle": "Example",
  "totalCatalogItems": 10, "ownedItems": 4, "wantedItems": 2,
  "missingItems": 4, "completionPercentage": 40,
  "items": [{"catalogItemId": 3, "title": "1", "sequenceLabel": "1",
    "sortOrder": 1, "firstPublicationYear": 2024,
    "calculatedStatus": "OWNED", "ownedCollectionItemIds": [8],
    "wantedCollectionItemIds": [], "selectedEditionIds": [9],
    "legacyStatusWarning": false}]
}
```

`completionPercentage = round(100 * ownedItems / totalCatalogItems)` con 0
para serie sin items ACTIVE. Items ordenados por `sortOrder`, titulo e id.

## 8. Seguridad, privacidad y /wanted

El progreso detallado es owner-only en 44F; ADMIN y EDITORIAL_ADMIN ajenos no
sustituyen propiedad. Nunca contiene `notes` ni `acquiredAt`. Serie o coleccion
inexistente usa el error coherente del contrato actual. Progreso publico queda
fuera de alcance. `/wanted` permanece comercial; necesidades personales y
faltantes viven inicialmente en coleccion/serie, sin convertirlo en marketplace.

## 9. Compatibilidad, rendimiento y casos limite

No se requiere migracion. 44F-B debe usar consultas por serie y coleccion,
evitar N+1, deduplicar en servicio y evaluar indices solo tras medir. Serie sin
items, coleccion sin referencias, todos OWNED, todos WANTED, items archivados,
manuales, legacy sin bridge, bridge verificado, referencias invalidas, dos
editions y usuarios ajenos siguen las reglas anteriores.

## 10. Estrategia de tests y plan

44F-B: DTOs, query/servicio de progreso, PUT de transicion, propiedad y tests
de dominio, servicio y MVC. 44F-C: modelos/servicio frontend, visualizacion y
accion de transicion con i18n y tests. 44F-D: regresion legacy, docs, exports y
datos demo minimos. E2E queda para 44H.

## 11. Criterios y fuera de alcance

Aceptar cuando el resultado es owner-only, determinista, sin filas missing
nuevas, con precedencia cerrada y transicion sin duplicados. Fuera: quantity,
duplicados finales, imagenes, rareza, valor, alertas, marketplace, pagos,
social, filtros/orden final y E2E.

## 12. Decisiones cerradas

1. Los cuatro estados de posesion cuentan para progreso.
2. `MISSING` persistido es legacy compatible y distinto de `MISSING` calculado.
3. Progreso inicial es por coleccion y serie.
4. La transicion reutiliza el PUT generico sobre la misma entrada.
5. No hay migracion ni cambio de `/wanted` en 44F-B.

## Estado de implementacion

EPIC 44F-B implementa el backend de este contrato. El frontend queda pendiente
para EPIC 44F-C.
