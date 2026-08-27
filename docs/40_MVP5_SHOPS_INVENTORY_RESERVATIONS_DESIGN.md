# MVP5 Shops, Inventory and Reservations Design

Fecha de auditoria: 2026-08-02
EPIC: 45A
Base auditada: `origin/dev` en
`522ce0b99812ba635e6f3bedefd21e3c11249463`

## 1. Resultado ejecutivo

CollectoHub ya contiene una base tecnica operativa de tiendas, miembros,
inventario, matching y reservas, pero todavia no constituye un flujo
profesional mantenible. La base actual permite crear y editar una tienda,
publicar inventario legacy o editorial, descubrir ofertas y registrar
solicitudes de reserva. Conserva, sin embargo, contratos compartidos entre
lectura publica e interna, reglas de stock no transaccionales, expiracion solo
modelada, ausencia de idempotencia y una incompatibilidad entre reservas e
inventario editorial puro.

MVP5 debe evolucionar esa base de forma aditiva. `catalogItemId` sera la
identidad editorial principal de las nuevas ofertas catalogadas;
`catalogItemEditionId` seguira siendo opcional y pertenecera al item;
`masterProductId` se conservara para datos y clientes legacy. La operacion
comercial termina en una reserva sin pago, pedido, envio ni marketplace.

## 2. Alcance de EPIC 45A

Esta EPIC es exclusivamente documental. Audita codigo, esquema, seguridad,
pruebas y documentacion; fija contratos objetivo y divide MVP5 en EPICs
ejecutables. No cambia comportamiento, API, base de datos, frontend, scripts,
dependencias ni workflows.

Fuera de MVP5 y de este diseno:

- pagos, carrito, pedidos, facturacion, envios, devoluciones y comisiones;
- marketplace abierto, comparador, alertas y mensajeria;
- social, movil, PWA avanzada y produccion;
- geolocalizacion, almacenes multiples y conciliacion de stock externa;
- busqueda publica global de ofertas o contacto directo fuera de la reserva;
- E2E/Playwright en EPIC 45A.

## 3. Fuentes auditadas

- dominios y contratos: `docs/01_ROADMAP.md`, `docs/03_PRODUCT_DOMAINS.md`,
  `docs/05_USER_SHOP_MATCHING.md`, `docs/13_DECISIONS.md` y
  `docs/16_MVP_API_ENDPOINTS.md`;
- esquema: changelogs `001`, `004` y `009`, mas `docs/export/`;
- backend: modulos `shops`, `inventory`, `reservations`, configuracion de
  seguridad y sus pruebas de servicio/MVC;
- frontend: rutas, modelos, servicios y componentes de tiendas, inventario y
  reservas, junto con sus pruebas;
- politica: `AGENTS.md` y `docs/32_QUALITY_GATES.md`.

La auditoria es estatica. Las cifras de pruebas siguientes describen casos
versionados encontrados; no son resultados de ejecucion.

## 4. Clasificacion de capacidades actuales

| Area | Capacidad | Estado | Evidencia y limite |
| --- | --- | --- | --- |
| Tiendas | Crear tienda y OWNER local | Vigente | `ShopService` crea `shops`, `shop_members.OWNER` y asigna rol global `SHOP_OWNER` si falta. |
| Tiendas | Listar tiendas asociadas | Vigente | `GET /api/shops/my`; solo memberships activas. |
| Tiendas | Detalle publico | Vigente con riesgo | `GET /api/shops/{id}` comparte `ShopResponse`; expone contacto de negocio y regla de expiracion sin DTO publico separado. |
| Tiendas | Editar perfil | Backend vigente, UX incompleta | OWNER/MANAGER pueden usar `PUT`; `ShopService.updateShop()` no tiene pantalla dedicada. |
| Miembros | OWNER/MANAGER/EMPLOYEE | Esquema vigente | La pertenencia autoriza por tienda; no existe API ni UI para listar o administrar miembros. |
| Inventario | Alta/edicion legacy | Vigente | `masterProductId` sigue admitido. |
| Inventario | Alta/edicion editorial | Vigente parcial | Item puro, item+edicion y puente verificado existen; filtros publicos legacy no cubren identidad editorial. |
| Inventario | Lectura interna | Vigente | Cualquier miembro activo ve el inventario completo. |
| Inventario | Lectura publica | Vigente con riesgo | Mismo `ShopProductResponse` interno/publico; incluye `notes` y stock fisico. |
| Matching | Coincidencias con stock | Vigente parcial | Usa item, edicion o master legacy y exige stock mayor que cero; queda fuera del recorrido visible principal. |
| Reservas | Crear/listar/detalle/transiciones | Vigente legacy | Solo se ha cubierto con `masterProduct`; no bloquea ni reduce stock. |
| Reservas | Expiracion | Incompleta | Se calcula `expiresAt` y existe `EXPIRED`, pero no hay reconciliacion ni job. |
| Reservas | Idempotencia/concurrencia | Futura | No hay clave idempotente, version optimista, lock de fila ni constraint que evite sobre-reserva. |
| Metricas | Demanda basica de tienda | Futura | No hay endpoint ni UI. |

