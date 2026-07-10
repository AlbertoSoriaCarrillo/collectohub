# CollectoHub MVP status

Fecha de revision: 2026-07-10.

## Estado general

Este estado corresponde a **MVP 1 - Nucleo coleccionista**, no al producto
final. La vision completa conecta coleccionistas, catalogo comun, tiendas y
creadores, con matching, social y comercio desarrollados por fases.

MVP 1 permanece cerrado y estable. MVP 2 ha completado EPIC 31 a EPIC 38:
fundamentos editoriales, fachada editorial, frontend editorial, referencias
editoriales en colecciones e inventario, matching editorial basico, creators
editoriales y relaciones entre items.

La auditoria de cierre de MVP 2 esta en
`docs/20_MVP2_CLOSURE_REVIEW.md`. Estado recomendado:
`MVP2_CLOSED_WITH_LIMITATIONS`. MVP 2 queda cerrado como biblioteca editorial
comun y adopcion gradual con limitaciones documentadas. Siguen fuera de MVP 2:
frontend admin editorial, moderacion editorial, reservas editoriales,
marketplace, pagos, alertas, pantalla de grafo avanzada, social y movil.
Siguiente gran bloque: MVP 3 Admin editorial y carga real de datos. Social,
tiendas, marketplace, pagos y movil quedan en fases posteriores.

MVP 3 tiene validado un primer bloque parcial: EPIC 40A-40G entrega shell
protegido para `ADMIN`, gestion de entidades editoriales, relaciones,
reconciliacion legacy, datos demo por API y validacion local. No queda cerrado
como producto final: siguen pendientes una auditoria UX/admin, calidad de datos
y decisiones de siguiente bloque. La siguiente tarea recomendada es EPIC 41A -
Auditoria UX/Admin editorial y bugs de usabilidad.

| Dominio | Estado actual |
| --- | --- |
| Identity & Access | Implementado para MVP |
| Catalog Knowledge Base | Parcial; fundamentos MVP 2, fachada de lectura, frontend editorial publico, creators y relaciones de items implementados |
| User Collections | Implementado con referencias legacy/editoriales duales desde EPIC 36 |
| Social | Futuro |
| Shops & Inventory | Inventario legacy/editorial implementado; fuera del recorrido principal |
| Matching | Recomendaciones por edicion, item y fallback legacy |
| Commerce | Reservas sin pago; resto futuro |
| Content Creators | Base editorial de creators implementada; herramientas sociales/creador futuras |

## Cierre MVP 1 - 2026-06-29

MVP 1 queda cerrado como base tecnica/producto tras una auditoria de alcance,
navegacion, UI, rutas, i18n, demo, seguridad basica y documentacion.

Correcciones de cierre:

- estados visibles de coleccion limitados a `OWNED`, `WANTED`, `MISSING` y
  `DUPLICATED`;
- Buscados sin filtros comerciales ni enlaces a productos de tienda;
- subtitulo global alineado con biblioteca personal, sin prometer social;
- URLs demo separadas entre MVP 1 y legacy/futuro;
- healthcheck nginx corregido para usar `127.0.0.1`;
- selectores E2E de Buscados estabilizados para ES/EN.

Checklist y backlog: `docs/07_MVP1_ACCEPTANCE_CHECKLIST.md` y
`docs/08_NEXT_BACKLOG.md`.

## Reenfoque 2026-06-23

El MVP tecnico de backend y frontend esta implementado. El foco visible de
producto pasa a ser la gestion de colecciones de libros, comics y manga usando
un catalogo comun.

Recorrido principal actual:

1. Home publica.
2. Registro y login con JWT.
3. Catalogo maestro publico.
4. Colecciones personales e items.
5. Buscados por items `MISSING` o `WANTED`.
6. Perfil basico.

Tiendas, inventario y reservas permanecen implementados y operativos como base
tecnica/futura, pero quedan fuera de la navegacion principal del frontend.

El MVP funcional de backend y frontend esta implementado para el flujo base:

1. Registro y login con JWT.
2. Usuario autenticado y `GET /api/users/me`.
3. Creacion y gestion basica de tiendas.
4. Asignacion automatica de `SHOP_OWNER` al crear la primera tienda.
5. Catalogo maestro de productos.
6. Inventario de tienda.
7. Colecciones personales e items.
8. Recomendaciones simples por items `MISSING` o `WANTED`.
9. Reservas sin pago.
10. Empaquetado local con Docker Compose.
11. Tests E2E locales basicos con Playwright.
12. CI con jobs de documentacion, backend y frontend.
13. Rediseño UI/UX frontend con layout social responsive.
14. Internacionalizacion frontend ligera ES/EN.
15. Script local de datos de demo por API para capturas y portfolio.

