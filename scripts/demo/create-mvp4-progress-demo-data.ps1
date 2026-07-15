[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$FrontendBaseUrl = "http://localhost:4200",
    [string]$AdminEmail = "admin@collectohub.local",
    [string]$AdminPassword = "admin123",
    [string]$DemoSuffix,
    [string]$CollectorPassword = "Demo1234!"
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

function New-DemoSuffix {
    "$(Get-Date -Format 'yyyyMMdd-HHmmss')-$([guid]::NewGuid().ToString('N').Substring(0, 6))"
}

function Invoke-ProgressApi {
    param([Parameter(Mandatory = $true)][ValidateSet('GET', 'POST')][string]$Method,
          [Parameter(Mandatory = $true)][string]$Path,
          [object]$Body,
          [string]$Token)
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{ Method = $Method; Uri = "$ApiBaseUrl$Path"; Headers = $headers }
    if ($null -ne $Body) { $parameters.ContentType = 'application/json'; $parameters.Body = $Body | ConvertTo-Json -Depth 10 }
    try { return Invoke-RestMethod @parameters }
    catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { $null }
        if ($status -eq 409) { throw "Conflict 409. The demo suffix '$DemoSuffix' already exists; use another -DemoSuffix." }
        if ($status) { throw "HTTP $status calling $Method $Path. $($_.ErrorDetails.Message)" }
        throw "Unable to call $Method $Path. $($_.Exception.Message)"
    }
}

function Assert-PositiveId {
    param([object]$Value, [string]$Name)
    if ($null -eq $Value -or [int64]$Value -le 0) { throw "Editorial demo summary has no valid $Name." }
    return [int64]$Value
}

if ([string]::IsNullOrWhiteSpace($DemoSuffix)) { $DemoSuffix = New-DemoSuffix }
$ApiBaseUrl = $ApiBaseUrl.TrimEnd('/')
$FrontendBaseUrl = $FrontendBaseUrl.TrimEnd('/')
$editorialScript = Join-Path $PSScriptRoot 'create-editorial-admin-demo-data.ps1'
$summaryPath = Join-Path $PSScriptRoot '.last-mvp4-progress-demo-data.json'

if ($WhatIfPreference) {
    Write-Host "WhatIf: no API calls or local files will be created."
    Write-Host "Would create or reuse a three-item editorial series, register a collector, create a collection, add OWNED and WANTED entries, leave item 3 as calculated MISSING, and validate 1/1/1 at 33%."
    & $editorialScript -ApiBaseUrl $ApiBaseUrl -AdminEmail $AdminEmail -AdminPassword $AdminPassword -DemoSuffix $DemoSuffix -WhatIf
    return
}

if (-not $PSCmdlet.ShouldProcess("MVP4 progress demo $DemoSuffix", 'create API demo data')) { return }

& $editorialScript -ApiBaseUrl $ApiBaseUrl -AdminEmail $AdminEmail -AdminPassword $AdminPassword -DemoSuffix $DemoSuffix
$editorial = Get-Content -Raw (Join-Path $PSScriptRoot '.last-editorial-admin-demo-data.json') | ConvertFrom-Json
$seriesId = Assert-PositiveId $editorial.seriesId 'seriesId'
$item1Id = Assert-PositiveId $editorial.item1Id 'item1Id'
$item2Id = Assert-PositiveId $editorial.item2Id 'item2Id'
$item3Id = Assert-PositiveId $editorial.item3Id 'item3Id'
$editionId = Assert-PositiveId $editorial.editionId 'editionId'

