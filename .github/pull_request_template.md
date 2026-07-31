# EPIC

## Scope and acceptance criteria

- [ ] This pull request contains one EPIC only.
- [ ] The intended scope and excluded scope are stated.
- [ ] Every acceptance criterion has current evidence.
- [ ] No unrelated refactor or product behavior is included.

## Validation matrix

- [ ] New behavior and regression tests are included where applicable.
- [ ] Positive, negative, boundary, error, and concurrency cases are covered where applicable.
- [ ] Backend unit/service/MVC/integration tests are complete or marked `SKIPPED_WITH_REASON`.
- [ ] Frontend HTTP/component/routing tests are complete or marked `SKIPPED_WITH_REASON`.
- [ ] Maven `clean verify` passed when required.
- [ ] Frontend `npm ci`, non-watch tests, and production build passed when required.
- [ ] PostgreSQL/Testcontainers validation passed when the database engine matters.
- [ ] Liquibase empty-base and upgrade paths were validated when migrations changed.
- [ ] Every changed PowerShell script parses; `-WhatIf`, idempotence, resume, and safe failure were checked where applicable.
- [ ] Security roles, ownership, privacy, and negative access cases were checked.
- [ ] Functional QA covers owner, reader, visitor, loading, success, error, retry, empty, navigation, and reload where applicable.
- [ ] Basic keyboard, label, role, focus, and responsive behavior was checked for visible UI.

## Change safety

- [ ] Dependencies or lockfiles did not change, or explicit authorization and justification are attached.
- [ ] Tests deleted: 0.
- [ ] Tests newly ignored or weakened: 0.
- [ ] No skip flags, secrets, personal data, destructive operations, E2E, or Playwright were introduced outside scope.
- [ ] `git diff --check` and `scripts/quality/verify.ps1` passed.
- [ ] The EPIC evidence document or attached report contains real commands, counts, warnings, and omissions.

## Evidence and risks

Base commit:

Validation evidence:

Security/privacy evidence:

Known risks or `SKIPPED_WITH_REASON`:

## Merge declaration

- [ ] I will not merge while any required check is red, pending, or absent.