## Implementado backend

- Spring Boot 4.1.x con Java 25 y Maven Wrapper.
- PostgreSQL gestionado por Spring Data JPA.
- Liquibase con esquema MVP y datos iniciales.
- Seguridad stateless con JWT.
- Refresh token basico persistido con hash.
- Endpoints publicos: health, categorias, catalogo, detalle de tienda/productos publicos.
- Endpoints protegidos: usuario actual, tiendas propias, catalogo protegido, inventario, colecciones, recomendaciones y reservas.
- Aislamiento logico por usuario, tienda y miembro de tienda.
- OpenAPI/Swagger con esquema `bearer-jwt`.
- Perfil `docker` para ejecucion con PostgreSQL por nombre de servicio.
- Tests unitarios, web/security e integracion de migraciones con Testcontainers cuando Docker esta disponible.

## Implementado frontend

- Angular 21 con Angular Material.
- Login, registro, guard de rutas e interceptor JWT.
- Home publica y perfil basico.
- Modulos UI principales para catalogo, colecciones y buscados.
- Rutas legacy/futuras para tiendas, inventario y reservas conservadas por URL manual.
- Header global con logo, navegacion, selector unico de idioma y login/avatar segun sesion.
- Sidebar desktop limpia, limitada a navegacion principal.
- Persistencia MVP de sesion en `localStorage`.
- Mensajes de error normalizados para errores HTTP comunes.
- Tests de servicios, guards, rutas y pantallas principales.
- Build de produccion con `npm run build`.
- Dockerfile multi-stage con build Node.js y nginx para servir estaticos.
- Tests E2E Playwright en `frontend/e2e` para smoke, auth, i18n y flujo MVP principal.
- Sistema visual dark-soft con variables SCSS, sidebar desktop, bottom nav movil, estados vacios y tarjetas consistentes.
- Login, registro, home y pantallas principales de coleccionista redisenadas sin cambiar endpoints backend.
- Internacionalizacion ligera ES/EN con selector, persistencia local, fallback, interpolacion y enums visibles traducidos.

## No implementado en MVP

- Pagos.
- Marketplace avanzado.
- Chat.
- Feed social.
- OAuth.
- 2FA.
- Notificaciones y email.
- Uploads de imagenes/archivos.
- IA.
- Geolocalizacion.
- Jobs automaticos de expiracion de reservas.
- Roles globales avanzados y panel global de administracion.
- PWA completa/offline avanzado.
- Internacionalizacion avanzada, pluralizacion compleja o traducciones servidas desde backend.
- Edicion real de perfil, cambio de password, avatar/upload.
- Frontend admin editorial para alta/edicion visual de publishers, series,
  items, editions, creators y relaciones.
- Moderacion editorial, historial avanzado y revision colaborativa.
- Reservas editoriales.
- Marketplace, pagos y alertas.
- Pantalla de grafo avanzada.
- Perfiles publicos, follows, posts, comentarios, likes, resenas y moderacion.
- Pedidos, carrito, pagos, facturas, envios, devoluciones y almacen comercial.
- Herramientas funcionales para creadores, comunidades, eventos o monetizacion.

## Limitaciones conocidas

- Tras crear la primera tienda, el backend actualiza roles en base de datos, pero el JWT existente no cambia. El usuario debe volver a iniciar sesion para que la UI vea `SHOP_OWNER`.
- Las reservas no reducen stock automaticamente y no caducan por job automatico.
- Tiendas, inventario y reservas estan implementadas, pero en el frontend actual son rutas legacy/futuras fuera de la navegacion principal.
- `Register` no se muestra como accion global; se accede desde login o por URL manual.
- La sesion frontend usa `localStorage`, aceptado solo como simplificacion MVP.
- Algunos filtros frontend MVP son numericos/manuales, como `shopId`, `userId` o `shopProductId`.
- No hay subida real de imagenes; los productos y colecciones se gestionan sin archivos.
- El Docker frontend usa `apiBaseUrl = "http://localhost:8080"` y depende del backend publicado en el host.
- `npm ci` informa 7 vulnerabilidades en dependencias de desarrollo/transitivas; no se han actualizado versiones fuera del alcance de esta fase.
- Los tests Testcontainers se saltan si Docker no esta disponible.
- Los E2E Playwright no se ejecutan en CI y requieren entorno local ya levantado.

