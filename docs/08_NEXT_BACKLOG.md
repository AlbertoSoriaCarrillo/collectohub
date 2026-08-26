# CollectoHub backlog after MVP 1

Estado actual: modo `SUPERVISED_ACTIVE_NO_ENFORCEMENT`, MVP4
`MVP4_CLOSED_WITH_LIMITATIONS` y 45B reabierta por EPIC 45B-FIX tras una
revision tardia. Las fases 45C-A a 45C-D estan integradas, pero 45C permanece
abierta. Perfil
profesional, reconciliacion `STAFF`, transferencia de ownership y cierre del
esquema aditivo requieren EPICs independientes antes de implementarse. El orden
funcional sigue `docs/01_ROADMAP.md`.

## Must fix antes de ensenar portfolio

No quedan bloqueos conocidos de rutas, copy, Docker o tests tras EPIC 28.
Antes de cada demo se debe confirmar:

- [ ] no hay errores visuales evidentes en desktop y mobile;
- [ ] Home, Login, Catalogo, Colecciones, Buscados y Perfil cargan sin errores;
- [ ] Docker alcanza `healthy` y `/api/health` devuelve `UP`;
- [ ] tests frontend/backend siguen verdes; E2E se registra como
      `SKIPPED_WITH_REASON` mientras permanezca pospuesto;
- [ ] no reaparecen enlaces principales a tiendas, inventario o reservas.

Pulido recomendado para portfolio, no bloqueante del MVP:

- anadir capturas reales desktop/mobile a `docs/assets/screenshots/`;
- revisar las 7 vulnerabilidades npm transitivas/dev con actualizaciones
  compatibles;
- reducir o redefinir conscientemente el budget del bundle inicial;
- revisar warnings futuros de Mockito/JDK y APIs de test deprecadas.

## MVP 2 - Catalogo editorial

EPIC 30 completa el diseno tecnico y recomienda modelo nuevo con puente de
compatibilidad. EPIC 31 implementa y valida ya el primer tramo. Orden:

1. EPIC 31: publishers, franchises y series. Completada y validada mediante
   EPIC 31B el 2026-06-30.
2. EPIC 32: items y editions. Completada el 2026-06-30.
3. EPIC 33: puente con `master_products`, backfill y reconciliacion. Completada
   el 2026-06-30.
4. EPIC 34: API editorial y fachada legacy. Completada el 2026-06-30.
5. EPIC 35: frontend editorial. Completada el 2026-07-01.
6. EPIC 36: referencias editoriales en colecciones. Completada el 2026-07-01.
7. EPIC 37: inventario y matching por item/edicion. Completada el 2026-07-07.
8. EPIC 38: creators y relaciones priorizadas. Completada el 2026-07-08.

Creators y relaciones priorizadas quedan implementados en backend, fachada y
detalle frontend sin activar administracion visual avanzada. La auditoria de
cierre esta en `docs/20_MVP2_CLOSURE_REVIEW.md`.

Estado recomendado: `MVP2_CLOSED_WITH_LIMITATIONS`. MVP 2 puede cerrarse como
biblioteca editorial comun y adopcion gradual, dejando fuera admin editorial,
moderacion, carga masiva, marketplace, pagos, social y movil.

Secuencia historica: EPIC 39 cerro formalmente MVP2 y preparo MVP3 Admin
Editorial.

EPIC 39C cierra la preparacion de MVP3 mediante
`docs/21_MVP3_ADMIN_EDITORIAL_PLAN.md`. EPIC 40A-40G completa el primer bloque
parcial de administracion editorial y validacion local. EPIC 41A, tambien
completada, audito UX/Admin editorial y bugs de usabilidad.

MVP 3 ya no es Social basico. MVP 3 pasa a ser Admin editorial y carga real de
datos. Social basico pasa a MVP 6. El cambio de orden evita saltar a
social/comercio antes de tener gestion editorial mantenible.

## MVP 3 - Admin editorial y carga real de datos

- panel admin editorial para publishers, franquicias, series, items, ediciones,
  creators y relaciones;
- reconciliacion visual de enlaces `master_products`;
- revision de borradores, duplicados y calidad de datos;
- carga real de datos controlada.

Orden recomendado MVP3:

1. EPIC 40A - Admin editorial shell y navegacion protegida. Completada el
   2026-07-10.
2. EPIC 40B - Admin publishers/franchises/series. Completada el 2026-07-10.
3. EPIC 40C - Admin items/editions. Completada el 2026-07-10.
4. EPIC 40D - Admin creators y creditos. Completada el 2026-07-10.
5. EPIC 40E - Admin relationships. Completada el 2026-07-10.
6. EPIC 40F - Admin master product links/reconciliation. Completada el 2026-07-10.
7. EPIC 40G - Datos demo editoriales y validacion integral MVP3 parcial.
   Completada el 2026-07-10.

