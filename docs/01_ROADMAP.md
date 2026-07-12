# CollectoHub product roadmap

Estado: roadmap estrategico por capacidades. No contiene fechas ni compromisos
de entrega. Cada fase debe validarse antes de ampliar la siguiente.

Este documento es la fuente vigente para fases de producto. El roadmap tecnico
original de `docs/10_ROADMAP.md` se conserva como registro del orden de
implementacion del repositorio.

## Principio de secuencia

El producto final es amplio, pero no se construye como un unico MVP. El orden
separa validacion de usuario, calidad del catalogo, administracion editorial,
colecciones finales, tiendas, social, comunicaciones, marketplace, comercio y
produccion para reducir riesgo y mantener un alcance verificable.

## MVP 1 - Nucleo coleccionista

**Objetivo:** validar que los usuarios quieren gestionar colecciones personales
usando un catalogo comun.

Incluye:

- usuarios, registro y login;
- perfil basico;
- catalogo inicial;
- colecciones personales e items;
- estados `OWNED`, `WANTED`, `MISSING` y `DUPLICATED`;
- buscados y faltantes;
- visibilidad basica publica o privada;
- i18n ES/EN;
- UI responsive;
- datos demo, Docker, tests y CI.

No incluye marketplace, pagos, tiendas como flujo principal, chat, feed social
completo, monetizacion, pedidos ni almacen.

**Estado actual:** cerrado como base tecnica/producto el 2026-06-29. Alcance en
`docs/02_MVP1_SCOPE.md` y evidencia en
`docs/07_MVP1_ACCEPTANCE_CHECKLIST.md`.

## MVP 2 - Catalogo editorial solido

**Objetivo:** convertir el catalogo generico en una biblioteca bien modelada.

Incluye:

- franquicias;
- series de catalogo;
- items y ediciones de catalogo;
- editoriales;
- autores y creadores;
- relaciones entre obras cuando existan casos validados;
- tipos de objeto;
- URLs de portada compatibles; uploads reales quedan fuera de esta fase inicial;
- filtros avanzados.

La migracion desde `master_products` debe preservar compatibilidad y distinguir
claramente catalogo, coleccion personal e inventario.

**Estado actual:** EPIC 31 a EPIC 38 implementadas y validadas. La auditoria de
cierre recomienda `MVP2_CLOSED_WITH_LIMITATIONS`: el catalogo editorial comun,
el puente de compatibilidad y la adopcion gradual por colecciones, inventario y
matching estan implementados, mientras que admin editorial, moderacion, carga
masiva, marketplace, pagos, social y movil quedan fuera de MVP 2.

Diseno y cierre: `docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md` y
`docs/20_MVP2_CLOSURE_REVIEW.md`.

## MVP 3 - Admin editorial y carga real de datos

**Objetivo:** hacer mantenible el catalogo editorial ya creado, con herramientas
de administracion, calidad de datos y carga real antes de escalar social o
comercio.

Incluye:

- panel admin editorial para publishers, franquicias, series, items, ediciones,
  creators y relaciones;
- revision de borradores y registros archivados;
- reconciliacion visual de enlaces `master_products`;
- carga real de datos controlada;
- criterios de calidad, duplicados y auditoria editorial;
- datos demo/capturas editoriales mantenibles.

No incluye social, marketplace, pagos, importadores masivos abiertos,
colaboracion wiki publica ni produccion comercial.

**Estado actual:** siguiente gran bloque recomendado tras cerrar MVP 2 con
limitaciones. EPIC 39 debe cerrar formalmente MVP 2 y preparar este MVP sin
implementar todavia el admin funcional. Plan de entrada:
`docs/21_MVP3_ADMIN_EDITORIAL_PLAN.md`.

## MVP 4 - Colecciones finales de usuario

EPIC 44A completa el diseno y auditoria inicial. La primera entrega funcional
planificada es EPIC 44B - Contrato backend de collection items y compatibilidad
editorial.

**Objetivo:** completar la experiencia coleccionista sobre el catalogo editorial
como fuente principal.

Incluye:

- migracion gradual de flujos principales desde `master_products` al catalogo
  editorial;
