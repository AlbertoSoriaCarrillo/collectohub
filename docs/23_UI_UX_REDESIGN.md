# CollectoHub UI/UX redesign

Fecha: 2026-06-18.

## Objetivo

Mejorar la presentacion del frontend MVP para que CollectoHub se perciba como
un producto social/marketplace moderno, sin ampliar el alcance funcional ni
copiar marcas existentes.

## Alcance

Incluido:

- Rediseño visual de layout, auth, dashboard y pantallas principales del MVP.
- Sistema dark-soft basado en variables SCSS.
- Sidebar desktop, bottom navigation movil, contenido central y panel derecho.
- Tarjetas, chips, tokens visuales, estados vacios y formularios consistentes.
- Mantenimiento de rutas, endpoints, modelos y `data-testid`.

Fuera de alcance:

- Backend.
- Nuevos endpoints.
- Cambios de base de datos.
- Feed social real.
- Chat.
- Pagos.
- Marketplace avanzado.
- OAuth.
- 2FA.
- Uploads.
- IA.

## Principios visuales

- Fondo oscuro suave, con contraste legible y acentos teal, azul y calidos.
- Tarjetas con radio maximo de 8px, bordes sutiles y sombras contenidas.
- Navegacion persistente en escritorio y ergonomica en movil.
- Formularios densos pero claros, usando Angular Material.
- Estados vacios con accion siguiente cuando existe una accion natural.
- Jerarquia de informacion orientada a escaneo rapido.

## Componentes redisenados

- `MainLayoutComponent`: shell social con sidebar, right panel y bottom nav.
- Login y registro: panel introductorio + tarjeta de formulario.
- Dashboard: hero autenticado, perfil, roles y accesos MVP.
- Tiendas: listado, creacion y detalle publico.
- Catalogo: filtros, cards, detalle y creacion.
- Inventario: listado, creacion y detalle publico.
- Colecciones: listado, creacion, detalle e items.
- Recomendaciones: resumen, filtros, cards y estados vacios.
- Reservas: listado propio, gestion de tienda y detalle.

## Compatibilidad E2E

Se mantienen los `data-testid` existentes para Playwright:

- Layout y sesion: `app-toolbar`, `app-brand`, `login-link`, `register-link`,
  `session-label`, `logout-button`.
- Auth: `login-form`, `login-email`, `login-password`, `login-submit`,
  `register-form`, `register-email`, `register-password`,
  `register-display-name`, `register-submit`.
- Flujos MVP: ids de tiendas, catalogo, inventario, colecciones,
  recomendaciones y reservas ya usados por `frontend/e2e`.

## Validacion recomendada

```powershell
cd frontend
npm.cmd ci
npm.cmd test -- --watch=false
npm.cmd run build
```

Validacion E2E local con Docker:

```powershell
cd infra
docker compose up --build -d
cd ..\frontend
npm.cmd run e2e
cd ..\infra
docker compose down
```

## Capturas recomendadas

Guardar capturas reales en `docs/assets/screenshots/`:

- Dashboard desktop.
- Catalogo desktop.
- Detalle de tienda con inventario publico.
- Recomendaciones.
- Reservas.
- Login o registro.
- Vista movil con bottom navigation.
