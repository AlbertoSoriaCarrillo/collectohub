# CollectoHub public navigation UX

Fecha: 2026-06-23.

## 1. Nueva estructura de navegacion

La experiencia publica se organiza alrededor de un header global. La navegacion
principal visible es:

- Inicio.
- Catalogo.
- Mis colecciones, solo con sesion.
- Buscados, solo con sesion.

Tiendas, inventario y reservas permanecen como rutas legacy/futuras y no se
promocionan en la navegacion principal.

## 2. Header global

El header contiene:

- Marca `CollectoHub` enlazada a `/home`.
- Navegacion principal.
- Selector de idioma.
- Accion de sesion.

Visitante:

```text
Logo | Inicio | Catalogo | Idioma | Iniciar sesion
```

Usuario autenticado:

```text
Logo | Inicio | Catalogo | Mis colecciones | Buscados | Idioma | Avatar
```

## 3. Sidebar limpia

La sidebar desktop queda solo para navegacion principal. Ya no contiene:

- Login.
- Register.
- Selector ES/EN.
- Tarjeta de sesion.
- Logout.
- Perfil duplicado.
- CTAs de tienda.

## 4. Register solo desde login

`/register` sigue existiendo como ruta publica, pero la UI solo lo enlaza desde
la pantalla de login:

```text
No tienes cuenta? Crear cuenta
```

El header, la sidebar y Home no muestran Register como CTA global.

## 5. Selector de idioma unico

El selector de idioma principal vive en el header. Es un boton compacto con menu
Material y mantiene:

- `LanguageService`.
- Persistencia en `localStorage`.
- Cambio en caliente.
- `data-testid="language-selector"`.
- Opciones `language-es` y `language-en`.

Login y Register ya no renderizan un selector de idioma duplicado. Register
mantiene `preferredInterfaceLanguage` como campo del formulario porque es dato de
usuario.

## 6. Avatar y menu de usuario

Cuando hay sesion, el header sustituye `Iniciar sesion` por un avatar circular
con iniciales. El menu Material contiene:

- Perfil.
- Cerrar sesion.

No hay subida real de avatar ni edicion de perfil en esta fase.

## 7. Fuera de navegacion principal

Quedan fuera del recorrido principal:

- Tiendas.
- Inventario.
- Reservas.
- Marketplace.
- Feed social.
- Chat.
- Pagos.

## 8. Mobile

En mobile el header es compacto y mantiene idioma + login/avatar. La bottom nav
se conserva para usuario autenticado con:

- Inicio.
- Catalogo.
- Colecciones.
- Buscados.

Perfil queda en el menu de avatar.

## 9. Tests actualizados

Se actualizan tests unitarios y E2E para cubrir:

- Header publico con login y sin register.
- Selector unico de idioma en header.
- Avatar/menu con perfil y logout.
- Login como unico punto visible hacia register.
- Flujo principal de coleccionista.
