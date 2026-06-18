# CollectoHub portfolio review

Este documento resume CollectoHub como proyecto para presentar en GitHub,
LinkedIn o una entrevista tecnica.

## 1. Resumen Del Producto

CollectoHub es un MVP para coleccionistas y tiendas especializadas. Permite que
un usuario registre sus colecciones, que una tienda publique inventario y que la
plataforma recomiende productos disponibles cuando coinciden con items buscados.

El MVP cubre un flujo completo: registro, login, tienda, catalogo, inventario,
coleccion, recomendacion y reserva sin pago.

## 2. Problema Que Resuelve

Los coleccionistas suelen tener listas dispersas de productos que poseen o
buscan. Las tiendas, por su parte, tienen stock que no siempre conecta con esa
demanda. CollectoHub valida una idea simple: cruzar colecciones personales con
inventario visible de tiendas para facilitar reservas.

## 3. Alcance Del MVP

Incluido:

- Autenticacion email/password con JWT.
- Usuarios con roles acumulables.
- Tiendas con miembros internos.
- Catalogo maestro de productos.
- Inventario de tienda.
- Colecciones personales.
- Recomendaciones basicas.
- Reservas sin pago.
- Frontend Angular.
- Rediseño UI/UX responsive con estilo social propio.
- Internacionalizacion frontend ligera ES/EN.
- Docker Compose local.
- Tests backend, frontend y E2E Playwright.

Fuera de alcance:

- Pagos.
- Chat.
- Feed social.
- Marketplace avanzado.
- OAuth y 2FA.
- Uploads reales de imagenes.
- Email/notificaciones.
- Despliegue cloud o Kubernetes.

## 4. Decisiones Tecnicas Principales

- Monolito modular en lugar de microservicios.
- PostgreSQL como base relacional principal.
- Liquibase para versionar migraciones.
- JWT stateless con refresh token basico.
- Roles globales para capacidades de plataforma y roles internos para permisos de tienda.
- Angular standalone con Angular Material para entregar una UI MVP completa.
- i18n frontend propio para ES/EN sin dependencia externa.
- Docker Compose para ejecucion local reproducible.
- Playwright como primera capa E2E local, fuera de CI por ahora.

## 5. Arquitectura Backend

El backend usa Java 25 y Spring Boot 4.1.x. Esta organizado por modulos de
paquete:

- `auth`
- `users`
- `shops`
- `catalog`
- `inventory`
- `collections`
- `recommendations`
- `reservations`
- `shared`
- `config`

Los controladores exponen DTOs, no entidades JPA. La logica de permisos vive en
servicios de aplicacion y Spring Security protege endpoints por JWT/authority.

## 6. Arquitectura Frontend

El frontend usa Angular 21, Angular Material y rutas standalone. Tiene modulos de
UI para:

- Auth.
- Dashboard.
- Tiendas.
- Catalogo.
- Inventario.
- Colecciones.
- Recomendaciones.
- Reservas.

La sesion MVP se guarda en `localStorage`. Un interceptor HTTP anade el Bearer
token y los guards bloquean rutas protegidas.

La capa visual usa SCSS y Angular Material con un layout social propio:

- Sidebar desktop.
- Bottom navigation movil.
- Contenido central orientado a cards.
- Panel contextual derecho.
- Estados vacios, tokens visuales, chips y formularios consistentes.

No implementa feed social real, chat ni marketplace avanzado; es un rediseño de
presentacion sobre las funcionalidades MVP existentes.

La internacionalizacion frontend cubre textos estaticos ES/EN, selector visible,
persistencia local, fallback e interpolacion simple. Los datos dinamicos de API
y usuario se muestran sin alterar.

## 7. Modelo De Seguridad

El acceso usa JWT en `Authorization: Bearer <token>`.

Roles globales:

- `USER`
- `SHOP_OWNER`
- `ADMIN`
- `CONTENT_CREATOR`

Roles internos de tienda:

- `OWNER`
- `MANAGER`
- `STAFF`

El rol global `SHOP_OWNER` indica capacidad de gestion en plataforma. El rol
interno `OWNER` controla permisos dentro de una tienda concreta. Esto evita que
un usuario con una tienda pueda modificar tiendas ajenas.

## 8. Base De Datos Y Migraciones

La base es PostgreSQL. Liquibase crea las tablas del MVP y datos iniciales como
roles y categorias.

Tablas principales:

- `users`, `roles`, `user_roles`
- `shops`, `shop_members`
- `product_categories`, `master_products`
- `shop_products`
- `collections`, `collection_items`
- `reservations`
- `refresh_tokens`

Las entidades principales incluyen auditoria y borrado logico cuando aplica.

## 9. Testing

Backend:

