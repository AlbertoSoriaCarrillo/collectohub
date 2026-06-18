# CollectoHub MVP status

Fecha de revision: 2026-06-18.

## Estado general

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
11. CI con jobs de documentacion, backend y frontend.

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
- Dashboard autenticado.
- Modulos UI para tiendas, catalogo, inventario, colecciones, recomendaciones y reservas.
- Persistencia MVP de sesion en `localStorage`.
- Mensajes de error normalizados para errores HTTP comunes.
- Tests de servicios, guards, rutas y pantallas principales.
- Build de produccion con `npm run build`.
- Dockerfile multi-stage con build Node.js y nginx para servir estaticos.

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
- Internacionalizacion avanzada.

## Limitaciones conocidas

- Tras crear la primera tienda, el backend actualiza roles en base de datos, pero el JWT existente no cambia. El usuario debe volver a iniciar sesion para que la UI vea `SHOP_OWNER`.
- Las reservas no reducen stock automaticamente y no caducan por job automatico.
- La sesion frontend usa `localStorage`, aceptado solo como simplificacion MVP.
- Algunos filtros frontend MVP son numericos/manuales, como `shopId`, `userId` o `shopProductId`.
- No hay subida real de imagenes; los productos y colecciones se gestionan sin archivos.
- El Docker frontend usa `apiBaseUrl = "http://localhost:8080"` y depende del backend publicado en el host.
- `npm ci` informa 5 vulnerabilidades en dependencias de desarrollo/transitivas; no se han actualizado versiones fuera del alcance de esta fase.
- Los tests Testcontainers se saltan si Docker no esta disponible.

## Decisiones vigentes relevantes

- Java 25 y Spring Boot 4.1.x para backend.
- Maven Wrapper dentro de `backend` para evitar Maven global.
- Angular 21.x por compatibilidad con Node.js 24.14/24.x del entorno.
- Docker Compose local levanta PostgreSQL, backend y frontend; no implica despliegue cloud.
- `SHOP_OWNER` global es acumulable con `USER`.
- El rol interno `OWNER` de `shop_members` controla permisos dentro de una tienda concreta.
- El rol global `SHOP_OWNER` permite capacidades de gestion de tiendas/plataforma.
- Recomendaciones MVP se calculan contra items propios `MISSING` o `WANTED` y productos visibles/disponibles de tienda.
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
2. Seguir `docs/18_DEMO_FLOW.md` para la demo desde UI.
3. Usar `docs/17_MANUAL_TESTING_FLOW.md` si se quiere repetir el flujo por API con PowerShell.

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

## Estado CI

El workflow local `.github/workflows/ci.yml` define:

- Validacion de estructura documental.
- Backend con Java 25, `chmod +x mvnw` y `./mvnw clean verify`.
- Frontend con Node 24, `npm ci`, `npm test -- --watch=false` y `npm run build`.

La revision de este archivo no encontro necesidad de cambios en esta fase.

## Validacion realizada

- Backend: `.\mvnw.cmd clean verify` correcto.
- Frontend: `npm ci` correcto.
- Frontend: `npm test -- --watch=false` correcto.
- Frontend: `npm run build` correcto.
- Backend local: arranque con perfil `local` y health check correcto.
- Frontend local: `npm start` compila y responde `200 OK` en `http://127.0.0.1:4200/`.
- Smoke test por API del flujo MVP completo correcto: registro, login, tienda, relogin con `SHOP_OWNER`, producto maestro, inventario, coleccion, recomendacion, reserva completada y reserva cancelada.
- Docker Compose: preparado con PostgreSQL, backend y frontend; no ejecutado en esta revision porque `docker` no esta instalado o no esta en PATH en el entorno local actual.

## Siguientes pasos recomendados

1. Ejecutar una demo humana completa desde navegador usando `docs/18_DEMO_FLOW.md`.
2. Anadir pruebas end-to-end ligeras cuando se decida una herramienta, por ejemplo Playwright.
3. Revisar vulnerabilidades npm y compatibilidad de versiones Angular/Node antes de preparar un entorno compartido.
4. Definir la siguiente fase funcional sin ampliar accidentalmente pagos, chat, marketplace u OAuth.
