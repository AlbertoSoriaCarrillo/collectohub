# CollectoHub quality gates

Date established: 2026-07-31
Last operational update: 2026-08-26
Scope: permanent repository policy, local verification, CI, pull requests, and auditable evidence.

An EPIC is not complete merely because code exists. Closure requires the checks applicable to its actual diff, real evidence, a focused pull request, and green required checks. An unexecuted requirement is `SKIPPED_WITH_REASON`, `NOT_RUN`, or `BLOCKED`; it is never `PASS`.

The documented target delivery mode is `AUTONOMOUS_DEV_AUTO_MERGE_GUARDED`.
`dev` is the effective integration branch. GitHub does not enforce branch
protection or rulesets for this private repository on the current plan, so the
guard remains procedural and is not `PROTECTED_ACTIVE`. Scheduled execution is
still `PAUSED` pending the prerequisites in
`docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`.

## Current tooling audit

The repository currently has Java 25, Maven Wrapper, Spring Boot tests, PostgreSQL Testcontainers, Angular 21, Vitest through the Angular unit-test builder, Prettier, and the pre-existing `.github/workflows/ci.yml`. Testcontainers tests use `disabledWithoutDocker = true`, so Docker-dependent evidence must report a skip honestly when Docker is unavailable.

There is no configured JaCoCo threshold, frontend coverage gate, ESLint, Java static-analysis plugin, dependency scanner, or secret scanner. Prettier exists but no repository-wide format gate was previously defined. The lockfile exposes optional Vitest coverage peers but the project does not declare or configure a coverage provider. E2E and Playwright remain postponed. The latest recorded npm installation reported 23 vulnerabilities; the previous 16 are historical evidence. This policy does not reclassify or repair them and forbids `npm audit fix` and `npm audit fix --force`.

## Universal evidence rules

- Record the real base SHA, branch, date, changed paths, commands, results, warnings, and omissions.
- Review `git diff --check`, conflicts, deleted tests, new ignored-test markers, skip flags, manifests, lockfiles, migrations, secrets, and scope.
- Never weaken a check or assertion to obtain green output.
- A required check that cannot run blocks commit. An intentionally inapplicable check must have a precise reason.
- Local success permits pushing only a temporary `codex/*` or `quality/*` branch.
- A delivery pull request targets `dev` and requires all seven remote checks in
  `SUCCESS`, final self-review, exact `expected_head_sha`, ten minutes since
  ready-for-review, and a fresh re-query of every guarded condition.
- The automation may squash-merge only `codex/* -> dev` or `quality/* -> dev`
  under the complete guarded contract. It never uses GitHub native auto-merge,
  never pushes directly to a permanent branch, and never merges promotions.
- Use `docs/templates/EPIC_QUALITY_EVIDENCE.md` for durable EPIC evidence.

## Quality matrix

### Documentation only

Validate link and path coherence, current facts, exact commands and versions, `git diff --check`, and scope. Maven and npm may be `NOT_RUN: documentation only` only when no code, configuration, script, API contract, executable command, manifest, or lockfile changes. Historical output must not be presented as current.

### Backend domain and service

Test the successful case; null, empty, and boundary inputs; invariants; errors; incompatible states; precedence; idempotence; concurrency when applicable; and a regression case for every fix. Run `mvnw clean verify`. Tests must assert observable behavior and important side effects, not only constructors or line execution.

### API, authorization, and security

Validate DTO shape and sanitization, request validation, success codes, and the applicable `400`, `401`, `403`, `404`, and `409` cases. Exercise authentication, roles, ownership, privacy, non-inferability of private resources, and both service and MVC/integration layers. `ADMIN` does not substitute for ownership where the contract forbids it. Run `mvnw clean verify`.

### Persistence

Use PostgreSQL/Testcontainers whenever engine behavior matters. Validate filters, stable ordering and tie-breaks, soft delete, constraints, joins, archived-row exclusion, transaction boundaries, representative volume, and N+1 behavior. If Docker is required and unavailable, the EPIC is blocked rather than passed.

### Liquibase

Validate an empty database and upgrade from the prior schema, checksums, constraints, indexes, data preservation, and rollback documentation where applicable using PostgreSQL/Testcontainers. Do not make destructive migration changes without explicit authorization.

### Frontend HTTP

Test method, URL, repeated query parameters, body, types, errors, and absence of private fields. Run the HTTP tests, complete frontend suite, and production build.

### Frontend visual behavior

Cover render, loading, error, retry, empty and filtered-empty states; owner, reader, and visitor; navigation and direct reload; forms; query parameters; concurrent and out-of-order responses; deletion and canonical reload; i18n; keyboard, labels, roles, focus; responsive layout; and privacy. Run the complete suite and build.

### PowerShell

Parse every `.ps1`. Changed scripts must use strict mode and fail visibly. Scripts that write must support `ShouldProcess` where appropriate; `-WhatIf` must have no effects. Validate idempotence, resumption, secret-free output, safe failure, and absence of destructive operations.

### Dependencies

Manifest or lockfile changes require explicit authorization and justification, compatibility review, full suites, and a vulnerability review. Do not combine dependency maintenance with product behavior. Do not use automatic vulnerability fixes. A new critical risk without an accepted mitigation blocks delivery.

### Security and privacy

Define the actor matrix and test positive and negative access, least privilege, DTO/search/order/error-message leakage, escalation resistance, logs, and a regression case. Ownership and privacy must be demonstrated, not inferred from roles.

