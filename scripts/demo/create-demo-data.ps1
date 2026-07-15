[CmdletBinding()]
param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$Suffix,
    [string]$FrontendBaseUrl = "http://localhost:4200"
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

function New-DemoSuffix {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $shortGuid = [guid]::NewGuid().ToString("N").Substring(0, 6)
    return "$timestamp-$shortGuid"
}

function Get-NumericTail {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value,

        [Parameter(Mandatory = $true)]
        [int]$Length
    )

    $digits = -join ([char[]]$Value | Where-Object { $_ -match "\d" })

    if ([string]::IsNullOrWhiteSpace($digits)) {
        $hash = [Math]::Abs($Value.GetHashCode())
        $digits = $hash.ToString()
    }

    if ($digits.Length -lt $Length) {
        $digits = $digits.PadLeft($Length, "0")
    }

    return $digits.Substring($digits.Length - $Length)
}

function New-ProductCodes {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Suffix,

        [Parameter(Mandatory = $true)]
        [int]$Index
    )

    $isbnTail = Get-NumericTail -Value "$Suffix-$Index" -Length 10
    $eanTail = Get-NumericTail -Value "$Index-$Suffix" -Length 11

    return [ordered]@{
        isbn = "978$isbnTail"
        ean = "20$eanTail"
    }
}

function ConvertTo-RequestBody {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Body
    )

    return $Body | ConvertTo-Json -Depth 20
}

function Get-ErrorBody {
    param(
        [Parameter(Mandatory = $true)]
        [System.Management.Automation.ErrorRecord]$ErrorRecord
    )

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        return $ErrorRecord.ErrorDetails.Message
    }

    $response = $ErrorRecord.Exception.Response
    if (-not $response) {
        return $ErrorRecord.Exception.Message
    }

    try {
        $stream = $response.GetResponseStream()
        if (-not $stream) {
            return $ErrorRecord.Exception.Message
        }

        $reader = New-Object System.IO.StreamReader($stream)
        return $reader.ReadToEnd()
    }
    catch {
        return $ErrorRecord.Exception.Message
    }
}

function Invoke-CollectoHubApi {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "POST", "PUT", "DELETE")]
        [string]$Method,

        [Parameter(Mandatory = $true)]
        [string]$Path,

        [object]$Body,

        [string]$Token
    )

    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $parameters = @{
        Method = $Method
        Uri = "$ApiBaseUrl$Path"
        Headers = $headers
    }

    if ($null -ne $Body) {
        $parameters["ContentType"] = "application/json"
        $parameters["Body"] = ConvertTo-RequestBody -Body $Body
    }

    try {
        return Invoke-RestMethod @parameters
    }
    catch {
        $statusCode = $null
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        $details = Get-ErrorBody -ErrorRecord $_

        if ($statusCode -eq 409) {
            throw "Conflict 409 calling $Method $Path. The suffix '$Suffix' may already exist. Run again with a new -Suffix. Details: $details"
        }

        if ($statusCode) {
            throw "API call failed with HTTP $statusCode calling $Method $Path. Details: $details"
        }

        throw "API call failed calling $Method $Path. Details: $details"
    }
}

function Register-DemoUser {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Email,

        [Parameter(Mandatory = $true)]
        [string]$Password,

        [Parameter(Mandatory = $true)]
        [string]$DisplayName
    )

    $body = [ordered]@{
        email = $Email
        password = $Password
        displayName = $DisplayName
        preferredInterfaceLanguage = "es"
    }

    return Invoke-CollectoHubApi -Method "POST" -Path "/api/auth/register" -Body $body
}

function Login-DemoUser {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Email,

        [Parameter(Mandatory = $true)]
        [string]$Password
    )

    $body = [ordered]@{
        email = $Email
        password = $Password
    }

    return Invoke-CollectoHubApi -Method "POST" -Path "/api/auth/login" -Body $body
}

function New-MasterProduct {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Definition,

        [Parameter(Mandatory = $true)]
        [int]$Index,

        [Parameter(Mandatory = $true)]
        [string]$Token
    )

    $codes = New-ProductCodes -Suffix $Suffix -Index $Index
    $body = [ordered]@{
        name = "$($Definition.name) $Suffix"
        description = $Definition.description
        categoryCode = $Definition.categoryCode
        franchise = $Definition.franchise
        collectionName = $Definition.collectionName
        volumeNumber = $Definition.volumeNumber
        publisher = "CollectoHub Demo"
        ean = $codes.ean
        language = $Definition.language
        limitedEdition = $Definition.limitedEdition
        publicationCountries = $Definition.publicationCountries
        attributes = $Definition.attributes
    }

    if ($Definition.includeIsbn) {
        $body["isbn"] = $codes.isbn
    }

    if ($Definition.limitedEditionTotalUnits) {
        $body["limitedEditionTotalUnits"] = $Definition.limitedEditionTotalUnits
    }

    return Invoke-CollectoHubApi -Method "POST" -Path "/api/master-products" -Body $body -Token $Token
}