- Unit tests con JUnit y Mockito.
- Tests web/security con MockMvc.
- Tests de migraciones y contexto con PostgreSQL/Testcontainers cuando Docker esta disponible.

Frontend:

- Tests de servicios, guards, rutas y componentes.
- Build de produccion validado con Angular CLI.

E2E:

- Playwright en `frontend/e2e`.
- Smoke de frontend y health backend.
- Registro/login/dashboard.
- Flujo MVP principal completo.

## 10. Docker Y Despliegue Local

Docker Compose levanta:

- PostgreSQL 17.
- Backend Spring Boot.
- Frontend Angular servido por nginx.

URLs locales:

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

No es un despliegue productivo. Sirve para demo local reproducible.

## 11. CI

GitHub Actions ejecuta:

- Validacion documental minima.
- Backend con Java 25 y Maven Wrapper.
- Frontend con Node 24, `npm ci`, tests y build.

Playwright E2E queda fuera del CI en esta fase porque requiere levantar base de
datos, backend y frontend, lo que puede hacer el pipeline mas lento y fragil.

## 12. Que Demuestra Tecnicamente

- Diseno de un MVP full-stack con alcance controlado.
- Backend modular con seguridad y persistencia real.
- Migraciones SQL versionadas.
- Separacion entre roles globales y permisos por tienda.
- Frontend SPA consumiendo API real.
- UI responsive lista para demo con una experiencia mas cercana a producto.
- Internacionalizacion basica de UI sin tocar backend ni contratos de API.
- Pruebas a varios niveles: unitarias, integracion, build y E2E.
- Dockerizacion local de un sistema con tres servicios.
- Documentacion clara para operar y explicar el proyecto.

## 13. Que No Esta Implementado Aun

- Produccion real con secretos gestionados.
- Pagos y bloqueo de stock.
- Jobs automaticos de expiracion.
- Uploads de imagenes.
- OAuth/2FA.
- Email y notificaciones.
- Internacionalizacion avanzada o gestion de traducciones desde backend.
- Observabilidad avanzada.
- Despliegue cloud.

## 14. Como Explicarlo En Una Entrevista

Una explicacion breve:

> CollectoHub es un MVP full-stack para coleccionistas y tiendas. Construye un
> flujo completo desde registro hasta reserva, con backend Spring Boot, frontend
> Angular, PostgreSQL/Liquibase, seguridad JWT, Docker Compose y tests E2E. Lo
> enfoque como monolito modular para mantener el dominio claro y evitar
> complejidad prematura.

Puntos a destacar:

- El alcance esta deliberadamente limitado.
- Hay decisiones documentadas.
- La seguridad combina roles globales y permisos por recurso.
- El repositorio se puede levantar localmente y validar con comandos claros.
- Los E2E prueban el flujo de producto real.

## 15. Preguntas Tecnicas Y Respuestas Breves

### Por que monolito modular y no microservicios?

Porque el MVP necesita validar dominio y flujo de producto, no complejidad
distribuida. El monolito modular permite separar responsabilidades sin pagar el
coste operativo de microservicios.

### Por que PostgreSQL?

Porque el dominio es relacional: usuarios, tiendas, productos, colecciones,
reservas y permisos encajan bien con constraints, indices y transacciones.

### Por que Liquibase?

Para versionar el esquema, hacer reproducible la base y evitar depender de
`ddl-auto` de Hibernate en entornos compartidos.

### Como se controla el acceso entre tiendas?

Cada tienda tiene miembros en `shop_members`. Para modificar inventario o
reservas de una tienda, el backend comprueba que el usuario sea `OWNER` o
`MANAGER` de esa tienda.

### Por que el JWT no se actualiza automaticamente tras crear tienda?

El JWT es stateless. Al asignar `SHOP_OWNER` en base de datos, el token emitido
antes no cambia. El usuario debe reloguear o usar una estrategia futura de
refresh para recibir roles actualizados.

### Por que reservas sin pago?

Porque el MVP valida interes y flujo antes de introducir pagos, bloqueo de stock,
concurrencia avanzada y conciliacion.

### Por que Playwright no esta en CI?

Porque requiere backend, frontend y base de datos levantados. Se deja local en
esta fase para mantener el pipeline rapido y estable.

### Que harias antes de produccion?

Gestion de secretos, HTTPS, estrategia robusta de refresh/logout, revision de
`localStorage`, observabilidad, backups, hardening CORS, rate limiting,
vulnerabilidades npm, migraciones revisadas y despliegue cloud.

### Como separarias frontend, backend y base de datos en produccion?

Frontend como build estatico en CDN o hosting web, backend como servicio
containerizado detras de reverse proxy/API gateway, PostgreSQL gestionado y
variables/secretos inyectados por el proveedor cloud.
