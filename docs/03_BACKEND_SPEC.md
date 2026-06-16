# Especificación backend

## Stack

- Java 25.
- Spring Boot 4.x.
- Maven.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- PostgreSQL.
- Liquibase.
- Validation.
- Swagger/OpenAPI.
- JUnit.
- Mockito.
- Testcontainers.

Si el entorno no soporta alguna versión concreta, Codex debe documentar el cambio en `docs/13_DECISIONS.md` antes de modificarla.

## Módulos MVP

### auth

Responsable de:

- Registro.
- Login.
- Refresh token.
- Logout lógico.
- Hash de contraseña.
- Validación de credenciales.

No implementar OAuth ni 2FA en MVP.

### users

Responsable de:

- Usuario global.
- Perfil personal.
- Roles acumulables.
- Preferencias de idioma.
- Preferencias básicas.

Un usuario puede ser usuario normal y propietario de tienda.

### shops

Responsable de:

- Tiendas.
- Propietario de tienda.
- Miembros de tienda.
- Roles internos de tienda.
- Configuración de reservas.

Roles internos futuros:

- SHOP_OWNER.
- SHOP_MANAGER.
- SHOP_EMPLOYEE.

En MVP debe existir la estructura para miembros, aunque solo se use realmente SHOP_OWNER.

### catalog

Responsable de:

- Catálogo maestro.
- Productos base.
- Ediciones.
- Variantes.
- Detección básica de duplicados.
- Sugerencias de productos por usuarios.

Los productos maestros pueden crearlos ADMIN y SHOP_OWNER.

Los USER pueden sugerir productos, pero deben quedar pendientes de revisión.

### inventory

Responsable de:

- Productos de tienda.
- Precio.
- Stock.
- Estado físico.
- Estado comercial.
- Relación con producto maestro.
- Relleno automático de datos si ya existen en el catálogo.

### collections

Responsable de:

- Colecciones de usuario.
- Productos dentro de colección.
- Estados de colección.
- Estado físico personal.
- Visibilidad pública/privada.
- Varias colecciones por usuario.

### reservations

Responsable de:

- Reserva sin pago.
- Solicitud de reserva.
- Aceptación/rechazo por tienda.
- Cancelación.
- Expiración.
- Finalización manual.

La duración de la reserva debe ser configurable por tienda.

### recommendations

Responsable de:

- Productos buscados disponibles en tiendas.
- Productos marcados como MISSING o WANTED.
- Productos en stock que coincidan con intereses.

No usar IA en MVP.

### files

Responsable de:

- Subida básica de imágenes.
- Portada de producto.
- Logo de tienda.
- Abstracción para almacenamiento futuro en S3/Blob/Cloud Storage.

### audit

Responsable de:

- created_at.
- created_by.
- updated_at.
- updated_by.
- deleted_at.
- deleted_by.
- Borrado lógico.

## Reglas técnicas

- No exponer entidades JPA directamente.
- Usar DTOs para request/response.
- Usar mappers explícitos o MapStruct si se decide añadirlo.
- Validar entrada con Bean Validation.
- Responder errores con formato común.
- No mezclar lógica de negocio en controladores.
- No usar `System.out.println`.
- Usar logs estructurados.
- Cada endpoint protegido debe validar usuario, rol y tienda si aplica.

## Paquetes compartidos

`shared` debe contener:

- Excepciones comunes.
- Respuesta de error.
- Utilidades de paginación.
- Tipos comunes.
- Constantes de seguridad.

## Criterio de terminado

Un módulo se considera terminado cuando:

1. Tiene entidades/modelo.
2. Tiene migraciones Liquibase.
3. Tiene repositorios.
4. Tiene servicios.
5. Tiene controladores.
6. Tiene DTOs.
7. Tiene validaciones.
8. Tiene tests.
9. Tiene endpoints documentados en Swagger.
10. Tiene reglas de autorización.