EPIC 41A - Auditoria UX/Admin editorial y bugs de usabilidad. Completada el
2026-07-10. EPIC 41B - Cierre documental/exportable MVP3 parcial, tambien
completada.

EPIC 41B - Cierre documental/exportable MVP3 parcial. Completada el
2026-07-10. EPIC 41C - E2E basico Admin Editorial y validacion con datos demo
quedo pospuesta.

Decision: los E2E se posponen hasta recorridos mas maduros. EPIC 42A - Calidad
de datos editorial y reglas anti-duplicados, y EPIC 42B - Hardening admin
editorial y validaciones backend selectivas, estan completadas.

EPIC 42B quedo completada el 2026-07-10 y EPIC 42C - Calidad editorial avanzada
y reporte de duplicados, tambien se completo.

EPIC 42C, 42C-FIX y 42C-FIX2 completadas el 2026-07-12. EPIC 43A - Diseno de
roles editoriales separados de ADMIN queda completada el 2026-07-12. El diseno
recomendo `EDITORIAL_ADMIN`; EPIC 43B, 43C y 43D quedaron completadas el
2026-07-12. `EDITORIAL_ADMIN` se
provisiona solo mediante operacion controlada, sin autoasignacion ni interfaz
publica. EPIC 44A, EPIC 44B, EPIC 44B-FIX, EPIC 44C y EPIC 44D quedan completadas.
EPIC 44E, EPIC 44F y EPIC 44G-A a 44G-D-FIX quedan completadas. La FIX cierra
la concurrencia del listado y evita estados obsoletos en el detalle.
EPIC 44H-A documenta la auditoria y el diseno ejecutable. EPIC 44H-B implementa
el orquestador idempotente, WhatIf sin efectos, resumen seguro y prueba offline.
EPIC 44H-B-FIX corrige la resolucion predeterminada de `SummaryPath` en Windows
PowerShell 5.1 sin cambiar el alcance funcional. La primera ejecucion real de
44H-C detecta que una pagina vacia se convierte en `null`; EPIC 44H-C-FIX
corrige exclusivamente ese contrato. Tras integrar la FIX, EPIC 44H-C completa
el escenario `44hc3`, su doble ejecucion idempotente, API/DB, privacidad,
propiedad, filtros, ordenaciones, progreso y recorrido UI humano. Su resultado
inicial fue `MVP4_PARTIALLY_CLOSED`; la decision posterior vigente es
`MVP4_CLOSED_WITH_LIMITATIONS` en
`docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md`.

## Quality gates transversales

Antes de continuar con EPIC 44H-B se ejecuta **EPIC QUALITY-A - Politica
permanente y puertas de calidad** desde la base
`51b8eff54953f78ff51d99e097d801f53dd675bf` y la rama
`quality/quality-gates`.

QUALITY-A incorpora `AGENTS.md`, matriz de pruebas, verificador local, cuatro
checks estables de GitHub Actions, plantilla de PR, evidencia por EPIC y guia
manual de proteccion de `main`. No cambia producto, dependencias, migraciones ni
contratos. La proteccion remota queda como accion administrativa supervisada
fuera del repositorio.

El modelo `dev -> pre -> main` esta activo como
`SUPERVISED_ACTIVE_NO_ENFORCEMENT`. El commit de activacion
`5f5c45c6cec89e442c246508eb421ac641f8a967` esta en `origin/main`, `origin/dev`
y `origin/pre`, y la primera ejecucion supervisada termino correctamente sin
cambios de producto, commit, push, PR ni fusion. `dev` es la rama de integracion
efectiva. La automatizacion nunca fusiona, las entregas terminan en
`HUMAN_MERGE_REQUIRED` y toda fusion es humana. GitHub no aplica protecciones
enforced y este estado no equivale a `PROTECTED_ACTIVE`. Cualquier PR abierta
desde `codex/*` o `quality/*` hacia `dev` bloquea otra EPIC.

El horario automatico no se activa en este cierre. QUALITY-B permanece definida
y no implementada; no compite con la unica siguiente tarea seleccionada.

## MVP 4 - Colecciones finales de usuario

Estado: `MVP4_CLOSED_WITH_LIMITATIONS`. Quedan demostrados la alta editorial,
edicion opcional, items manuales y enlace posterior, WANTED/OWNED, missing
calculado, privacidad, propiedad, filtros, ordenaciones, progreso,
compatibilidad legacy y el recorrido integral con UI humana.

Limitaciones conservadas: E2E/Playwright, imagenes y almacenamiento real,
`quantity` frente a ejemplares separados, paginacion avanzada, decisiones de
taxonomia y MISSING legacy/persistido, y produccion, social, marketplace y pagos
fuera de MVP4.

- adopcion principal del catalogo editorial en colecciones;
- seleccion refinada de item y edicion;
- buscados/faltantes mejorados;
- estabilidad E2E del recorrido coleccionista.

## MVP 5 - Tiendas profesionales y reservas

