# CollectoHub user-shop matching

## Idea de valor

```text
El usuario marca que le falta o quiere un item.
La tienda publica stock del mismo item de catalogo.
El sistema detecta coincidencias.
El usuario ve donde puede conseguirlo.
La tienda llega a demanda real.
```

El catalogo comun es la clave: usuario y tienda deben referirse a la misma
identidad de item para evitar matching por texto fragil.

## MVP actual

- La vista `Buscados` reutiliza el modulo backend `recommendations`.
- Se consideran items propios en estado `MISSING` o `WANTED`.
- Las recomendaciones actuales pueden cruzarlos con productos de tienda
  visibles, disponibles y con stock.
- Tiendas, inventario y reservas no son foco visible del recorrido principal.
- Las reservas existen tecnicamente como solicitudes sin pago, sin bloqueo ni
  reduccion automatica de stock.

Esto demuestra una primera regla tecnica, pero no constituye matching de
producto completo ni marketplace.

## Evolucion futura

- matching estable por identidad de catalogo y edicion;
- alertas cuando aparece stock relevante;
- disponibilidad y precio actualizados;
- preferencias de condicion, idioma, edicion, distancia y presupuesto;
- comparacion de tiendas;
- compra o reserva con reglas transaccionales;
- metricas agregadas de demanda;
- soporte de almacen e inventario operativo.

## Flujo futuro esperado

1. El coleccionista marca un item como buscado o faltante.
2. La tienda asocia una unidad de inventario al mismo item de catalogo.
3. Matching filtra ofertas activas y compatibles.
4. El usuario recibe resultados o alertas segun sus preferencias.
5. Una accion comercial pasa al dominio Commerce.
6. La tienda consulta demanda agregada sin acceder a datos privados
   innecesarios.

## Responsabilidades por dominio

- User Collections conserva intencion y estado personal.
- Catalog Knowledge Base aporta identidad comun y metadatos.
- Shops & Inventory publica oferta, precio, stock y disponibilidad.
- Matching calcula relevancia y coincidencias.
- Commerce gestiona reserva, pedido, pago y cumplimiento.

## Seguridad y confianza

- No exponer colecciones privadas ni identidad personal a una tienda por defecto.
- Presentar demanda mediante datos agregados cuando no exista consentimiento.
- Explicar por que se muestra una coincidencia.
- Evitar anunciar stock o precio obsoleto como garantia.
- Incorporar controles de spam, fraude y abuso antes de alertas o contacto
  directo.

## Fuera de MVP 1

Alertas, comparador, ubicacion, compra, pedidos, pagos, almacen y metricas de
demanda no forman parte del MVP 1 aunque existan tablas o modulos tecnicos que
preparen fases posteriores.
