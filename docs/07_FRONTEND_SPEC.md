# Especificación frontend

## Stack

- Angular.
- TypeScript.
- Angular Material.
- PWA.
- Web responsive.
- Multiidioma.

## Decisión Angular vs React

Se elige Angular porque:

- Encaja mejor con aplicaciones grandes de formularios, paneles, rutas, servicios y módulos.
- Tiene una arquitectura más cerrada y consistente.
- Reduce decisiones accidentales cuando Codex genere código.
- El backend será Spring Boot, y Angular encaja bien con APIs REST empresariales.
- El usuario ya tiene contexto previo con Angular.

React no se descarta para otros proyectos, pero para CollectoHub se prioriza orden y mantenibilidad.

## Fase del frontend

El frontend no debe desarrollarse antes de tener un backend funcional y documentado con Swagger.

## Pantallas MVP

1. Login.
2. Registro.
3. Home privada básica.
4. Catálogo de productos.
5. Detalle de producto.
6. Mis colecciones.
7. Detalle de colección.
8. Añadir producto a colección.
9. Panel de tienda.
10. Inventario de tienda.
11. Crear/editar producto de tienda.
12. Reservas de usuario.
13. Reservas de tienda.
14. Recomendaciones básicas.

## Diseño visual

Diseño inicial:

- Oscuro.
- Moderno.
- Tarjetas.
- Dashboard.
- Angular Material.

Futuro:

- Tema claro.
- Tema retro.
- Personalización de estilo por usuario.

## Multiidioma

Idiomas iniciales:

- Castellano.
- Inglés.

Debe separarse el idioma de interfaz del idioma del producto.

## PWA

Debe prepararse para:

- Instalable.
- Responsive.
- Uso móvil mediante navegador.

No crear app móvil nativa en MVP.

## Estructura recomendada

```text
frontend/src/app
 ├── core
 ├── shared
 ├── features
 │   ├── auth
 │   ├── catalog
 │   ├── collections
 │   ├── shops
 │   ├── inventory
 │   ├── reservations
 │   └── recommendations
 └── layout
```

## Seguridad frontend

- Guardar tokens de forma segura según decisión técnica documentada.
- Renovar access token con refresh token.
- Proteger rutas por autenticación.
- Proteger vistas por rol.
- No confiar nunca solo en el frontend para permisos.

## No implementar en MVP

- Chat.
- Feed social.
- Comentarios multimedia.
- Vídeos/audios.
- Pagos.
- Directos.
- Marketplace entre usuarios.
