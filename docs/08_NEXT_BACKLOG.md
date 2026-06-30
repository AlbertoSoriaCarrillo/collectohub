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
2. EPIC 32: items y editions.
3. EPIC 33: puente con `master_products`, backfill y reconciliacion.
4. EPIC 34: API editorial y fachada legacy.
5. EPIC 35: frontend editorial.
6. EPIC 36: referencias editoriales en colecciones.
7. EPIC 37: inventario y matching por item/edicion.
8. EPIC 38: creators y relaciones priorizadas.

Creators pertenece a MVP 2, pero despues del nucleo. Relaciones entre obras se
implementan solo con casos de uso validados. Fuente de diseno:
`docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md`.

Siguiente tarea: EPIC 32, catalog items y editions, sin activar todavia el
puente con `master_products`.

## MVP 3 - Social basico

- perfiles publicos;
- follows;
- actividad de colecciones compartidas;
- posts simples;
- comentarios;
- likes;
- resenas;
- reportes, bloqueos y moderacion basica.

## MVP 4 - Tiendas y matching

- perfil de tienda listo para usuario final;
- stock y disponibilidad fiables;
- matching por identidad editorial;
- alertas de coincidencia;
- preferencias de precio, idioma, edicion, condicion y ubicacion;
- demanda agregada con privacidad;
- metricas basicas para tienda.

## MVP 5 - Comercio

- reservas transaccionales y expiracion automatica;
- carrito y pedidos;
- pagos y conciliacion;
- facturacion;
- envios y devoluciones;
- almacen;
- comisiones y soporte operativo.

## MVP 6 - Creadores

- perfiles de creador;
- contenido avanzado y multimedia;
- comunidades;
- eventos;
- suscripciones;
- newsletters/notificaciones;
- monetizacion con requisitos legales y de moderacion.

## Reglas de backlog

- No adelantar una fase porque exista una tabla, rol o pantalla legacy.
- Cada EPIC debe declarar que objetivo de producto valida.
- Matching no absorbe Commerce; Social no absorbe herramientas de creadores.
- Seguridad, privacidad, moderacion y cumplimiento forman parte de cada fase.
