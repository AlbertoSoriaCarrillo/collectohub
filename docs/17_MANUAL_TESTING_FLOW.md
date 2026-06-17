# CollectoHub MVP manual testing flow

Esta guia usa PowerShell e `Invoke-RestMethod` contra el backend local:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

En otra ventana de PowerShell, ejecuta el flujo siguiente. Usa valores con timestamp
para evitar conflictos al repetir la prueba.

```powershell
$baseUrl = "http://localhost:8080"
$suffix = Get-Date -Format "yyyyMMddHHmmss"

$email = "manual-$suffix@collectohub.local"
$password = "Password123!"

$registerBody = @{
    email = $email
    password = $password
    displayName = "Manual Tester $suffix"
    preferredInterfaceLanguage = "es"
} | ConvertTo-Json

$registered = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/auth/register" `
    -ContentType "application/json" `
    -Body $registerBody

$loginBody = @{
    email = $email
    password = $password
} | ConvertTo-Json

$login = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$token = $login.accessToken
$headers = @{ Authorization = "Bearer $token" }

$me = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/users/me" `
    -Headers $headers
```

## Crear tienda

```powershell
$shopBody = @{
    name = "Manual Shop $suffix"
    description = "Shop created from manual MVP flow"
    contactEmail = "shop-$suffix@collectohub.local"
    country = "ES"
    currency = "EUR"
    defaultReservationExpirationHours = 48
} | ConvertTo-Json

$shop = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/shops" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $shopBody

$shopId = $shop.id

$myShops = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/shops/my" `
    -Headers $headers
```

Al crear la primera tienda se anade `SHOP_OWNER` al usuario en base de datos. Como
el JWT ya estaba emitido, vuelve a iniciar sesion para obtener un token con el rol
actualizado antes de crear productos maestros.

```powershell
$login = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$token = $login.accessToken
$headers = @{ Authorization = "Bearer $token" }
```

## Crear producto maestro

```powershell
$categories = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/product-categories"

$ean = "84$($suffix.Substring(0, 12))"
$isbn = "978$($suffix.Substring(0, 10))"

$masterProductBody = @{
    name = "Manual Manga Volume $suffix"
    description = "Product created from manual MVP flow"
    categoryCode = "MANGA_COMIC"
    franchise = "Manual Franchise"
    collectionName = "Manual Collection"
    volumeNumber = "1"
    publisher = "Manual Publisher"
    isbn = $isbn
    ean = $ean
    language = "es"
    limitedEdition = $false
    publicationCountries = @("ES")
    attributes = @{
        format = "paperback"
        manualRun = $suffix
    }
} | ConvertTo-Json -Depth 10

$masterProduct = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/master-products" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $masterProductBody

$masterProductId = $masterProduct.id

$masterProducts = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/master-products?categoryCode=MANGA_COMIC&name=Manual"
```

## Anadir inventario de tienda

```powershell
$shopProductBody = @{
    masterProductId = $masterProductId
    priceAmount = 12.95
    currency = "EUR"
    stockQuantity = 3
    commercialStatus = "AVAILABLE"
    physicalCondition = "NEW"
    visible = $true
    notes = "Manual inventory item"
} | ConvertTo-Json

$shopProduct = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/shops/$shopId/products" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $shopProductBody

$shopProductId = $shopProduct.id

$publicShopProducts = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/shops/$shopId/products"
```

## Crear coleccion y item MISSING/WANTED

```powershell
$collectionBody = @{
    name = "Manual Collection $suffix"
    description = "Collection created from manual MVP flow"
    visibility = "PRIVATE"
    categoryCode = "MANGA_COMIC"
} | ConvertTo-Json

$collection = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/collections" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $collectionBody

$collectionId = $collection.id

$collectionItemBody = @{
    masterProductId = $masterProductId
    collectionStatus = "MISSING"
    physicalCondition = "NEW"
    notes = "Missing item for recommendation flow"
} | ConvertTo-Json

$collectionItem = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/collections/$collectionId/items" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $collectionItemBody

$collectionItems = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/collections/$collectionId/items" `
    -Headers $headers
```

Para probar `WANTED`, cambia `collectionStatus = "WANTED"` en el body anterior o
actualiza el item con `PUT /api/collections/{collectionId}/items/{itemId}`.

## Recomendaciones

```powershell
$recommendations = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/recommendations/my?categoryCode=MANGA_COMIC&currency=EUR" `
    -Headers $headers

$recommendationSummary = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/recommendations/my/summary?categoryCode=MANGA_COMIC&currency=EUR" `
    -Headers $headers
```

## Crear y consultar reserva

```powershell
$reservationBody = @{
    shopProductId = $shopProductId
    quantity = 1
    userMessage = "I would like to reserve this item."
} | ConvertTo-Json

$reservation = Invoke-RestMethod `
    -Method Post `
    -Uri "$baseUrl/api/reservations" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $reservationBody

$reservationId = $reservation.id

$myReservations = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/reservations/my" `
    -Headers $headers

$shopReservations = Invoke-RestMethod `
    -Method Get `
    -Uri "$baseUrl/api/shops/$shopId/reservations" `
    -Headers $headers
```

## Aceptar, cancelar o completar reserva

Primero acepta la reserva como tienda:

```powershell
$acceptBody = @{
    status = "ACCEPTED"
    shopResponse = "Reservation accepted from manual MVP flow."
} | ConvertTo-Json

$acceptedReservation = Invoke-RestMethod `
    -Method Put `
    -Uri "$baseUrl/api/shops/$shopId/reservations/$reservationId/status" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $acceptBody
```

Despues elige una de estas dos opciones. No ejecutes ambas sobre la misma reserva.

Cancelar como usuario:

```powershell
$cancelledReservation = Invoke-RestMethod `
    -Method Put `
    -Uri "$baseUrl/api/reservations/$reservationId/cancel" `
    -Headers $headers
```

Completar como tienda:

```powershell
$completeBody = @{
    status = "COMPLETED"
    shopResponse = "Reservation completed from manual MVP flow."
} | ConvertTo-Json

$completedReservation = Invoke-RestMethod `
    -Method Put `
    -Uri "$baseUrl/api/shops/$shopId/reservations/$reservationId/status" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $completeBody
```

## Comprobaciones rapidas esperadas

- `$registered.roles` contiene `USER`.
- Tras crear tienda y reloguear, `$login.roles` contiene `USER` y `SHOP_OWNER`.
- `$shop.currentUserMembership.role` es `OWNER`.
- `$masterProduct.id`, `$shopProduct.id`, `$collection.id` y `$reservation.id` tienen valor.
- Las recomendaciones contienen el producto visible y `AVAILABLE` cuando existe un item propio `MISSING` o `WANTED`.
- La reserva pasa por `PENDING -> ACCEPTED -> COMPLETED` o `PENDING/ACCEPTED -> CANCELLED`.