if ([string]::IsNullOrWhiteSpace($Suffix)) {
    $Suffix = New-DemoSuffix
}

$ApiBaseUrl = $ApiBaseUrl.TrimEnd("/")
$FrontendBaseUrl = $FrontendBaseUrl.TrimEnd("/")
$password = "Demo1234!"

Write-Host "Checking CollectoHub backend at $ApiBaseUrl ..."
$health = Invoke-CollectoHubApi -Method "GET" -Path "/api/health"
Write-Host "Backend status: $($health.status) ($($health.service))"

$shopEmail = "demo.shop.$Suffix@collectohub.local"
$collectorEmail = "demo.collector.$Suffix@collectohub.local"

Write-Host "Registering shop owner $shopEmail ..."
$shopUser = Register-DemoUser -Email $shopEmail -Password $password -DisplayName "Demo Shop Owner $Suffix"
$shopLogin = Login-DemoUser -Email $shopEmail -Password $password
$shopToken = $shopLogin.accessToken

Write-Host "Creating shop ..."
$shopBody = [ordered]@{
    name = "Akihabara Collectibles $Suffix"
    description = "Tienda especializada en manga, figuras y cartas coleccionables."
    contactEmail = "shop.$Suffix@collectohub.local"
    country = "ES"
    currency = "EUR"
    defaultReservationExpirationHours = 48
}

$shop = Invoke-CollectoHubApi -Method "POST" -Path "/api/shops" -Body $shopBody -Token $shopToken
$shopId = $shop.id

# Creating the first shop grants SHOP_OWNER in the database. The current JWT was
# issued before that role existed, so this login refreshes roles for catalog writes.
Write-Host "Re-login shop owner to refresh SHOP_OWNER role in JWT ..."
$shopLogin = Login-DemoUser -Email $shopEmail -Password $password
$shopToken = $shopLogin.accessToken

$masterProductDefinitions = @(
    @{
        key = "dragonQuest"
        name = "Dragon Quest Collectors Vol. 1"
        description = "Manga de demo para capturas del catalogo y recomendaciones."
        categoryCode = "MANGA_COMIC"
        franchise = "Dragon Quest"
        collectionName = "Collectors Edition"
        volumeNumber = "1"
        language = "es"
        limitedEdition = $false
        limitedEditionTotalUnits = $null
        publicationCountries = @("ES")
        includeIsbn = $true
        attributes = @{
            demoSuffix = $Suffix
            format = "paperback"
            visualGroup = "manga"
        }
    },
    @{
        key = "cyberSamurai"
        name = "Cyber Samurai Figure"
        description = "Figura de demo para inventario visual de tienda."
        categoryCode = "FIGURE"
        franchise = "Cyber Samurai"
        collectionName = "Premium Figures"
        volumeNumber = $null
        language = "en"
        limitedEdition = $true
        limitedEditionTotalUnits = 500
        publicationCountries = @("JP", "ES")
        includeIsbn = $false
        attributes = @{
            demoSuffix = $Suffix
            scale = "1/8"
            visualGroup = "figure"
        }
    },
    @{
        key = "galaxyDragon"
        name = "Galaxy Dragon Rare Card"
        description = "Carta rara de demo para recomendaciones WANTED."
        categoryCode = "TRADING_CARD"
        franchise = "Galaxy Cards"
        collectionName = "Rare Set"
        volumeNumber = $null
        language = "en"
        limitedEdition = $false
        limitedEditionTotalUnits = $null
        publicationCountries = @("US", "ES")
        includeIsbn = $false
        attributes = @{
            demoSuffix = $Suffix
            rarity = "rare"
            visualGroup = "card"
        }
    },
    @{
        key = "retroQuest"
        name = "Retro Quest DX"
        description = "Videojuego de demo para completar la coleccion visual."
        categoryCode = "VIDEOGAME"
        franchise = "Retro Quest"
        collectionName = "DX Classics"
        volumeNumber = $null
        language = "es"
        limitedEdition = $false
        limitedEditionTotalUnits = $null
        publicationCountries = @("ES")
        includeIsbn = $false
        attributes = @{
            demoSuffix = $Suffix
            platform = "Switch"
            visualGroup = "videogame"
        }
    }
)

