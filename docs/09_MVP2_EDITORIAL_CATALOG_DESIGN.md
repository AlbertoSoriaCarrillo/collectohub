# CollectoHub MVP 2 editorial catalog technical design

Estado: diseno tecnico aprobado en EPIC 30. EPIC 31 y 32 implementan el catalogo
editorial; EPIC 33 implementa puente, propuestas y reconciliacion ADMIN. La
fachada de compatibilidad ya esta implementada; la UI editorial sigue pendiente.

## Estado de implementacion

EPIC 31, completada el 2026-06-30, anade de forma aditiva:

- `publishers`, `catalog_franchises` y `catalog_series` mediante Liquibase 005;
- entidades, enums, repositorios, servicios y DTOs paginados;
- doce endpoints bajo `/api/catalog`;
- lectura publica de registros `ACTIVE` y escritura solo `ADMIN`;
- auditoria compatible con actores `BIGINT` y borrado logico;
- tests de migracion, servicios, validacion y seguridad.

EPIC 32, completada el 2026-06-30, anade de forma aditiva:

- `catalog_items` y `catalog_item_editions` mediante Liquibase 006;
- identidad coleccionable separada de sus ediciones concretas;
- ocho formatos editoriales, ISBN/EAN normalizados y unicidad parcial;
- ocho endpoints paginados con lectura publica y escritura `ADMIN`;
- validacion de la cadena `series -> item -> edition` y publisher opcional;
- tests de servicios, seguridad, validacion, duplicados y migracion.

EPIC 33, completada el 2026-06-30, anade `master_product_catalog_links`,
backfill idempotente que solo crea propuestas, reconciliacion ADMIN y siete
endpoints protegidos. No modifica consumidores legacy.

No modifica `master_products`, `collection_items`, `shop_products`, matching ni
frontend. Los elementos aun no implementados de este documento siguen siendo
diseno futuro.

## 1. Objetivo de MVP 2

MVP 2 debe convertir el catalogo generico actual en una biblioteca comun capaz
de distinguir:

```text
Franquicia -> Serie de catalogo -> Item de catalogo -> Edicion
                                      |
                                      +-> Item de coleccion personal
```

El primer alcance editorial cubre libros, comics y manga. El modelo debe poder
admitir despues video, cartas, merchandising, figuras, videojuegos, consolas y
otros objetos sin mezclar esas fases con la implementacion inicial.

MVP 2 no sustituye de golpe `master_products`, no activa marketplace, no
implementa uploads ni convierte las sugerencias de usuarios en una wiki abierta.

## 2. Recomendacion ejecutiva

Se recomienda la opcion 3: **modelo editorial nuevo con puente de
compatibilidad**.

- Las nuevas entidades editoriales pasan a representar identidad y conocimiento.
- `master_products` conserva sus IDs y contratos durante la transicion.
- Una tabla `master_product_catalog_links` enlaza cada producto maestro migrado
  con un item editorial y, cuando se conoce, una edicion concreta.
- `collection_items` y `shop_products` continuan usando `master_product_id` hasta
  que cada dominio adopte referencias editoriales en sub-EPICs separadas.
- No se usan triggers ni una migracion destructiva; la compatibilidad se
  resuelve en servicios de aplicacion y procesos de backfill idempotentes.

Esta estrategia es viable con el codigo actual porque todos los consumidores
convergen en `master_product_id`. El puente permite resolver identidad editorial
sin cambiar simultaneamente 25 archivos backend y 36 archivos frontend que hoy
dependen directa o indirectamente de ese contrato.

## 3. Conclusiones por perspectiva

### Product Agent

- El valor imprescindible es navegar una jerarquia editorial fiable y evitar
  duplicados de la misma obra o edicion.
- Para MVP 2 bastan franquicias opcionales, series, items, ediciones y
  publishers; la experiencia debe seguir siendo util aunque falten creators o
  relaciones avanzadas.
- El coleccionista debe poder marcar una obra/volumen sin saber su edicion
  exacta. Obligar a elegir ISBN reduciria adopcion y calidad de datos.
- Authors/creators entran despues de estabilizar items y ediciones. Adaptaciones,
  precuelas, spin-offs y grafos de relaciones pueden esperar.
- No se debe presentar el catalogo como wiki colaborativa hasta tener revision,
  trazabilidad y moderacion.

### Domain Agent

- `CatalogSeries` es el contenedor editorial ordenado. Puede tener una sola obra
  para evitar dos ramas de navegacion distintas para titulos independientes.