- seleccion refinada de item y edicion en colecciones;
- mejoras de buscados/faltantes;
- imagenes personales o URLs controladas si se decide;
- filtros y ordenacion finales para biblioteca personal;
- estabilidad E2E del recorrido coleccionista.

No incluye social, tiendas profesionales, pagos, marketplace ni movil.

**Estado actual:** parcialmente preparado por EPIC 36; no es el siguiente bloque
inmediato hasta cerrar Admin Editorial.

## MVP 5 - Tiendas profesionales y reservas

**Objetivo:** convertir la base tecnica de tiendas, inventario y reservas en un
flujo profesional mantenible.

Incluye:

- perfiles de tienda listos para usuario final;
- inventario profesional con referencias editoriales;
- reservas simples con reglas claras;
- contacto controlado tienda/usuario;
- metricas basicas de demanda;
- permisos y panel de tienda revisados.

No incluye pagos, pedidos completos, envios, facturacion ni marketplace abierto.
Los modulos actuales son base tecnica parcial, no producto comercial completo.

## MVP 6 - Social basico

**Objetivo:** permitir que usuarios compartan actividad y contenido alrededor de
colecciones y catalogo ya administrables.

Incluye:

- perfiles publicos;
- follows;
- actividad de colecciones compartidas;
- posts simples;
- comentarios;
- likes;
- resenas;
- reportes, bloqueos y moderacion basica.

No incluye herramientas avanzadas de creadores, monetizacion, comunidades
complejas, eventos, chat completo ni marketplace.

## MVP 7 - Notificaciones, mensajeria y eventos

**Objetivo:** anadir comunicacion y recurrencia sin mezclarla con pagos o
marketplace.

Incluye:

- notificaciones internas;
- email transaccional basico;
- alertas de actividad o coincidencias;
- mensajeria limitada y segura;
- eventos o recordatorios simples.

No incluye monetizacion, pagos, pedidos ni mensajeria avanzada sin moderacion.

## MVP 8 - Marketplace inicial

**Objetivo:** validar oferta/demanda visible sin procesar pagos dentro de la
plataforma.

Incluye:

- escaparate inicial de ofertas;
- matching con stock de tiendas;
- comparacion basica;
- solicitudes o reservas comerciales mejoradas;
- reglas de visibilidad, abuso y confianza.

No incluye pagos, pedidos completos, facturacion, envios ni conciliacion.

## MVP 9 - Pagos, pedidos, envios y legal comercial

**Objetivo:** convertir marketplace en operacion comercial real con seguridad y
cumplimiento.

Incluye:

- carrito y pedidos;
- pagos;
- facturacion;
- estados de pedido;
- envios y devoluciones;
- comisiones;
- conciliacion, soporte y requisitos legales/fiscales.

No incluye produccion a gran escala sin observabilidad, soporte y seguridad
operacional maduras.

## MVP 10 - Producto comercial, movil, seguridad avanzada y produccion

**Objetivo:** preparar CollectoHub para una operacion comercial madura y
multiplataforma.

Incluye:

- hardening de seguridad;
- observabilidad y operacion;
- app movil o PWA avanzada;
- internacionalizacion avanzada;
- herramientas avanzadas para creadores y comunidades;
- rendimiento, escalabilidad y soporte.

No incluye nuevas lineas de producto sin validar las fases anteriores.

## Dependencias entre fases

1. El nucleo coleccionista genera la senal de interes.
2. El catalogo editorial mejora la calidad de esa senal.
3. Admin editorial hace mantenible la calidad y carga real de datos.
4. Colecciones finales adoptan el catalogo editorial como experiencia principal.
5. Tiendas y reservas conectan stock profesional con demanda.
6. Social anade relaciones y contenido sobre objetos bien identificados.
7. Notificaciones y mensajeria aumentan recurrencia y comunicacion.
8. Marketplace valida oferta/demanda visible.
9. Comercio convierte coincidencias en operaciones seguras.
10. Producto comercial y movil amplian alcance y operacion.

Puede mantenerse codigo tecnico adelantado a una fase posterior, pero no debe
cambiar el foco visible ni los criterios de exito de la fase activa.
