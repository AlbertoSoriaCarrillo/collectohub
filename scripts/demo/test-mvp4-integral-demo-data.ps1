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

Write-Host "PASS: parser, deterministic WhatIf, no-effects summary check, safe unavailable-backend failure, idempotence primitives and destructive-command policy."
