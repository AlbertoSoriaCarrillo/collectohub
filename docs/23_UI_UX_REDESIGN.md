# CollectoHub UI/UX redesign

Fecha: 2026-06-23.

## Objetivo

Mejorar la presentacion del frontend MVP para que CollectoHub se perciba como
una red social/catalogo para coleccionistas de libros, comics y manga, sin
ampliar backend ni copiar marcas existentes.

## Alcance

Incluido:

- Rediseño visual de layout, auth, home y pantallas principales del MVP de coleccionista.
- Sistema dark-soft basado en variables SCSS.
- Sidebar desktop, bottom navigation movil y contenido central.
- Tarjetas, chips, tokens visuales, estados vacios y formularios consistentes.
- Mantenimiento de rutas, endpoints, modelos y `data-testid`.
- Compatibilidad con i18n frontend ES/EN posterior al rediseño.

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

- `MainLayoutComponent`: shell social con sidebar, sesion, selector de idioma y bottom nav.
- `HomeComponent`: primera pantalla publica del producto.
- Login y registro: panel introductorio + tarjeta de formulario.
- Perfil: datos basicos y acciones futuras deshabilitadas.
- Tiendas: listado, creacion y detalle publico.
- Catalogo: filtros, cards, detalle y creacion.
- Inventario: listado, creacion y detalle publico.
- Colecciones: listado, creacion, detalle e items.
- Buscados: resumen, filtros, cards y estados vacios.
- Tiendas, inventario y reservas: conservadas como rutas legacy/futuras.

## Compatibilidad E2E

Se mantienen los `data-testid` existentes para Playwright:

- Layout y sesion: `app-toolbar`, `app-brand`, `login-link`, `register-link`,
  `session-label`, `logout-button`.
- Auth: `login-form`, `login-email`, `login-password`, `login-submit`,
  `register-form`, `register-email`, `register-password`,
  `register-display-name`, `register-submit`.
- Flujos MVP: ids de home, perfil, catalogo, colecciones y buscados usados por
  `frontend/e2e`, manteniendo ids legacy donde ya existian.
- Los campos traducibles nuevos o migrados usan `data-testid` para evitar que
  Playwright dependa de labels visibles en ES/EN.

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

Antes de capturar, puedes poblar datos locales con:

```powershell
.\scripts\demo\create-demo-data.ps1 -ApiBaseUrl "http://localhost:8080"
```

Guia completa: `docs/25_DEMO_DATA.md`.

Guardar capturas reales en `docs/assets/screenshots/`:

- Home desktop.
- Catalogo desktop.
- Colecciones.
- Buscados.
- Perfil.
- Login o registro.
- Vista movil con bottom navigation.