- perfil de tienda listo para usuario final;
- inventario profesional con referencias editoriales;
- reservas simples con reglas claras;
- metricas basicas para tienda.

EPIC 45A deja auditado y disenado MVP5 en
`docs/40_MVP5_SHOPS_INVENTORY_RESERVATIONS_DESIGN.md`. No implementa
funcionalidad. El plan ejecutable queda dividido en 45B-45J.

EPIC 45B esta integrada historicamente en `dev`, pero no debe considerarse
correctamente cerrada hasta integrar EPIC 45B-FIX.

**EPIC 45B - Contratos seguros y compatibilidad editorial de reservas.**

45B debe separar DTO publicos e internos de tienda/inventario, impedir que
`notes` internos aparezcan en lectura publica, hacer que reservas y su frontend
soporten ofertas editoriales puras sin asumir `masterProductId`, preservar
clientes legacy y anadir regresiones de API, privacidad y autorizacion. No
implementa todavia locks, holds o idempotencia de stock, reservados a 45G.

**EPIC 45B-FIX - Contratos publicos y coherencia de reservas.**

Es la unica entrega prioritaria antes de seleccionar otra fase. Elimina
`stockQuantity` de `PublicShopProductResponse` sin inventar
`availableQuantity`, comparte entre inventario y reservas la regla de
referencia publica y prioriza el nombre de edicion/item sobre legacy en
`ReservationResponse`. Incluye regresiones MVC, de servicio y frontend. No
implementa holds, locks, migraciones ni trabajo de 45C.

**EPIC 45C - Perfil profesional y miembros de tienda (desglosada).**

45C incorpora lectura backend de memberships activas en orden estable mediante
`GET /api/shops/{shopId}/members` para OWNER/MANAGER y alta acotada mediante
`POST /api/shops/{shopId}/members` solo para OWNER. El alta referencia una cuenta
activa existente por email normalizado, admite MANAGER/EMPLOYEE, no devuelve
datos personales y rechaza duplicados de forma estable. El cambio seguro entre
MANAGER y EMPLOYEE tambien esta integrado. Estas unidades quedan identificadas
como 45C-A (lectura), 45C-B (alta) y 45C-C (cambio de rol). La EPIC 45C-D,
cerrada por esta entrega, permite solo al OWNER desactivar una membership activa
MANAGER o EMPLOYEE, sin desactivar al OWNER y con auditoria del actor. No existen
todavia transferencia de ownership, reconciliacion `STAFF`, cierre del esquema
aditivo ni edicion de perfil profesional; cada alcance restante debe definirse
como una EPIC independiente antes de implementarse.

La reconciliacion completa del estado 45C y de la promocion incorrecta
`dev -> main` mediante PR #21 queda para una tarea posterior independiente.

## MVP 6 - Social basico

- perfiles publicos;
- follows;
- actividad de colecciones compartidas;
- posts simples, comentarios, likes y resenas;
- reportes, bloqueos y moderacion basica.

## MVP 7 - Notificaciones, mensajeria y eventos

- notificaciones internas;
- email transaccional basico;
- alertas;
- mensajeria limitada y segura;
- eventos simples.

## MVP 8 - Marketplace inicial

- escaparate inicial de ofertas;
- matching con stock de tiendas;
- comparacion basica;
- solicitudes o reservas comerciales mejoradas.

## MVP 9 - Pagos, pedidos, envios y legal comercial

- carrito y pedidos;
- pagos y conciliacion;
- facturacion;
- envios, devoluciones, comisiones y soporte.

## MVP 10 - Producto comercial, movil, seguridad avanzada y produccion

- hardening de seguridad;
- observabilidad y operacion;
- movil o PWA avanzada;
- herramientas avanzadas para creadores y comunidades.

## Reglas de backlog

QUALITY-A debe estar validada localmente y sus cuatro checks remotos deben estar
verdes antes de fusionarla. No empezar QUALITY-B ni 44H-B en la misma ejecucion.

El modelo de ramas documentado no autoriza iniciar QUALITY-B ni otra EPIC. Una
ejecucion procesa como maximo una EPIC o completa una PR pendiente, y la rama de
integracion solo cambia de `main` a `dev` tras verificar que `origin/dev` y
`origin/pre` existen.

EPIC 44F, EPIC 44G-A a 44G-D-FIX y EPIC 44H-A a 44H-C estan completadas. MVP4
queda `MVP4_CLOSED_WITH_LIMITATIONS`; los limites se enumeran en
`docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md`. EPIC 45B-FIX es la entrega secuencial
actual; no iniciar QUALITY-B, reconciliacion de 45C ni otra EPIC mientras su PR
permanezca abierta.

- No adelantar una fase porque exista una tabla, rol o pantalla legacy.
- Cada EPIC debe declarar que objetivo de producto valida.
- Matching no absorbe Commerce; Social no absorbe herramientas de creadores.
- Seguridad, privacidad, moderacion y cumplimiento forman parte de cada fase.