- `CatalogItem` representa la identidad coleccionable que una persona puede
  tener, querer o echar en falta: libro, volumen o numero.
- `CatalogItemEdition` representa una publicacion concreta con editorial,
  idioma, formato e identificadores comerciales.
- Publisher pertenece principalmente a la edicion. Una serie puede tener un
  publisher principal opcional solo como metadato editorial.
- Franchise es opcional: no debe inventarse para obras independientes.
- El estado editorial de una serie (`ONGOING`, etc.) es distinto del estado de
  publicacion del registro (`DRAFT`, `ACTIVE`, `ARCHIVED`).

### Backend Agent

- `master_products` mezcla hoy item y edicion: nombre, volumen, editorial,
  ISBN/EAN, idioma, fechas, portada y atributos conviven en una sola fila.
- La deteccion de duplicados actual por ISBN, EAN o
  `name + franchise + volumeNumber + language` no modela equivalencia entre
  obras y ediciones.
- Colecciones, inventario, recomendaciones y reservas dependen del ID existente;
  reemplazarlo de una vez produciria un cambio transversal de alto riesgo.
- Los endpoints nuevos deben vivir bajo `/api/catalog/**`; los endpoints
  `/api/master-products` permanecen compatibles y se deprecan solo con criterios
  medibles.
- La nueva API debe paginar listados desde el principio.

### Frontend Agent

- `/catalog` debe evolucionar de lista plana a buscador editorial con resultados
  por serie/item y filtros, no a una portada decorativa de franquicias.
- La navegacion natural es franquicia, serie, item y edicion, con breadcrumbs y
  URLs estables.
- El selector para colecciones debe elegir primero item y ofrecer edicion como
  refinamiento opcional.
- `/catalog/:id` debe mantenerse como pantalla/alias de producto maestro durante
  la transicion. Las rutas literales nuevas deben declararse antes de ese
  parametro en Angular.
- No se implementa ningun cambio frontend en EPIC 30.

### Data/Migration Agent

- La migracion debe ser aditiva, idempotente, observable y reversible mientras
  el modelo anterior siga siendo fuente operativa.
- Los textos `franchise`, `collection_name` y `publisher` sirven para crear
  candidatos, no para afirmar automaticamente identidades verificadas.
- ISBN/EAN permiten proponer ediciones con mas confianza, pero se normalizan y
  revisan antes de imponer unicidad.
- El puente debe registrar estado y origen del enlace para separar candidatos
  automaticos de relaciones verificadas.
- No se elimina ni se hace nullable ninguna FK actual durante las primeras
  sub-EPICs.

### QA Agent

- Cada migracion se prueba sobre base vacia y sobre un dataset MVP 1 poblado.
- Los 35 endpoints anteriores a MVP 2 y el flujo
  Home/Catalogo/Colecciones/Buscados deben conservar comportamiento durante la
  transicion.
- El backfill necesita pruebas de repeticion, duplicados, huerfanos y
  reconciliacion manual.
- Deben existir tests de permisos, visibilidad de borradores, constraints,
  paginacion, filtros y rendimiento de consultas.
- Matching se prueba por separado para coincidencia exacta de edicion y flexible
  por item cuando esas reglas se implementen.

## 4. Modelo actual y limites

### Tablas y calculos existentes

| Elemento | Que resuelve hoy | Limite para MVP 2 |
| --- | --- | --- |
| `product_categories` | Clasificacion amplia y filtros actuales | `MANGA_COMIC` no distingue manga de comic; no representa jerarquia editorial. |
| `master_products` | Ficha comun reutilizada por todos los dominios | Mezcla obra, volumen, edicion, identificadores y publisher. |
| `collections` | Agrupacion personal y visibilidad | Su categoria es amplia; no expresa una serie editorial. |
| `collection_items` | Estado personal sobre un producto maestro | Solo conoce `master_product_id`; no distingue deseo de obra frente a edicion. |
| `shop_products` | Stock, precio, condicion y disponibilidad | Vende un master product que puede no representar una edicion concreta. |
| `recommendations` | Calcula matches `MISSING/WANTED` contra stock | La igualdad se basa exclusivamente en el mismo `master_product_id`. |

### Lo que `master_products` hace bien

- Da una identidad comun a colecciones e inventario.
- Conserva IDs estables y contratos ya probados.
- Permite busqueda simple y campos flexibles JSONB.
- Tiene auditoria, borrado logico, estado y deteccion inicial de duplicados.