### Functional QA

For a visible EPIC, cover owner, reader, visitor, initial/loading/success/error/retry/empty states, multiple records, legacy data, update and canonical reload, navigation and direct reload, permissions, privacy, responsive behavior, and basic accessibility. E2E/Playwright remain outside the current scope. A manual journey not executed is not `PASS`.

## Local verifier

From the repository root:

```powershell
.\scripts\quality\verify.ps1 -BaseRef origin/dev
```

Options:

- `-SkipBackend` or `-SkipFrontend` may be used only when the diff does not affect that layer and the EPIC explicitly permits the omission.
- `-DocumentationOnly` is accepted only for a true Markdown-only diff and records Maven/npm as not run.

The verifier checks the diff and scope indicators, parses PowerShell, runs the applicable suites, and writes ignored local evidence to `scripts/quality/.last-quality-verification.json`. The JSON contains only repository paths, commands, counts, statuses, and warnings; it must not contain credentials or tokens.

## GitHub checks and delivery

The complete required check set is:

```text
Validate repository structure
Backend build and tests
Frontend build and tests
quality-policy
backend-verify
frontend-verify
powershell-parse
```

The workflows detect structural, backend, frontend, policy, parser, and verification failures. GitHub does not technically prevent merge on this private repository under the current plan. Therefore, a red, pending, absent, or stale result blocks human authorization procedurally. The reviewer must confirm all seven checks against the current head SHA immediately before merge.

A temporary delivery pull request into `dev` uses **Squash and merge** only after:

- exact base `dev`;
- head equal to the recorded `expected_head_sha`;
- expected single-EPIC diff;
- seven checks in `SUCCESS`;
- self-review without blocking findings;
- zero unresolved conversations;
- zero `CHANGES_REQUESTED` reviews;
- at least ten minutes since ready-for-review followed by a fresh GitHub query;
- no post-review changes.

The full 31-condition decision gate is normative in
`docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`. If every condition passes, guarded
automation may squash-merge the temporary branch, must not delete it
automatically, must re-synchronize `dev`, and reports `EPIC_MERGED_TO_DEV`. If
any condition fails it does not merge and reports the applicable blocking
state. A process-policy PR that explicitly requires human review finishes with
`HUMAN_MERGE_REQUIRED`.

Promotions `dev -> pre` and `pre -> main` are outside ordinary EPIC delivery and follow `docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`.

## Sequential scheduled automation

Scheduled Codex delivery is sequential. Before any EPIC begins, the automation must query GitHub for open pull requests targeting `dev` whose head branch starts with `codex/` or `quality/`. A matching pull request blocks the next execution even when it is a draft or its checks are green. Pending, red, or absent checks also block.

While a matching pull request exists, the automation must not create another branch, modify files for a new EPIC, execute another EPIC, commit, or push. It may inspect and, when explicitly safe, repair only that same pending delivery. Otherwise it must report:

```text
EPIC EN ESPERA DE REVISION
PR pendiente:
Rama:
Checks:
Acción necesaria:
Cambios: NO
Commit: NO
Push: NO
```

Only after the prior delivery pull request is reviewed and merged or closed may a later execution return to `dev`, fetch `origin`, require a clean worktree, fast-forward `dev`, verify `HEAD == origin/dev`, and determine the next EPIC. Closing without merge does not authorize reusing unreviewed branch contents; the next task must be determined from the resulting `dev` and GitHub state.

## Current Codex automation contract

While scheduled execution is `PAUSED` and root `AGENTS.md` retains the
supervised policy, this is the active fail-closed contract. The guarded merge
steps above are a future activation target, not current authority.

Before starting an EPIC, Codex must:

1. Query GitHub for open `codex/*` or `quality/*` pull requests with base `dev`.
2. Stop new work when one exists.
3. Return to `dev`, fetch, require a clean worktree, update only by fast-forward, and verify `HEAD == origin/dev`.
4. Determine exactly one documented EPIC.
5. Create one temporary branch from the updated `dev`.
6. Run the applicable tests and `scripts/quality/verify.ps1 -BaseRef origin/dev`.
7. Push only the temporary branch and open a pull request with base `dev`.
8. Wait for the seven required checks, perform self-review, and record the current head as `expected_head_sha`.
9. Wait at least ten minutes after ready-for-review and re-query head, base, diff,
   checks, reviews and conversations.
10. Report `HUMAN_MERGE_REQUIRED` and stop without merging or starting another
    EPIC.

Only after root policy is explicitly adapted, branch ancestry is repaired, and
an atomic base-SHA guard is available may a separate supervised activation test
replace step 10 with the guarded squash-merge outcome described above.

## QUALITY-B definition (not implemented)

`EPIC QUALITY-B - Coverage, static analysis, and dependency security` will establish baselines from measured results before choosing thresholds. It should add JaCoCo, compatible Vitest/Angular coverage, a no-coverage-regression policy, Java static analysis, frontend lint, secret scanning, and dependency assessment with an explicit baseline for existing vulnerabilities. It must review the current 23-vulnerability measurement and retain the previous 16 as historical evidence, prohibit automatic fixes, and introduce gradual thresholds. It must not invent percentages or impose an arbitrary 100% target.

QUALITY-B remains defined but not implemented. It does not displace the active
MVP5 sequence; the single next documented product task is EPIC 45C-E.
