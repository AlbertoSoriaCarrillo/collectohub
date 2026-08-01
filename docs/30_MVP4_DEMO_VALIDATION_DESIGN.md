# MVP4 Demo, Integral Validation and Partial Closure Design

Fecha: 2026-07-31

## 1. Objetivo de producto

EPIC 44H debe demostrar de forma reproducible que una persona puede gestionar
una coleccion sobre el catalogo editorial, combinar referencias directas,
manuales y legacy, distinguir OWNED/WANTED/MISSING y compartir solo los datos
publicos permitidos. El resultado es un cierre parcial tecnico de MVP4, no una
declaracion de produccion ni el cierre del producto completo.

## 2. Estado auditado

El repositorio contiene tres scripts demo:

| Script | Estado | Cobertura | Deuda |
| --- | --- | --- | --- |
| `create-demo-data.ps1` | Legacy util | Tienda, productos maestros, inventario, coleccion, WANTED/OWNED, recomendacion y reserva | No es MVP4 integral, no soporta WhatIf y guarda passwords demo en un resumen local ignorado |
| `create-editorial-admin-demo-data.ps1` | Reutilizable parcial | Publisher, franchise, serie de tres items, edition, creator, credit, relationship y link opcional | Requiere ADMIN existente; su get-or-create es parcial y no valida todo el estado reutilizado |
| `create-mvp4-progress-demo-data.ps1` | Demo 44F | Coleccion publica con 1 OWNED, 1 WANTED, 1 MISSING calculado y 33% | Un conflicto de usuario exige otro sufijo; no cubre manual, legacy, bridge, privacidad ni filtros finales |

Los resumenes `.last-*.json` estan ignorados por Git. En el arbol local existe
`.last-demo-data.json`; no se usa como evidencia versionada ni se leen sus
credenciales. No existen usuarios de aplicacion preinsertados garantizados.
Liquibase prepara roles y datos estructurales, pero ADMIN/EDITORIAL_ADMIN deben
existir o provisionarse de forma controlada. Compose levanta PostgreSQL, backend
y frontend, pero no crea cuentas demo.

Credenciales documentadas actuales:

- `admin@collectohub.local` / `admin123` son defaults locales de scripts, no una
  cuenta garantizada ni credenciales aceptables fuera de local.
- `Demo1234!` se usa para usuarios demo con dominios `.local`.
- `EDITORIAL_ADMIN` solo se concede a un usuario existente mediante
  `scripts/admin/manage-editorial-admin.ps1`; la operacion es transaccional e
  idempotente y exige `psql`.

## 3. Alcance y fuera de alcance

44H cubre datos demo idempotentes, validacion API/DB/frontend minima, evidencia
reproducible y revision de cierre parcial. No incluye E2E, Playwright, nuevas
funciones, migraciones, dependencias, cambios de contratos, social, marketplace,
pagos, imagenes, uploads, quantity, paginacion ni limpieza destructiva.

## 4. Prerequisitos

- PowerShell 5.1+.
- Java 25, Node/npm y PostgreSQL compatibles con la documentacion local, o
  Docker Desktop/Engine con Compose v2.
- Backend healthy en `/api/health` y frontend accesible cuando se valide UI.
- Base con Liquibase aplicado.
- Cuenta local ADMIN o EDITORIAL_ADMIN valida para crear catalogo; para crear
  master products y verificar bridges se usara ADMIN.
- Passwords recibidos mediante `SecureString`, variable de entorno local o
  parametro consciente; nunca escritos en resumenes o logs.

## 5. Personas demo y permisos

| Persona | Identidad segura | Roles | Uso |
| --- | --- | --- | --- |
| Operador editorial | Cuenta local proporcionada, no creada silenciosamente | ADMIN; EDITORIAL_ADMIN solo no basta para operaciones globales | Catalogo, master product y bridge |
| Propietario | `demo.mvp4.owner.<scenario>@collectohub.local` | USER | Colecciones, items, datos privados y progreso |
| Lector autenticado | `demo.mvp4.reader.<scenario>@collectohub.local` | USER | Comprobar lectura publica y denegacion privada/owner-only |
| Visitante | Sin cuenta | Ninguno | Catalogo y coleccion publica sanitizada |

Todos los nombres incluyen `Demo`, el escenario y dominio `.local`. No se usan
nombres, emails, direcciones o notas que puedan confundirse con datos reales.

## 6. Dataset exacto esperado

Clave estable: `mvp4-<scenario>`. El escenario debe poder indicarse; el valor por
defecto puede ser `local`, no un timestamp obligatorio.

Catalogo editorial:

- publisher `CollectoHub Demo Editorial <scenario>`;
- franchise `CollectoHub Demo Saga <scenario>`;
- serie ACTIVE `CollectoHub MVP4 Series <scenario>`;
- item 1 `Demo Volume 1 <scenario>`, orden 1, con edition paperback ES;
- item 2 `Demo Volume 2 <scenario>`, orden 2, sin edition obligatoria;
- item 3 `Demo Volume 3 <scenario>`, orden 3, sin fila de coleccion;
- creator y credit AUTHOR sobre item 1;
- relationship SEQUEL de item 1 a item 2.

