# Editorial data quality

## Objetivo

EPIC 42A define calidad editorial basica y ayudas visuales futuras sin cambiar
constraints, migraciones ni validaciones backend. Estas reglas no sustituyen la
autorizacion ni los conflictos ya devueltos por la API.

## Riesgos y reglas anti-duplicados

| Entidad | Regla visual recomendada | Check manual |
| --- | --- | --- |
| Publishers | Nombre con `trim`, no vacio y pais de dos letras. Advertir coincidencia exacta en resultados cargados. | Revisar variantes de marca/editorial. |
| Franchises | Nombre y slug normalizados; slug en minusculas. Advertir nombre o slug ya cargado. | Revisar franquicias con titulos alternativos. |
| Series | Titulo obligatorio, IDs positivos y anos coherentes. Advertir mismo titulo en franquicia si esta disponible. | Revisar publisher/franchise antes de crear. |
| Items | Serie obligatoria, titulo obligatorio y orden no negativo. Advertir titulo o secuencia repetida en serie cargada. | Revisar volumen y etiqueta de secuencia. |
| Editions | Item obligatorio, publisher positivo, paginas positivas y URL http/https. Advertir editionName/ISBN/EAN repetido en item cargado. | Revisar formato, pais e identificadores. |
| Creators | Nombre obligatorio, slug normalizado, pais ISO de dos letras y anos coherentes. Advertir nombre/slug ya cargado. | Revisar alias y orden del nombre. |
| Credits | Item, creator, rol y orden positivo. Advertir creator+rol repetido en item. | Revisar si es un credito distinto. |
| Relationships | Source/target distintos y orden positivo. Advertir source+target+tipo repetido. | Revisar direccion y tipo. |
| Master links | IDs positivos y confianza 0..1. Advertir triple masterProduct/item/edition repetido. | Revisar evidencia y estado VERIFIED. |

## Normalizacion y estado

El frontend ya aplica `trim` a textos, convierte opcionales vacios a `null`,
normaliza `country` a mayusculas y normaliza slugs cuando el formulario lo
requiere. Los warnings deben ser no bloqueantes y solo usar resultados en
memoria: no se anaden llamadas HTTP ni contratos.

## Pendiente

Faltan reglas fuertes backend, datos reales, metricas de catalogo, roles
editoriales separados y E2E futuros. La siguiente evolucion recomendada es
EPIC 42B - Hardening admin editorial y validaciones backend selectivas.

## Reglas ya endurecidas en backend

- Credits: unicidad activa por item, creator y rol, incluida la exclusion del
  propio registro al actualizar.
- Relationships: source y target distintos, orden positivo por DTO y unicidad
  activa por source, target y tipo.
- Master product links: IDs y confianza validados por DTO, enlace exacto por
  estado y un solo VERIFIED activo por master product.
- Creators: slug normalizado unico y, desde EPIC 42B, nombre exacto unico sin
  distincion de mayusculas entre registros no eliminados.

## Reglas pendientes para futuras EPICs

Publishers, franchises, series, items y editions requieren decisiones con datos
reales, normalizacion y posiblemente constraints/migraciones. Siguen pendientes
fuzzy matching, aliases, metricas y reportes de duplicados.

## Reporte de lectura

`GET /api/catalog/admin/data-quality/report` es ADMIN-only y no modifica datos.
Acepta `scope` y `limit` (1..200, 50 por defecto). Usa agrupaciones exactas de
registros no eliminados para publishers, franchises, series con franchise,
items, editions, creators y master links. No realiza fuzzy matching, auto-fix,
merge ni borrado.
