[CmdletBinding()]
param(
    [string]$ApiBaseUrl = 'http://localhost:8080',
    [Parameter(Mandatory = $true)][string]$EditorialEmail,
    [Parameter(Mandatory = $true)][securestring]$EditorialPassword,
    [string]$RegularUserEmail,
    [securestring]$RegularUserPassword,
    [string]$AdminEmail,
    [securestring]$AdminPassword,
    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbName = 'collectohub',
    [string]$DbUser = 'collectohub',
    [switch]$Cleanup
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ApiBaseUrl = $ApiBaseUrl.TrimEnd('/')
$manager = Join-Path $PSScriptRoot 'manage-editorial-admin.ps1'

function Convert-SecurePassword {
    param([Parameter(Mandatory = $true)][securestring]$Value)
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
}

function Invoke-Api {
    param([string]$Method, [string]$Path, [object]$Body, [string]$Token, [int]$ExpectedStatus = 200)
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{ Method = $Method; Uri = "$ApiBaseUrl$Path"; Headers = $headers; ErrorAction = 'Stop' }
    if ($null -ne $Body) { $parameters.ContentType = 'application/json'; $parameters.Body = $Body | ConvertTo-Json -Compress }
    try {
        $response = Invoke-RestMethod @parameters
        if ($ExpectedStatus -ne 200) { throw "Expected HTTP $ExpectedStatus but request succeeded." }
        return $response
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        if ($status -ne $ExpectedStatus) { throw "Expected HTTP $ExpectedStatus for $Method $Path, received $status." }
        return $null
    }
}

function Get-Status {
    return & $manager -Email $EditorialEmail -Action Status -DbHost $DbHost -DbPort $DbPort -DbName $DbName -DbUser $DbUser -AsJson | ConvertFrom-Json
}

$initial = Get-Status
if (-not $initial.UserFound -or -not $initial.UserActive) { throw 'Editorial user must exist and be active.' }
$addedByValidation = $false
if (-not $initial.EditorialAdminAssigned) {
    & $manager -Email $EditorialEmail -Action Grant -DbHost $DbHost -DbPort $DbPort -DbName $DbName -DbUser $DbUser -Force | Out-Null
    $addedByValidation = $true
}

$editorialPlain = Convert-SecurePassword -Value $EditorialPassword
try { $editorialLogin = Invoke-Api -Method 'POST' -Path '/api/auth/login' -Body @{ email = $EditorialEmail; password = $editorialPlain } } finally { $editorialPlain = $null }
if (-not ($editorialLogin.roles -contains 'EDITORIAL_ADMIN')) { throw 'Renewed editorial login does not contain EDITORIAL_ADMIN.' }
$me = Invoke-Api -Method 'GET' -Path '/api/users/me' -Token $editorialLogin.accessToken
if (-not ($me.roles -contains 'EDITORIAL_ADMIN')) { throw '/api/users/me does not contain EDITORIAL_ADMIN after renewed login.' }
Invoke-Api -Method 'GET' -Path '/api/catalog/admin/data-quality/report?scope=CREATORS&limit=1' -Token $editorialLogin.accessToken | Out-Null
Write-Host 'EDITORIAL_ADMIN data-quality access: HTTP 200.'

if ($RegularUserEmail -and $RegularUserPassword) {
    $regularPlain = Convert-SecurePassword -Value $RegularUserPassword
    try { $regularLogin = Invoke-Api -Method 'POST' -Path '/api/auth/login' -Body @{ email = $RegularUserEmail; password = $regularPlain } } finally { $regularPlain = $null }
    Invoke-Api -Method 'GET' -Path '/api/catalog/admin/data-quality/report?scope=CREATORS&limit=1' -Token $regularLogin.accessToken -ExpectedStatus 403 | Out-Null
    Write-Host 'Regular USER data-quality access: HTTP 403.'
}

if ($AdminEmail -and $AdminPassword) {
    $adminPlain = Convert-SecurePassword -Value $AdminPassword
    try { $adminLogin = Invoke-Api -Method 'POST' -Path '/api/auth/login' -Body @{ email = $AdminEmail; password = $adminPlain } } finally { $adminPlain = $null }
    Invoke-Api -Method 'GET' -Path '/api/catalog/admin/data-quality/report?scope=CREATORS&limit=1' -Token $adminLogin.accessToken | Out-Null
    Write-Host 'ADMIN data-quality access: HTTP 200.'
}

if ($Cleanup -and $addedByValidation) {
    & $manager -Email $EditorialEmail -Action Revoke -DbHost $DbHost -DbPort $DbPort -DbName $DbName -DbUser $DbUser -Force | Out-Null
    Write-Host 'Cleanup: EDITORIAL_ADMIN revoked because this validation granted it.'
}

Write-Host 'Validation completed. Existing tokens can retain prior UI roles; sign in again after a role change.'