Compatibilidad legacy:

- master product `Demo Verified Bridge <scenario>` enlazado y verificado con
  item 1 y su edition;
- master product `Demo Legacy Unresolved <scenario>` sin bridge;
- no ejecutar backfill global ni crear coincidencias ambiguas.

Colecciones:

- publica `MVP4 Demo Public <scenario>` con descripcion inequivocamente demo;
- privada `MVP4 Demo Private <scenario>` para comprobar ocultacion.

## 7. Matriz de coleccion e items

| Caso | Coleccion | Identidad | Estado | Datos personales | Resultado esperado |
| --- | --- | --- | --- | --- | --- |
| D1 | Publica | catalog item 1 + edition | OWNED | GOOD, notes y acquiredAt demo | DIRECT_CATALOG; privados solo owner |
| D2 | Publica | catalog item 2 sin edition | WANTED | notes demo | DIRECT_CATALOG; accion WANTED disponible |
| D3 | Publica | item 3 sin fila | Calculado MISSING | Ninguno | Aparece solo en progreso; no existe `collection_items` |
| M1 | Publica | manual sin enlace | OWNED | title/type/description y notes demo | MANUAL; no participa en progreso |
| B1 | Publica | master product con bridge VERIFIED | DUPLICATED | unitNumber demo | VERIFIED_BRIDGE; participa como posesion de item 1 sin duplicar progreso |
| L1 | Publica | master product sin bridge | WANTED | Sin datos sensibles | LEGACY_UNRESOLVED; no participa en progreso editorial |
| P1 | Privada | manual | OWNED | notes/acquiredAt centinela | Owner 200; lector y anonimo 404 |

El resumen persistido de la coleccion publica cuenta filas reales. El progreso
de la serie mantiene 3 items y precedencia de posesion sobre WANTED/MISSING. La
validacion debe registrar el resultado real; no fijara 33% si B1 cambia la
precedencia sin recalcular la expectativa a partir de la matriz.

## 8. Contratos y recorridos a validar

Backend/API:

1. health, register/login y `/api/users/me`;
2. catalogo editorial publico y detalles de serie/item/edition;
3. CRUD de coleccion owner-only y lectura PUBLIC/PRIVATE;
4. POST/GET/PUT/DELETE de collection items;
5. enlace manual por `catalog-reference`;
6. filtros `q`, `status`, `referenceKind`, `seriesId` y los cinco sorts;
7. progreso agregado `/series-progress` y detalle `/series/{seriesId}/progress`;
8. transicion WANTED a OWNED sobre la misma fila, preservando datos;
9. creacion/verificacion de bridge con permisos ADMIN.

Frontend manual minimo:

- login del propietario y apertura de colecciones;
- resumen persistido, filtros, orden, estados vacio/loading/error/retry;
- cards directas, manuales, bridge y legacy;
- progreso agregado y detalle 1/serie;
- transicion WANTED a OWNED y recarga canonica;
- lector publico sin notes/acquiredAt ni acciones owner;
- coleccion privada no visible para lector;
- `/wanted` solo como regresion visual, sin cambiar su contrato.

## 9. Privacidad y seguridad

- Los resumenes guardan IDs, emails `.local`, URLs, estados y timestamps; nunca
  passwords, access/refresh tokens, headers, hashes, PGPASSWORD o JWT secrets.
- Notes y acquiredAt usan valores centinela (`OWNER_PRIVATE_*`) y deben estar
  presentes para owner y ausentes para lector/anonimo.
- ADMIN y EDITORIAL_ADMIN ajenos no sustituyen propiedad de coleccion.
- Una PRIVATE ajena debe responder 404; progreso ajeno debe responder 403/404
  segun el contrato verificado, sin inferir datos privados.

## 10. Estrategia idempotente de scripts

44H-B debe crear un orquestador unico con `SupportsShouldProcess` y `-WhatIf`.
Cada recurso tendra una clave determinista por escenario y flujo
`find -> validate -> create only if absent`. Si existe con datos incompatibles,
el script falla con diagnostico y no lo reescribe silenciosamente.

- Catalogo: reutilizar busquedas exactas y validar padre, estado y campos clave.
- Usuarios: registrar si faltan; ante 409, iniciar sesion solo con la credencial
  demo proporcionada y validar email/rol. Nunca apropiarse de otra cuenta.
- Colecciones/items: descubrir por nombre e identidad exacta, reutilizar una
  sola fila compatible y rechazar duplicados ambiguos.
- Bridges: buscar por master product/item, crear PROPOSED y verificar solo el
  link exacto del escenario.
