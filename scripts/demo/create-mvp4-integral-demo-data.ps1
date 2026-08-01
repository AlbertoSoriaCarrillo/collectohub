[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = "Medium")]
param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$FrontendBaseUrl = "http://localhost:4200",
    [ValidatePattern("^[a-z0-9][a-z0-9-]{0,40}$")]
    [string]$Scenario = "local",
    [string]$EditorialEmail = "admin@collectohub.local",
    [Security.SecureString]$EditorialPassword,
    [Security.SecureString]$DemoUserPassword,
    [string]$SummaryPath
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($SummaryPath)) {
    $SummaryPath = Join-Path -Path $PSScriptRoot `
        -ChildPath ".last-mvp4-integral-demo-data.json"
}

function Get-PlainTextSecret {
    param(
        [Security.SecureString]$Value,
        [Parameter(Mandatory = $true)][string]$EnvironmentVariable,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if ($null -ne $Value) {
        return (New-Object System.Net.NetworkCredential("", $Value)).Password
    }

    $environmentValue = [Environment]::GetEnvironmentVariable($EnvironmentVariable)
    if ([string]::IsNullOrWhiteSpace($environmentValue)) {
        throw "$Label is required. Pass a SecureString or set $EnvironmentVariable for this local process."
    }
    return $environmentValue
}

function Get-HttpStatusCode {
    param([Parameter(Mandatory = $true)][System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($ErrorRecord.Exception.Response -and $ErrorRecord.Exception.Response.StatusCode) {
        return [int]$ErrorRecord.Exception.Response.StatusCode
    }
    return $null
}

function Get-ErrorDetails {
    param([Parameter(Mandatory = $true)][System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        return $ErrorRecord.ErrorDetails.Message
    }
    return $ErrorRecord.Exception.Message
}

function Invoke-Mvp4Api {
    param(
        [Parameter(Mandatory = $true)][ValidateSet("GET", "POST", "PUT")][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [object]$Body,
        [string]$Token,
        [switch]$AllowConflict,
        [int[]]$AllowedStatusCodes = @()
    )

    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers.Authorization = "Bearer $Token"
    }
    $parameters = @{
        Method = $Method
        Uri = "$ApiBaseUrl$Path"
        Headers = $headers
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = $Body | ConvertTo-Json -Depth 20
    }

    try {
        return Invoke-RestMethod @parameters
    }
    catch {
        $statusCode = Get-HttpStatusCode -ErrorRecord $_
        if ($AllowConflict -and $statusCode -eq 409) {
            return $null
        }
        if ($AllowedStatusCodes -contains $statusCode) {
            return [pscustomobject]@{ __ExpectedHttpStatus = $statusCode }
        }
        $details = Get-ErrorDetails -ErrorRecord $_
        if ($statusCode) {
            throw "Phase API failed with HTTP $statusCode calling $Method $Path. $details"
        }
        throw "Phase API failed calling $Method $Path. $details"
    }
}

function Get-ResponseItems {
    param([object]$Response)

    if ($null -eq $Response) { return @() }
    if ($null -ne $Response.PSObject.Properties["content"]) { return @($Response.content) }
    return @($Response)
}

function Assert-Compatible {
    param(
        [Parameter(Mandatory = $true)][object]$Resource,
        [Parameter(Mandatory = $true)][hashtable]$Expected,
        [Parameter(Mandatory = $true)][string]$Label
    )

    foreach ($key in $Expected.Keys) {
        $property = $Resource.PSObject.Properties[$key]
        if ($null -eq $property) {
            throw "$Label is incompatible: response has no '$key' field."
        }
        $actual = $property.Value
        $wanted = $Expected[$key]
        if ($null -eq $actual -and $null -eq $wanted) { continue }
        if ([string]$actual -cne [string]$wanted) {
            throw "$Label is incompatible: '$key' is '$actual', expected '$wanted'."
        }
    }
    return $Resource
}

function Select-UniqueMatch {
    param(
        [Parameter(Mandatory = $true)][object[]]$Items,
        [Parameter(Mandatory = $true)][scriptblock]$Predicate,
        [Parameter(Mandatory = $true)][string]$Label
    )

    $matches = @($Items | Where-Object { & $Predicate $_ })
    if ($matches.Count -gt 1) {
        throw "$Label is ambiguous: found $($matches.Count) exact resources."
    }
    if ($matches.Count -eq 1) { return $matches[0] }
    return $null
}

function Get-OrCreateNamedEntity {
    param(
        [Parameter(Mandatory = $true)][string]$SearchPath,
        [Parameter(Mandatory = $true)][string]$CreatePath,
        [Parameter(Mandatory = $true)][string]$NameProperty,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][hashtable]$Body,
        [Parameter(Mandatory = $true)][hashtable]$Expected,
        [Parameter(Mandatory = $true)][string]$Token,
        [Parameter(Mandatory = $true)][string]$Label
    )

    $response = Invoke-Mvp4Api -Method GET -Path $SearchPath -Token $Token
    $existing = Select-UniqueMatch -Items (Get-ResponseItems $response) -Label $Label -Predicate {
        param($item) $item.$NameProperty -ceq $Name
    }
    if ($existing) {
        Write-Host "Reusing $Label (ID $($existing.id))."
        return Assert-Compatible -Resource $existing -Expected $Expected -Label $Label
    }

    $created = Invoke-Mvp4Api -Method POST -Path $CreatePath -Body $Body -Token $Token -AllowConflict
    if (-not $created) {
        $response = Invoke-Mvp4Api -Method GET -Path $SearchPath -Token $Token
        $created = Select-UniqueMatch -Items (Get-ResponseItems $response) -Label $Label -Predicate {
            param($item) $item.$NameProperty -ceq $Name
        }
    }
    if (-not $created) { throw "$Label could not be created or found after a conflict." }
    return Assert-Compatible -Resource $created -Expected $Expected -Label $Label
}

function Write-AtomicSummary {
    param(
        [Parameter(Mandatory = $true)][hashtable]$Summary,
        [Parameter(Mandatory = $true)][string]$Phase
    )

    $Summary.phase = $Phase
    $Summary.updatedAt = (Get-Date).ToString("o")
    $directory = Split-Path -Parent $SummaryPath
    if ([string]::IsNullOrWhiteSpace($directory)) { $directory = "." }
    if (-not (Test-Path -LiteralPath $directory)) {
        New-Item -ItemType Directory -Path $directory | Out-Null
    }
    $temporaryPath = "$SummaryPath.tmp"
    $Summary | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $temporaryPath -Encoding UTF8
    Move-Item -LiteralPath $temporaryPath -Destination $SummaryPath -Force
}

function Ensure-DemoUser {
    param(
        [Parameter(Mandatory = $true)][string]$Email,
        [Parameter(Mandatory = $true)][string]$Password,
        [Parameter(Mandatory = $true)][string]$DisplayName
    )

    $registered = Invoke-Mvp4Api -Method POST -Path "/api/auth/register" -AllowConflict -Body @{
        email = $Email
        password = $Password
        displayName = $DisplayName
        preferredInterfaceLanguage = "es"
    }
    $login = Invoke-Mvp4Api -Method POST -Path "/api/auth/login" -Body @{ email = $Email; password = $Password }
    $me = Invoke-Mvp4Api -Method GET -Path "/api/users/me" -Token $login.accessToken
    if ($me.email -cne $Email -or -not ($me.roles -contains "USER")) {
        throw "Demo user '$Email' exists with incompatible identity or roles."
    }
    if ($registered -and [int64]$registered.id -ne [int64]$me.id) {
        throw "Registered user '$Email' does not match the authenticated identity."
    }
    return [pscustomobject]@{ user = $me; login = $login }
}

function Ensure-Collection {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Description,
        [Parameter(Mandatory = $true)][ValidateSet("PUBLIC", "PRIVATE")][string]$Visibility,
        [Parameter(Mandatory = $true)][string]$Token
    )

    $collections = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/my" -Token $Token)
    $collection = Select-UniqueMatch -Items $collections -Label "collection '$Name'" -Predicate {
        param($item) $item.name -ceq $Name
    }
    if (-not $collection) {
        $collection = Invoke-Mvp4Api -Method POST -Path "/api/collections" -Token $Token -Body @{
            name = $Name; description = $Description; visibility = $Visibility; categoryCode = "MANGA_COMIC"
        }
    }
    return Assert-Compatible -Resource $collection -Label "collection '$Name'" -Expected @{
        name = $Name; description = $Description; visibility = $Visibility; categoryCode = "MANGA_COMIC"
    }
}

function Ensure-CollectionItem {
    param(
        [Parameter(Mandatory = $true)][long]$CollectionId,
        [Parameter(Mandatory = $true)][scriptblock]$Predicate,
        [Parameter(Mandatory = $true)][hashtable]$Body,
        [Parameter(Mandatory = $true)][hashtable]$Expected,
        [Parameter(Mandatory = $true)][string]$Token,
        [Parameter(Mandatory = $true)][string]$Label
    )

    $items = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$CollectionId/items" -Token $Token)
    $item = Select-UniqueMatch -Items $items -Predicate $Predicate -Label $Label
    if (-not $item) {
        $item = Invoke-Mvp4Api -Method POST -Path "/api/collections/$CollectionId/items" -Body $Body -Token $Token
    }
    return Assert-Compatible -Resource $item -Expected $Expected -Label $Label
}

function Get-OrCreateMasterProduct {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Token,
        [Parameter(Mandatory = $true)][string]$Kind
    )

    $path = "/api/master-products?name=$([uri]::EscapeDataString($Name))&status=ACTIVE"
    $products = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path $path)
    $product = Select-UniqueMatch -Items $products -Label "master product '$Name'" -Predicate {
        param($item) $item.name -ceq $Name
    }
    if (-not $product) {
        $product = Invoke-Mvp4Api -Method POST -Path "/api/master-products" -Token $Token -Body @{
            name = $Name
            description = "CollectoHub MVP4 demo $Kind for scenario $Scenario."
            categoryCode = "MANGA_COMIC"
            franchise = "CollectoHub Demo Saga $Scenario"
            collectionName = "CollectoHub MVP4 Series $Scenario"
            volumeNumber = if ($Kind -eq "verified bridge") { "1" } else { "legacy" }
            publisher = "CollectoHub Demo Editorial $Scenario"
            language = "es"
            limitedEdition = $false
            publicationCountries = @("ES")
            attributes = @{ demoScenario = $Scenario; demoReferenceKind = $Kind }
        }
    }
    $product = Assert-Compatible -Resource $product -Label "master product '$Name'" -Expected @{ name = $Name }
    if ($null -eq $product.category -or $product.category.code -ne "MANGA_COMIC") {
        throw "Master product '$Name' is incompatible: category is not MANGA_COMIC."
    }
    return $product
}

function Assert-ExpectedStatus {
    param([object]$Response, [int[]]$Allowed, [string]$Label)
    $property = $Response.PSObject.Properties["__ExpectedHttpStatus"]
    if ($null -eq $property -or -not ($Allowed -contains [int]$property.Value)) {
        throw "$Label did not return an expected status: $($Allowed -join ', ')."
    }
}

$ApiBaseUrl = $ApiBaseUrl.TrimEnd("/")
$FrontendBaseUrl = $FrontendBaseUrl.TrimEnd("/")
$scenarioKey = "mvp4-$Scenario"
$publisherName = "CollectoHub Demo Editorial $Scenario"
$franchiseName = "CollectoHub Demo Saga $Scenario"
$seriesTitle = "CollectoHub MVP4 Series $Scenario"
$itemTitles = @("Demo Volume 1 $Scenario", "Demo Volume 2 $Scenario", "Demo Volume 3 $Scenario")
$editionName = "Demo Volume 1 Paperback ES $Scenario"
$creatorName = "CollectoHub Demo Author $Scenario"
$ownerEmail = "demo.mvp4.owner.$Scenario@collectohub.local"
$readerEmail = "demo.mvp4.reader.$Scenario@collectohub.local"
$publicCollectionName = "MVP4 Demo Public $Scenario"
$privateCollectionName = "MVP4 Demo Private $Scenario"

if ($WhatIfPreference) {
    Write-Output "WhatIf: no HTTP, psql, or file operations will run."
    Write-Output "Scenario key: $scenarioKey"
    Write-Output "Would find, validate, and create only if absent: editorial catalog, two demo users, two collections, D1-D3, M1, B1, L1, P1, and one VERIFIED bridge."
    Write-Output "Would atomically update the secret-free summary after each validated phase: $SummaryPath"
    return
}

if (-not $PSCmdlet.ShouldProcess("CollectoHub local scenario '$scenarioKey'", "create or resume idempotent MVP4 demo data")) { return }

$summary = [ordered]@{
    schemaVersion = 1
    scenario = $Scenario
    scenarioKey = $scenarioKey
    generatedAt = (Get-Date).ToString("o")
    apiBaseUrl = $ApiBaseUrl
    frontendBaseUrl = $FrontendBaseUrl
    phase = "STARTED"
    updatedAt = (Get-Date).ToString("o")
    services = [ordered]@{ backend = "NOT_RUN" }
    users = [ordered]@{}
    catalog = [ordered]@{}
    collections = [ordered]@{}
    items = [ordered]@{}
    validation = [ordered]@{}
    urls = [ordered]@{}
}

Write-Host "Checking backend health at $ApiBaseUrl ..."
$health = Invoke-Mvp4Api -Method GET -Path "/api/health"
if ($health.status -ne "UP") { throw "Phase PREFLIGHT failed: backend health is not UP." }
$editorialPlainPassword = Get-PlainTextSecret -Value $EditorialPassword -EnvironmentVariable "COLLECTOHUB_DEMO_EDITORIAL_PASSWORD" -Label "Editorial password"
$editorialLogin = Invoke-Mvp4Api -Method POST -Path "/api/auth/login" -Body @{ email = $EditorialEmail; password = $editorialPlainPassword }
if (-not ($editorialLogin.roles -contains "ADMIN")) {
    throw "Phase PREFLIGHT failed: the editorial operator must have ADMIN for master products and bridge verification."
}
$editorialToken = $editorialLogin.accessToken
$summary.services.backend = "PASS"
$summary.services.editorialOperatorRole = "ADMIN"
Write-AtomicSummary -Summary $summary -Phase "PREFLIGHT_VALIDATED"

Write-Host "Creating or validating the editorial catalog ..."
$publisher = Get-OrCreateNamedEntity -SearchPath "/api/catalog/publishers?q=$([uri]::EscapeDataString($publisherName))&recordStatus=ACTIVE&size=50" -CreatePath "/api/catalog/publishers" -NameProperty "name" -Name $publisherName -Token $editorialToken -Label "publisher '$publisherName'" -Body @{
    name = $publisherName; country = "ES"; recordStatus = "ACTIVE"
} -Expected @{ name = $publisherName; country = "ES"; recordStatus = "ACTIVE" }

$franchiseSlug = ("collectohub-demo-saga-$Scenario" -replace "[^a-z0-9-]", "-").ToLowerInvariant()
$franchise = Get-OrCreateNamedEntity -SearchPath "/api/catalog/franchises?q=$([uri]::EscapeDataString($franchiseName))&recordStatus=ACTIVE&size=50" -CreatePath "/api/catalog/franchises" -NameProperty "name" -Name $franchiseName -Token $editorialToken -Label "franchise '$franchiseName'" -Body @{
    name = $franchiseName; slug = $franchiseSlug; description = "CollectoHub MVP4 demo franchise $Scenario."; recordStatus = "ACTIVE"
} -Expected @{ name = $franchiseName; slug = $franchiseSlug; recordStatus = "ACTIVE" }

$series = Get-OrCreateNamedEntity -SearchPath "/api/catalog/series?q=$([uri]::EscapeDataString($seriesTitle))&recordStatus=ACTIVE&size=50" -CreatePath "/api/catalog/series" -NameProperty "title" -Name $seriesTitle -Token $editorialToken -Label "series '$seriesTitle'" -Body @{
    franchiseId = $franchise.id; primaryPublisherId = $publisher.id; title = $seriesTitle; type = "MANGA"
    publicationStatus = "ONGOING"; description = "CollectoHub MVP4 demo series $Scenario."
    originCountry = "ES"; originalLanguage = "es"; startYear = 2026; recordStatus = "ACTIVE"
} -Expected @{ title = $seriesTitle; franchiseId = $franchise.id; primaryPublisherId = $publisher.id; recordStatus = "ACTIVE" }

$catalogItems = @()
for ($index = 0; $index -lt 3; $index++) {
    $title = $itemTitles[$index]
    $order = $index + 1
    $search = Invoke-Mvp4Api -Method GET -Path "/api/catalog/series/$($series.id)/items?q=$([uri]::EscapeDataString($title))&recordStatus=ACTIVE&size=50" -Token $editorialToken
    $item = Select-UniqueMatch -Items (Get-ResponseItems $search) -Label "catalog item '$title'" -Predicate { param($candidate) $candidate.title -ceq $title }
    if (-not $item) {
        $item = Invoke-Mvp4Api -Method POST -Path "/api/catalog/series/$($series.id)/items" -Token $editorialToken -AllowConflict -Body @{
            title = $title; sequenceLabel = [string]$order; sortOrder = $order
            description = "CollectoHub MVP4 demo volume $order for scenario $Scenario."
            firstPublicationYear = 2026; originalLanguage = "es"; originCountry = "ES"; recordStatus = "ACTIVE"
        }
        if (-not $item) {
            $search = Invoke-Mvp4Api -Method GET -Path "/api/catalog/series/$($series.id)/items?q=$([uri]::EscapeDataString($title))&recordStatus=ACTIVE&size=50" -Token $editorialToken
            $item = Select-UniqueMatch -Items (Get-ResponseItems $search) -Label "catalog item '$title'" -Predicate { param($candidate) $candidate.title -ceq $title }
        }
    }
    if (-not $item) { throw "Catalog item '$title' could not be created or found after a conflict." }
    $catalogItems += Assert-Compatible -Resource $item -Label "catalog item '$title'" -Expected @{
        title = $title; seriesId = $series.id; sequenceLabel = [string]$order; recordStatus = "ACTIVE"
    }
}

$editionResponse = Invoke-Mvp4Api -Method GET -Path "/api/catalog/items/$($catalogItems[0].id)/editions?recordStatus=ACTIVE&size=50" -Token $editorialToken
$edition = Select-UniqueMatch -Items (Get-ResponseItems $editionResponse) -Label "edition '$editionName'" -Predicate { param($candidate) $candidate.editionName -ceq $editionName }
if (-not $edition) {
    $edition = Invoke-Mvp4Api -Method POST -Path "/api/catalog/items/$($catalogItems[0].id)/editions" -Token $editorialToken -AllowConflict -Body @{
        publisherId = $publisher.id; format = "PAPERBACK"; editionName = $editionName; publicationDate = "2026-01-01"
        publicationYear = 2026; language = "es"; country = "ES"; pageCount = 192; recordStatus = "ACTIVE"
    }
}
if (-not $edition) { throw "Edition '$editionName' could not be created or reused." }
$edition = Assert-Compatible -Resource $edition -Label "edition '$editionName'" -Expected @{
    editionName = $editionName; catalogItemId = $catalogItems[0].id; publisherId = $publisher.id; format = "PAPERBACK"; recordStatus = "ACTIVE"
}

$creatorSlug = "collectohub-demo-author-$Scenario"
$creator = Get-OrCreateNamedEntity -SearchPath "/api/catalog/creators?q=$([uri]::EscapeDataString($creatorName))&recordStatus=ACTIVE&size=50" -CreatePath "/api/catalog/creators" -NameProperty "name" -Name $creatorName -Token $editorialToken -Label "creator '$creatorName'" -Body @{
    name = $creatorName; slug = $creatorSlug; biography = "CollectoHub MVP4 demo author $Scenario."; country = "ES"; recordStatus = "ACTIVE"
} -Expected @{ name = $creatorName; slug = $creatorSlug; country = "ES"; recordStatus = "ACTIVE" }

$credits = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/catalog/items/$($catalogItems[0].id)/creators" -Token $editorialToken)
$credit = Select-UniqueMatch -Items $credits -Label "AUTHOR credit" -Predicate { param($candidate) [int64]$candidate.creatorId -eq [int64]$creator.id -and $candidate.creditRole -eq "AUTHOR" }
if (-not $credit) {
    $credit = Invoke-Mvp4Api -Method POST -Path "/api/catalog/items/$($catalogItems[0].id)/creators" -Token $editorialToken -AllowConflict -Body @{ creatorId = $creator.id; creditRole = "AUTHOR"; creditOrder = 1 }
}
if (-not $credit) { throw "AUTHOR credit could not be created or reused." }
$credit = Assert-Compatible -Resource $credit -Label "AUTHOR credit" -Expected @{ creatorId = $creator.id; creditRole = "AUTHOR"; creditOrder = 1 }

$relationships = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/catalog/items/$($catalogItems[0].id)/relationships?recordStatus=ACTIVE" -Token $editorialToken)
$relationship = Select-UniqueMatch -Items $relationships -Label "SEQUEL relationship" -Predicate { param($candidate) [int64]$candidate.targetCatalogItemId -eq [int64]$catalogItems[1].id -and $candidate.relationshipType -eq "SEQUEL" }
if (-not $relationship) {
    $relationship = Invoke-Mvp4Api -Method POST -Path "/api/catalog/items/$($catalogItems[0].id)/relationships" -Token $editorialToken -AllowConflict -Body @{
        targetCatalogItemId = $catalogItems[1].id; relationshipType = "SEQUEL"; relationshipOrder = 1
        description = "CollectoHub MVP4 demo sequel $Scenario."; recordStatus = "ACTIVE"
    }
}
if (-not $relationship) { throw "SEQUEL relationship could not be created or reused." }
$relationship = Assert-Compatible -Resource $relationship -Label "SEQUEL relationship" -Expected @{
    targetCatalogItemId = $catalogItems[1].id; relationshipType = "SEQUEL"; relationshipOrder = 1; recordStatus = "ACTIVE"
}

$verifiedMaster = Get-OrCreateMasterProduct -Name "Demo Verified Bridge $Scenario" -Token $editorialToken -Kind "verified bridge"
$legacyMaster = Get-OrCreateMasterProduct -Name "Demo Legacy Unresolved $Scenario" -Token $editorialToken -Kind "legacy unresolved"
$linksResponse = Invoke-Mvp4Api -Method GET -Path "/api/catalog/master-product-links?masterProductId=$($verifiedMaster.id)&catalogItemId=$($catalogItems[0].id)&size=50" -Token $editorialToken
$link = Select-UniqueMatch -Items (Get-ResponseItems $linksResponse) -Label "verified bridge" -Predicate {
    param($candidate) [int64]$candidate.masterProductId -eq [int64]$verifiedMaster.id -and [int64]$candidate.catalogItemId -eq [int64]$catalogItems[0].id
}
if (-not $link) {
    $link = Invoke-Mvp4Api -Method POST -Path "/api/catalog/master-product-links" -Token $editorialToken -AllowConflict -Body @{
        masterProductId = $verifiedMaster.id; catalogItemId = $catalogItems[0].id; catalogItemEditionId = $edition.id
        linkStatus = "PROPOSED"; linkSource = "MANUAL"; confidenceScore = 1.0
        matchReason = "CollectoHub MVP4 demo exact bridge $Scenario"; reviewNote = "Local demo data only"
    }
}
if (-not $link) { throw "Verified bridge could not be created or reused." }
if ($link.linkStatus -eq "PROPOSED") {
    $link = Invoke-Mvp4Api -Method PUT -Path "/api/catalog/master-product-links/$($link.id)/verify" -Token $editorialToken
}
$link = Assert-Compatible -Resource $link -Label "verified bridge" -Expected @{
    masterProductId = $verifiedMaster.id; catalogItemId = $catalogItems[0].id; catalogItemEditionId = $edition.id; linkStatus = "VERIFIED"
}

$summary.catalog = [ordered]@{
    publisherId = $publisher.id; franchiseId = $franchise.id; seriesId = $series.id
    item1Id = $catalogItems[0].id; item2Id = $catalogItems[1].id; item3Id = $catalogItems[2].id
    editionId = $edition.id; creatorId = $creator.id; creditId = $credit.id; relationshipId = $relationship.id
    verifiedMasterProductId = $verifiedMaster.id; unresolvedMasterProductId = $legacyMaster.id; verifiedLinkId = $link.id
}
Write-AtomicSummary -Summary $summary -Phase "CATALOG_VALIDATED"

Write-Host "Creating or validating demo users ..."
$demoPlainPassword = Get-PlainTextSecret -Value $DemoUserPassword -EnvironmentVariable "COLLECTOHUB_DEMO_USER_PASSWORD" -Label "Demo user password"
$ownerContext = Ensure-DemoUser -Email $ownerEmail -Password $demoPlainPassword -DisplayName "Demo MVP4 Owner $Scenario"
$readerContext = Ensure-DemoUser -Email $readerEmail -Password $demoPlainPassword -DisplayName "Demo MVP4 Reader $Scenario"
$ownerToken = $ownerContext.login.accessToken
$readerToken = $readerContext.login.accessToken
$summary.users.owner = [ordered]@{ id = $ownerContext.user.id; email = $ownerEmail; roles = @($ownerContext.user.roles) }
$summary.users.reader = [ordered]@{ id = $readerContext.user.id; email = $readerEmail; roles = @($readerContext.user.roles) }
Write-AtomicSummary -Summary $summary -Phase "USERS_VALIDATED"

Write-Host "Creating or validating collections ..."
$publicCollection = Ensure-Collection -Name $publicCollectionName -Description "CollectoHub public MVP4 demo for scenario $Scenario." -Visibility PUBLIC -Token $ownerToken
$privateCollection = Ensure-Collection -Name $privateCollectionName -Description "CollectoHub private MVP4 demo for scenario $Scenario." -Visibility PRIVATE -Token $ownerToken
$summary.collections.publicId = $publicCollection.id
$summary.collections.privateId = $privateCollection.id
$summary.urls.publicCollection = "$FrontendBaseUrl/collections/$($publicCollection.id)"
$summary.urls.privateCollection = "$FrontendBaseUrl/collections/$($privateCollection.id)"
$summary.urls.seriesProgress = "$FrontendBaseUrl/collections/$($publicCollection.id)/series/$($series.id)/progress"
Write-AtomicSummary -Summary $summary -Phase "COLLECTIONS_VALIDATED"

Write-Host "Creating or validating the D1-D3, M1, B1, L1 and P1 matrix ..."
$d1 = Ensure-CollectionItem -CollectionId $publicCollection.id -Token $ownerToken -Label "D1 direct OWNED" -Predicate {
    param($item) [int64]$item.catalogItemId -eq [int64]$catalogItems[0].id -and $null -eq $item.masterProductId
} -Body @{
    catalogItemId = $catalogItems[0].id; catalogItemEditionId = $edition.id; collectionStatus = "OWNED"
    physicalCondition = "GOOD"; notes = "OWNER_PRIVATE_D1_$Scenario"; acquiredAt = "2026-07-31"
} -Expected @{ catalogItemId = $catalogItems[0].id; catalogItemEditionId = $edition.id; collectionStatus = "OWNED"; referenceKind = "DIRECT_CATALOG"; notes = "OWNER_PRIVATE_D1_$Scenario" }

$d2 = Ensure-CollectionItem -CollectionId $publicCollection.id -Token $ownerToken -Label "D2 direct WANTED" -Predicate {
    param($item) [int64]$item.catalogItemId -eq [int64]$catalogItems[1].id -and $null -eq $item.masterProductId
} -Body @{
    catalogItemId = $catalogItems[1].id; collectionStatus = "WANTED"; notes = "OWNER_PRIVATE_D2_$Scenario"
} -Expected @{ catalogItemId = $catalogItems[1].id; collectionStatus = "WANTED"; referenceKind = "DIRECT_CATALOG"; notes = "OWNER_PRIVATE_D2_$Scenario" }

$manualTitle = "Demo Manual Unlinked $Scenario"
$m1 = Ensure-CollectionItem -CollectionId $publicCollection.id -Token $ownerToken -Label "M1 manual OWNED" -Predicate {
    param($item) $item.manualTitle -ceq $manualTitle -and $null -eq $item.catalogItemId -and $null -eq $item.masterProductId
} -Body @{
    collectionStatus = "OWNED"; physicalCondition = "GOOD"; manualTitle = $manualTitle
    manualType = "MANGA_COMIC"; manualDescription = "CollectoHub MVP4 manual demo $Scenario."
    notes = "OWNER_PRIVATE_M1_$Scenario"
} -Expected @{ manualTitle = $manualTitle; collectionStatus = "OWNED"; referenceKind = "MANUAL"; notes = "OWNER_PRIVATE_M1_$Scenario" }

$b1 = Ensure-CollectionItem -CollectionId $publicCollection.id -Token $ownerToken -Label "B1 verified bridge DUPLICATED" -Predicate {
    param($item) [int64]$item.masterProductId -eq [int64]$verifiedMaster.id
} -Body @{
    masterProductId = $verifiedMaster.id; collectionStatus = "DUPLICATED"; physicalCondition = "GOOD"
    unitNumber = "DEMO-$Scenario"; notes = "OWNER_PRIVATE_B1_$Scenario"
} -Expected @{ masterProductId = $verifiedMaster.id; catalogItemId = $catalogItems[0].id; collectionStatus = "DUPLICATED"; referenceKind = "VERIFIED_BRIDGE"; unitNumber = "DEMO-$Scenario" }

$l1 = Ensure-CollectionItem -CollectionId $publicCollection.id -Token $ownerToken -Label "L1 unresolved legacy WANTED" -Predicate {
    param($item) [int64]$item.masterProductId -eq [int64]$legacyMaster.id
} -Body @{
    masterProductId = $legacyMaster.id; collectionStatus = "WANTED"
} -Expected @{ masterProductId = $legacyMaster.id; collectionStatus = "WANTED"; referenceKind = "LEGACY_UNRESOLVED" }

$privateManualTitle = "Demo Private Manual $Scenario"
$p1 = Ensure-CollectionItem -CollectionId $privateCollection.id -Token $ownerToken -Label "P1 private manual OWNED" -Predicate {
    param($item) $item.manualTitle -ceq $privateManualTitle -and $null -eq $item.catalogItemId -and $null -eq $item.masterProductId
} -Body @{
    collectionStatus = "OWNED"; physicalCondition = "GOOD"; manualTitle = $privateManualTitle
    manualType = "MANGA_COMIC"; manualDescription = "CollectoHub MVP4 private demo $Scenario."
    notes = "OWNER_PRIVATE_P1_$Scenario"; acquiredAt = "2026-07-31"
} -Expected @{ manualTitle = $privateManualTitle; collectionStatus = "OWNED"; referenceKind = "MANUAL"; notes = "OWNER_PRIVATE_P1_$Scenario" }

$summary.items = [ordered]@{
    D1 = $d1.id; D2 = $d2.id; D3 = $null; M1 = $m1.id; B1 = $b1.id; L1 = $l1.id; P1 = $p1.id
}
Write-AtomicSummary -Summary $summary -Phase "ITEMS_VALIDATED"

Write-Host "Validating idempotent matrix, progress, filters, sorts and privacy ..."
$ownerItems = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items" -Token $ownerToken)
if ($ownerItems.Count -ne 5) { throw "Validation failed: public collection must contain exactly five persisted demo rows, found $($ownerItems.Count)." }
if (@($ownerItems | Where-Object { $_.collectionStatus -eq "MISSING" }).Count -ne 0) { throw "Validation failed: D3 MISSING must not be persisted." }

$progress = Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/series/$($series.id)/progress" -Token $ownerToken
if ($progress.totalCatalogItems -ne 3 -or $progress.ownedItems -ne 1 -or $progress.wantedItems -ne 1 -or $progress.missingItems -ne 1 -or $progress.completionPercentage -ne 33) {
    throw "Validation failed: expected progress 3 total, 1 OWNED, 1 WANTED, 1 MISSING and 33 percent."
}
$byCatalogId = @{}
foreach ($entry in $progress.items) { $byCatalogId[[int64]$entry.catalogItemId] = $entry }
if ($byCatalogId[[int64]$catalogItems[2].id].calculatedStatus -ne "MISSING") { throw "Validation failed: D3 is not calculated MISSING." }

foreach ($sort in @("CATALOG_ORDER", "TITLE_ASC", "TITLE_DESC", "STATUS_ASC", "NEWEST_ENTRY")) {
    $sorted = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items?sort=$sort" -Token $ownerToken)
    if ($sorted.Count -ne 5) { throw "Validation failed: sort $sort changed the result set." }
}
$manualItems = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items?referenceKind=MANUAL" -Token $ownerToken)
if ($manualItems.Count -ne 1 -or [int64]$manualItems[0].id -ne [int64]$m1.id) { throw "Validation failed: MANUAL filter does not isolate M1." }
$seriesItems = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items?seriesId=$($series.id)" -Token $ownerToken)
if ($seriesItems.Count -ne 3) { throw "Validation failed: series filter must return D1, D2 and B1." }

$readerItems = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items" -Token $readerToken)
if (@($readerItems | Where-Object { $null -ne $_.notes -or $null -ne $_.acquiredAt }).Count -ne 0) { throw "Validation failed: reader received private fields." }
$anonymousItems = Get-ResponseItems (Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/items")
if (@($anonymousItems | Where-Object { $null -ne $_.notes -or $null -ne $_.acquiredAt }).Count -ne 0) { throw "Validation failed: anonymous visitor received private fields." }

$readerPrivate = Invoke-Mvp4Api -Method GET -Path "/api/collections/$($privateCollection.id)" -Token $readerToken -AllowedStatusCodes @(404)
Assert-ExpectedStatus -Response $readerPrivate -Allowed @(404) -Label "Reader private collection"
$anonymousPrivate = Invoke-Mvp4Api -Method GET -Path "/api/collections/$($privateCollection.id)" -AllowedStatusCodes @(404)
Assert-ExpectedStatus -Response $anonymousPrivate -Allowed @(404) -Label "Anonymous private collection"
$readerProgress = Invoke-Mvp4Api -Method GET -Path "/api/collections/$($publicCollection.id)/series-progress" -Token $readerToken -AllowedStatusCodes @(403, 404)
Assert-ExpectedStatus -Response $readerProgress -Allowed @(403, 404) -Label "Reader owner-only progress"

$summary.validation = [ordered]@{
    persistedPublicRows = 5; persistedMissingRows = 0; progressTotal = 3; owned = 1; wanted = 1; missing = 1
    completionPercentage = 33; filters = "PASS"; sorts = "PASS"; publicSanitization = "PASS"; privateVisibility = "PASS"; ownerOnlyProgress = "PASS"
}
Write-AtomicSummary -Summary $summary -Phase "COMPLETE"

$editorialPlainPassword = $null
$demoPlainPassword = $null
Write-Host "MVP4 integral demo data is complete and compatible for scenario '$Scenario'."
Write-Host "Public collection: $($summary.urls.publicCollection)"
Write-Host "Series progress: $($summary.urls.seriesProgress)"
Write-Host "Secret-free summary: $SummaryPath"
