# CollectoHub MVP 1 acceptance checklist

Fecha de cierre: 2026-06-29.

Estado: **CERRADO COMO BASE TECNICA Y DE PRODUCTO MVP**.

Este cierre confirma coherencia, ejecucion local y presentacion del alcance. No
significa que CollectoHub este preparado para produccion ni que la vision larga
este implementada.

## 1. Criterio general

- [x] El producto visible valida gestion de colecciones personales sobre un
  catalogo comun de libros, comics y manga.
- [x] La navegacion principal no presenta tiendas, inventario ni reservas.
- [x] No se presenta marketplace, pagos, social completo ni creadores como
  funcionalidad disponible.
- [x] Vision larga y MVP 1 estan separados en README y documentacion.

## 2. Checklist funcional

- [x] Home publica y catalogo publico.
- [x] Registro accesible desde Login y login email/password.
- [x] Registro valida email, password, confirmacion y nombre visible.
- [x] `confirmPassword` no forma parte del payload backend.
- [x] Perfil basico muestra nombre, email, idioma y roles.
- [x] Crear, listar, consultar, editar y borrar colecciones personales.
- [x] Anadir, editar y borrar items de coleccion.
- [x] Estados visibles: `OWNED`, `WANTED`, `MISSING`, `DUPLICATED`.
- [x] Buscados resume faltantes, deseados y coincidencias sin CTA comercial.
- [x] Colecciones publicas pueden leerse; las privadas se protegen en backend.

## 3. Checklist UI y navegacion

- [x] Header visitante: marca, Inicio, Catalogo, idioma y Login.
- [x] Header autenticado: anade Colecciones, Buscados y menu de usuario.
- [x] Sidebar limitada a navegacion principal.
- [x] Mobile conserva header compacto y bottom nav de cuatro destinos.
- [x] Register no aparece como CTA global.
- [x] Login y Register usan cards limpias sin selector interno duplicado.
- [x] Home explica solo el valor de MVP 1.
- [x] Catalogo usa lenguaje de obras y publicaciones.
- [x] Colecciones se presentan como biblioteca personal.
- [x] Perfil mantiene acciones no implementadas deshabilitadas.

## 4. Checklist i18n

- [x] Selector unico ES/EN en header.
- [x] Textos visibles del recorrido principal usan claves de traduccion.
- [x] Diccionarios ES y EN tienen las mismas claves hoja.
- [x] Enums visibles se traducen sin cambiar valores de API.
- [x] Idioma persiste tras recarga.
- [x] Datos dinamicos de usuario/API no se traducen.

## 5. Checklist de seguridad basica

- [x] Rutas privadas usan `authGuard`.
- [x] JWT se envia mediante interceptor a la API.
- [x] UI no muestra tokens, `passwordHash` ni hashes de refresh token.
- [x] Permisos efectivos permanecen en backend.
- [x] Colecciones privadas ajenas se ocultan desde backend.
- [x] El uso de `localStorage` esta documentado como simplificacion MVP, no
  como estrategia de produccion.

## 6. Checklist de demo

- [x] Flujo principal: Home, Login, Catalogo, Colecciones, detalle, Buscados y
  Perfil.
- [x] Script demo usa endpoints existentes y no SQL directo.
- [x] La salida del script separa `MVP 1 URLs` de `Legacy/future URLs`.
- [x] Los datos legacy de tienda/reserva se mantienen solo para matching y
  validacion tecnica.
- [x] Parser PowerShell valida `create-demo-data.ps1` sin errores.

## 7. Checklist Docker

- [x] `docker compose down` correcto.
- [x] `docker compose up --build -d` correcto.
- [x] PostgreSQL, backend y frontend alcanzan estado `healthy`.
- [x] `GET /api/health` devuelve `UP`.
- [x] nginx responde en `/health`.
- [x] Healthcheck frontend usa `127.0.0.1` y no depende de resolucion IPv6 de
  `localhost`.

## 8. Checklist de tests

- [x] `npm.cmd ci` correcto.
- [x] Frontend: 38 archivos y 80 tests correctos.
- [x] Build Angular de produccion correcto.
- [x] Backend: 161 tests correctos, incluidos Testcontainers.
- [x] Playwright: 4 E2E correctos sobre el stack Docker final.
- [x] CI conserva validacion documental, backend y frontend.

## 9. Fuera de MVP 1

- Modelo editorial avanzado, nuevas tablas y migraciones.
- Tiendas, inventario y reservas como flujo principal.
- Marketplace, carrito, pedidos, pagos, envios y facturas.
- Feed, posts, follows, comentarios, likes y chat.
- Creadores, comunidades, eventos y monetizacion.
- Upload real de imagenes, APIs externas, scraping e IA.
- Edicion real de perfil, avatar, OAuth y 2FA.

## 10. Criterios para pasar a MVP 2

- [x] No quedan fallos funcionales, de rutas, Docker o tests conocidos que
  bloqueen una demo de MVP 1.
- [x] El alcance actual y el futuro estan documentados sin contradicciones.
- [x] El backlog posterior separa dominios y fases.
- [x] El modelo conceptual de catalogo existe antes del diseno fisico.
- [ ] Antes de migraciones, aprobar el diseno detallado de franquicias,
  colecciones de catalogo, items, autores, editoriales y relaciones.

## Avisos no bloqueantes

- `npm ci` informa 7 vulnerabilidades transitivas/dev: 3 low y 4 high.
- El bundle inicial es 592.30 kB y supera el budget de 500 kB en 92.30 kB.
- Maven muestra avisos de APIs de test deprecadas y futura carga de agente
  Mockito/JDK.
- Faltan capturas reales de portfolio; no afecta al cierre funcional.