## 5. Mapa backend, frontend, esquema y pruebas

### 5.1 Tiendas y miembros

- Esquema: `shops` contiene propietario, perfil, contacto, moneda, expiracion y
  estado. `shop_members` contiene rol y estado, con unicidad `(shop_id,
  user_id)`. No hay constraints SQL sobre los valores de rol/estado ni una
  garantia de un unico OWNER activo.
- Backend: cuatro endpoints de tienda. La autorizacion real de update usa
  membership activa OWNER/MANAGER; el rol global `SHOP_OWNER` no concede acceso
  a una tienda concreta.
- Frontend: `/shops`, `/shops/new` y `/shops/:id`; el detalle calcula la
  capacidad de gestion mediante una segunda llamada a `/api/shops/my`. No hay
  pantalla de edicion o miembros y `ShopDetailComponent` no tiene spec.
- Pruebas versionadas: seis casos de servicio y siete casos MVC, centrados en
  creacion, asignacion de rol, listado y update autorizado/no autorizado. En
  frontend hay dos casos de servicio y dos casos de componentes.

### 5.2 Inventario

- Esquema: `shop_products` conserva `master_product_id` nullable y admite
  `catalog_item_id`/`catalog_item_edition_id`. Las constraints exigen master o
  item, y una edicion exige item. No existe version de concurrencia.
- Backend: OWNER/MANAGER crean y editan; cualquier miembro activo lista el
  inventario interno; lectura publica exige `visible=true`, estado `AVAILABLE`
  y tienda activa. La lista publica no exige stock positivo. Los filtros de
  categoria, nombre, franquicia y coleccion navegan por `masterProduct`, por lo
  que no representan de forma completa ofertas editoriales puras.
- DTO: `ShopProductResponse` mezcla metadatos legacy/editoriales con precio,
  stock, visibilidad, numero de unidad y `notes`; se reutiliza en endpoints
  publicos e internos.
- Frontend: gestion interna, alta/edicion legacy/editorial, detalle publico y
  reserva. La seleccion editorial usa la fachada existente. No hay paginacion,
  orden configurable ni control de cambios concurrentes.
- Pruebas versionadas: 21 casos de servicio y 12 MVC; 6 casos HTTP y 13 de
  componentes. Cubren referencias editoriales y autorizacion, pero no lock,
  idempotencia, filtros editoriales completos ni sanitizacion por DTO separado.

### 5.3 Reservas

- Esquema: `reservations` guarda usuario, tienda, producto, cantidad, estado,
  mensajes y timestamps. Solo hay check positivo de cantidad; no hay clave
  idempotente, version, constraint de hold ni relacion con stock disponible.
- Backend: el usuario crea y cancela; OWNER/MANAGER leen y transicionan. Se
  valida stock en una lectura no bloqueante y nunca se descuenta ni reserva.
  `expiresAt` se calcula, pero no cambia el estado automaticamente.
- Incompatibilidad editorial: `ReservationService` exige
  `shopProduct.getMasterProduct().isActive()` y `ReservationResponse` accede a
  `masterProduct.getId()/getName()`. Una oferta editorial pura valida en
  inventario puede fallar al reservarse o proyectarse.
- Frontend: recorridos de usuario y tienda existen, con filtros numericos y
  acciones basicas. El modelo exige `masterProductId` y `productName` legacy.
- Pruebas versionadas: 22 casos de servicio y 14 MVC; 6 casos HTTP y 5 de
  componentes. Todos los fixtures de reserva usan `MasterProduct`; no cubren
  identidad editorial pura, concurrencia, reintento idempotente o expiracion.