## Decisiones vigentes relevantes

- Java 25 y Spring Boot 4.1.x para backend.
- Maven Wrapper dentro de `backend` para evitar Maven global.
- Angular 21.x por compatibilidad con Node.js 24.14/24.x del entorno.
- Docker Compose local levanta PostgreSQL, backend y frontend; no implica despliegue cloud.
- Playwright E2E se ubica en `frontend/e2e` y usa Chromium en esta primera fase.
- El rediseño UI/UX mantiene Angular Material + SCSS, sin introducir Tailwind ni nuevas librerias de UI.
- La internacionalizacion frontend usa una capa propia ligera en Angular, sin librerias externas.
- `SHOP_OWNER` global es acumulable con `USER`.
- El rol interno `OWNER` de `shop_members` controla permisos dentro de una tienda concreta.
- El rol global `SHOP_OWNER` permite capacidades de gestion de tiendas/plataforma.
- La ruta publica principal es `/home`; `/dashboard` redirige a `/home`.
- La ruta visible de buscados es `/wanted`; `/recommendations` redirige a `/wanted`.
- Recomendaciones/buscados MVP se calculan contra items propios `MISSING` o `WANTED` y productos visibles/disponibles de tienda cuando existen datos legacy.
- Reservas MVP no incluyen pago ni bloqueo transaccional de stock.

## Como ejecutar backend

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Health check:

```powershell
curl.exe http://localhost:8080/api/health
```

## Como ejecutar frontend

```powershell
cd frontend
npm ci
npm test -- --watch=false
npm run build
npm start
```

Abrir:

```text
http://localhost:4200
```

## Como ejecutar la demo

1. Levantar backend y frontend.
2. Opcionalmente generar datos con `docs/25_DEMO_DATA.md`.
3. Seguir `docs/18_DEMO_FLOW.md` para la demo desde UI.
4. Usar `docs/17_MANUAL_TESTING_FLOW.md` si se quiere repetir el flujo por API con PowerShell.

## Como ejecutar Docker Compose

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

URLs Docker:

```text
Backend:   http://localhost:8080
Swagger:   http://localhost:8080/swagger-ui.html
Frontend:  http://localhost:4200
Postgres:  localhost:5432
```

Guia completa: `docs/20_DEPLOYMENT_LOCAL.md`.

## Como ejecutar E2E

Con backend y frontend ya levantados:

```powershell
cd frontend
npm run e2e:install
npm run e2e
```

Variables opcionales:

```powershell
$env:E2E_BASE_URL="http://localhost:4200"
$env:E2E_API_BASE_URL="http://localhost:8080"
```

Guia completa: `docs/21_E2E_TESTING.md`.

## UI/UX frontend

La fase de rediseño visual mantiene el alcance funcional del MVP y mejora la
presentacion para demo/portfolio:

- Header global con marca, navegacion, selector de idioma y menu de usuario.
- Sidebar desktop limpia solo con navegacion principal.
- Bottom navigation movil para rutas principales autenticadas.
- Login y registro con cards centradas, limpias y formularios Material.
- Home, catalogo, colecciones, buscados y perfil como recorrido principal.
- Listados y formularios de catalogo, colecciones y buscados con tarjetas y estados vacios.
- `data-testid` conservados para Playwright.

Guia completa: `docs/23_UI_UX_REDESIGN.md`.

## Internacionalizacion frontend

La fase i18n mantiene el alcance funcional del MVP:

- Idiomas `es` y `en`.
- Selector unico en el header global.
- Persistencia en `localStorage`.
- `preferredInterfaceLanguage` del registro sincronizado con el idioma activo.
- Enums visibles traducidos sin cambiar valores de backend.
- Datos dinamicos de backend y usuario conservados tal cual.

Guia completa: `docs/24_I18N_FRONTEND.md`.

## Estado CI

El workflow local `.github/workflows/ci.yml` define:

- Validacion de estructura documental.
- Backend con Java 25, `chmod +x mvnw` y `./mvnw clean verify`.
- Frontend con Node 24, `npm ci`, `npm test -- --watch=false` y `npm run build`.
- Playwright E2E queda fuera de CI en esta fase.

