[CmdletBinding()]
param(
    [string]$BaseRef = 'origin/main',
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$DocumentationOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ($PSVersionTable.PSVersion -lt [version]'5.1') {
    throw 'PowerShell 5.1 or newer is required.'
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$summaryPath = Join-Path $PSScriptRoot '.last-quality-verification.json'
$failure = $null

$summary = [ordered]@{
    SchemaVersion = 1
    Timestamp = (Get-Date).ToString('o')
    BaseRef = $BaseRef
    Branch = 'unknown'
    Head = 'unknown'
    ChangedPaths = @()
    DocumentationOnly = [bool]$DocumentationOnly
    Result = 'BLOCKED'
    Policy = [ordered]@{
        DiffCheck = 'NOT_RUN'
        Conflicts = 'NOT_RUN'
        DeletedTests = 'unknown'
        NewIgnoredTestsOrSkipFlags = 'unknown'
        PowerShellParser = 'NOT_RUN'
    }
    Backend = [ordered]@{
        Status = 'NOT_RUN'
        Command = '.\mvnw.cmd clean verify'
        Tests = 'unknown'
        Failures = 'unknown'
        Errors = 'unknown'
        Skipped = 'unknown'
        Docker = 'unknown'
    }
    Frontend = [ordered]@{
        Status = 'NOT_RUN'
        InstallCommand = 'npm.cmd ci'
        TestCommand = 'npm.cmd test -- --watch=false'
        BuildCommand = 'npm.cmd run build'
        TestFiles = 'unknown'
        Tests = 'unknown'
        Vulnerabilities = 'unknown'
        Warnings = @()
    }
    Failure = $null
}

function Invoke-GitText {
    param([string[]]$Arguments)

    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = @(& git -c core.safecrlf=false @Arguments 2>&1)
        $exitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    if ($exitCode -ne 0) {
        throw "git $($Arguments -join ' ') failed: $($output -join [Environment]::NewLine)"
    }
    return @($output | ForEach-Object { $_.ToString() })
}

function Invoke-LoggedCommand {
    param(
        [string]$Executable,
        [string[]]$Arguments,
        [string]$WorkingDirectory
    )

    Push-Location $WorkingDirectory
    try {
        $previousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        $lines = @(& $Executable @Arguments 2>&1 | ForEach-Object {
            $line = [regex]::Replace($_.ToString(), '\x1B\[[0-?]*[ -/]*[@-~]', '')
            Write-Host $line
            $line
        })
        $exitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
        Pop-Location
    }

    if ($exitCode -ne 0) {
        throw "$Executable $($Arguments -join ' ') failed with exit code $exitCode."
    }
    return $lines
}

function Test-PolicyScanPath {
    param([string]$Path)

    $normalized = $Path.Replace('\', '/')
    if ($normalized -like 'docs/*' -or
        $normalized -eq 'AGENTS.md' -or
        $normalized -eq '.github/pull_request_template.md' -or
        $normalized -eq '.github/workflows/quality-gates.yml' -or
        $normalized -like 'scripts/quality/*') {
        return $false
    }

    return $normalized -match '(?i)(\.java|\.kt|\.ts|\.tsx|\.js|\.jsx|\.ps1|\.xml|\.json|\.ya?ml)$'
}

function Get-BackendCounts {
    $files = @(Get-ChildItem (Join-Path $repoRoot 'backend\target\surefire-reports') -Filter 'TEST-*.xml' -File -ErrorAction SilentlyContinue)
    if ($files.Count -eq 0) {
        return $null
    }

    $counts = [ordered]@{ Tests = 0; Failures = 0; Errors = 0; Skipped = 0 }
    foreach ($file in $files) {
        [xml]$report = Get-Content -Raw -LiteralPath $file.FullName
        $suite = $report.testsuite
        $counts.Tests += [int]$suite.tests
        $counts.Failures += [int]$suite.failures
        $counts.Errors += [int]$suite.errors
        $counts.Skipped += [int]$suite.skipped
    }
    return $counts
}

function Get-LastRegexValue {
    param(
        [string[]]$Lines,
        [string]$Pattern
    )

    $value = $null
    foreach ($line in $Lines) {
        if ($line -match $Pattern) {
            $value = $Matches[1]
        }
    }
    return $value
}

Push-Location $repoRoot
try {
    try {
        if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
            throw 'git was not found in PATH.'
        }

        $null = Invoke-GitText @('rev-parse', '--verify', $BaseRef)
        $summary.Branch = (Invoke-GitText @('branch', '--show-current') | Select-Object -First 1)
        $summary.Head = (Invoke-GitText @('rev-parse', 'HEAD') | Select-Object -First 1)
        $trackedChanges = @(Invoke-GitText @('diff', '--name-only', $BaseRef, '--') | Where-Object { $_ })
        $untrackedChanges = @(Invoke-GitText @('ls-files', '--others', '--exclude-standard') | Where-Object { $_ })
        $changedPaths = @($trackedChanges + $untrackedChanges | Sort-Object -Unique)
        $summary.ChangedPaths = $changedPaths

        $backendChanged = @($changedPaths | Where-Object { $_.Replace('\', '/') -like 'backend/*' }).Count -gt 0
        $frontendChanged = @($changedPaths | Where-Object { $_.Replace('\', '/') -like 'frontend/*' }).Count -gt 0
        $migrationChanged = @($changedPaths | Where-Object { $_.Replace('\', '/') -like 'backend/src/main/resources/db/*' }).Count -gt 0
        $nonDocumentation = @($changedPaths | Where-Object { $_.Replace('\', '/') -notmatch '(?i)(^docs/.*\.md$|^README\.md$)' })

        if ($DocumentationOnly -and $nonDocumentation.Count -gt 0) {
            throw "-DocumentationOnly is invalid because non-documentation paths changed: $($nonDocumentation -join ', ')"
        }
        if ($SkipBackend -and $backendChanged) {
            throw '-SkipBackend is forbidden because the diff changes backend/.'
        }
        if ($SkipFrontend -and $frontendChanged) {
            throw '-SkipFrontend is forbidden because the diff changes frontend/.'
        }

        $previousErrorActionPreference = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            $diffCheckOutput = @(& git -c core.safecrlf=false diff --check $BaseRef -- 2>&1)
            $diffCheckExitCode = $LASTEXITCODE
        }
        finally {
            $ErrorActionPreference = $previousErrorActionPreference
        }
        if ($diffCheckExitCode -ne 0) {
            throw "git diff --check failed: $($diffCheckOutput -join [Environment]::NewLine)"
        }
        $untrackedWhitespaceFailures = @()
        foreach ($path in $untrackedChanges) {
            if ($path -notmatch '(?i)\.(md|ps1|yml|yaml|json|xml|java|ts|tsx|js|jsx|txt)$') {
                continue
            }
            $absolutePath = Join-Path $repoRoot $path
            $raw = [System.IO.File]::ReadAllText($absolutePath)
            $lineNumber = 0
            foreach ($line in ($raw -split '\r?\n')) {
                $lineNumber++
                if ($line -match '[ \t]+$') {
                    $untrackedWhitespaceFailures += "$path`:$lineNumber`: trailing whitespace"
                }
            }
            if ($raw.Length -gt 0 -and -not $raw.EndsWith("`n")) {
                $untrackedWhitespaceFailures += "$path`: missing final newline"
            }
        }
        if ($untrackedWhitespaceFailures.Count -gt 0) {
            throw "Untracked-file whitespace failures: $($untrackedWhitespaceFailures -join ' | ')"
        }
        $summary.Policy.DiffCheck = 'PASS'

        $conflicts = @(Invoke-GitText @('diff', '--name-only', '--diff-filter=U') | Where-Object { $_ })
        if ($conflicts.Count -gt 0) {
            throw "Unresolved conflicts detected: $($conflicts -join ', ')"
        }
        $summary.Policy.Conflicts = 'PASS'

        $deleted = @(Invoke-GitText @('diff', '--name-status', '--diff-filter=D', $BaseRef, '--'))
        $deletedTests = @($deleted | ForEach-Object {
            $parts = $_ -split "`t", 2
            if ($parts.Count -eq 2 -and $parts[1] -match '(?i)(Test\.java|IT\.java|\.spec\.ts)$') { $parts[1] }
        } | Where-Object { $_ })
        if ($deletedTests.Count -gt 0) {
            throw "Deleted tests are forbidden: $($deletedTests -join ', ')"
        }
        $summary.Policy.DeletedTests = 0

        $forbiddenPattern = '@Disabled|@Ignore|\.skip\s*\(|\b(?:xit|xdescribe)\s*\(|\b(?:test|it)\.todo\s*\(|-DskipTests\b|maven\.test\.skip|--passWithNoTests\b'
        $diffLines = @(Invoke-GitText @('diff', '--unified=0', $BaseRef, '--'))
        $currentPath = ''
        $violations = @()
        foreach ($line in $diffLines) {
            if ($line -match '^diff --git a/(.+) b/(.+)$') {
                $currentPath = $Matches[2]
                continue
            }
            if ($line -match '^\+(?!\+\+)' -and (Test-PolicyScanPath $currentPath)) {
                $added = $line.Substring(1)
                if ($added -match $forbiddenPattern) {
                    $violations += "$currentPath`: $added"
                }
            }
        }
        foreach ($path in $untrackedChanges) {
            if (-not (Test-PolicyScanPath $path)) {
                continue
            }
            foreach ($line in [System.IO.File]::ReadAllLines((Join-Path $repoRoot $path))) {
                if ($line -match $forbiddenPattern) {
                    $violations += "$path`: $line"
                }
            }
        }
        if ($violations.Count -gt 0) {
            throw "New ignored-test markers or skip flags are forbidden: $($violations -join ' | ')"
        }
        $summary.Policy.NewIgnoredTestsOrSkipFlags = 0

        $parseFailures = @()
        Get-ChildItem (Join-Path $repoRoot 'scripts') -Recurse -Filter '*.ps1' -File | ForEach-Object {
            $tokens = $null
            $errors = $null
            [System.Management.Automation.Language.Parser]::ParseFile($_.FullName, [ref]$tokens, [ref]$errors) | Out-Null
            if ($errors.Count -gt 0) {
                $parseFailures += "$($_.FullName): $($errors.Message -join '; ')"
            }
        }
        if ($parseFailures.Count -gt 0) {
            throw "PowerShell parser failures: $($parseFailures -join ' | ')"
        }
        $summary.Policy.PowerShellParser = 'PASS'

        $dockerAvailable = $false
        if (Get-Command docker -ErrorAction SilentlyContinue) {
            $previousErrorActionPreference = $ErrorActionPreference
            try {
                $ErrorActionPreference = 'Continue'
                & docker info *> $null
                $dockerAvailable = $LASTEXITCODE -eq 0
            }
            finally {
                $ErrorActionPreference = $previousErrorActionPreference
            }
        }
        if ($dockerAvailable) { $summary.Backend.Docker = 'available' } else { $summary.Backend.Docker = 'unavailable' }
        if ($migrationChanged -and -not $dockerAvailable) {
            throw 'A migration changed but Docker/Testcontainers is unavailable.'
        }

        if ($DocumentationOnly) {
            $summary.Backend.Status = 'NOT_RUN: documentation only'
            $summary.Frontend.Status = 'NOT_RUN: documentation only'
        }
        else {
            if ($SkipBackend) {
                $summary.Backend.Status = 'SKIPPED_WITH_REASON: explicitly skipped; backend unchanged'
            }
            else {
                Write-Host '=== Backend: Maven clean verify ==='
                $null = Invoke-LoggedCommand -Executable '.\mvnw.cmd' -Arguments @('clean', 'verify') -WorkingDirectory (Join-Path $repoRoot 'backend')
                $counts = Get-BackendCounts
                if ($null -ne $counts) {
                    $summary.Backend.Tests = $counts.Tests
                    $summary.Backend.Failures = $counts.Failures
                    $summary.Backend.Errors = $counts.Errors
                    $summary.Backend.Skipped = $counts.Skipped
                }
                $summary.Backend.Status = 'PASS'
            }

            if ($SkipFrontend) {
                $summary.Frontend.Status = 'SKIPPED_WITH_REASON: explicitly skipped; frontend unchanged'
            }
            else {
                if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) {
                    throw 'npm.cmd was not found in PATH.'
                }
                Write-Host '=== Frontend: npm ci ==='
                $installOutput = Invoke-LoggedCommand -Executable 'npm.cmd' -Arguments @('ci') -WorkingDirectory (Join-Path $repoRoot 'frontend')
                $vulnerabilities = Get-LastRegexValue -Lines $installOutput -Pattern '(?i)(\d+)\s+vulnerabilit'
                if ($null -ne $vulnerabilities) { $summary.Frontend.Vulnerabilities = [int]$vulnerabilities }

                Write-Host '=== Frontend: unit tests ==='
                $testOutput = Invoke-LoggedCommand -Executable 'npm.cmd' -Arguments @('test', '--', '--watch=false') -WorkingDirectory (Join-Path $repoRoot 'frontend')
                $testFiles = Get-LastRegexValue -Lines $testOutput -Pattern '(?i)Test Files\s+(\d+)\s+passed'
                $tests = Get-LastRegexValue -Lines $testOutput -Pattern '(?i)^\s*Tests\s+(\d+)\s+passed'
                if ($null -ne $testFiles) { $summary.Frontend.TestFiles = [int]$testFiles }
                if ($null -ne $tests) { $summary.Frontend.Tests = [int]$tests }

                Write-Host '=== Frontend: production build ==='
                $buildOutput = Invoke-LoggedCommand -Executable 'npm.cmd' -Arguments @('run', 'build') -WorkingDirectory (Join-Path $repoRoot 'frontend')
                $summary.Frontend.Warnings = @($buildOutput | Where-Object { $_ -match '(?i)warning|budget|exceeded' } | Select-Object -Unique)
                $summary.Frontend.Status = 'PASS'
            }
        }

        $summary.Result = 'PASS'
    }
    catch {
        $failure = $_.Exception.Message
        $summary.Result = 'FAIL'
        $summary.Failure = $failure
    }
    finally {
        $json = $summary | ConvertTo-Json -Depth 8
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($summaryPath, $json, $utf8NoBom)
    }
}
finally {
    Pop-Location
}

if ($null -ne $failure) {
    Write-Host "QUALITY VERIFICATION FAILED: $failure" -ForegroundColor Red
    exit 1
}

Write-Host "QUALITY VERIFICATION PASS. Summary: $summaryPath" -ForegroundColor Green
