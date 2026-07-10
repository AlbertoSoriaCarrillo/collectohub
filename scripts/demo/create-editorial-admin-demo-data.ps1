[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$AdminEmail = "admin@collectohub.local",
    [string]$AdminPassword = "admin123",
    [string]$DemoSuffix = "local",
    [int]$MasterProductId = 0
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

function Get-ErrorDetails {
    param([Parameter(Mandatory = $true)][System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($ErrorRecord.ErrorDetails -and $ErrorRecord.ErrorDetails.Message) {
        return $ErrorRecord.ErrorDetails.Message
    }
    return $ErrorRecord.Exception.Message
}

function Get-HttpStatusCode {
    param([Parameter(Mandatory = $true)][System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($ErrorRecord.Exception.Response -and $ErrorRecord.Exception.Response.StatusCode) {
        return [int]$ErrorRecord.Exception.Response.StatusCode
    }
    return $null
}

function Invoke-EditorialApi {
    param(
        [Parameter(Mandatory = $true)][ValidateSet("GET", "POST", "PUT")][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [object]$Body,
        [string]$Token,
        [switch]$AllowConflict
    )

    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $parameters = @{ Method = $Method; Uri = "$ApiBaseUrl$Path"; Headers = $headers }
    if ($null -ne $Body) {
        $parameters["ContentType"] = "application/json"
        $parameters["Body"] = $Body | ConvertTo-Json -Depth 10
    }

    try {
        return Invoke-RestMethod @parameters
    }
    catch {
        $status = Get-HttpStatusCode -ErrorRecord $_
        if ($AllowConflict -and $status -eq 409) { return $null }
        $details = Get-ErrorDetails -ErrorRecord $_
        if ($status) { throw "HTTP $status calling $Method $Path. $details" }
        throw "Unable to call $Method $Path. $details"
    }
}

function Find-ByName {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Property,
        [Parameter(Mandatory = $true)][string]$Token
    )

    $separator = if ($Path.Contains("?")) { "&" } else { "?" }
    $response = Invoke-EditorialApi -Method "GET" -Path "$Path$separator`q=$([uri]::EscapeDataString($Name))&recordStatus=ACTIVE&size=50" -Token $Token
    return @($response.content | Where-Object { $_.$Property -eq $Name }) | Select-Object -First 1
}

function Get-OrCreateEntity {
    param(
        [Parameter(Mandatory = $true)][string]$SearchPath,
        [Parameter(Mandatory = $true)][string]$CreatePath,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Property,
        [Parameter(Mandatory = $true)][hashtable]$Body,
        [Parameter(Mandatory = $true)][string]$Token
    )

    $existing = Find-ByName -Path $SearchPath -Name $Name -Property $Property -Token $Token
    if ($existing) {
        Write-Host "Reusing $Property '$Name' (ID $($existing.id))."
        return $existing
    }

    $created = Invoke-EditorialApi -Method "POST" -Path $CreatePath -Body $Body -Token $Token -AllowConflict
    if ($created) { return $created }

    $existing = Find-ByName -Path $SearchPath -Name $Name -Property $Property -Token $Token
    if ($existing) {
        Write-Host "Conflict treated as existing $Property '$Name' (ID $($existing.id))."
        return $existing
    }
    throw "The API reported a conflict but '$Name' could not be found afterwards."
}

function Find-Item {
    param([int]$SeriesId, [string]$Title, [string]$Token)
    $response = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/series/$SeriesId/items?q=$([uri]::EscapeDataString($Title))&recordStatus=ACTIVE&size=50" -Token $Token
    return @($response.content | Where-Object { $_.title -eq $Title }) | Select-Object -First 1
}

function Get-OrCreateItem {
    param([int]$SeriesId, [string]$Title, [int]$Order, [string]$Token)
    $existing = Find-Item -SeriesId $SeriesId -Title $Title -Token $Token
    if ($existing) { return $existing }
    $body = [ordered]@{
        title = $Title; originalTitle = $null; sequenceLabel = "$Order"; sortOrder = $Order
        description = "Demo editorial item $Order created through the CollectoHub API."
        firstPublicationDate = $null; firstPublicationYear = 2026; originalLanguage = "es"
        originCountry = "ES"; recordStatus = "ACTIVE"
    }
    $created = Invoke-EditorialApi -Method "POST" -Path "/api/catalog/series/$SeriesId/items" -Body $body -Token $Token -AllowConflict
    if ($created) { return $created }
    $existing = Find-Item -SeriesId $SeriesId -Title $Title -Token $Token
    if ($existing) { return $existing }
    throw "The API reported a conflict but item '$Title' could not be found afterwards."
}

if ($WhatIfPreference) {
    Write-Host "WhatIf: no API calls will be made."
    Write-Host "Would authenticate ADMIN and create or reuse publisher, franchise, series, two items, edition, creator, credit, relationship and optional master-product link."
    return
}

$ApiBaseUrl = $ApiBaseUrl.TrimEnd("/")
$publisherName = "Demo Editorial CollectoHub $DemoSuffix"
$franchiseName = "Demo Saga CollectoHub $DemoSuffix"
$seriesTitle = "Demo Manga Series $DemoSuffix"
$item1Title = "Demo Volume 1 $DemoSuffix"
$item2Title = "Demo Volume 2 $DemoSuffix"
$creatorName = "Demo Author CollectoHub $DemoSuffix"
$slug = ("demo-saga-collectohub-$DemoSuffix" -replace "[^a-zA-Z0-9-]", "-").ToLowerInvariant()

Write-Host "Checking backend health at $ApiBaseUrl ..."
$health = Invoke-EditorialApi -Method "GET" -Path "/api/health"
if ($health.status -ne "UP") { throw "Backend health is not UP." }

Write-Host "Authenticating ADMIN '$AdminEmail' ..."
$login = Invoke-EditorialApi -Method "POST" -Path "/api/auth/login" -Body @{ email = $AdminEmail; password = $AdminPassword }
if (-not ($login.roles -contains "ADMIN")) { throw "The supplied account does not have the ADMIN role." }
$token = $login.accessToken

$publisher = Get-OrCreateEntity -SearchPath "/api/catalog/publishers" -CreatePath "/api/catalog/publishers" -Name $publisherName -Property "name" -Token $token -Body @{ name = $publisherName; country = "ES"; recordStatus = "ACTIVE" }
$franchise = Get-OrCreateEntity -SearchPath "/api/catalog/franchises" -CreatePath "/api/catalog/franchises" -Name $franchiseName -Property "name" -Token $token -Body @{ name = $franchiseName; slug = $slug; description = "Demo franchise for editorial admin validation."; recordStatus = "ACTIVE" }
$series = Get-OrCreateEntity -SearchPath "/api/catalog/series" -CreatePath "/api/catalog/series" -Name $seriesTitle -Property "title" -Token $token -Body @{
    franchiseId = $franchise.id; primaryPublisherId = $publisher.id; title = $seriesTitle; originalTitle = $null
    type = "MANGA"; publicationStatus = "ONGOING"; description = "Demo series for editorial admin validation."
    originCountry = "ES"; originalLanguage = "es"; startYear = 2026; endYear = $null; recordStatus = "ACTIVE"
}
$item1 = Get-OrCreateItem -SeriesId $series.id -Title $item1Title -Order 1 -Token $token
$item2 = Get-OrCreateItem -SeriesId $series.id -Title $item2Title -Order 2 -Token $token

$editionSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/editions?recordStatus=ACTIVE&size=50" -Token $token
$edition = @($editionSearch.content | Where-Object { $_.editionName -eq "Demo Volume 1 Paperback 2026 $DemoSuffix" }) | Select-Object -First 1
if (-not $edition) {
    $edition = Invoke-EditorialApi -Method "POST" -Path "/api/catalog/items/$($item1.id)/editions" -Token $token -AllowConflict -Body @{
        publisherId = $publisher.id; isbn = $null; ean = $null; format = "PAPERBACK"; editionName = "Demo Volume 1 Paperback 2026 $DemoSuffix"
        publicationDate = "2026-01-01"; publicationYear = 2026; language = "es"; country = "ES"; pageCount = 192; coverImageUrl = $null; recordStatus = "ACTIVE"
    }
    if (-not $edition) {
        $editionSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/editions?recordStatus=ACTIVE&size=50" -Token $token
        $edition = @($editionSearch.content | Where-Object { $_.editionName -eq "Demo Volume 1 Paperback 2026 $DemoSuffix" }) | Select-Object -First 1
    }
}
if (-not $edition) { throw "Could not create or reuse the demo edition." }

$creator = Get-OrCreateEntity -SearchPath "/api/catalog/creators" -CreatePath "/api/catalog/creators" -Name $creatorName -Property "name" -Token $token -Body @{
    name = $creatorName; slug = "demo-author-collectohub-$DemoSuffix"; sortName = $null; biography = "Demo creator for editorial admin validation."
    country = "ES"; birthYear = $null; deathYear = $null; recordStatus = "ACTIVE"
}

$creditSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/creators" -Token $token
$credit = @($creditSearch | Where-Object { $_.creatorId -eq $creator.id -and $_.creditRole -eq "AUTHOR" }) | Select-Object -First 1
if (-not $credit) {
    $credit = Invoke-EditorialApi -Method "POST" -Path "/api/catalog/items/$($item1.id)/creators" -Token $token -AllowConflict -Body @{ creatorId = $creator.id; creditRole = "AUTHOR"; creditOrder = 1; creditLabel = $null }
    if (-not $credit) {
        $creditSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/creators" -Token $token
        $credit = @($creditSearch | Where-Object { $_.creatorId -eq $creator.id -and $_.creditRole -eq "AUTHOR" }) | Select-Object -First 1
    }
}

$relationshipSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/relationships?recordStatus=ACTIVE" -Token $token
$relationship = @($relationshipSearch | Where-Object { $_.targetCatalogItemId -eq $item2.id -and $_.relationshipType -eq "SEQUEL" }) | Select-Object -First 1
if (-not $relationship) {
    $relationship = Invoke-EditorialApi -Method "POST" -Path "/api/catalog/items/$($item1.id)/relationships" -Token $token -AllowConflict -Body @{ targetCatalogItemId = $item2.id; relationshipType = "SEQUEL"; relationshipOrder = 1; description = "Demo sequel relationship."; recordStatus = "ACTIVE" }
    if (-not $relationship) {
        $relationshipSearch = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/items/$($item1.id)/relationships?recordStatus=ACTIVE" -Token $token
        $relationship = @($relationshipSearch | Where-Object { $_.targetCatalogItemId -eq $item2.id -and $_.relationshipType -eq "SEQUEL" }) | Select-Object -First 1
    }
}

$masterProductLink = $null
if ($MasterProductId -gt 0) {
    $links = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/master-product-links?masterProductId=$MasterProductId&catalogItemId=$($item1.id)&size=50" -Token $token
    $masterProductLink = @($links.content | Where-Object { $_.masterProductId -eq $MasterProductId -and $_.catalogItemId -eq $item1.id }) | Select-Object -First 1
    if (-not $masterProductLink) {
        $masterProductLink = Invoke-EditorialApi -Method "POST" -Path "/api/catalog/master-product-links" -Token $token -AllowConflict -Body @{
            masterProductId = $MasterProductId; catalogItemId = $item1.id; catalogItemEditionId = $edition.id
            linkStatus = "PROPOSED"; linkSource = "MANUAL"; confidenceScore = 1.0; matchReason = "Editorial admin demo"; reviewNote = "Created by demo script"
        }
        if (-not $masterProductLink) {
            $links = Invoke-EditorialApi -Method "GET" -Path "/api/catalog/master-product-links?masterProductId=$MasterProductId&catalogItemId=$($item1.id)&size=50" -Token $token
            $masterProductLink = @($links.content | Where-Object { $_.masterProductId -eq $MasterProductId -and $_.catalogItemId -eq $item1.id }) | Select-Object -First 1
        }
    }
} else {
    Write-Host "MasterProductId is 0; skipping the optional master product link."
}

$summary = [ordered]@{
    generatedAt = (Get-Date).ToString("o"); apiBaseUrl = $ApiBaseUrl; demoSuffix = $DemoSuffix
    publisherId = $publisher.id; franchiseId = $franchise.id; seriesId = $series.id
    item1Id = $item1.id; item2Id = $item2.id; editionId = $edition.id; creatorId = $creator.id
    creditId = if ($credit) { $credit.id } else { $null }; relationshipId = if ($relationship) { $relationship.id } else { $null }
    masterProductLinkId = if ($masterProductLink) { $masterProductLink.id } else { $null }
}
$summaryPath = Join-Path $PSScriptRoot ".last-editorial-admin-demo-data.json"
$summary | ConvertTo-Json -Depth 10 | Set-Content -Path $summaryPath -Encoding UTF8

Write-Host ""
Write-Host "Editorial admin demo data ready:"
$summary.GetEnumerator() | Where-Object { $_.Key -match "Id$" } | ForEach-Object { Write-Host "  $($_.Key): $($_.Value)" }
Write-Host "Saved local summary: $summaryPath"