### 5.4 Persistencia e integracion

Los modulos auditados no tienen pruebas dedicadas de repositorio o
Testcontainers/PostgreSQL. Las pruebas MVC mockean la capa de servicio y las de
servicio mockean repositorios. La suite global puede validar el contexto y las
migraciones, pero no demuestra las futuras garantias transaccionales de stock,
unicidad idempotente o carreras de estado.

## 6. Hallazgos que ordenan MVP5

1. `P0` de compatibilidad: una reserva no soporta una oferta editorial pura.
2. `P0` de privacidad contractual: lectura publica e interna comparten
   `ShopProductResponse`; `notes` no debe publicarse por defecto.
3. `P0` de consistencia: dos creaciones concurrentes pueden aceptar mas
   cantidad que el stock existente.
4. `P1` de expiracion: el timestamp no libera capacidad ni actualiza estado.
5. `P1` de idempotencia: un retry de POST puede crear reservas duplicadas.
6. `P1` de permisos: el esquema de miembros existe sin administracion y el
   rol EMPLOYEE queda reducido a lectura interna de inventario.
7. `P1` de producto: no existe edicion de perfil, miembros, filtros
   profesionales ni metricas en UI.
8. `P1` de pruebas: faltan persistencia real, concurrencia, privacidad de DTO,
   flujos de error/retry y cobertura de `ShopDetailComponent`.

Los P0 son prerrequisitos de cualquier recorrido MVP5 de reservas. No se debe
presentar el flujo como cerrado hasta corregirlos y probarlos.

## 7. Identidad editorial y compatibilidad legacy

Reglas objetivo:

1. Toda nueva oferta catalogada usa `catalogItemId` como identidad principal.
2. `catalogItemEditionId` es opcional y debe pertenecer al item.
3. `masterProductId` permanece nullable para compatibilidad. Un puente
   `VERIFIED` puede enriquecer una oferta legacy, pero no sustituye una
   seleccion editorial explicita ni permite contradicciones.
4. Una oferta editorial pura es valida en inventario, detalle, matching y
   reserva. Ningun contrato de reserva puede asumir master legacy presente.
5. La proyeccion de producto de una reserva incluye IDs y etiquetas editoriales
   opcionales, mas fallback legacy. El nombre visible usa el primer valor no
   vacio en este orden: nombre de edicion, titulo del item y nombre del master
   legacy.
6. No se introduce inventario libre sin identidad catalogada en MVP5.
7. Filas legacy permanecen legibles/editables. No hay backfill automatico salvo
   puente `VERIFIED`; las propuestas no verificadas nunca se aplican.

## 8. Propiedad, roles y permisos

El rol global `SHOP_OWNER` solo habilita UX/capacidad general. Toda autorizacion
de una tienda se resuelve con membership activa y `shopId`; `ADMIN` no sustituye
propiedad salvo un endpoint administrativo futuro expresamente separado.

El rol canonico de dominio, persistencia y API es `EMPLOYEE`, como define
`ShopMemberRole`. En los artefactos versionados auditados, `STAFF` es un nombre
legacy presente solo en documentacion, exports y claves de traduccion; no
representa un cuarto rol ni existe evidencia versionada de filas persistidas con
ese valor. 45C-F debe inspeccionar evidencia de datos reales antes de decidir si
hace falta una migracion. Si existe, debe mapear `STAFF` a `EMPLOYEE` de forma
aditiva e idempotente y demostrar que conserva usuario, tienda, estado y
permisos. `docs/16_MVP_API_ENDPOINTS.md` y el export Markdown usan ya
`EMPLOYEE`, pero `docs/export/database-tables.csv` y
`docs/22_PORTFOLIO_REVIEW.md` conservan referencias legacy pendientes de
reconciliacion en 45C-F. 45D reconciliara las claves de traduccion frontend sin
mezclar ese trabajo en 45C.

| Accion | OWNER | MANAGER | EMPLOYEE | Usuario autenticado | Visitante |
| --- | --- | --- | --- | --- | --- |
| Ver perfil e inventario publicos | Si | Si | Si | Si | Si |
| Editar perfil | Si | Si | No | No | No |
| Listar miembros | Si | Si, solo lectura | No | No | No |
| Alta/cambio/baja de miembros | Si | No | No | No | No |
| Transferir ownership | No en MVP5 | No | No | No | No |
| Leer inventario interno | Si | Si | Si | No | No |
| Crear/editar inventario | Si | Si | No | No | No |
| Ver/gestionar reservas de tienda | Si | Si | No | No | No |
| Crear/cancelar reserva propia | Si | Si | Si | Si | No |
| Ver reservas ajenas | Solo de su tienda | Solo de su tienda | No | No | No |
| Ver metricas agregadas | Si | Si | No | No | No |