### Lo que no puede expresar bien

- Una franquicia con ramas editoriales distintas.
- Una serie ordenada con items independientes.
- Una obra con varias ediciones, idiomas, formatos, ISBN o publishers.
- Credits estructurados de autores, guionistas, dibujantes o traductores.
- Relaciones entre obras sin usar texto o JSON sin integridad referencial.
- Matching flexible entre una necesidad de obra y varias ediciones aceptables.

`franchise` y `collection_name` son textos repetidos. No tienen ID, constraints,
estado, auditoria propia ni una forma segura de renombrarse. Variantes de
mayusculas, traducciones o escritura crean grupos falsamente distintos.

Migrar demasiado pronto puede romper IDs, contratos y datos personales. Mantener
el modelo plano demasiado tiempo aumenta duplicados, hace ambiguo el matching y
convierte `attributes` en un esquema oculto sin integridad.

## 5. Modelo conceptual propuesto

```text
CatalogFranchise (opcional)
  -> CatalogSeries
       -> CatalogItem
            -> CatalogItemEdition (0..n)

Publisher -> CatalogItemEdition
Creator -> CatalogItemCreator -> CatalogItem

MasterProduct -> MasterProductCatalogLink
              -> CatalogItem
              -> CatalogItemEdition (opcional)
```

Reglas:

1. Todo `CatalogItem` pertenece a una `CatalogSeries`.
2. Una serie puede ser un contenedor de un unico item para obras independientes.
3. Una serie puede no tener franquicia.
4. Un item puede existir sin edicion conocida.
5. Una edicion siempre pertenece a un item y representa una publicacion concreta.
6. Datos personales nunca se guardan en las tablas editoriales.
7. Stock pertenece a una edicion cuando esa identidad ya esta disponible.

### Papel de product_categories

`product_categories` se conserva para los endpoints y filtros actuales. No se
reutiliza como sustituto de `catalog_series.type`: una categoria amplia y un
tipo editorial tienen responsabilidades distintas. En particular,
`MANGA_COMIC` no permite inferir de forma segura `MANGA` frente a `COMIC`, por lo
que el backfill requiere la informacion legacy disponible o revision manual.
No se renombran ni eliminan categorias durante MVP 2.

## 6. Entidades fisicas candidatas

Todas las tablas nuevas seguirian el audit set actual:
`created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`,
`deleted_by`.

### catalog_franchises

Imprescindible para MVP 2, pero opcional en las relaciones.

| Campo | Proposito |
| --- | --- |
| `id` | Identificador estable. |
| `name` | Nombre publico principal. |
| `slug` | Identificador URL normalizado y unico entre filas activas. |
| `description` | Contexto editorial breve. |
| `record_status` | `DRAFT`, `ACTIVE`, `ARCHIVED`. |
| audit set | Trazabilidad y borrado logico. |

No se modelan propietarios de IP, traducciones de nombre ni aliases en la
primera subfase.

### publishers

Imprescindible para identificar ediciones.

| Campo | Proposito |
| --- | --- |
| `id` | Identificador estable. |
| `name` | Nombre editorial/marca. |
| `country` | Pais opcional. |
| `record_status` | Estado de publicacion del registro. |
| audit set | Trazabilidad y borrado logico. |

Aliases, grupos empresariales y datos legales se difieren.

### catalog_series

Imprescindible para MVP 2. Representa serie, linea o agrupacion editorial,
incluido un contenedor de un solo item.

| Campo | Proposito |
| --- | --- |
| `id` | Identificador estable. |
| `franchise_id` | FK opcional a `catalog_franchises`. |
| `primary_publisher_id` | FK opcional; metadata por defecto, no sustituye publisher de edicion. |
| `title` | Titulo principal. |
| `original_title` | Titulo original opcional. |
| `type` | Inicialmente `BOOK`, `COMIC`, `MANGA`; enum preparado para tipos futuros. |
| `publication_status` | `ONGOING`, `COMPLETED`, `CANCELLED`, `HIATUS`, `UNKNOWN`. |
| `description` | Descripcion editorial. |
| `origin_country` | Pais de origen opcional. |
| `original_language` | Idioma original opcional. |
| `start_year`, `end_year` | Rango editorial opcional. |
| `record_status` | `DRAFT`, `ACTIVE`, `ARCHIVED`; evita un booleano `active` ambiguo. |
| audit set | Trazabilidad y borrado logico. |

