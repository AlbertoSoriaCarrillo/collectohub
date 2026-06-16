# Decisiones del proyecto

## 2026-06-16 - Nombre provisional

- Decisión: usar CollectoHub como nombre provisional.
- Motivo: permite trabajar con un nombre estable sin decidir todavía marca comercial.

## 2026-06-16 - Arquitectura inicial

- Decisión: monorepo y monolito modular.
- Motivo: reduce complejidad inicial y facilita validar el MVP.

## 2026-06-16 - Backend primero

- Decisión: desarrollar primero el backend.
- Motivo: el backend define seguridad, datos, reglas de negocio y API.

## 2026-06-16 - Base de datos

- Decisión: usar PostgreSQL con una única base de datos.
- Motivo: no se ven beneficios suficientes en separar bases por tienda durante MVP.

## 2026-06-16 - Multitenancy lógico

- Decisión: aislamiento lógico por tienda desde la capa Java y mediante claves como shop_id o tenant_id.
- Motivo: permite separar datos de tiendas sin complejidad de múltiples bases.

## 2026-06-16 - Frontend

- Decisión: Angular con TypeScript y Angular Material.
- Motivo: estructura más cerrada y mantenible para una aplicación grande.

## 2026-06-16 - Reservas

- Decisión: en MVP habrá reservas sin pago.
- Motivo: permite validar la conexión usuario-tienda sin la complejidad legal y técnica de pagos.

## 2026-06-16 - Social y marketplace

- Decisión: dejar chat, feed social, comentarios multimedia, pagos y marketplace completo para fases futuras.
- Motivo: evitar que el MVP sea inabarcable.

## 2026-06-16 - Idiomas

- Decisión: castellano e inglés desde el inicio.
- Motivo: preparar la plataforma para crecimiento internacional.

## 2026-06-16 - Edad recomendada

- Decisión: plataforma recomendada para mayores de 18 años.
- Motivo: en fases futuras habrá contenido publicado por usuarios y tiendas difícil de controlar completamente.
