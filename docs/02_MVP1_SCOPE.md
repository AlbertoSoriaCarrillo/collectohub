# CollectoHub MVP 1 scope

## Objetivo

Validar que una persona coleccionista obtiene valor al gestionar su biblioteca
personal sobre un catalogo comun.

MVP 1 no intenta validar marketplace, red social completa ni gestion comercial.

## Entra en MVP 1

- usuario normal;
- registro, login y sesion;
- perfil basico;
- catalogo inicial de libros, comics y manga;
- colecciones personales;
- items personales;
- estados `OWNED`, `WANTED`, `MISSING` y `DUPLICATED`;
- buscados y faltantes;
- visibilidad publica o privada;
- i18n ES/EN;
- navegacion principal limpia;
- UI responsive;
- datos demo;
- Docker, tests y CI.

Los estados tecnicos adicionales ya soportados, como `SELLABLE` o `TRADABLE`,
no convierten MVP 1 en un marketplace.

## No entra en MVP 1

- tiendas visibles como flujo principal;
- pagos;
- pedidos;
- inventario comercial real como producto operativo;
- reservas reales con garantia de stock;
- marketplace;
- chat;
- feed social completo;
- posts avanzados;
- creadores de contenido;
- monetizacion;
- eventos;
- almacenamiento complejo de archivos;
- APIs externas;
- scraping;
- importaciones masivas.

## Recorrido principal

1. Descubrir el producto en Home.
2. Explorar el catalogo.
3. Registrarse o iniciar sesion.
4. Crear una coleccion personal.
5. Anadir items y marcar que se tiene, se quiere o falta.
6. Revisar Buscados.
7. Consultar el perfil basico.

## Base tecnica fuera del foco

El repositorio contiene modulos funcionales de tiendas, inventario,
recomendaciones contra stock y reservas sin pago. Se conservan porque son base
para MVP 4 y MVP 5, pero:

- no se promocionan en la navegacion principal;
- no cambian el objetivo de validacion de MVP 1;
- no deben describirse como marketplace operativo;
- no justifican anadir nuevas funciones comerciales durante esta fase.

## Criterios de cierre

MVP 1 puede considerarse tecnicamente completo cuando:

- el recorrido principal funciona de extremo a extremo;
- los permisos protegen datos privados;
- catalogo, colecciones y buscados son comprensibles en desktop y movil;
- ES/EN cubre la UI principal;
- tests y CI protegen el flujo;
- la documentacion distingue claramente presente y vision futura.

La validacion real de producto requiere despues observar uso, retencion y valor
para coleccionistas; completar codigo no demuestra por si solo demanda de
mercado.
