# CollectoHub backlog after MVP 1

Estado: backlog posterior al cierre de MVP 1. El orden funcional sigue
`docs/01_ROADMAP.md`.

## Must fix antes de ensenar portfolio

No quedan bloqueos conocidos de rutas, copy, Docker o tests tras EPIC 28.
Antes de cada demo se debe confirmar:

- [ ] no hay errores visuales evidentes en desktop y mobile;
- [ ] Home, Login, Catalogo, Colecciones, Buscados y Perfil cargan sin errores;
- [ ] Docker alcanza `healthy` y `/api/health` devuelve `UP`;
- [ ] tests frontend/backend y E2E siguen verdes;
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

Siguiente tarea: EPIC 39 - Cierre formal MVP2 y preparacion de MVP3 Admin
Editorial.

EPIC 39C cierra la preparacion de MVP3 mediante
`docs/21_MVP3_ADMIN_EDITORIAL_PLAN.md`. EPIC 40A-40G completa el primer bloque
parcial de administracion editorial y validacion local. Siguiente tarea
recomendada: EPIC 41A - Auditoria UX/Admin editorial y bugs de usabilidad.

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
2026-07-10. Siguiente tarea recomendada: EPIC 41B - Cierre
documental/exportable MVP3 parcial.

EPIC 41B - Cierre documental/exportable MVP3 parcial. Completada el
2026-07-10. Siguiente tarea recomendada: EPIC 41C - E2E basico Admin Editorial
y validacion con datos demo.

Decision: los E2E se posponen hasta recorridos mas maduros. EPIC 42A - Calidad
de datos editorial y reglas anti-duplicados. Completada el 2026-07-10.
Siguiente tarea recomendada: EPIC 42B - Hardening admin editorial y
validaciones backend selectivas.

EPIC 42B completada el 2026-07-10. Siguiente tarea recomendada: EPIC 42C -
Calidad editorial avanzada y reporte de duplicados.

EPIC 42C, 42C-FIX y 42C-FIX2 completadas el 2026-07-12. EPIC 43A - Diseno de
roles editoriales separados de ADMIN queda completada el 2026-07-12. El diseno
recomienda `EDITORIAL_ADMIN` sin implementarlo todavia. Siguiente tarea:
EPIC 43B, 43C y 43D quedan completadas el 2026-07-12. `EDITORIAL_ADMIN` se
provisiona solo mediante operacion controlada, sin autoasignacion ni interfaz
publica. EPIC 44A, EPIC 44B, EPIC 44B-FIX, EPIC 44C y EPIC 44D quedan completadas.
EPIC 44E, EPIC 44F y EPIC 44G-A a 44G-D-FIX quedan completadas. La FIX cierra
la concurrencia del listado y evita estados obsoletos en el detalle.
Siguiente tarea: EPIC 44H - Datos demo, validacion integral y cierre parcial de MVP4.

## MVP 4 - Colecciones finales de usuario

- adopcion principal del catalogo editorial en colecciones;
- seleccion refinada de item y edicion;
- buscados/faltantes mejorados;
- estabilidad E2E del recorrido coleccionista.

## MVP 5 - Tiendas profesionales y reservas

- perfil de tienda listo para usuario final;
- inventario profesional con referencias editoriales;
- reservas simples con reglas claras;
- metricas basicas para tienda.

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

EPIC 44F y EPIC 44G-A a 44G-D-FIX estan completadas. Siguiente EPIC: EPIC 44H -
Datos demo, validacion integral y cierre parcial de MVP4.

- No adelantar una fase porque exista una tabla, rol o pantalla legacy.
- Cada EPIC debe declarar que objetivo de producto valida.
- Matching no absorbe Commerce; Social no absorbe herramientas de creadores.
- Seguridad, privacidad, moderacion y cumplimiento forman parte de cada fase.
