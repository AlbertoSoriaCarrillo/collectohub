# CollectoHub product refocus: books, comics and manga

Fecha: 2026-06-23.

## 1. Contexto

CollectoHub habia evolucionado hasta un MVP tecnico con backend para auth,
usuarios, tiendas, catalogo, inventario, colecciones, recomendaciones y
reservas. El nuevo foco visible es mas concreto: gestionar colecciones de
libros, comics y manga usando un catalogo comun.

Este reenfoque define el MVP 1, no la vision final. La vision completa sigue
siendo una plataforma para coleccionistas, catalogo, tiendas y creadores de
contenido, desarrollada por fases.

## 2. Decision de producto

El recorrido principal de frontend pasa a ser:

1. Home publica.
2. Catalogo publico.
3. Login y registro desde login.
4. Colecciones personales.
5. Buscados.
6. Perfil.

Tiendas, inventario y reservas no se eliminan. Permanecen como base tecnica y
rutas legacy/futuras, accesibles manualmente pero fuera de navegacion principal,
home, dashboard y CTAs primarios.

## 3. Conclusiones Product Owner

- El MVP debe explicarse como herramienta de coleccionista, no como marketplace.
- El valor inmediato esta en saber que tengo, que quiero y que me falta.
- `SHOP_OWNER`, tiendas e inventario son capacidades futuras, no el gancho de
  la demo principal.
- El foco en libros, comics y manga valida primero el nucleo coleccionista; no
  elimina futuras ramas como cartas, figuras, videojuegos o audiovisual.

## 4. Conclusiones UX/UI

- La primera pantalla debe mostrar la propuesta de biblioteca social.
- La navegacion principal debe ser corta: Home, Catalogo, Colecciones, Buscados
  y Perfil.
- Header global: marca, navegacion, selector de idioma, login o avatar/menu.
- Register no debe mostrarse como CTA global; solo se ofrece desde login.
- `/dashboard` deja de ser destino primario y redirige a `/home`.
- El perfil existe como pantalla simple con acciones futuras deshabilitadas.

## 5. Conclusiones Backend

- No se cambia backend en esta fase.
- No se borran entidades, endpoints, migraciones ni tests de tiendas,
  inventario o reservas.
- `GET /api/recommendations/my` se reutiliza para la vista `Buscados`.

## 6. Conclusiones i18n/copy

- El copy visible debe hablar de obras, biblioteca, colecciones y buscados.
- Se mantienen claves de traduccion legacy para pantallas no promocionadas.
- Home y Perfil pasan a formar parte de los dominios i18n principales.

## 7. Conclusiones QA

- Los tests unitarios deben cubrir rutas nuevas/redirecciones, home, perfil,
  registro con confirmacion de password y buscados sin filtro manual de tienda.
- Los E2E deben validar el flujo principal de coleccionista.
- Los E2E de tienda/reserva pueden mantenerse como legacy en fases posteriores,
  pero no deben definir el smoke principal.

## 8. Rutas principales

- `/home`: publica.
- `/catalog`: publica.
- `/login`: publica.
- `/register`: publica, pero visible solo desde login.
- `/collections`: protegida.
- `/wanted`: protegida.
- `/profile`: protegida.

Redirecciones:

- `/` -> `/home`.
- `/dashboard` -> `/home`.
- `/recommendations` -> `/wanted`.

## 9. Rutas legacy/futuras conservadas

- `/shops`
- `/shops/new`
- `/shops/:id`
- `/shops/:shopId/inventory`
- `/shops/:shopId/inventory/new`
- `/shops/:shopId/inventory/:shopProductId/edit`
- `/shops/:shopId/reservations`
- `/shop-products/:shopProductId`
- `/reservations`
- `/reservations/:reservationId`

## 10. Demo recomendada

1. Abrir Home.
2. Explorar Catalogo.
3. Crear cuenta.
4. Crear una coleccion.
5. Revisar Buscados.
6. Abrir Perfil.

El script `scripts/demo/create-demo-data.ps1` puede seguir preparando datos
legacy de tienda/reserva para validar backend, pero sus URLs principales deben
priorizar el recorrido anterior.

## 11. Fuera de alcance

- Feed social real.
- Chat.
- Marketplace.
- Pagos.
- OAuth.
- 2FA.
- Uploads.
- App movil.
- Cambios de base de datos o backend para este reenfoque.

## 12. Relacion con la vision completa

- `docs/00_PRODUCT_VISION.md`: producto final y cuatro pilares.
- `docs/01_ROADMAP.md`: secuencia MVP 1 a MVP 6.
- `docs/02_MVP1_SCOPE.md`: limites del foco coleccionista actual.
- `docs/03_PRODUCT_DOMAINS.md`: estado implementado, parcial y futuro.
- `docs/export/`: esquema, API, rutas y mapa frontend-backend descargables con
  estado `MVP1_VISIBLE`, `LEGACY_FUTURE`, `TECHNICAL` o `REDIRECT`.

Tiendas, inventario, reservas, social y creadores no se descartan. Se mantienen
fuera del foco actual para evitar que MVP 1 se convierta en un producto
inabarcable.

## 13. Cierre de MVP 1

La auditoria de cierre del 2026-06-29 confirma este reenfoque como experiencia
principal. Evidencia y pendientes posteriores:

- `docs/07_MVP1_ACCEPTANCE_CHECKLIST.md`.
- `docs/08_NEXT_BACKLOG.md`.