$collectorEmail = "demo.progress.$DemoSuffix@collectohub.local"
$collector = Invoke-ProgressApi -Method POST -Path '/api/auth/register' -Body @{ email = $collectorEmail; password = $CollectorPassword; displayName = "Demo Progress Collector $DemoSuffix"; preferredInterfaceLanguage = 'es' }
$login = Invoke-ProgressApi -Method POST -Path '/api/auth/login' -Body @{ email = $collectorEmail; password = $CollectorPassword }
$token = $login.accessToken
$collection = Invoke-ProgressApi -Method POST -Path '/api/collections' -Token $token -Body @{ name = "Progreso manga $DemoSuffix"; description = 'Demo de OWNED, WANTED y MISSING calculado.'; visibility = 'PUBLIC'; categoryCode = 'MANGA_COMIC' }
$owned = Invoke-ProgressApi -Method POST -Path "/api/collections/$($collection.id)/items" -Token $token -Body @{ masterProductId = $null; catalogItemId = $item1Id; catalogItemEditionId = $editionId; collectionStatus = 'OWNED'; physicalCondition = 'GOOD'; notes = 'Owned demo item' }
$wanted = Invoke-ProgressApi -Method POST -Path "/api/collections/$($collection.id)/items" -Token $token -Body @{ masterProductId = $null; catalogItemId = $item2Id; catalogItemEditionId = $null; collectionStatus = 'WANTED'; notes = 'Wanted demo item' }
$progress = Invoke-ProgressApi -Method GET -Path "/api/collections/$($collection.id)/series/$seriesId/progress" -Token $token

if ($progress.totalCatalogItems -ne 3 -or $progress.ownedItems -ne 1 -or $progress.wantedItems -ne 1 -or $progress.missingItems -ne 1 -or $progress.completionPercentage -ne 33) { throw 'Progress validation failed: expected 3 total, 1 OWNED, 1 WANTED, 1 MISSING and 33%.' }
$byId = @{}; foreach ($entry in $progress.items) { $byId[[int64]$entry.catalogItemId] = $entry }
if ($byId[$item1Id].calculatedStatus -ne 'OWNED' -or $byId[$item2Id].calculatedStatus -ne 'WANTED' -or $byId[$item3Id].calculatedStatus -ne 'MISSING') { throw 'Progress validation failed: calculated statuses do not match the three editorial items.' }
if (-not ($byId[$item1Id].ownedCollectionItemIds -contains $owned.id) -or -not ($byId[$item2Id].wantedCollectionItemIds -contains $wanted.id) -or $byId[$item3Id].ownedCollectionItemIds.Count -ne 0 -or $byId[$item3Id].wantedCollectionItemIds.Count -ne 0) { throw 'Progress validation failed: persisted entry identifiers do not match the calculated progress.' }

$urls = [ordered]@{ collectionDetail = "$FrontendBaseUrl/collections/$($collection.id)"; seriesProgress = "$FrontendBaseUrl/collections/$($collection.id)/series/$seriesId/progress" }
$summary = [ordered]@{ generatedAt = (Get-Date).ToString('o'); demoSuffix = $DemoSuffix; apiBaseUrl = $ApiBaseUrl; frontendBaseUrl = $FrontendBaseUrl; collectorId = $collector.id; collectorEmail = $collectorEmail; collectionId = $collection.id; seriesId = $seriesId; item1Id = $item1Id; item2Id = $item2Id; item3Id = $item3Id; ownedCollectionItemId = $owned.id; wantedCollectionItemId = $wanted.id; progressSummary = @{ totalCatalogItems = $progress.totalCatalogItems; ownedItems = $progress.ownedItems; wantedItems = $progress.wantedItems; missingItems = $progress.missingItems; completionPercentage = $progress.completionPercentage }; urls = $urls }
$summary | ConvertTo-Json -Depth 10 | Set-Content -Path $summaryPath -Encoding UTF8
Write-Host "MVP4 progress demo created and validated (1 OWNED, 1 WANTED, 1 calculated MISSING, 33%)."
Write-Host "Collection: $($urls.collectionDetail)"
Write-Host "Series progress: $($urls.seriesProgress)"
Write-Host "Saved local summary: $summaryPath (no passwords or tokens)."
