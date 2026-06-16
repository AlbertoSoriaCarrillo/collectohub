# Especificación de arquitectura

## Tipo de arquitectura

El proyecto debe empezar como monolito modular, no como microservicios.

Motivos:

- Menor complejidad inicial.
- Más fácil de desarrollar y probar.
- Más adecuado para validar el producto.
- Permite separar módulos sin desplegar servicios independientes.

## Monorepo

Estructura objetivo:

```text
collectohub/
 ├── backend/
 ├── frontend/
 ├── docs/
 ├── infra/
 ├── .github/workflows/
 ├── PROMPT_FOR_CODEX.md
 ├── README.md
 └── .gitignore
```

## Backend

Arquitectura por módulos funcionales:

```text
backend/src/main/java/com/collectohub
 ├── auth
 ├── users
 ├── shops
 ├── catalog
 ├── inventory
 ├── collections
 ├── reservations
 ├── recommendations
 ├── files
 ├── i18n
 ├── audit
 ├── shared
 └── config
```

## Capas recomendadas por módulo

Cada módulo debe seguir una estructura similar:

```text
module/
 ├── api
 ├── application
 ├── domain
 ├── infrastructure
 └── dto
```

Donde:

- `api`: controladores REST.
- `application`: servicios de caso de uso.
- `domain`: entidades, enums y reglas de negocio.
- `infrastructure`: repositorios, adapters y persistencia.
- `dto`: requests, responses y mappers.

## Base de datos

- Una única base de datos PostgreSQL.
- Aislamiento lógico por `tenant_id` o `shop_id`.
- No usar una base de datos por tienda.
- No usar schema separado por tienda en MVP.

## Multitenancy lógico

El aislamiento debe hacerse desde la capa Java de acceso a datos y lógica de negocio.

Reglas:

- Una tienda no puede ver datos internos de otra tienda.
- Un usuario puede tener perfil personal y perfil tienda.
- Los roles son acumulables.
- Un `SHOP_OWNER` puede gestionar una o varias tiendas.
- Debe existir un modelo preparado para miembros de tienda con diferentes permisos.

## Internacionalización

Separar:

- Idioma de interfaz.
- Idioma del producto.
- Países de publicación del producto.
- País de la tienda.
- Moneda.
- Fiscalidad futura.

Idiomas iniciales:

- Español/castellano.
- Inglés.

## Archivos

MVP:

- Almacenamiento local para imágenes.
- Debe quedar abstraído mediante una interfaz para migrar en el futuro a S3, Google Cloud Storage o Azure Blob Storage.

## Buscador

MVP:

- Búsqueda con PostgreSQL e índices.

Futuro:

- OpenSearch o Elasticsearch.

## Seguridad

- JWT + refresh token.
- Email/password en MVP.
- OAuth/Google en fase futura.
- 2FA en fase futura.

## Decisión importante

No optimizar prematuramente. El objetivo del MVP es construir una base limpia y validable.
