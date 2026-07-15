# CollectoHub MVP demo flow

Esta guia prepara una demo local del MVP usando la UI Angular contra el backend
Spring Boot local. El recorrido principal presenta MVP 1 como gestion de
colecciones personales de libros, comics y manga usando un catalogo comun. Los
datos son ejemplos desechables para entorno local, no secretos reales.

## 1. Arrancar el entorno

Terminal 1:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Terminal 2:

```powershell
cd frontend
npm start
```

Abrir:

```text
http://localhost:4200
```

El frontend consume el backend en:

```text
http://localhost:8080
```

Alternativa Docker Compose:

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

## 1.1. Generar datos de demo por API

Si quieres preparar rapidamente capturas o una demo visual, puedes crear datos
locales usando la API real:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -Suffix "demo001"
```

El script crea usuario tienda, tienda, productos maestros, inventario, usuario
coleccionista, coleccion, items para recomendaciones y una reserva. Esos datos
legacy siguen ayudando a probar backend, pero las URLs principales impresas
priorizan Home, Catalogo, Colecciones, Buscados y Perfil. Guia completa:
`docs/25_DEMO_DATA.md`.

## 2. Datos sugeridos

Usa un sufijo distinto en cada demo para evitar duplicados:

```text
Sufijo: 20260618-demo
Email usuario: demo-20260618-demo@collectohub.local
Password local: Password123!
Nombre usuario: Demo Collector
Producto maestro: Demo Manga Volume 1
Franquicia: Demo Franchise
Coleccion: Demo Missing Manga
```

## 3. Home publica

1. Entrar en `/home` o en `/`.
2. Revisar el posicionamiento: colecciones, catalogo, estados de item y buscados.
3. Entrar en `Catalogo` desde la navegacion publica.

Resultado esperado:

- La primera pantalla no promociona tiendas, inventario ni reservas.
- El catalogo es publico.

## 4. Registro y login

1. Entrar en `/login` y usar `Crear cuenta`.
2. Registrar el usuario con email, password, confirmacion de password y nombre visible.
3. Confirmar que se abre la sesion y aparece `Tu biblioteca`.
4. Cerrar sesion y volver a entrar por `/login` para verificar login normal.

Resultado esperado:

- El usuario entra con rol global `USER`.
- No se muestran tokens ni hashes en pantalla.
- El formulario esta en ES/EN y valida confirmacion de password.

## 5. Catalogo publico

1. Ir a `Catalogo`.
2. Buscar por nombre, franquicia o categoria si hay datos cargados.
3. Abrir la ficha de una obra.

Resultado esperado:

- El catalogo se lee como fichas de obras/libros/comics/manga.
- El detalle no usa lenguaje de inventario ni reserva como CTA principal.

## 6. Crear coleccion

1. Ir a `Colecciones`.
2. Crear una coleccion privada de categoria `MANGA_COMIC`.
3. Abrir la coleccion.
4. Anadir un item y marcarlo como `OWNED`, `WANTED` o `DUPLICATED`. Una alta
   nueva no puede persistir `MISSING`.

Resultado esperado:

- La coleccion aparece solo para el propietario si es privada.
- `MISSING` se calcula en el progreso por serie, no como una nueva fila.

## 6.1 Progreso por serie

Con los datos de `docs/25_DEMO_DATA.md`, abre la URL de progreso de la serie.
Muestra grupos OWNED, WANTED y MISSING, porcentaje y la transicion de una misma
entrada WANTED a OWNED. No ejecutes esa transicion antes de capturar el escenario
1/1/1. `/wanted` sigue siendo el flujo comercial existente.

## 7. Revisar buscados

1. Ir a `Buscados` (`/wanted`).
2. Revisar resumen de `Me falta`, `Lo quiero` y coincidencias.
3. Filtrar por categoria si hay datos.

Resultado esperado:

- La pantalla funciona como lista de intereses/faltantes del coleccionista.
- No se pide introducir `shopId` en el recorrido principal.
- No hay CTA a producto de tienda, reserva o compra.

## 8. Revisar perfil

1. Ir a `Perfil`.
2. Confirmar email, nombre visible, idioma preferido y roles.
3. Verificar que editar perfil, cambiar password y cambiar avatar aparecen como acciones futuras deshabilitadas.

Resultado esperado:

- El perfil muestra datos seguros y no expone tokens.
- No hay upload de avatar ni gestion avanzada de cuenta en esta fase.

## 9. Comprobaciones rapidas

- `GET /api/health` sigue siendo publico.
- `/`, `/dashboard` y rutas desconocidas redirigen a `/home`.
- `/recommendations` redirige a `/wanted`.
- Las rutas privadas redirigen o bloquean si no hay sesion.
- La UI no muestra `passwordHash`, hashes de refresh token ni valores secretos.
- La navegacion principal no promociona tiendas, inventario ni reservas.

## 10. Apéndice legacy/futuro

El backend y varias pantallas legacy siguen existiendo para preservar el trabajo
tecnico ya implementado:

- `/shops`
- `/shops/{shopId}`
- `/shops/{shopId}/inventory`
- `/shop-products/{shopProductId}`
- `/reservations`
- `/shops/{shopId}/reservations`

Estas rutas no forman parte de la demo principal del nuevo enfoque de producto.
