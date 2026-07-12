# Controlled Editorial Admin Provisioning

## Objective

These scripts are the initial controlled operational mechanism for assigning
`EDITORIAL_ADMIN` to an existing CollectoHub user. They do not expose an API or
user interface, create users, create roles, change passwords, or alter any role
other than the one requested.

## Requirements

- PowerShell 5.1+ (PowerShell 7 is supported).
- PostgreSQL client `psql` available in `PATH`.
- Liquibase migration 012 already applied.
- An existing active, non-deleted user.
- `PGPASSWORD` set for password authentication, or another secure local `psql`
  authentication mechanism. No password is stored by these scripts.

```powershell
$env:PGPASSWORD = '<local-password>'
```

## Provisioning

Run commands from the repository root. Defaults match local development
(`localhost:5432`, database and user `collectohub`). The email match is
case-insensitive.

```powershell
.\scripts\admin\manage-editorial-admin.ps1 -Email 'editor@collectohub.local' -Action Status
.\scripts\admin\manage-editorial-admin.ps1 -Email 'editor@collectohub.local' -Action Grant -WhatIf
.\scripts\admin\manage-editorial-admin.ps1 -Email 'editor@collectohub.local' -Action Grant
.\scripts\admin\manage-editorial-admin.ps1 -Email 'editor@collectohub.local' -Action Revoke
```

`Grant` and `Revoke` ask for confirmation unless `-Force` is supplied for a
controlled automation. Both run in a transaction and are idempotent. `Grant`
inserts only the `EDITORIAL_ADMIN` relation; `Revoke` deletes only that
relation. Inactive or soft-deleted users are rejected before any write.

## Integral validation

Use secure prompts rather than passing passwords in command history:

```powershell
$editorPassword = Read-Host 'Editorial password' -AsSecureString
$userPassword = Read-Host 'Regular user password' -AsSecureString
.\scripts\admin\validate-editorial-admin-access.ps1 `
  -EditorialEmail 'editor@collectohub.local' `
  -EditorialPassword $editorPassword `
  -RegularUserEmail 'user@collectohub.local' `
  -RegularUserPassword $userPassword `
  -Cleanup
```

The validation grants the role only when missing, renews the editorial session,
checks `/api/users/me`, expects HTTP 200 from the read-only data-quality report,
and expects HTTP 403 for an optional regular user. Optional ADMIN credentials
also verify the superuser remains authorized. `-Cleanup` revokes only a role
added by that same validation run.

## Session renewal, safety and rollback

Access tokens and the frontend session can retain older role snapshots. The
affected user must sign out and sign in again after Grant or Revoke. No global
token invalidation is performed.

Do not run these scripts against production without an approved operational
procedure. Errors from `psql`, missing users, inactive users and absent roles
stop with a non-zero exit code. To reverse a successful grant, run `Revoke` for
the same email. A visual role-management interface remains a future, separate
EPIC.
