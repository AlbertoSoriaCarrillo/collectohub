[CmdletBinding()]
param()

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$target = Join-Path $PSScriptRoot "create-mvp4-integral-demo-data.ps1"
$errors = $null
[void][System.Management.Automation.Language.Parser]::ParseFile($target, [ref]$null, [ref]$errors)
if ($errors.Count -gt 0) {
    throw "Parser validation failed for create-mvp4-integral-demo-data.ps1: $($errors -join '; ')"
}

$windowsPowerShell = Join-Path $env:SystemRoot "System32\WindowsPowerShell\v1.0\powershell.exe"
if (-not (Test-Path -LiteralPath $windowsPowerShell)) {
    throw "Windows PowerShell 5.1 executable was not found."
}
$windowsPowerShellMajor = (& $windowsPowerShell -NoProfile -Command '$PSVersionTable.PSVersion.Major' | Out-String).Trim()
if ($windowsPowerShellMajor -ne "5") {
    throw "Expected Windows PowerShell 5.x but found major version '$windowsPowerShellMajor'."
}

$tokens = $null
$functionErrors = $null
$targetAst = [System.Management.Automation.Language.Parser]::ParseFile(
    $target,
    [ref]$tokens,
    [ref]$functionErrors
)
if ($functionErrors.Count -gt 0) {
    throw "Function isolation parse failed: $($functionErrors -join '; ')"
}

$functionSources = foreach ($functionName in @("Get-ResponseItems", "Select-UniqueMatch")) {
    $definition = $targetAst.Find({
        param($node)
        $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and
            $node.Name -ceq $functionName
    }, $true)
    if ($null -eq $definition) {
        throw "Required function '$functionName' was not found for isolated regression testing."
    }
    $definition.Extent.Text
}

$responseItemsRegression = @'
Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

function Assert-EmptyResponseItems {
    param(
        [Parameter(Mandatory = $true)]
        [AllowNull()]
        [AllowEmptyCollection()]
        [object[]]$Items,
        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    if ($null -eq $Items) {
        throw "$Label returned null instead of an empty collection."
    }
    if ($Items.Count -ne 0) {
        throw "$Label returned $($Items.Count) items instead of zero."
    }
    $match = Select-UniqueMatch -Items $Items -Label $Label -Predicate { param($item) $true }
    if ($null -ne $match) {
        throw "$Label unexpectedly returned a match."
    }
}

$nullItems = Get-ResponseItems -Response $null
Assert-EmptyResponseItems -Items $nullItems -Label "null response"

$nullContentItems = Get-ResponseItems -Response ([pscustomobject]@{ content = $null })
Assert-EmptyResponseItems -Items $nullContentItems -Label "null content"

$emptyContentItems = Get-ResponseItems -Response ([pscustomobject]@{ content = @() })
Assert-EmptyResponseItems -Items $emptyContentItems -Label "empty content"

$defensiveNullMatch = Select-UniqueMatch -Items $null -Label "defensive null" -Predicate { param($item) $true }
if ($null -ne $defensiveNullMatch) {
    throw "Defensive null input unexpectedly returned a match."
}

$single = [pscustomobject]@{ id = 1; name = "target" }
$singleItems = Get-ResponseItems -Response ([pscustomobject]@{ content = @($single) })
if ($null -eq $singleItems -or $singleItems.Count -ne 1) {
    throw "Single-item page did not remain a one-item collection."
}
$singleMatch = Select-UniqueMatch -Items $singleItems -Label "single match" -Predicate {
    param($item) $item.name -ceq "target"
}
if ($null -eq $singleMatch -or $singleMatch.id -ne 1) {
    throw "Single exact match was not returned."
}

$twoItems = Get-ResponseItems -Response ([pscustomobject]@{ content = @(
    [pscustomobject]@{ id = 2; name = "other" },
    [pscustomobject]@{ id = 3; name = "target" }
) })
$uniqueMatch = Select-UniqueMatch -Items $twoItems -Label "one of two" -Predicate {
    param($item) $item.name -ceq "target"
}
if ($null -eq $uniqueMatch -or $uniqueMatch.id -ne 3) {
    throw "The unique exact match among two items was not returned."
}

$ambiguousItems = Get-ResponseItems -Response ([pscustomobject]@{ content = @(
    [pscustomobject]@{ id = 4; name = "target" },
    [pscustomobject]@{ id = 5; name = "target" }
) })
$ambiguityPreserved = $false
try {
    Select-UniqueMatch -Items $ambiguousItems -Label "ambiguous match" -Predicate {
        param($item) $item.name -ceq "target"
    } | Out-Null
}
catch {
    if ($_.Exception.Message -notmatch "ambiguous: found 2 exact resources") {
        throw
    }
    $ambiguityPreserved = $true
}
if (-not $ambiguityPreserved) {
    throw "Two exact matches did not preserve the ambiguity error."
}

$nonPaged = [pscustomobject]@{ id = 6; name = "standalone" }
$nonPagedItems = Get-ResponseItems -Response $nonPaged
if ($null -eq $nonPagedItems -or $nonPagedItems.Count -ne 1 -or $nonPagedItems[0].id -ne 6) {
    throw "A non-paged object was not interpreted as one item."
}

Write-Output "PASS: isolated empty-response collection contract."
'@

$isolatedSource = (($functionSources -join [Environment]::NewLine) +
    [Environment]::NewLine + $responseItemsRegression)
$encodedIsolation = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($isolatedSource))
$isolatedOutput = (& $windowsPowerShell -NoProfile -ExecutionPolicy Bypass `
    -EncodedCommand $encodedIsolation 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Windows PowerShell 5.1 isolated response regression failed: $isolatedOutput"
}
if ($isolatedOutput -notmatch "PASS: isolated empty-response collection contract") {
    throw "Windows PowerShell 5.1 isolated response regression did not report PASS."
}

$defaultSummary = Join-Path $PSScriptRoot ".last-mvp4-integral-demo-data.json"
$defaultSummaryExisted = Test-Path -LiteralPath $defaultSummary
$defaultSummaryBefore = if ($defaultSummaryExisted) {
    $item = Get-Item -LiteralPath $defaultSummary
    @{
        Hash = (Get-FileHash -LiteralPath $defaultSummary -Algorithm SHA256).Hash
        Length = $item.Length
        LastWriteTimeUtc = $item.LastWriteTimeUtc
    }
}

$defaultPlan = (& $windowsPowerShell -NoProfile -ExecutionPolicy Bypass -File $target `
    -Scenario "summary-default-regression" -WhatIf 2>&1 | Out-String).Trim()
