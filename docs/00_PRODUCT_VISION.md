# CollectoHub product vision

Estado: vision estrategica de producto. No implica que todas las capacidades
descritas esten disponibles actualmente.

## Vision completa

CollectoHub es una plataforma para coleccionistas, tiendas y creadores de
contenido, apoyada en un catalogo comun tipo biblioteca/wiki de objetos
coleccionables.

El MVP 1 visible se concentra en colecciones personales de libros, comics y
manga. Ese foco permite validar el nucleo de usuario sin confundirlo con el
producto final, que debe conectar progresivamente cuatro pilares.

## Problema compartido

- Los coleccionistas necesitan saber que tienen, que les falta y que buscan.
- La informacion sobre objetos coleccionables esta dispersa y tiene calidad
  desigual.
- Las tiendas tienen stock que no siempre llega a la demanda adecuada.
- Las personas que crean contenido necesitan espacios ligados a intereses y
  catalogos concretos.

CollectoHub busca convertir esos problemas aislados en un ciclo de valor: un
catalogo comun organiza el conocimiento; las colecciones expresan intereses y
faltantes; el stock de tiendas puede responder a demanda real; y la comunidad
genera contexto, contenido y descubrimiento.

## Pilar 1 - Coleccionistas

Personas que quieren:

- guardar sus colecciones;
- saber que tienen;
- saber que les falta;
- marcar que buscan;
- encontrar informacion fiable;
- compartir aficiones;
- descubrir personas con gustos similares.

El primer valor debe existir incluso sin marketplace ni red social: una
biblioteca personal clara, util y apoyada en un catalogo reutilizable.

## Pilar 2 - Catalogo y biblioteca comun

Una base de conocimiento tipo wiki centrada en objetos coleccionables. Su
primer alcance editorial cubre:

- libros;
- comics;
- manga.

En fases futuras podra cubrir:

- peliculas, series y documentales;
- cartas;
- merchandising y figuras;
- videojuegos y consolas;
- ediciones especiales y otros objetos coleccionables.

El modelo deseado debe organizar franquicias, colecciones de catalogo, items de
catalogo, obras relacionadas, autores o creadores, editoriales o marcas, datos
tecnicos, imagenes y relaciones entre productos. El `master_products` actual es
una base generica, no el modelo editorial definitivo.

## Pilar 3 - Tiendas

Tiendas especializadas que necesitan mostrar su stock a las personas que
realmente buscan esos objetos. La vision incluye progresivamente:

- perfil y miembros de tienda;
- inventario, stock, precio y disponibilidad;
- coincidencias con faltantes o buscados de usuarios;
- reserva, pedido y venta;
- gestion de almacen;
- metricas agregadas de demanda.

El backend actual ya contiene una base de tiendas, inventario y reservas sin
pago. No debe presentarse todavia como marketplace ni operacion comercial
completa.

## Pilar 4 - Comunidad y creadores

Usuarios que quieren publicar contenido, compartir colecciones, escribir
resenas, comentar, crear guias, hablar con personas afines y formar comunidad
alrededor de franquicias o colecciones.

La evolucion debe ser gradual:

1. perfiles publicos y actividad basica;
2. posts, comentarios, likes y resenas;
3. contenido avanzado y comunidades;
4. eventos, suscripciones y monetizacion cuando exista suficiente valor y una
   estrategia de seguridad y moderacion.

## Principios de producto

- Desarrollar por fases y validar un problema principal en cada una.
- Mantener un catalogo comun como eje entre dominios.
- Separar coleccion personal, conocimiento de catalogo e inventario comercial.
- No presentar una base tecnica parcial como una funcionalidad lista para
  usuario final.
- Proteger privacidad, propiedad de datos y permisos por recurso desde el
  diseno.
- Incorporar moderacion, reportes y seguridad antes de escalar contenido social
  o comercio.

## Foco actual frente a vision larga

**Foco actual:** MVP 1 permanece cerrado como nucleo coleccionista y MVP 2 queda
recomendado como cerrado con limitaciones tras implementar la biblioteca
editorial comun y su adopcion gradual. El siguiente bloque es MVP 3 Admin
editorial y carga real de datos. Social basico, comercio, marketplace y movil
son fases posteriores, no capacidades disponibles ni el siguiente bloque
inmediato.

**Vision larga:** plataforma que conecta coleccionistas, catalogo, tiendas y
creadores mediante conocimiento compartido, matching oferta/demanda y funciones
sociales y comerciales desarrolladas por etapas.

El roadmap oficial de esta vision se documenta en `docs/01_ROADMAP.md`.