$masterProductIds = [ordered]@{}
$masterProducts = [ordered]@{}
$productIndex = 1

foreach ($definition in $masterProductDefinitions) {
    Write-Host "Creating master product: $($definition.name) ..."
    $product = New-MasterProduct -Definition $definition -Index $productIndex -Token $shopToken
    $masterProductIds[$definition.key] = $product.id
    $masterProducts[$definition.key] = $product
    $productIndex++
}

$inventoryDefinitions = @(
    @{
        key = "dragonQuest"
        priceAmount = 19.95
        stockQuantity = 4
        physicalCondition = "NEW"
        commercialStatus = "AVAILABLE"
        visible = $true
        notes = "Demo stock for manga recommendations."
    },
    @{
        key = "cyberSamurai"
        priceAmount = 54.90
        stockQuantity = 2
        physicalCondition = "LIKE_NEW"
        commercialStatus = "AVAILABLE"
        visible = $true
        notes = "Demo stock for figure detail pages."
    },
    @{
        key = "galaxyDragon"
        priceAmount = 12.50
        stockQuantity = 10
        physicalCondition = "GOOD"
        commercialStatus = "AVAILABLE"
        visible = $true
        notes = "Demo stock for wanted card recommendations."
    },
    @{
        key = "retroQuest"
        priceAmount = 39.99
        stockQuantity = 3
        physicalCondition = "NEW"
        commercialStatus = "AVAILABLE"
        visible = $true
        notes = "Demo stock for retro videogame browsing."
    }
)

$shopProductIds = [ordered]@{}
$shopProducts = [ordered]@{}

foreach ($inventory in $inventoryDefinitions) {
    Write-Host "Adding inventory item: $($inventory.key) ..."
    $body = [ordered]@{
        masterProductId = $masterProductIds[$inventory.key]
        priceAmount = $inventory.priceAmount
        currency = "EUR"
        stockQuantity = $inventory.stockQuantity
        commercialStatus = $inventory.commercialStatus
        physicalCondition = $inventory.physicalCondition
        visible = $inventory.visible
        notes = $inventory.notes
    }

    $shopProduct = Invoke-CollectoHubApi -Method "POST" -Path "/api/shops/$shopId/products" -Body $body -Token $shopToken
    $shopProductIds[$inventory.key] = $shopProduct.id
    $shopProducts[$inventory.key] = $shopProduct
}

Write-Host "Registering collector $collectorEmail ..."
$collectorUser = Register-DemoUser -Email $collectorEmail -Password $password -DisplayName "Demo Collector $Suffix"
$collectorLogin = Login-DemoUser -Email $collectorEmail -Password $password
$collectorToken = $collectorLogin.accessToken

Write-Host "Creating public collection ..."
$collectionBody = [ordered]@{
    name = "Mi coleccion manga y retro $Suffix"
    description = "Coleccion de prueba para demo visual de CollectoHub."
    visibility = "PUBLIC"
    categoryCode = "MANGA_COMIC"
}

$collection = Invoke-CollectoHubApi -Method "POST" -Path "/api/collections" -Body $collectionBody -Token $collectorToken
$collectionId = $collection.id

$collectionItemDefinitions = @(
    @{
        key = "dragonQuest"
        collectionStatus = "WANTED"
        physicalCondition = "NEW"
        notes = "Item buscado principal para activar recomendaciones."
    },
    @{
        key = "galaxyDragon"
        collectionStatus = "WANTED"
        physicalCondition = "GOOD"
        notes = "Item deseado para recomendaciones secundarias."
    },
    @{
        key = "retroQuest"
        collectionStatus = "OWNED"
        physicalCondition = "NEW"
        notes = "Item poseido para completar la vista de coleccion."
    }
)

$collectionItemIds = [ordered]@{}

foreach ($item in $collectionItemDefinitions) {
    Write-Host "Adding collection item: $($item.key) as $($item.collectionStatus) ..."
    $itemBody = [ordered]@{
        masterProductId = $masterProductIds[$item.key]
        collectionStatus = $item.collectionStatus
        physicalCondition = $item.physicalCondition
        notes = $item.notes
    }

    $collectionItem = Invoke-CollectoHubApi -Method "POST" -Path "/api/collections/$collectionId/items" -Body $itemBody -Token $collectorToken
    if ($collectionItem.collectionStatus -ne $item.collectionStatus) {
        throw "Collection item '$($item.key)' returned unexpected status '$($collectionItem.collectionStatus)'."
    }
    $collectionItemIds[$item.key] = $collectionItem.id
}