Gestion minima de miembros:

- solo OWNER agrega, cambia rol o desactiva;
- se referencia una cuenta existente por email exacto normalizado, sin endpoint
  de busqueda de usuarios;
- la respuesta de error no permite enumeracion publica y toda operacion queda
  auditada;
- no se puede eliminar/desactivar al ultimo OWNER ni autoasignar OWNER;
- alta repetida devuelve el mismo estado o conflicto estable, nunca duplica la
  fila;
- invitaciones por email, transferencia de ownership y administracion global
  quedan fuera de MVP5.

## 9. Privacidad y contratos de respuesta

Se separan proyecciones publicas, internas y de reserva:

- `PublicShopResponse`: perfil comercial y contacto de negocio introducido
  expresamente como publico; nunca owner ID ni membership.
- `ManagedShopResponse`: perfil, membership actual y campos de gestion.
- `PublicShopProductResponse`: identidad editorial/legacy publica, precio,
  condicion y estado visible; nunca `notes`, campos de auditoria ni
  `stockQuantity` fisico interno. Durante 45B-FIX tampoco expone
  `availableQuantity`, porque su semantica depende de holds aun no implementados.
- El contrato gestionado vigente conserva `stockQuantity`, visibilidad y
  `notes`; cantidades reservada y disponible se incorporaran con la contabilidad
  transaccional correspondiente.
- `ReservationResponse`: para el usuario, solo su reserva; para la tienda,
  display name consentido y mensaje de la reserva. No se expone email, telefono,
  colecciones privadas ni un buscador de usuarios.

El contacto de tienda se etiqueta en UI como informacion comercial publica.
Campos existentes se conservan para clientes legacy durante una transicion
aditiva; los endpoints nuevos o versionados usan las proyecciones separadas.

## 10. Regla de stock y reserva sin pagos

`stockQuantity` sigue representando existencias fisicas declaradas por la
tienda. La disponibilidad se calcula:

```text
availableQuantity = stockQuantity - activeHeldQuantity
```

Una reserva mantiene cantidad cuando esta `PENDING` o `ACCEPTED` y
`expiresAt > now`. `REJECTED`, `CANCELLED`, `EXPIRED` y `COMPLETED` no retienen
stock. Al pasar a `COMPLETED`, la cantidad se descuenta una sola vez de
`stockQuantity`; la reserva pasa a terminal y deja de contar como hold en la
misma transaccion.

Estas reglas solo se aplican a reservas creadas con la contabilidad MVP5. 45G
debe introducir un marcador persistente no nulo con dos valores:
`LEGACY_REQUEST` y `MVP5_HOLD`. La migracion asigna `LEGACY_REQUEST` a todas las
filas preexistentes, sin backfill de holds ni cambio de estado, cantidad,
expiracion o stock; las nuevas reservas creadas por el flujo MVP5 se guardan
explicitamente como `MVP5_HOLD`. Una `LEGACY_REQUEST`, incluso `PENDING` o
`ACCEPTED`, nunca cuenta en `activeHeldQuantity`, y completarla no descuenta
stock. Conserva las transiciones legacy hasta quedar terminal. No se convierte
automaticamente una reserva legacy a `MVP5_HOLD`.

Reglas:

- crear exige oferta activa, visible, `AVAILABLE`, tienda activa y cantidad
  menor o igual a disponibilidad;
- la lectura publica solo muestra ofertas con disponibilidad positiva;
- una reduccion manual de stock por debajo de holds activos devuelve `409`;
- aumentar stock es seguro y no cambia reservas;
- cancelar, rechazar o expirar libera disponibilidad;
- aceptar confirma la solicitud pero no duplica ni descuenta la existencia;
- completar con stock insuficiente por inconsistencia detectada falla de forma
  visible y no deja un estado parcial;
- no hay pago, precio congelado, pedido, envio ni garantia comercial fuera de
  la reserva registrada.

## 11. Concurrencia, idempotencia y expiracion

