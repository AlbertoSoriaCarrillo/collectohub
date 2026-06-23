# Frontend i18n ES/EN

Fecha: 2026-06-23.

## Objetivo

Anadir internacionalizacion ligera al frontend Angular del MVP sin tocar backend,
base de datos, endpoints ni modelos de dominio.

Idiomas soportados:

- `es`
- `en`

## Arquitectura

La capa i18n vive en:

```text
frontend/src/app/core/i18n/
```

Ficheros principales:

- `language.service.ts`: idioma activo, persistencia, traduccion y fallback.
- `translate.pipe.ts`: pipe standalone `translate` para plantillas.
- `language-selector.component.ts`: selector visible ES/EN.
- `translations.ts`: tipos, constantes y mapa de diccionarios.
- `translations/es.ts` y `translations/en.ts`: diccionarios por dominio.

No se introduce libreria externa de i18n. La solucion es deliberadamente ligera
porque el MVP solo necesita textos estaticos ES/EN, cambio en caliente,
fallback e interpolacion simple.

## Persistencia Y Fallback

- La seleccion se guarda en `localStorage` con la clave
  `collectohub.language`.
- Si no hay idioma guardado y el navegador esta en ingles, se inicia en `en`.
- Si no hay idioma guardado y el navegador no esta en ingles, se usa `es`.
- Si `localStorage` contiene un idioma no soportado, se vuelve a `es`.
- Si falta una clave de traduccion, se muestra la clave solicitada para evitar
  pantallas vacias.
- La interpolacion usa placeholders `{{name}}`.

## Dominios De Traduccion

Los diccionarios estan agrupados por:

- `common`
- `actions`
- `layout`
- `language`
- `home`
- `auth`
- `dashboard`
- `shops`
- `catalog`
- `inventory`
- `collections`
- `recommendations`
- `profile`
- `reservations`
- `validation`
- `errors`
- `enums`

## Enums Y Datos Dinamicos

Se traducen solo representaciones visibles de enums/estados:

- roles globales.
- roles internos de tienda.
- visibilidad y estado de colecciones.
- estado comercial de inventario.
- condicion fisica.
- estado de reservas.
- estados visibles de tienda y producto maestro.

No se traducen datos dinamicos que vienen del backend o del usuario:

- emails.
- nombres de usuario, tienda, producto o coleccion.
- descripciones, notas y mensajes.
- IDs.
- codigos de categoria cuando se muestran como dato tecnico.
- precios, monedas y fechas.

## Selector De Idioma

El selector se muestra en:

- `MainLayoutComponent`.
- Login.
- Registro.

El reenfoque de producto del 2026-06-23 anade textos principales para Home,
Buscados y Perfil, y mantiene las claves de tiendas/inventario/reservas para
rutas legacy/futuras.

En registro, el campo `preferredInterfaceLanguage` se sincroniza con el idioma
activo para que la preferencia enviada al backend coincida con la seleccion del
usuario.

## Errores HTTP

`ErrorMessageService` traduce errores genericos:

- red / backend no disponible.
- 400.
- 401.
- 403.
- 404.
- 409.
- 500.

Los mensajes especificos devueltos por el backend se conservan tal cual.

## Testing

Tests anadidos:

- `LanguageService`: idioma inicial, `localStorage`, idioma de navegador,
  fallback, cambio en caliente, idiomas disponibles e interpolacion.
- `TranslatePipe`: traduccion, cambio de idioma y fallback de clave.
- E2E Playwright: cambio de idioma en login y persistencia tras recarga.

Los helpers E2E del flujo MVP usan `data-testid` en campos traducibles para no
depender del texto visible.

## Textos Hardcodeados Restantes

Tras la migracion, quedan intencionadamente fuera de traduccion:

- `CH` como marca visual de CollectoHub.
- Letras de iconos visuales en estados vacios (`C`, `R`, `W`, `K`, `B`, etc.).
- Nombres internos de iconos Material como `add_business`.
- Datos dinamicos de backend/usuario.
- Strings tecnicos en tests, modelos, rutas y helpers.

## Comandos De Validacion

```powershell
cd frontend
npm.cmd ci
npm.cmd test -- --watch=false
npm.cmd run build
```

E2E recomendado con entorno levantado:

```powershell
cd frontend
npm.cmd run e2e
```