Tipos reservados para el futuro: `VIDEO`, `CARD`, `MERCHANDISING`, `FIGURE`,
`VIDEOGAME`, `CONSOLE`, `OTHER`. La UI de MVP 2 solo ofrece los tres iniciales.

### catalog_items

Imprescindible para MVP 2. Es la identidad que una persona colecciona aunque no
conozca la edicion exacta.

| Campo | Proposito |
| --- | --- |
| `id` | Identificador estable. |
| `series_id` | FK obligatoria a `catalog_series`. |
| `title` | Titulo principal del item. |
| `original_title` | Titulo original opcional. |
| `sequence_label` | Numero/volumen visible, admite `1`, `1A` o `Special`. |
| `sort_order` | Valor numerico opcional para ordenar sin interpretar el label. |
| `description` | Descripcion comun a todas las ediciones. |
| `first_publication_date` | Fecha original opcional. |
| `first_publication_year` | Ano cuando no se conoce fecha exacta. |
| `original_language` | Idioma original opcional. |
| `origin_country` | Pais de origen opcional. |
| `record_status` | `DRAFT`, `ACTIVE`, `ARCHIVED`. |
| audit set | Trazabilidad y borrado logico. |

No se mantienen simultaneamente campos ambiguos `number` y `volume`: un label
de secuencia y un orden opcional cubren ambos casos sin perder valores como
numeros especiales.

### catalog_item_editions

Entra en MVP 2 porque ISBN, idioma, formato y publisher pertenecen a una edicion
y las tiendas venden copias concretas. Puede implementarse despues de items en
una sub-EPIC separada.

| Campo | Proposito |
| --- | --- |
| `id` | Identificador estable. |
| `catalog_item_id` | FK obligatoria a `catalog_items`. |
| `publisher_id` | FK opcional a `publishers`; puede desconocerse inicialmente. |
| `isbn` | ISBN normalizado opcional. |
| `ean` | EAN normalizado opcional. |
| `format` | Formato editorial como `HARDCOVER`, `PAPERBACK`, `DIGITAL` u `OTHER`. |
| `edition_name` | Nombre comercial de la edicion. |
| `publication_date`, `publication_year` | Fecha o ano de esta edicion. |
| `language` | Idioma de esta edicion. |
| `country` | Mercado/pais de publicacion. |
| `page_count` | Numero de paginas opcional y positivo. |
| `cover_image_url` | URL compatible temporal; uploads siguen fuera de alcance. |
| `record_status` | `DRAFT`, `ACTIVE`, `ARCHIVED`. |
| audit set | Trazabilidad y borrado logico. |

ISBN y EAN deben tener indices unicos parciales para registros no eliminados
despues de normalizar guiones y espacios. La equivalencia sin identificador se
valida en aplicacion con item, publisher, idioma, formato, nombre y fecha.

### creators y catalog_item_creators

Necesarios para completar MVP 2, pero no para el primer corte de persistencia.
Se implementan cuando item/edicion sean estables.

`creators` contiene identidad y nombre. `catalog_item_creators` enlaza item,
creator, rol (`AUTHOR`, `WRITER`, `ARTIST`, `ILLUSTRATOR`, `TRANSLATOR`,
`EDITOR`, `OTHER`) y orden de credito. Un creator puede tener varios roles.

No se recomienda `catalog_series_creators` inicialmente: los creditos de serie
se pueden derivar de sus items y el significado de un credito global es
ambiguo. Se reconsidera con casos reales.

### catalog_relationships

Se documenta, pero queda fuera del nucleo MVP 2. Las relaciones `ADAPTATION`,
`REMAKE`, `REPRINT`, `SAME_WORK`, `SPIN_OFF`, `PREQUEL`, `SEQUEL` y `RELATED`
requieren definir direccion, simetria y moderacion antes de persistirlas.

### Constraints e indices candidatos

- Indice por cada FK y por `record_status` en listados publicos.
- Slug de franchise unico entre filas no eliminadas.
- Indices de series por `franchise_id`, `type`, `publication_status` y titulo.
- Indices de items por `series_id`, `sort_order` y titulo; no se impone unicidad
  solo por numero porque existen especiales, variantes y numeraciones repetidas.
- ISBN y EAN normalizados con unicidad parcial en editions activas/no eliminadas.
- Indices de editions por item, publisher, idioma, formato y fecha.
- Checks de anos coherentes, `page_count > 0` y pertenencia de edition al item
  enlazado.