$createdStatuses = $collectionItemDefinitions | ForEach-Object { $_.collectionStatus }
if ($createdStatuses -contains "MISSING" -or -not ($createdStatuses -contains "WANTED") -or -not ($createdStatuses -contains "OWNED")) {
    throw "The legacy demo must create WANTED and OWNED collection items and must not persist MISSING."
}

Write-Host "Checking collector recommendations ..."
$recommendations = Invoke-CollectoHubApi -Method "GET" -Path "/api/recommendations/my?categoryCode=MANGA_COMIC&currency=EUR" -Token $collectorToken

Write-Host "Creating reservation from collector ..."
$reservationBody = [ordered]@{
    shopProductId = $shopProductIds["dragonQuest"]
    quantity = 1
    userMessage = "Hola, me interesa reservar este producto para recogerlo esta semana."
}

$reservation = Invoke-CollectoHubApi -Method "POST" -Path "/api/reservations" -Body $reservationBody -Token $collectorToken
$reservationId = $reservation.id

$urls = [ordered]@{
    home = "$FrontendBaseUrl/home"
    login = "$FrontendBaseUrl/login"
    catalog = "$FrontendBaseUrl/catalog"
    collections = "$FrontendBaseUrl/collections"
    collectionDetail = "$FrontendBaseUrl/collections/$collectionId"
    wanted = "$FrontendBaseUrl/wanted"
    profile = "$FrontendBaseUrl/profile"
    legacyShopDetail = "$FrontendBaseUrl/shops/$shopId"
    legacyShopInventory = "$FrontendBaseUrl/shops/$shopId/inventory"
    legacyShopProductDetail = "$FrontendBaseUrl/shop-products/$($shopProductIds["dragonQuest"])"
    legacyReservations = "$FrontendBaseUrl/reservations"
    legacyReservationDetail = "$FrontendBaseUrl/reservations/$reservationId"
    legacyShopReservations = "$FrontendBaseUrl/shops/$shopId/reservations"
}

$summary = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    suffix = $Suffix
    apiBaseUrl = $ApiBaseUrl
    frontendBaseUrl = $FrontendBaseUrl
    users = [ordered]@{
        shopOwner = [ordered]@{
            id = $shopUser.id
            email = $shopEmail
            displayName = $shopUser.displayName
            password = $password
            rolesAfterRelogin = $shopLogin.roles
        }
        collector = [ordered]@{
            id = $collectorUser.id
            email = $collectorEmail
            displayName = $collectorUser.displayName
            password = $password
            roles = $collectorLogin.roles
        }
    }
    shopId = $shopId
    masterProductIds = $masterProductIds
    shopProductIds = $shopProductIds
    collectionId = $collectionId
    collectionItemIds = $collectionItemIds
    reservationId = $reservationId
    recommendations = [ordered]@{
        totalRecommendations = $recommendations.totalRecommendations
    }
    urls = $urls
}

$lastDataPath = Join-Path $PSScriptRoot ".last-demo-data.json"
$summary | ConvertTo-Json -Depth 20 | Set-Content -Path $lastDataPath -Encoding UTF8

Write-Host ""
Write-Host "CollectoHub demo data created successfully."
Write-Host "Suffix: $Suffix"
Write-Host "API: $ApiBaseUrl"
Write-Host "Frontend: $FrontendBaseUrl"
Write-Host ""
Write-Host "Demo users:"
Write-Host "  Shop owner: $shopEmail"
Write-Host "  Collector:  $collectorEmail"
Write-Host "  Password:   $password"
Write-Host ""
Write-Host "Created data:"
Write-Host "  Shop ID:        $shopId"
Write-Host "  Collection ID:  $collectionId"
Write-Host "  Reservation ID: $reservationId"
Write-Host "  Recommendations: $($recommendations.totalRecommendations)"
Write-Host "  Collection statuses: Dragon Quest WANTED, Galaxy Dragon WANTED, Retro Quest OWNED"
Write-Host ""
Write-Host "MVP 1 URLs:"
foreach ($key in @("home", "login", "catalog", "collections", "collectionDetail", "wanted", "profile")) {
    Write-Host "  ${key}: $($urls[$key])"
}
Write-Host ""
Write-Host "Legacy/future URLs:"
foreach ($key in @("legacyShopDetail", "legacyShopInventory", "legacyShopProductDetail", "legacyReservations", "legacyReservationDetail", "legacyShopReservations")) {
    Write-Host "  ${key}: $($urls[$key])"
}
Write-Host ""
Write-Host "Saved local summary: $lastDataPath"
Write-Host "This JSON is ignored by Git and should remain local."
