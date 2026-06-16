# Seguridad y privacidad

## Autenticación MVP

- Email/password.
- JWT access token.
- Refresh token.
- Password hash seguro.
- Logout lógico invalidando refresh token.

No implementar en MVP:

- OAuth/Google.
- 2FA.
- Passkeys.

Deben quedar como evolutivos.

## Autorización

Roles globales:

- ADMIN.
- USER.
- SHOP_OWNER.
- CONTENT_CREATOR.

Los roles son acumulables.

Un usuario puede tener:

- Perfil personal privado.
- Perfil tienda público/comercial.

## Permisos por tienda

Una tienda puede tener miembros con distintos permisos.

Roles internos previstos:

- OWNER.
- MANAGER.
- EMPLOYEE.

En MVP basta con OWNER, pero el modelo debe quedar preparado.

## Multitenancy lógico

- Una sola base de datos.
- Aislamiento por `shop_id` / `tenant_id` en entidades de tienda.
- Validación obligatoria en servicios y repositorios.
- Una tienda no puede consultar ni modificar recursos de otra tienda.
- ADMIN puede acceder globalmente.

## Datos personales

Minimizar datos personales en MVP.

Datos mínimos:

- Email.
- Nombre visible.
- Preferencia de idioma.

No recopilar ubicación exacta en MVP.

## Edad recomendada

La plataforma se diseñará como recomendada para mayores de 18 años.

## Imágenes y archivos

MVP:

- Portada de producto.
- Logo de tienda.

Fase futura:

- Foto de perfil.
- Fotos de colección.
- Multimedia en publicaciones y comentarios.

Todas las subidas futuras deberán validar:

- Tamaño.
- Tipo MIME.
- Extensión.
- Seguridad del nombre del archivo.

## Comentarios y contenido generado por usuarios

No entra en MVP.

Cuando se implemente:

- Usuarios y tiendas deben poder decidir si aceptan comentarios en publicaciones.
- Solo debe permitirse comentar publicaciones públicas.
- Debe existir moderación/reportes.
- Deben existir reglas de contenido.

## Pagos

No entran en MVP.

Cuando se implementen:

- Usar proveedor externo seguro.
- No almacenar datos de tarjeta.
- Tener en cuenta SCA/PSD2 en Europa.
- Registrar estados de pago y reembolsos.

## GDPR/RGPD

Desde el diseño se deben tener en cuenta:

- Derecho de acceso.
- Derecho de rectificación.
- Derecho de supresión.
- Portabilidad futura.
- Política de privacidad clara.
- Consentimiento para comunicaciones comerciales.

## Logs

No registrar:

- Contraseñas.
- Tokens.
- Datos sensibles.
- Información de pago futura.

## Errores

No devolver trazas técnicas al frontend.

Usar formato común de error.
