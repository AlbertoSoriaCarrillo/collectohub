# CollectoHub MVP demo flow

Esta guia prepara una demo local del MVP usando la UI Angular contra el backend
Spring Boot local. Los datos son ejemplos desechables para entorno local, no
secretos reales.

## 1. Arrancar el entorno

Terminal 1:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Terminal 2:

```powershell
cd frontend
npm start
```

Abrir:

```text
http://localhost:4200
```

El frontend consume el backend en:

```text
http://localhost:8080
```

Alternativa Docker Compose:

```powershell
Copy-Item infra\.env.example infra\.env
cd infra
docker compose up --build
```

## 1.1. Generar datos de demo por API

Si quieres preparar rapidamente capturas o una demo visual, puedes crear datos
locales usando la API real:

```powershell
.\scripts\demo\create-demo-data.ps1 `
  -ApiBaseUrl "http://localhost:8080" `
  -Suffix "demo001"
```

El script crea usuario tienda, tienda, productos maestros, inventario, usuario
coleccionista, coleccion, items para recomendaciones y una reserva. Al terminar
imprime los usuarios, password local y URLs utiles. Guia completa:
`docs/25_DEMO_DATA.md`.

## 2. Datos sugeridos

Usa un sufijo distinto en cada demo para evitar duplicados:

```text
Sufijo: 20260618-demo
Email usuario: demo-20260618-demo@collectohub.local
Password local: Password123!
Nombre usuario: Demo Collector
Tienda: Demo Akihabara Store
Email tienda: shop-20260618-demo@collectohub.local
Producto maestro: Demo Manga Volume 1
Franquicia: Demo Franchise
Coleccion: Demo Missing Manga
Precio inventario: 12.95 EUR
Stock: 3
Condicion: NEW
Estado comercial: AVAILABLE
```

## 3. Registro y login

1. Entrar en `/register`.
2. Registrar el usuario con el email y password de demo.
3. Confirmar que se abre la sesion y aparece el dashboard.
4. Cerrar sesion y volver a entrar por `/login` para verificar login normal.

Resultado esperado:

- El usuario entra con rol global `USER`.
- No se muestran tokens ni hashes en pantalla.

## 4. Crear tienda

1. Ir a `Mis tiendas`.
2. Entrar en `Nueva tienda`.
3. Crear la tienda con nombre, email, pais `ES`, moneda `EUR` y reserva por defecto de 48 horas.
4. Volver a `Mis tiendas` y abrir el detalle publico de la tienda.

Resultado esperado:

- La tienda queda asociada al usuario.
- El usuario es miembro interno `OWNER`.
- El backend asigna el rol global `SHOP_OWNER`.

Nota: el JWT actual no cambia al crear la tienda. Cierra sesion y vuelve a entrar
antes de crear productos maestros para que la UI vea `SHOP_OWNER`.

## 5. Crear producto maestro

1. Cerrar sesion y volver a iniciar sesion.
2. Ir a `Catalogo`.
3. Entrar en `Nuevo producto`.
4. Crear un producto con categoria `MANGA_COMIC`, nombre, franquicia, coleccion, volumen, idioma `es`, ISBN/EAN unicos, paises de publicacion `ES` y atributos JSON opcionales como `{"format":"paperback"}`.
5. Buscarlo desde el listado publico del catalogo.

Resultado esperado:

- El producto aparece en `GET /api/master-products`.
- Un usuario sin `SHOP_OWNER` o `ADMIN` no puede crear productos maestros.

## 6. Anadir inventario de tienda

1. Ir a `Mis tiendas`.
2. Abrir la tienda creada.
3. Entrar en `Gestionar inventario`.
4. Anadir producto al inventario seleccionando el producto maestro de demo.
5. Informar precio `12.95`, moneda `EUR`, stock `3`, estado `AVAILABLE`, condicion `NEW` y visible.
6. Abrir el detalle publico del producto de tienda.

Resultado esperado:

- El producto aparece en el inventario interno.
- El producto visible aparece en la tienda publica y en su detalle publico.

## 7. Crear coleccion e item buscado

1. Ir a `Mis colecciones`.
2. Crear una coleccion privada de categoria `MANGA_COMIC`.
3. Abrir la coleccion.
4. Anadir un item usando el mismo producto maestro.
5. Marcar el item como `MISSING`.

Resultado esperado:

- La coleccion aparece solo para el propietario si es privada.
- El item `MISSING` queda listo para recomendaciones.

Para una segunda pasada se puede repetir con estado `WANTED`.

## 8. Ver recomendaciones

1. Ir a `Recomendaciones`.
2. Filtrar por categoria `MANGA_COMIC` y moneda `EUR` si se desea.
3. Abrir el producto de tienda recomendado.

Resultado esperado:

- Aparece el producto visible y disponible de la tienda porque coincide con un item propio `MISSING` o `WANTED`.

## 9. Crear reserva

1. Desde el detalle publico del producto de tienda, crear una reserva de cantidad `1`.
2. Ir a `Mis reservas`.
3. Abrir el detalle de la reserva.

Resultado esperado:

- La reserva queda en estado `PENDING`.
- El detalle muestra producto, tienda, cantidad y fechas principales.

## 10. Gestionar reserva como tienda

1. Ir a `Mis tiendas`.
2. Abrir la tienda.
3. Entrar en `Gestionar reservas`.
4. Aceptar la reserva.
5. Completar la reserva.

Resultado esperado:

- La reserva pasa por `PENDING -> ACCEPTED -> COMPLETED`.

## 11. Probar cancelacion valida

Para probar cancelacion, crea una segunda reserva y usa una de estas rutas:

- Cancelar como usuario desde `Mis reservas` mientras esta `PENDING`.
- Aceptarla como tienda y cancelarla como usuario mientras esta `ACCEPTED`.

Resultado esperado:

- La reserva pasa a `CANCELLED`.
- No se permite cancelar una reserva ya `COMPLETED`.

## 12. Comprobaciones rapidas

- `GET /api/health` sigue siendo publico.
- El catalogo y detalle de tienda son publicos.
- Las rutas privadas redirigen o bloquean si no hay sesion.
- La UI no muestra `passwordHash`, hashes de refresh token ni valores secretos.
- Tras crear tienda, el usuario debe reloguear para ver `SHOP_OWNER`.