- Creacion, cambio terminal y actualizacion de stock bloquean la fila
  `shop_products` dentro de una transaccion PostgreSQL. La disponibilidad se
  vuelve a calcular despues del lock.
- Las transiciones bloquean tambien la reserva o usan version optimista; una
  transicion perdida devuelve `409` y nunca sobrescribe silenciosamente.
- `POST /api/reservations` admite `Idempotency-Key`. La clave se guarda sin
  exponerla, queda acotada al usuario y la operacion, y tiene unicidad en base
  de datos. Misma clave+mismo payload devuelve la reserva original; misma clave
  con payload distinto devuelve `409`.
- Para compatibilidad, clientes legacy sin clave conservan el comportamiento
  actual durante la migracion. La UI MVP5 siempre genera una clave estable por
  intento y la reutiliza en retries.
- Una reconciliacion idempotente cambia a `EXPIRED` reservas activas vencidas.
  Debe poder ejecutarse por scheduler y de forma perezosa antes de calcular
  disponibilidad; repetirla no modifica terminales ni duplica efectos.
- Tiempos usan `Instant` UTC y un reloj inyectable en pruebas.

## 12. Recorridos objetivo

### 12.1 SHOP_OWNER

1. Crea o abre una tienda y ve claramente su membership.
2. Completa el perfil y confirma que el contacto indicado sera publico.
3. Administra miembros sin poder eliminar al ultimo OWNER.
4. Busca un item editorial, elige opcionalmente edicion y publica precio,
   condicion, stock y visibilidad.
5. Ve existencias, holds y disponibilidad sin exponer notas internas.
6. Filtra reservas por estado y producto, acepta/rechaza y completa/cancela
   solo transiciones validas.
7. Ve metricas agregadas basicas sin datos de colecciones privadas.

### 12.2 Usuario normal

1. Descubre una tienda y ofertas publicas con disponibilidad real.
2. Abre una oferta legacy o editorial sin diferencias rotas en el recorrido.
3. Envia una reserva con cantidad y mensaje; un retry no duplica.
4. Consulta estado, expiracion y respuesta de tienda.
5. Cancela una reserva propia permitida y observa disponibilidad liberada.
6. Nunca puede leer reservas ajenas, notas internas, miembros o metricas.

## 13. Contratos futuros minimos

Los paths definitivos se implementaran en sus EPICs, manteniendo los actuales
mientras exista compatibilidad:

- perfil gestionado y miembros bajo `/api/shops/{shopId}` y
  `/api/shops/{shopId}/members`;
- inventario interno con proyeccion gestionada y disponibilidad calculada;
- inventario publico con proyeccion sanitizada y filtros editoriales
  `catalogItemId`, `catalogItemEditionId`, `seriesId` y busqueda `q`;
- reserva idempotente por header y proyeccion de producto editorial/legacy;
- metricas agregadas de tienda por rango limitado, sin identidades personales.

Toda ampliacion debe documentar codigos `400/401/403/404/409`, orden estable,
limites de texto, propiedad, sanitizacion y compatibilidad.

## 14. Matriz de pruebas obligatoria

| Capa | Casos minimos |
| --- | --- |
| Dominio/servicio | exito, null/vacio, limites, estados incompatibles, idempotencia y efectos laterales |
| API/MVC | forma DTO, validacion, `400/401/403/404/409`, ownership y no inferencia |
| Persistencia PostgreSQL | constraints, locks, dos reservas concurrentes, retry idempotente, update de stock, expiracion repetida y cutover legacy/MVP5 |
| Identidad editorial | item puro, item+edicion, puente verificado, legacy sin puente, conflicto y edicion ajena |
| Privacidad | DTO publico sin `notes`/membership/auditoria; usuario sin reservas ajenas; tienda sin colecciones privadas |
| Frontend HTTP | paths, params, header idempotente, errores y tipos nullable editorial/legacy |
| Frontend componentes | loading, error, retry, vacio, vacio filtrado, permisos, acciones, doble click, respuesta fuera de orden y reload canonico |
| Accesibilidad/responsive | labels, roles, foco tras error, teclado y anchos movil/tablet/escritorio |
| QA integral | OWNER/MANAGER/EMPLOYEE/usuario/visitante, oferta editorial y legacy, carrera controlada, expiracion y privacidad |

Una prueba de concurrencia debe usar PostgreSQL real/Testcontainers y demostrar
que dos solicitudes simultaneas nunca superan la disponibilidad. Las pruebas
MVC con servicios mockeados no sustituyen esa evidencia.

