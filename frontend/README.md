# CollectoHub Frontend

Aplicacion Angular base para el MVP de CollectoHub.

## Stack

- Angular 21.
- TypeScript.
- Angular Material.
- Routing standalone.
- Formularios reactivos.
- HTTP Client con interceptor JWT.
- SCSS.

## Requisitos

- Node.js 24.x.
- npm 11.x.
- Backend local en `http://localhost:8080`.

En Windows PowerShell, si `npm` falla por politica de ejecucion de scripts,
usa `npm.cmd`:

```powershell
& "C:\Program Files\nodejs\npm.cmd" install
```

## Instalacion

```bash
npm install
```

Para CI o una instalacion limpia desde `package-lock.json`:

```bash
npm ci
```

## Backend local

El frontend usa:

```text
src/environments/environment.ts
apiBaseUrl = "http://localhost:8080"
```

Arranca el backend en otra terminal:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Comandos

```bash
npm start
npm test
npm run build
```

El workflow de CI ejecuta:

```bash
npm ci
npm test -- --watch=false
npm run build
```

## Rutas iniciales

- `/login`
- `/register`
- `/dashboard`
- `/`
- `**` redirige de forma controlada a dashboard/login.

## Sesion

En el MVP se usa `localStorage` para guardar `accessToken`, `refreshToken` y
datos publicos del usuario autenticado. No se muestran tokens en pantalla ni se
registran en logs.

Si el usuario crea su primera tienda, el backend le asigna `SHOP_OWNER` en base
de datos. El JWT actual no cambia; el usuario debe volver a iniciar sesion o usar
un futuro refresh token para ver los roles actualizados.
