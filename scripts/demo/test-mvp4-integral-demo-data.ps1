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

Write-Host "PASS: parser, Windows PowerShell 5.1 default SummaryPath, deterministic WhatIf, no-effects summary checks, explicit SummaryPath, safe unavailable-backend failure, idempotence primitives and destructive-command policy."