- Reanudacion: escribir un resumen local atomico sin secretos despues de cada
  fase validada. No usarlo como unica fuente de verdad; confirmar siempre API.

`-WhatIf` no llama HTTP, no ejecuta `psql` y no crea archivos. Un segundo run
real con la misma clave debe terminar sin duplicar recursos y producir la misma
matriz logica.

## 11. Servicios no disponibles

El script realiza preflight en orden: health, login editorial, PostgreSQL solo
si se solicita validacion DB y frontend solo si se solicita validacion UI. Si
falla un requisito, sale distinto de cero, indica la fase exacta y no afirma
validaciones posteriores. Parser y WhatIf pueden ejecutarse offline y deben
registrarse como tales, nunca como validacion integral.

## 12. Validaciones automaticas

Backend:

- `mvnw.cmd clean verify`, con tests/fallos/errores/omitidos reales;
- ejecucion API del orquestador dos veces para probar idempotencia;
- asserts de referenceKind, edition, estados, filtros, sorts, privacidad,
  propiedad, transicion e IDs de progreso;
- ausencia de POST MISSING y ausencia de fila para D3.

Frontend:

- `npm.cmd ci`, tests y build, registrando cifras y warnings reales;
- smoke HTTP de `/` y rutas solo si el frontend esta activo;
- comprobacion manual acotada sin E2E/Playwright.

Base de datos (solo lectura):

- consultas `SELECT` por IDs del resumen para confirmar usuarios/roles,
  visibilidad, identidades directa/manual/legacy, bridge VERIFIED, soft delete y
  ausencia de fila MISSING calculada;
- no imprimir password_hash, refresh tokens, notes ni acquired_at;
- no INSERT/UPDATE/DELETE, truncate, drop, reset ni borrado de volumen.

## 13. Evidencias e informe

Resumen local ignorado: `scripts/demo/.last-mvp4-integral-demo-data.json`.
Informe versionado de 44H-C: `docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md`.

Cada evidencia incluye fecha, commit, perfil, servicios disponibles, comando
sanitizado, resultado, conteos, warnings, IDs no secretos y estado
`PASS`, `FAIL`, `SKIPPED_WITH_REASON` o `NOT_RUN`. Nunca se copia una cifra
historica como si se hubiera ejecutado en la sesion actual.

## 14. Criterios de aceptacion

- Dataset completo y reanudable sin duplicados en dos ejecuciones.
- Casos D1-D3, M1, B1, L1 y P1 verificados por API.
- MISSING es calculado y no persistido en nuevas altas.
- Privacidad owner/public/private y permisos owner-only demostrados.
- Filtros, sorts, resumen, progreso y transicion verificados.
- Maven, frontend tests y build verdes, o bloqueo externo registrado sin falsear.
- Parser y WhatIf verdes; resumen sin secretos.
- Informe final distingue automatizado, manual, omitido y fuera de alcance.

`MVP4_PARTIALLY_CLOSED` significa que los recorridos MVP4 implementados estan
demostrados en local con compatibilidad y privacidad, pero permanecen fuera del
cierre: E2E, produccion, quantity/copias consolidadas, imagenes, paginacion,
social, marketplace, pagos y decisiones abiertas de `docs/26`.

## 15. Archivos permitidos y prohibidos

44H-B puede modificar `scripts/demo/`, `.gitignore` y documentacion directamente
relacionada. 44H-C puede crear el informe y actualizar estado/handoff. Backend,
frontend, migraciones, manifests y exports quedan prohibidos salvo que una
validacion demuestre un defecto real; ese defecto se aisla en una EPIC FIX y no
se corrige de forma oportunista dentro de 44H.

## 16. Rollback no destructivo

No hay rollback automatico por borrado. Ante fallo, conservar datos y resumen,
corregir la causa y reanudar con la misma clave. Si el escenario queda
incompatible, marcarlo abandonado y usar otra clave. Cualquier limpieza futura
debe enumerar IDs exactos, requerir confirmacion y ser una tarea separada. Nunca
usar `docker compose down -v`, prune, truncate o borrado global como rollback.

## 17. Division de EPIC 44H

1. **44H-A - Auditoria y diseno.** Este documento y estado operativo.
2. **44H-B - Datos demo y scripts idempotentes.** Implementada en
   `scripts/demo/create-mvp4-integral-demo-data.ps1`: orquestador, dataset
   completo, WhatIf, reanudacion, resumen seguro y prueba offline de
   parser/idempotencia/fallo seguro.
3. **44H-C - Validacion integral y cierre parcial.** Completada sobre el
   escenario `44hc3`: doble ejecucion real, API/DB, recorrido UI humano,
   regresion backend/frontend, rotacion final local y revision
   `docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md`.

Resultado de 44H-C: `MVP4_PARTIALLY_CLOSED`. Este estado demuestra los
recorridos implementados sin cerrar MVP4 como producto ni incorporar los
elementos expresamente fuera de alcance de la seccion 14.
