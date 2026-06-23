# CollectoHub demo data

Esta guia explica como generar datos locales de demo para preparar capturas,
portfolio y pruebas manuales rapidas del MVP. El recorrido principal actual es
Home, Catalogo, Colecciones, Buscados y Perfil.

El script usa exclusivamente la API publica/protegida existente. No ejecuta SQL
directo, no crea endpoints de seed/reset y no modifica reglas de negocio.

## Requisitos

- Backend disponible en `http://localhost:8080`.
- Liquibase aplicado sobre PostgreSQL con roles y categorias iniciales.
- Frontend disponible en `http://localhost:4200` si se quieren abrir las URLs.
- PowerShell.

Puedes levantar todo con Docker Compose:

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

O con ejecucion clasica:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

En otra terminal:

```powershell
cd frontend
npm.cmd start
```

## Ejecutar

Desde la raiz del repositorio:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -Suffix "demo001"
```

Si omites `-Suffix`, el script genera uno unico con timestamp y GUID corto:

```powershell
.\scripts\demo\create-demo-data.ps1
```

Si PowerShell bloquea la ejecucion de scripts por politica local, usa:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  .\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080"
```

Tambien acepta `-FrontendBaseUrl` si el frontend no esta en el puerto por
defecto:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -FrontendBaseUrl "http://localhost:4200"
```

## Datos creados

El script crea datos para el recorrido principal y conserva datos legacy/futuros
de tienda, inventario y reserva para validar el backend:

- Usuario tienda:
  - email: `demo.shop.<suffix>@collectohub.local`
  - password: `Demo1234!`
  - display name: `Demo Shop Owner <suffix>`
- Tienda:
  - `Akihabara Collectibles <suffix>`
  - pais `ES`, moneda `EUR`, reserva por defecto 48 horas.
- Productos maestros:
  - `Dragon Quest Collectors Vol. 1 <suffix>` en `MANGA_COMIC`.
  - `Cyber Samurai Figure <suffix>` en `FIGURE`.
  - `Galaxy Dragon Rare Card <suffix>` en `TRADING_CARD`.
  - `Retro Quest DX <suffix>` en `VIDEOGAME`.
- Inventario visible y disponible para la tienda.
- Usuario coleccionista:
  - email: `demo.collector.<suffix>@collectohub.local`
  - password: `Demo1234!`
  - display name: `Demo Collector <suffix>`
- Coleccion publica:
  - `Mi coleccion manga y retro <suffix>`.
- Items de coleccion:
  - `MISSING` para Dragon Quest.
  - `WANTED` para Galaxy Dragon.
  - `OWNED` para Retro Quest.
- Reserva del coleccionista sobre el producto de tienda Dragon Quest.

El usuario tienda se reloguea despues de crear la tienda porque el backend
asigna `SHOP_OWNER` en base de datos, pero el JWT emitido antes de esa asignacion
no cambia.

## URLs utiles

Al terminar, el script imprime primero enlaces principales como:

- `http://localhost:4200/home`
- `http://localhost:4200/login`
- `http://localhost:4200/catalog`
- `http://localhost:4200/collections`
- `http://localhost:4200/collections/{collectionId}`
- `http://localhost:4200/wanted`
- `http://localhost:4200/profile`

Tambien imprime URLs legacy/futuras:

- `http://localhost:4200/shops/{shopId}`
- `http://localhost:4200/shops/{shopId}/inventory`
- `http://localhost:4200/shop-products/{shopProductId}`
- `http://localhost:4200/reservations`
- `http://localhost:4200/reservations/{reservationId}`
- `http://localhost:4200/shops/{shopId}/reservations`

## Fichero local generado

El script guarda un resumen local en:

```text
scripts/demo/.last-demo-data.json
```

Incluye sufijo, usuarios demo, IDs creados y URLs. Este fichero esta ignorado en
Git y no debe versionarse.

## Repetir demos

Usa un sufijo distinto para cada ejecucion. Si reutilizas uno ya existente, la
API puede devolver `409 Conflict` por emails, ISBN/EAN o deteccion de duplicados
del catalogo.

Ejemplo:

```powershell
.\scripts\demo\create-demo-data.ps1 -Suffix "screenshots-001"
.\scripts\demo\create-demo-data.ps1 -Suffix "screenshots-002"
```

## Limpiar base local

El script no borra datos ni ejecuta limpieza automatica.

Si usas Docker Compose y quieres empezar desde una base vacia:

```powershell
cd infra
docker compose down -v
docker compose up --build
```

`down -v` elimina el volumen de PostgreSQL local. Usalo solo si no necesitas
conservar los datos existentes.

## Limitaciones

- No crea imagenes ni archivos reales.
- No acepta ni completa reservas automaticamente.
- No reduce stock, igual que el MVP actual.
- No sustituye los tests E2E.
- No esta pensado para produccion ni para datos persistentes reales.