- Unicidad activa de `master_product_id` en el puente y, tras reconciliar
  duplicados, de `catalog_item_edition_id` para enlaces `VERIFIED`.

## 7. Puente de compatibilidad

Tabla candidata:

```text
master_product_catalog_links
  id
  master_product_id              UNIQUE, NOT NULL
  catalog_item_id                NOT NULL
  catalog_item_edition_id        NULL
  link_status                    PROPOSED | VERIFIED | REJECTED
  link_source                    MIGRATION | MANUAL | AUTHORING
  audit fields
```

Reglas:

- El item es obligatorio cuando existe un enlace.
- La edicion es opcional y debe pertenecer al item enlazado.
- Solo enlaces `VERIFIED` alimentan compatibilidad automatica y matching futuro.
- Un master product tiene como maximo un enlace activo.
- Una edicion verificada debe tener como maximo un master product canonico; los
  duplicados se resuelven antes de imponer esa unicidad parcial.
- No se copian estados personales ni stock al puente.

Se prefiere tabla puente frente a cinco FKs opcionales en `master_products`
porque encapsula el estado de migracion, conserva limpia la tabla operativa y
permite auditar propuestas sin convertirlas en verdad editorial.

## 8. Comparacion de estrategias

| Opcion | Ventaja | Riesgo | Veredicto |
| --- | --- | --- | --- |
| Extender `master_products` | Menor cambio inicial | Mantiene mezcla conceptual y multiplica nullable FKs | No recomendada como modelo final |
| Modelo paralelo | Modelo limpio | Dos catalogos sin identidad comun; colecciones e inventario quedan aislados | Insuficiente por si sola |
| Modelo nuevo + puente | Transicion gradual y contratos estables | Exige backfill, reconciliacion y compatibilidad temporal | Recomendada |

## 9. Estrategia de migracion

### Fase 0 - Baseline

- Congelar ejemplos y contratos actuales con tests.
- Medir filas, duplicados por ISBN/EAN y combinaciones logicas.
- Identificar productos sin franchise, collection, publisher o identificadores.
- Preparar consultas de reconciliacion; no modificar datos.

### Fase 1 - Esquema aditivo

- Crear publishers, franchises y series.
- Crear items y editions en un changeset posterior.
- Crear constraints e indices sin tocar FKs existentes.
- Mantener Hibernate en `validate` y Liquibase como unica fuente de esquema.

### Fase 2 - Candidatos de backfill

- Normalizar texto sin sobrescribir `master_products`.
- Agrupar `franchise` y `collection_name` como candidatos `DRAFT`.
- Crear serie de un item cuando no exista agrupacion fiable.
- Crear item desde nombre y secuencia.
- Crear edicion cuando haya evidencia editorial suficiente, especialmente
  ISBN/EAN, publisher, idioma o fecha.
- Marcar enlaces automaticos `PROPOSED`, nunca `VERIFIED` por defecto.

### Fase 3 - Reconciliacion y puente

- Revisar colisiones y confirmar enlaces.
- Ejecutar backfill idempotente: repetirlo no crea nuevas filas equivalentes.
- Publicar metricas de enlazados, no enlazados, ambiguos y duplicados.
- Mantener todos los IDs y campos de `master_products`.

### Fase 4 - API editorial

- Activar endpoints `/api/catalog/**` sobre registros `ACTIVE`.
- Conservar `/api/master-products` sin cambios incompatibles.
- Para enlaces verificados, permitir que la fachada legacy lea datos
  editoriales con fallback a sus columnas actuales.
- Evitar dual-write por trigger; una capa de compatibilidad transaccional decide
  que entidad es fuente de verdad.

### Fase 5 - Adopcion por dominios

- Anadir referencias editoriales nullable a colecciones e inventario en EPICs
  independientes.
- Backfill desde el puente y comparar resultados antes de cambiar lecturas.
- Mantener `master_product_id` durante todo MVP 2.

### Fase 6 - Deprecacion futura

No se elimina `master_products` hasta que:

- todas las filas activas esten enlazadas o justificadas;
- clientes frontend y scripts demo usen la nueva API;
- colecciones e inventario tengan referencias editoriales completas;
- matching ya no dependa de igualdad de master product;
- exista al menos una version de compatibilidad y plan de rollback probado.

## 10. Impacto en colecciones

Modelo objetivo:

```text
collection_items
  catalog_item_id             requerido al final de la transicion
  catalog_item_edition_id     opcional
  master_product_id           conservado durante MVP 2
```

