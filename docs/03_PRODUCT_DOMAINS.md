# CollectoHub product domains

## Estados usados

- **Implementado:** existe un flujo MVP util en backend y frontend principal.
- **Parcial:** existe base tecnica o flujo legacy, pero faltan capacidades para
  cumplir la vision del dominio.
- **Futuro:** no existe funcionalidad de producto; puede haber nombres, roles o
  placeholders sin comportamiento real.

## Mapa actual

| Dominio | Responsabilidad | Estado |
| --- | --- | --- |
| Identity & Access | Usuarios, login, roles y permisos | Implementado |
| Catalog Knowledge Base | Conocimiento comun de objetos coleccionables | Implementado para MVP 2 con limitaciones documentadas |
| User Collections | Colecciones e items personales | Implementado para MVP 1 |
| Social | Perfiles publicos, posts, follows y actividad | Futuro |
| Shops & Inventory | Tiendas, miembros, stock, precios y disponibilidad | Parcial |
| Matching | Relacion entre buscados/faltantes y stock | Parcial |
| Commerce | Reservas, pedidos, pagos, envios y facturas | Parcial minimo |
| Content Creators | Contenido avanzado, comunidades y monetizacion | Futuro |

## 1. Identity & Access

Incluye usuarios, registro, login, JWT, refresh token basico, roles globales y
permisos internos de tienda. Esta implementado para el alcance actual.

Pendiente para produccion: refresh/logout completo, estrategia de almacenamiento
de sesion, OAuth, 2FA, recuperacion de cuenta y administracion avanzada.

## 2. Catalog Knowledge Base

Debe incluir franquicias, colecciones de catalogo, items de catalogo, autores,
editoriales, tipos de objeto y relaciones.

Estado MVP 2: implementado con limitaciones. Existen categorias y
`master_products` como contrato legacy, y tambien publishers, franquicias,
series, items, ediciones, creators, creditos por item y relaciones entre items
como biblioteca editorial comun. El puente `master_product_catalog_links`
mantiene compatibilidad con consumidores legacy.

La adopcion gradual ya alcanza colecciones, inventario y matching basico. Siguen
fuera de MVP 2 el frontend admin editorial, moderacion avanzada, carga masiva,
wiki colaborativa, marketplace, pagos, social y movil. Diseno y cierre:
`docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md` y
`docs/20_MVP2_CLOSURE_REVIEW.md`.

## 3. User Collections

Incluye colecciones personales, items, estados, notas, condicion, visibilidad y
futuras imagenes personales.

Esta implementado para MVP 1: alta, lectura, edicion, borrado logico, items,
estados y visibilidad. Faltan imagenes personales, comparticion social avanzada
e importacion masiva. La UI de MVP 1 limita la seleccion a `OWNED`, `WANTED`,
`MISSING` y `DUPLICATED`; otros estados del contrato quedan fuera del recorrido
actual.

## 4. Social

Debe incluir perfiles publicos, posts, comentarios, follows y actividad.

Estado futuro: el perfil actual es privado/basico para el usuario autenticado.
No hay grafo social, feed, posts, comentarios, likes, moderacion ni bloqueos.

## 5. Shops & Inventory

Incluye tiendas, miembros, stock, precios y disponibilidad.

Estado parcial: backend y rutas legacy permiten crear y gestionar tiendas,
miembros e inventario basico. No es un flujo principal y no hay gestion de
almacen, pedidos, envios, conciliacion ni operacion comercial completa.

## 6. Matching

Incluye faltantes, buscados, stock compatible y futuras alertas.

Estado parcial: `recommendations` relaciona items propios `MISSING` o `WANTED`
con productos visibles y disponibles de tienda. Faltan reglas de relevancia
avanzadas, alertas, preferencias, ubicacion, comparacion, historico y metricas
de demanda.

## 7. Commerce

Incluye reservas, pedidos, pagos, envios y facturas.

Estado parcial minimo: existen solicitudes de reserva sin pago y transiciones
basicas. No reducen stock automaticamente y no hay carrito, pedido, pago,
factura, envio, devolucion ni comisiones. No debe llamarse marketplace.

## 8. Content Creators

Incluye publicaciones, monetizacion, comunidades y eventos.

Estado futuro: existe el codigo de rol `CONTENT_CREATOR`, pero no hay flujo de
producto asociado. El rol sembrado no equivale a una funcionalidad
implementada.

## Limites entre dominios

- Catalogo describe el objeto comun; User Collections describe la relacion de
  una persona con ese objeto.
- Shops & Inventory describe una oferta comercial concreta; no modifica el
  conocimiento comun del catalogo.
- Matching propone coincidencias; Commerce gestiona una operacion posterior.
- Social distribuye actividad y conversacion; Content Creators anade
  herramientas avanzadas y modelos de comunidad.
- Identity & Access protege todos los dominios, pero no debe absorber sus reglas
  de negocio.