$defaultExitCode = $LASTEXITCODE
if ($defaultExitCode -ne 0) {
    throw "Windows PowerShell 5.1 default SummaryPath invocation failed with exit code $defaultExitCode`: $defaultPlan"
}
if ($defaultPlan -notmatch [regex]::Escape($defaultSummary)) {
    throw "WhatIf output does not contain the expected default summary path '$defaultSummary'."
}
if ($defaultPlan -notmatch "no HTTP, psql, or file operations") {
    throw "Default SummaryPath WhatIf does not state the no-effects contract."
}
if ($defaultSummaryExisted) {
    $item = Get-Item -LiteralPath $defaultSummary
    $defaultSummaryAfter = @{
        Hash = (Get-FileHash -LiteralPath $defaultSummary -Algorithm SHA256).Hash
        Length = $item.Length
        LastWriteTimeUtc = $item.LastWriteTimeUtc
    }
    if ($defaultSummaryBefore.Hash -ne $defaultSummaryAfter.Hash -or
        $defaultSummaryBefore.Length -ne $defaultSummaryAfter.Length -or
        $defaultSummaryBefore.LastWriteTimeUtc -ne $defaultSummaryAfter.LastWriteTimeUtc) {
        throw "Default SummaryPath WhatIf modified the existing summary file."
    }
}
elseif (Test-Path -LiteralPath $defaultSummary) {
    throw "Default SummaryPath WhatIf created a summary file."
}

$offlineSummary = Join-Path ([IO.Path]::GetTempPath()) "collectohub-mvp4-whatif-$([guid]::NewGuid().ToString('N')).json"
$arguments = @{
    ApiBaseUrl = "http://127.0.0.1:1"
    FrontendBaseUrl = "http://127.0.0.1:2"
    Scenario = "offline-test"
    SummaryPath = $offlineSummary
    WhatIf = $true
}

$firstPlan = (& $target @arguments *>&1 | Out-String).Trim()
$secondPlan = (& $target @arguments *>&1 | Out-String).Trim()

if (Test-Path -LiteralPath $offlineSummary) {
    throw "WhatIf created a summary file."
}
if ($firstPlan -cne $secondPlan) {
    throw "Repeated WhatIf runs produced different plans."
}
if ($firstPlan -notmatch "Scenario key: mvp4-offline-test") {
    throw "WhatIf output does not expose the deterministic scenario key."
}
if ($firstPlan -notmatch "no HTTP, psql, or file operations") {
    throw "WhatIf output does not state the no-effects contract."
}

$failureSummary = Join-Path ([IO.Path]::GetTempPath()) "collectohub-mvp4-failure-$([guid]::NewGuid().ToString('N')).json"
$failedSafely = $false
try {
    & $target -ApiBaseUrl "http://127.0.0.1:1" -Scenario "offline-failure" -SummaryPath $failureSummary -Confirm:$false
}
catch {
    if ($_.Exception.Message -notmatch "Phase API failed") {
        throw "Unavailable-backend failure did not identify the API phase: $($_.Exception.Message)"
    }
    $failedSafely = $true
}
if (-not $failedSafely) {
    throw "Unavailable backend did not produce a non-success result."
}
if (Test-Path -LiteralPath $failureSummary) {
    throw "Unavailable-backend failure created a summary file."
}

$source = Get-Content -Raw -LiteralPath $target
foreach ($required in @("SupportsShouldProcess", "Set-StrictMode", "Select-UniqueMatch", "Assert-Compatible", "Write-AtomicSummary")) {
    if ($source -notmatch [regex]::Escape($required)) {
        throw "Required safety/idempotence primitive '$required' is missing."
    }
}
foreach ($forbidden in @("docker compose down -v", "git clean", "TRUNCATE ", "DROP TABLE", "npm audit fix")) {
    if ($source -match [regex]::Escape($forbidden)) {
        throw "Forbidden destructive or automatic-fix command '$forbidden' was found."
    }
}

Write-Host "PASS: parser, Windows PowerShell 5.1 empty-response contract, default SummaryPath, deterministic WhatIf, no-effects summary checks, explicit SummaryPath, safe unavailable-backend failure, idempotence primitives and destructive-command policy."
