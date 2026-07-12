[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$Email,

    [Parameter(Mandatory = $true)]
    [ValidateSet('Status', 'Grant', 'Revoke')]
    [string]$Action,

    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbName = 'collectohub',
    [string]$DbUser = 'collectohub',
    [switch]$WhatIf,
    [switch]$Force,
    [switch]$AsJson
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-PsqlQuery {
    param([Parameter(Mandatory = $true)][string]$Sql)

    $output = & psql --no-psqlrc -X -v ON_ERROR_STOP=1 -h $DbHost -p $DbPort -U $DbUser -d $DbName `
        -At -F "`t" -v "email=$Email" -c $Sql 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw 'PostgreSQL operation failed. Verify psql, connectivity, permissions and PGPASSWORD or local authentication.'
    }
    return @($output | Where-Object { $_ -is [string] -and $_.Trim() -ne '' })
}

function Get-EditorialAdminStatus {
    $roleExists = (Invoke-PsqlQuery -Sql "SELECT EXISTS (SELECT 1 FROM roles WHERE code = 'EDITORIAL_ADMIN' AND deleted_at IS NULL);")[0] -eq 't'
    $rows = Invoke-PsqlQuery -Sql @'
SELECT
    u.id,
    u.email,
    u.status,
    (u.status = 'ACTIVE' AND u.deleted_at IS NULL) AS active,
    EXISTS (
        SELECT 1 FROM user_roles ur
        JOIN roles r ON r.id = ur.role_id
        WHERE ur.user_id = u.id AND r.code = 'EDITORIAL_ADMIN' AND r.deleted_at IS NULL
    ) AS editorial_admin_assigned,
    COALESCE(string_agg(r.code, ',' ORDER BY r.code), '') AS roles
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id AND r.deleted_at IS NULL
WHERE lower(u.email) = lower(:'email')
GROUP BY u.id, u.email, u.status, u.deleted_at;
'@

    if ($rows.Count -eq 0) {
        return [pscustomobject]@{
            UserFound = $false; UserActive = $false; RoleExists = $roleExists
            EditorialAdminAssigned = $false; CurrentRoles = @(); Email = $Email
        }
    }

    $parts = $rows[0].Split("`t", 6)
    return [pscustomobject]@{
        UserFound = $true
        UserActive = ($parts[3] -eq 't')
        RoleExists = $roleExists
        EditorialAdminAssigned = ($parts[4] -eq 't')
        CurrentRoles = @($parts[5].Split(',', [System.StringSplitOptions]::RemoveEmptyEntries))
        Email = $parts[1]
    }
}

function Write-Result {
    param([Parameter(Mandatory = $true)]$Status, [bool]$Changed = $false)

    $result = [ordered]@{
        Email = $Status.Email; Action = $Action; UserFound = $Status.UserFound
        UserActive = $Status.UserActive; RoleExists = $Status.RoleExists
        AlreadyAssigned = $Status.EditorialAdminAssigned; Changed = $Changed
        CurrentRoles = @($Status.CurrentRoles)
    }
    if ($AsJson) { $result | ConvertTo-Json -Compress; return }
    $result.GetEnumerator() | ForEach-Object {
        $value = if ($_.Value -is [array]) { $_.Value -join ', ' } else { $_.Value }
        Write-Host "$($_.Key): $value"
    }
}

if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    throw 'psql was not found in PATH. Install PostgreSQL client tools or add psql to PATH.'
}
if ([string]::IsNullOrWhiteSpace($Email)) { throw 'Email cannot be empty.' }
$Email = $Email.Trim()

$before = Get-EditorialAdminStatus
if ($Action -eq 'Status') { Write-Result -Status $before; return }
if (-not $before.UserFound) { throw 'User was not found.' }
if (-not $before.UserActive) { throw 'User is inactive or soft-deleted; no role change was made.' }
if (-not $before.RoleExists) { throw 'EDITORIAL_ADMIN is not configured; apply Liquibase migrations first.' }

$alreadyInTargetState = if ($Action -eq 'Grant') { $before.EditorialAdminAssigned } else { -not $before.EditorialAdminAssigned }
if ($alreadyInTargetState) {
    Write-Result -Status $before
    return
}

if ($WhatIf) {
    Write-Host "WhatIf: would $($Action.ToLowerInvariant()) EDITORIAL_ADMIN for $($before.Email)."
    Write-Result -Status $before
    return
}
if (-not $Force -and -not $PSCmdlet.ShouldContinue("$Action EDITORIAL_ADMIN for $($before.Email)?", 'Controlled role operation')) {
    Write-Host 'Cancelled. No database changes were made.'
    return
}

$sql = if ($Action -eq 'Grant') {
@'
BEGIN;
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'EDITORIAL_ADMIN' AND r.deleted_at IS NULL
WHERE lower(u.email) = lower(:'email')
  AND u.status = 'ACTIVE'
  AND u.deleted_at IS NULL
ON CONFLICT (user_id, role_id) DO NOTHING;
COMMIT;
'@
} else {
@'
BEGIN;
DELETE FROM user_roles ur
USING users u, roles r
WHERE ur.user_id = u.id
  AND ur.role_id = r.id
  AND lower(u.email) = lower(:'email')
  AND u.status = 'ACTIVE'
  AND u.deleted_at IS NULL
  AND r.code = 'EDITORIAL_ADMIN';
COMMIT;
'@
}

Invoke-PsqlQuery -Sql $sql | Out-Null
$after = Get-EditorialAdminStatus
Write-Result -Status $after -Changed $true
