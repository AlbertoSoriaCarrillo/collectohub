# Tareas para Codex

## Reglas

- Ejecutar tareas en orden.
- No saltar a frontend antes de terminar backend MVP.
- Actualizar `docs/12_TASK_LOG.md` tras cada tarea.
- Si se toma una decisión técnica, actualizar `docs/13_DECISIONS.md`.
- Añadir tests cuando se implemente funcionalidad.

## EPIC 1 - Inicialización backend

### Tarea 1.1

Crear proyecto Maven Spring Boot en `/backend`.

Criterios:

- Java 25.
- Spring Boot 4.x.
- Estructura de paquetes definida.
- Compila correctamente.

### Tarea 1.2

Configurar profiles.

Criterios:

- `local`.
- `test`.
- Variables por entorno.
- Sin secretos reales.

## EPIC 2 - Base de datos

### Tarea 2.1

Configurar PostgreSQL y Liquibase.

### Tarea 2.2

Crear changelog inicial.

Debe incluir:

- users.
- roles.
- user_roles.
- shops.
- shop_members.
- product_categories.
- master_products.
- product_suggestions.
- shop_products.
- collections.
- collection_items.
- reservations.

## EPIC 3 - Seguridad

### Tarea 3.1

Implementar registro.

### Tarea 3.2

Implementar login.

### Tarea 3.3

Implementar refresh token.

### Tarea 3.4

Implementar autorización por rol.

## EPIC 4 - Usuarios y tiendas

### Tarea 4.1

Implementar `GET /api/users/me`.

### Tarea 4.2

Implementar creación y consulta de tiendas.

### Tarea 4.3

Implementar modelo de miembros de tienda.

### Tarea 4.4

Aplicar aislamiento lógico por tienda.

## EPIC 5 - Catálogo maestro

### Tarea 5.1

CRUD de productos maestros.

### Tarea 5.2

Detección básica de duplicados.

### Tarea 5.3

Sugerencias de productos por usuarios.

## EPIC 6 - Inventario

### Tarea 6.1

CRUD de productos de tienda.

### Tarea 6.2

Validar stock, precio y estado físico.

### Tarea 6.3

Rellenar datos desde catálogo maestro cuando sea posible.

## EPIC 7 - Colecciones

### Tarea 7.1

CRUD de colecciones.

### Tarea 7.2

Añadir productos a colección.

### Tarea 7.3

Estados de colección.

### Tarea 7.4

Visibilidad pública/privada.

## EPIC 8 - Reservas

### Tarea 8.1

Crear reserva.

### Tarea 8.2

Aceptar/rechazar reserva por tienda.

### Tarea 8.3

Cancelar reserva por usuario.

### Tarea 8.4

Expirar reservas según configuración de tienda.

### Tarea 8.5

Completar reserva manualmente.

## EPIC 9 - Recomendaciones

### Tarea 9.1

Recomendar productos marcados como MISSING/WANTED que estén disponibles en tiendas.

### Tarea 9.2

Recomendar productos relacionados por franquicia/categoría.

## EPIC 10 - Archivos

### Tarea 10.1

Subida de portada de producto.

### Tarea 10.2

Subida de logo de tienda.

### Tarea 10.3

Crear interfaz de almacenamiento para migración futura.

## EPIC 11 - Calidad

### Tarea 11.1

Tests unitarios.

### Tarea 11.2

Tests integración con Testcontainers.

### Tarea 11.3

Swagger/OpenAPI.

### Tarea 11.4

GitHub Actions.

### Tarea 11.5

Docker Compose.

## EPIC 12 - Frontend

No empezar hasta cerrar backend MVP.
