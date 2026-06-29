# CollectoHub catalog concept model

## Cadena conceptual

```text
Franquicia -> Serie de catalogo -> Item de catalogo -> Edicion
                                -> Coleccion personal -> Item personal
```

La cadena separa conocimiento compartido de datos personales. Una franquicia y
sus obras son comunes; una coleccion personal expresa que relacion tiene una
persona con uno de esos items.

## Conceptos

### Franquicia

Universo o propiedad intelectual que agrupa distintas ramas y medios. Ejemplos:
`Harry Potter` o `Trigun`.

### Serie de catalogo

Serie, linea editorial o rama ordenada dentro de una franquicia. No pertenece a
un usuario. Puede representar libros, manga, peliculas, cartas o videojuegos.
Una obra independiente puede usar una serie de un unico item para mantener una
navegacion uniforme.

### Item de catalogo

Objeto identificable dentro de una serie de catalogo: libro, volumen, numero,
pelicula, carta, figura o videojuego. Contiene metadatos comunes y reutilizables
sin mezclar una publicacion comercial concreta.

### Edicion de catalogo

Publicacion concreta de un item con publisher, idioma, formato, fecha e
identificadores como ISBN/EAN. Un item puede tener cero o varias ediciones.

### Coleccion personal

Agrupacion creada por una persona para organizar sus objetos. Puede mezclar o
seleccionar items del catalogo segun su propio criterio.

### Item personal

Relacion entre una coleccion personal y un item de catalogo. Guarda estado,
notas, condicion, fechas u otros datos privados/personales sin duplicar la ficha
comun.

## Ejemplo Harry Potter

```text
Harry Potter
  -> Harry Potter - Libros
    -> Harry Potter y la piedra filosofal
      -> Mi coleccion Harry Potter
        -> Lo tengo / Me falta / Lo quiero
```

## Ejemplo Trigun

```text
Trigun
  -> Trigun Maximum - Manga
    -> Trigun Maximum Vol. 1
      -> Mi coleccion Trigun
        -> Lo tengo
```

## Ramas futuras de una franquicia

Una misma franquicia puede contener varias ramas:

- libros;
- comics o manga;
- anime, peliculas o series;
- cartas;
- merchandising y figuras;
- videojuegos y consolas;
- ediciones especiales.

Las relaciones entre ramas permiten expresar adaptaciones, continuidades,
ediciones equivalentes, obras relacionadas o productos derivados sin mezclar
sus identidades.

## Encaje con el modelo actual

El modelo actual dispone de `product_categories`, `master_products`,
`collections` y `collection_items`.

- `master_products` actua hoy como item generico de catalogo.
- `franchise` y `collection_name` son atributos planos, no entidades propias.
- `collections` y `collection_items` ya representan la parte personal.

MVP 2 debe evolucionar la parte comun con franquicias, series, items, ediciones,
publishers y creators, conservando una migracion clara desde los productos
maestros existentes. El diseno fisico, puente de compatibilidad y plan por
sub-EPICs estan en `docs/09_MVP2_EDITORIAL_CATALOG_DESIGN.md`.

## Reglas conceptuales

- Una ficha comun no debe contener estado personal como `OWNED` o `WANTED`.
- Un item personal debe referenciar una ficha comun siempre que exista.
- El inventario de tienda debe referenciar el item de catalogo, no convertirse
  en la fuente maestra de conocimiento.
- Distintas ediciones pertenecen al mismo item de catalogo y conservan identidad
  propia para publisher, idioma, formato e identificadores comerciales.
- Las imagenes oficiales y las imagenes personales tienen propiedad y reglas
  diferentes.