Las pruebas de upgrade de 45G deben partir de reservas legacy `PENDING`,
`ACCEPTED` y `COMPLETED`, demostrar que todas quedan `LEGACY_REQUEST`, que la
disponibilidad y el stock no cambian al migrar, y que completar una legacy
aceptada no descuenta stock. Un escenario mixto debe demostrar que solo las
nuevas `MVP5_HOLD` retienen disponibilidad, liberan hold y descuentan stock una
sola vez.

## 15. Plan numerado de EPICs ejecutables

1. **45B - Contratos seguros y compatibilidad editorial de reservas.** Separar
   DTO publicos/internos, sanitizar inventario, hacer nullable/editorial la
   proyeccion de reserva y anadir regresiones. Sin stock transaccional todavia.
2. **45C - Perfil profesional y miembros de tienda.** 45C-A a 45C-D estan
   integradas. El cierre restante se divide en:
   - **45C-E - Perfil backend y contratos de tienda.** Separar proyecciones
     publica y gestionada, endurecer sanitizacion y validar la edicion existente
     para OWNER/MANAGER, sin frontend ni esquema.
   - **45C-F - Compatibilidad y esquema de memberships.** Auditar datos antes de
     decidir migracion `STAFF -> EMPLOYEE`; cuando exista evidencia, aplicar
     upgrade aditivo e idempotente y constraints con pruebas PostgreSQL de
     preservacion.
   - **45C-G - Cierre backend de 45C.** Revalidar invariantes, autorizacion,
     privacidad, compatibilidad, API/exports y evidencia, sin absorber 45D.
   Transferencia de ownership permanece `FUTURE / OUT_OF_MVP5`.
3. **45D - UX de perfil y miembros.** Edicion de tienda, lista/gestion permitida,
   estados loading/error/retry/vacio y guardas UX sin sustituir backend.
4. **45E - Inventario profesional backend.** Filtros editoriales, proyecciones
   gestionada/publica, disponibilidad positiva, orden estable y pruebas
   PostgreSQL, preservando filas legacy.
5. **45F - Inventario profesional frontend.** Busqueda editorial, filtros,
   disponibilidad, errores concurrentes y experiencia responsive.
6. **45G - Reservas transaccionales e idempotentes.** Holds, locks, clave
   idempotente, expiracion y transiciones atomicas con Testcontainers.
7. **45H - Recorridos frontend de reservas.** Retry idempotente, estados y
   expiracion, filtros utilizables y privacidad para usuario/tienda.
8. **45I - Metricas basicas de tienda.** Conteos agregados por estado/item y
   rango limitado; sin exponer identidad o colecciones privadas.
9. **45J - Datos demo, validacion integral y cierre de MVP5.** Escenario
   idempotente, API/DB/UI, concurrencia controlada, accesibilidad y cierre con
   limitaciones reales.

45B fue integrada antes de terminar una revision tardia. EPIC 45B-FIX corrige
la exposicion de stock del DTO publico, unifica la regla observable de referencia
publica entre inventario y reservas y aplica la prioridad de nombre definida en
la seccion 7. No implementa disponibilidad calculada ni modifica 45C.

Cada ejecucion implementa como maximo una EPIC y se detiene ante una PR previa
abierta hacia `dev`. 45B queda `CLOSED_AFTER_45B_FIX`; la siguiente tarea unica
es `NEXT_EPIC=45C-E`.

## 16. Archivos permitidos y prohibidos en 45A

Permitidos: este diseno, evidencia de 45A y documentos de handoff/backlog/log/
estado. Prohibidos: backend, frontend, scripts, migraciones, exports, workflows,
Docker, manifests, lockfiles y dependencias.

## 17. Trazabilidad de aceptacion

- inventario real y mapa: secciones 4 y 5;
- limites e identidad: secciones 6 y 7;
- permisos y privacidad: secciones 8 y 9;
- stock y reservas: secciones 10 y 11;
- recorridos: seccion 12;
- contratos: seccion 13;
- matriz de pruebas: seccion 14;
- orden ejecutable: seccion 15;
- exclusiones: seccion 2.

EPIC 45A no demuestra ningun comportamiento nuevo. Su cierre significa que el
alcance y el orden son ejecutables y auditables, no que MVP5 este implementado.