- `WANTED` y `MISSING` deben poder apuntar solo al item: el usuario busca la
  obra/volumen y puede aceptar varias ediciones.
- `OWNED` puede indicar edicion exacta si la persona conoce ISBN, idioma o
  publisher, pero no debe exigirla.
- La edicion elegida debe pertenecer al item.
- Primero se anaden FKs nullable, se rellenan desde enlaces verificados y se
  comparan respuestas. Solo despues `catalog_item_id` puede hacerse obligatorio.
- Los DTOs antiguos con `masterProductId` permanecen aceptados. DTOs nuevos
  usaran `catalogItemId` y `catalogItemEditionId` opcional.
- Ninguna coleccion ni estado personal se pierde o se recrea durante el backfill.

## 11. Impacto en tiendas e inventario

Una tienda vende una edicion concreta, no una obra abstracta. El objetivo es:

```text
shop_products -> catalog_item_edition_id
              -> catalog_item_id derivado
```

Durante la transicion `master_product_id` sigue siendo obligatorio y el puente
aporta la edicion cuando esta verificada. Despues se anade una FK de edicion
nullable, se rellena y se valida antes de considerar hacerla obligatoria.

Una oferta sin edicion identificada puede seguir operando como legacy, pero no
debe entrar en coincidencias exactas. Gestion de almacen, pedidos, pagos,
bloqueo de stock y variantes fisicas complejas quedan fuera de MVP 2.

## 12. Impacto en Buscados y matching

Reglas futuras, en orden de precision:

1. **Exacta por edicion:** necesidad con edicion y stock de la misma edicion.
2. **Flexible por item:** necesidad sin edicion y cualquier edicion activa del
   mismo item, sujeta a preferencias.
3. **Por serie:** descubrimiento o completitud, no una coincidencia de compra
   automatica; debe mostrarse como sugerencia distinta.

Filtros posteriores: idioma, formato/edicion, condicion fisica, precio,
distancia y tienda. El motivo de cada match debe indicar `EXACT_EDITION`,
`COMPATIBLE_ITEM` o `SERIES_SUGGESTION`.

MVP 2 disena la identidad necesaria. Alertas, distancia, comparador y flujo
comercial pertenecen a MVP 4/5. El endpoint actual sigue igual hasta la sub-EPIC
de matching editorial.

## 13. Endpoints futuros

Todos los listados publicos deben paginar y aceptar `page`, `size` y `sort` con
limites de tamano. Solo registros `ACTIVE` son publicos.

### Franchises

```text
GET  /api/catalog/franchises
GET  /api/catalog/franchises/{id}
POST /api/catalog/franchises
PUT  /api/catalog/franchises/{id}
```

Filtros: `q`, `type`, `recordStatus` solo para administracion. DTOs:
`CatalogFranchiseSummaryResponse`, `CatalogFranchiseDetailResponse`,
`CreateCatalogFranchiseRequest`, `UpdateCatalogFranchiseRequest`.

### Series

```text
GET  /api/catalog/series
GET  /api/catalog/series/{id}
POST /api/catalog/series
PUT  /api/catalog/series/{id}
```

Filtros: `q`, `franchiseId`, `type`, `publicationStatus`, `publisherId`,
`language`, `country`. DTOs equivalentes `CatalogSeries*`.

### Items

```text
GET  /api/catalog/series/{seriesId}/items
GET  /api/catalog/items/{id}
POST /api/catalog/series/{seriesId}/items
PUT  /api/catalog/items/{id}
```

Filtros: `q`, `publicationYear`, `language`, `country`; orden por `sortOrder`.
DTOs: `CatalogItemSummaryResponse`, `CatalogItemDetailResponse`,
`CreateCatalogItemRequest`, `UpdateCatalogItemRequest`.

### Editions

```text
GET  /api/catalog/items/{itemId}/editions
GET  /api/catalog/editions/{id}
POST /api/catalog/items/{itemId}/editions
PUT  /api/catalog/editions/{id}
```

Filtros: `publisherId`, `isbn`, `ean`, `format`, `language`, `country`,
`publicationYear`. DTOs equivalentes `CatalogItemEdition*`.

### Publishers and creators

```text
GET  /api/catalog/publishers
GET  /api/catalog/publishers/{id}
POST /api/catalog/publishers
PUT  /api/catalog/publishers/{id}

GET  /api/catalog/creators
GET  /api/catalog/creators/{id}
POST /api/catalog/creators
PUT  /api/catalog/creators/{id}
```

