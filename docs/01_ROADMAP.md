# CollectoHub product roadmap

Estado: roadmap estrategico por capacidades. No contiene fechas ni compromisos
de entrega. Cada fase debe validarse antes de ampliar la siguiente.

Este documento es la fuente vigente para fases de producto. El roadmap tecnico
original de `docs/10_ROADMAP.md` se conserva como registro del orden de
implementacion del repositorio.

## Principio de secuencia

El producto final es amplio, pero no se construye como un unico MVP. El orden
separa validacion de usuario, calidad del catalogo, social, tiendas, matching,
comercio y creadores para reducir riesgo y mantener un alcance verificable.

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

**Estado actual:** implementado como recorrido principal, con limitaciones de
producto documentadas en `docs/02_MVP1_SCOPE.md`.

## MVP 2 - Catalogo editorial solido

**Objetivo:** convertir el catalogo generico en una biblioteca bien modelada.

Incluye:

- franquicias;
- colecciones de catalogo;
- items de catalogo;
- editoriales;
- autores y creadores;
- relaciones entre obras;
- tipos de objeto;
- primeras imagenes o portadas;
- filtros avanzados.

La migracion desde `master_products` debe preservar compatibilidad y distinguir
claramente catalogo, coleccion personal e inventario.

## MVP 3 - Social basico

**Objetivo:** permitir que usuarios compartan actividad y contenido alrededor
de sus colecciones.

Incluye:

- perfiles publicos;
- seguir usuarios;
- actividad;
- posts simples;
- comentarios;
- likes;
- resenas;
- reportes y bloqueos basicos.

Moderacion, privacidad y controles de abuso son requisitos de la fase, no
mejoras opcionales posteriores.

## MVP 4 - Tiendas y matching oferta/demanda

**Objetivo:** conectar automaticamente usuarios que buscan objetos con tiendas
que los tienen.

Incluye:

- perfil de tienda;
- inventario, stock, precio y disponibilidad;
- buscados del usuario;
- coincidencias automaticas;
- reservas simples;
- contacto tienda/usuario;
- metricas basicas y agregadas.

Los modulos actuales de tiendas, inventario, recomendaciones y reservas son una
base tecnica parcial para esta fase, pero no equivalen al flujo final.

## MVP 5 - Marketplace y gestion comercial

**Objetivo:** permitir compra/venta real y gestion operativa de tienda.

Incluye:

- carrito;
- pedidos;
- pagos;
- facturacion;
- estados de pedido;
- almacen;
- envios;
- devoluciones;
- comisiones;
- panel de tienda.

Esta fase requiere diseno transaccional, cumplimiento legal y fiscal,
seguridad de pagos, conciliacion y soporte operativo antes de considerarse
disponible.

## MVP 6 - Creadores y comunidades

**Objetivo:** dar herramientas a usuarios que crean contenido y comunidades.

Incluye:

- perfiles de creador;
- publicaciones avanzadas y multimedia;
- comunidades;
- eventos;
- suscripciones;
- monetizacion;
- newsletters o notificaciones.

La monetizacion solo se aborda despues de validar comunidad, moderacion y valor
recurrente.

## Dependencias entre fases

1. El nucleo coleccionista genera la senal de interes.
2. El catalogo editorial mejora la calidad de esa senal.
3. Social anade relaciones y contenido sobre objetos bien identificados.
4. Matching conecta buscados con stock real.
5. Comercio convierte coincidencias en operaciones seguras.
6. Creadores y comunidades amplian contenido y recurrencia.

Puede mantenerse codigo tecnico adelantado a una fase posterior, pero no debe
cambiar el foco visible ni los criterios de exito de la fase activa.