La revision de este archivo no encontro necesidad de cambios en esta fase.

## Validacion realizada

### Auditoria de cierre 2026-06-29

- Frontend `npm.cmd ci`: correcto; 7 vulnerabilidades transitivas/dev conocidas.
- Frontend tests: 38 archivos y 80 tests correctos.
- Frontend build: correcto; aviso no bloqueante de budget por 92.30 kB.
- Backend: 161 tests correctos, 0 fallos, 0 errores, 0 saltados con Docker.
- Docker Compose: PostgreSQL, backend y frontend `healthy`.
- Health: backend `UP` y frontend nginx `OK`.
- Playwright: 4 E2E correctos sobre el stack Docker final.
- Parser PowerShell de datos demo: correcto.

### Evidencias historicas acumuladas

- Backend: `.\mvnw.cmd clean verify` correcto.
- Frontend: `npm ci` correcto.
- Frontend: `npm test -- --watch=false` correcto.
- Frontend: `npm run build` correcto.
- Frontend UI/UX: rediseño social responsive aplicado sin cambios de API ni backend.
- Frontend i18n: capa ES/EN aplicada sin cambios de API ni backend.
- Frontend i18n tests: `npm.cmd test -- --watch=false` correcto; 33 archivos de test y 69 tests correctos.
- Frontend i18n build: `npm.cmd run build` correcto; se mantiene aviso de budget inicial excedido en 15.47 kB.
- Backend local: arranque con perfil `local` y health check correcto.
- Frontend local: `npm start` compila y responde `200 OK` en `http://127.0.0.1:4200/`.
- Smoke test por API del flujo MVP completo correcto: registro, login, tienda, relogin con `SHOP_OWNER`, producto maestro, inventario, coleccion, recomendacion, reserva completada y reserva cancelada.
- Script de datos demo preparado en `scripts/demo/create-demo-data.ps1`, usando solo endpoints MVP existentes y guardando resumen local ignorado por Git.
- Docker Compose: validado localmente con PostgreSQL, backend y frontend.
- Playwright headless: `npm run e2e` correcto con smoke, auth/colecciones, i18n y flujo MVP principal.
- Playwright headed: `npm run e2e:headed` correcto.

## Siguientes pasos recomendados

La referencia tecnica descargable queda versionada en `docs/export/`: 22 tablas
de aplicacion, 81 endpoints, 32 rutas Angular y 61 relaciones pantalla-backend.
Los estados distinguen recorrido MVP 1, base legacy/futura, infraestructura,
MVP 2 visible/foundation y redirecciones sin alterar codigo funcional.

MVP 2 queda cerrado con limitaciones y MVP3 Admin Editorial cuenta ya con un
primer bloque parcial validado: EPIC 40A-40G. La siguiente tarea recomendada es
**EPIC 41B - Cierre documental/exportable MVP3 parcial**. EPIC 41A ya aplico
una auditoria UX inicial a las pantallas admin y corrigio confirmaciones sin
i18n. Social, tiendas, marketplace, pagos y movil continúan en fases
posteriores.

EPIC 41B queda completada: el cierre parcial esta en
`docs/23_MVP3_PARTIAL_CLOSURE.md`. MVP3 sigue abierto; la siguiente tarea
recomendada es EPIC 41C - E2E basico Admin Editorial y validacion con datos demo.

1. Preparar capturas reales para `docs/assets/screenshots/` y enlazarlas desde el README.
2. Revisar el rediseño visual en desktop y movil real, anadir capturas a `docs/assets/screenshots/`.
3. Revisar vulnerabilidades npm transitivas y compatibilidad de versiones Angular/Node antes de compartir el entorno.
4. Valorar un job E2E opcional/separado en CI cuando Docker Compose en Actions sea estable.
5. Endurecer seguridad antes de produccion: gestion de secretos, estrategia de refresh/logout, cookies o almacenamiento revisado y cabeceras.
6. Definir la siguiente fase funcional sin ampliar accidentalmente pagos, chat, marketplace u OAuth.

Vision, alcance y roadmap: `docs/00_PRODUCT_VISION.md`,
`docs/01_ROADMAP.md`, `docs/02_MVP1_SCOPE.md` y
`docs/03_PRODUCT_DOMAINS.md`.