Creators y sus escrituras se activan en una subfase posterior. Los DTOs de
detalle deben incluir creditos, no ciclos completos de relaciones JPA.

### Compatibilidad

`GET/POST/PUT /api/master-products` conserva sus contratos durante MVP 2. No se
mezclan respuestas editoriales nuevas dentro del JSON legacy sin versionar o
anadir solo campos opcionales compatibles. Una herramienta administrativa de
enlace/reconciliacion se decide en EPIC 33; no debe ser publica.

## 14. Impacto frontend futuro

Rutas candidatas:

```text
/catalog
/catalog/franchises/:id
/catalog/series/:id
/catalog/items/:id
/catalog/editions/:id
/catalog/:id                    alias legacy temporal
```

- `/catalog`: busqueda global, filtros por tipo y resultados escaneables de
  series/items; franquicias como filtro o agrupacion, no como unica entrada.
- Franchise detail: ramas/series por tipo.
- Series detail: metadata editorial, progreso y lista ordenada de items.
- Item detail: ficha comun, creators y lista de ediciones.
- Edition detail: publisher, formato, idioma, ISBN/EAN y fecha.
- Collection picker: busqueda de item y selector de edicion opcional.

`/catalog/:id` continua mostrando `MasterProductDetailComponent`. Si el producto
esta enlazado, puede ofrecer navegacion al item/edicion editorial. Se retira solo
cuando URLs demo, colecciones e inventario ya no dependan de el.

## 15. Seguridad y permisos

- Lectura publica solo para registros `ACTIVE` y no eliminados.
- Borradores y archivos requieren autenticacion administrativa.
- Escritura editorial inicial: `ADMIN` exclusivamente.
- El permiso actual de `SHOP_OWNER` sobre `/api/master-products` se mantiene por
  compatibilidad, pero no concede publicacion directa en la nueva base editorial.
- En una fase posterior, `SHOP_OWNER` y usuarios pueden crear sugerencias
  revisables usando el concepto ya preparado por `product_suggestions`.
- Toda publicacion o enlace verificado registra actor y fecha.
- No se exponen datos privados de colecciones al editar catalogo.
- Rate limiting, historial de revisiones y moderacion avanzada son requisitos
  antes de abrir contribuciones masivas.

## 16. Estrategia QA

### Persistencia y migracion

- Testcontainers PostgreSQL para esquema vacio y upgrade desde MVP 1 poblado.
- Constraints de FKs, ISBN/EAN normalizados, soft delete y estados.
- Backfill idempotente ejecutado dos veces con los mismos resultados.
- Casos ambiguos: textos variantes, ISBN duplicado, publisher desconocido,
  master product sin franchise/collection y edicion no identificable.
- Consultas de huerfanos y conteos antes/despues.

### Backend y compatibilidad

- Contract tests de los endpoints `/api/master-products` actuales.
- Tests de lectura publica frente a `DRAFT`, `ARCHIVED` y borrado logico.
- Tests de autorizacion `ADMIN`, `SHOP_OWNER`, `USER` y anonimo.
- Paginacion, limites, filtros combinados, orden estable y errores 400/404/409.
- Prevencion de consultas N+1 y medicion de planes sobre listados principales.

### Dominios consumidores

- Colecciones siguen creando, leyendo y editando items legacy durante el puente.
- Inventario y reservas conservan IDs y respuestas existentes.
- Matching actual sigue igual antes de la sub-EPIC editorial.
- Matching futuro cubre exact edition, flexible item y ausencia de falso match.

### Frontend y E2E

- Servicios y modelos de cada recurso editorial.
- Rutas literales antes de `/catalog/:id`.
- Estados loading/error/empty, filtros y paginacion.
- E2E de catalogo -> serie -> item -> edicion -> coleccion.
- Reejecucion completa del smoke MVP 1 como prueba de no regresion.

## 17. Riesgos y mitigaciones

| Riesgo | Mitigacion |
| --- | --- |
| Duplicar franquicias/series por texto | Normalizacion, candidatos DRAFT y revision antes de verificar. |
| Dos fuentes de verdad | Fases explicitas y fachada de compatibilidad en aplicacion, sin triggers. |
| Romper IDs/URLs actuales | Mantener master products y `/catalog/:id` durante MVP 2. |
| Obligar a conocer edicion | Item requerido y edicion opcional en colecciones. |
| Matching demasiado amplio | Motivos y niveles exact/flexible/series separados. |
| Publisher incorrecto en serie | Publisher de edicion es autoritativo; el de serie solo metadata opcional. |
| Sobrediseno de creators/relaciones | Diferirlos hasta estabilizar core y validar casos reales. |
| Enum de tipos cerrado | Reservar tipos futuros, exponer solo BOOK/COMIC/MANGA ahora. |
| Rendimiento de jerarquia | Paginacion, indices por FK/filtros y pruebas de planes. |
| Permisos actuales de tienda | Mantener endpoint legacy, limitar escritura editorial nueva a ADMIN. |

## 18. Plan por sub-EPICs

### EPIC 31 - Fundamentos editoriales

Liquibase, entidades, repositorios y API inicial de publishers, franchises y
series. Todo aditivo y con lectura publica/escritura ADMIN.

Estado: implementada el 2026-06-30 mediante el changeset
`005-create-editorial-catalog-foundations`.

### EPIC 32 - Items y ediciones

Catalog items, editions, constraints, filtros, paginacion y tests de identidad
editorial. Sin cambiar todavia colecciones o inventario.

Estado: implementada el 2026-06-30 mediante el changeset
`006-create-editorial-catalog-items-and-editions`.

### EPIC 33 - Puente y backfill

`master_product_catalog_links`, proceso idempotente, informe de reconciliacion,
enlaces verificados y contract tests legacy.

Estado: implementada el 2026-06-30 mediante el changeset
`007-create-master-product-catalog-links`.

### EPIC 34 - API/fachada de compatibilidad

Completar `/api/catalog/**`, definir fuente de verdad de escrituras y mantener
`/api/master-products` sin regresiones.

Estado: implementada el 2026-06-30 mediante una fachada de lectura agregada
bajo `/api/catalog/editorial`, con busqueda publica de cadenas `ACTIVE`,
detalles jerarquicos y consulta ADMIN del puente legacy.

### EPIC 35 - Frontend editorial

Busqueda jerarquica, detalles de franchise/series/item/edition y alias legacy.

Estado: implementada el 2026-07-01 con busqueda publica agregada, detalles de
serie/item/edicion, navegacion principal e i18n ES/EN. El catalogo legacy se
mantiene disponible y no se expone reconciliacion ADMIN.

### EPIC 36 - Colecciones editoriales

Referencias duales, item requerido conceptualmente, edicion opcional, backfill y
actualizacion gradual de DTOs/UI.

Estado: implementada el 2026-07-01 mediante la migracion 008. Los items de
coleccion admiten referencia legacy, referencia enriquecida por puente
`VERIFIED` o referencia editorial manual. `master_product_id` pasa a nullable,
pero una constraint exige `master_product_id` o `catalog_item_id`. La UI de
alta/edicion permite ambos catalogos y el detalle conserva fallback legacy.

### EPIC 37 - Inventario y matching editorial

Enlace de stock a edicion y reglas exact/flexible. Alertas, ubicacion y comercio
siguen fuera de alcance.

### EPIC 38 - Creators y relaciones priorizadas

Creators y creditos de item. `catalog_relationships` solo se implementa si los
casos de uso validados justifican direccion, tipos y moderacion.

## 19. Criterios de salida de MVP 2

- Navegacion editorial publica por serie, item y edicion.
- Libros, comics y manga modelados sin campos planos como identidad principal.
- Duplicados e identificadores normalizados con constraints y revision.
- Master products activos enlazados, no enlazados o ambiguos cuantificados.
- Endpoints y recorrido MVP 1 sin regresiones.
- Colecciones pueden expresar item con edicion opcional al cerrar EPIC 36.
- Seguridad, auditoria, migraciones y pruebas de compatibilidad completas.

## 20. Decisiones pendientes

1. Convencion exacta de slugs y politica de cambios de URL.
2. Si `primary_publisher_id` aporta valor real en series o debe eliminarse.
3. Catalogo completo de formatos editoriales para BOOK/COMIC/MANGA.
4. Regla de unicidad de ediciones sin ISBN/EAN.
5. Herramienta administrativa para verificar enlaces y fusionar duplicados.
6. Cuando congelar escrituras directas de `/api/master-products`.
7. Estrategia de alias/traducciones de titulos posterior al par
   `title/original_title`.
8. Si creators entra antes o despues de la primera UI editorial.

Estas decisiones se resuelven en la sub-EPIC que las necesite; no bloquean el
diseno de compatibilidad ni justifican implementar tablas en EPIC 30.
