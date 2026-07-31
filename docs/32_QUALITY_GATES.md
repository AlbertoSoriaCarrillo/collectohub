# CollectoHub quality gates

Date established: 2026-07-31
Scope: permanent repository policy, local verification, CI, pull requests, and auditable evidence.

An EPIC is not complete merely because code exists. Closure requires the checks applicable to its actual diff, real evidence, a focused pull request, and green required checks. An unexecuted requirement is `SKIPPED_WITH_REASON`, `NOT_RUN`, or `BLOCKED`; it is never `PASS`.

## Current tooling audit

The repository currently has Java 25, Maven Wrapper, Spring Boot tests, PostgreSQL Testcontainers, Angular 21, Vitest through the Angular unit-test builder, Prettier, and the pre-existing `.github/workflows/ci.yml`. Testcontainers tests use `disabledWithoutDocker = true`, so Docker-dependent evidence must report a skip honestly when Docker is unavailable.

There is no configured JaCoCo threshold, frontend coverage gate, ESLint, Java static-analysis plugin, dependency scanner, or secret scanner. Prettier exists but no repository-wide format gate was previously defined. The lockfile exposes optional Vitest coverage peers but the project does not declare or configure a coverage provider. E2E and Playwright remain postponed. The latest recorded npm installation reported 16 historical vulnerabilities; QUALITY-A does not reclassify or repair them and forbids `npm audit fix` and `npm audit fix --force`.

## Universal evidence rules

- Record the real base SHA, branch, date, changed paths, commands, results, warnings, and omissions.
- Review `git diff --check`, conflicts, deleted tests, new ignored-test markers, skip flags, manifests, lockfiles, migrations, secrets, and scope.
- Never weaken a check or assertion to obtain green output.
- A required check that cannot run blocks commit. An intentionally inapplicable check must have a precise reason.
- Local success permits a branch push; only green remote checks and branch protection permit merge.
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
.\scripts\quality\verify.ps1 -BaseRef origin/main
```

Options:

- `-SkipBackend` or `-SkipFrontend` may be used only when the diff does not affect that layer and the EPIC explicitly permits the omission.
- `-DocumentationOnly` is accepted only for a true Markdown-only diff and records Maven/npm as not run.

The verifier checks the diff and scope indicators, parses PowerShell, runs the applicable suites, and writes ignored local evidence to `scripts/quality/.last-quality-verification.json`. The JSON contains only repository paths, commands, counts, statuses, and warnings; it must not contain credentials or tokens.

## GitHub checks and delivery

`.github/workflows/quality-gates.yml` exposes stable required jobs:

```text
quality-policy
backend-verify
frontend-verify
powershell-parse
```

The workflow detects violations. The ruleset described in `docs/33_GITHUB_MAIN_PROTECTION.md` prevents integrating red, pending, or missing checks into `main`.

## Future Codex automation

Replace any instruction that pushes directly to `origin/main` with this exact block:

```text
NUEVO_PROMPT_PARA_AUTOMATIZACION
Antes de cada EPIC, actualiza las referencias remotas y exige main limpia, sin divergencia y alineada con origin/main. Crea una rama codex/<epic> y ejecuta una sola EPIC. Ejecuta las pruebas aplicables y .\scripts\quality\verify.ps1 -BaseRef origin/main. Si falla cualquier validacion, no hagas commit ni push y entrega EPIC BLOQUEADA con evidencia real. Solo con validacion local PASS, crea un unico commit logico y publica exclusivamente la rama de la EPIC; nunca hagas push directo a origin/main. Abre una pull request hacia main, espera quality-policy, backend-verify, frontend-verify y powershell-parse, y no fusiones si un check esta rojo, pendiente o ausente. Informa SHA, URL de PR, resultado de cada check, evidencia, riesgos y siguiente tarea; despues termina sin empezar otra EPIC.
```

## QUALITY-B definition (not implemented)

`EPIC QUALITY-B - Coverage, static analysis, and dependency security` will establish baselines from measured results before choosing thresholds. It should add JaCoCo, compatible Vitest/Angular coverage, a no-coverage-regression policy, Java static analysis, frontend lint, secret scanning, and dependency assessment with an explicit baseline for existing vulnerabilities. It must review the 16 historically recorded npm vulnerabilities under controlled updates, prohibit automatic fixes, and introduce gradual thresholds. It must not invent percentages or impose an arbitrary 100% target.

After QUALITY-A, the next functional task remains EPIC 44H-B unless a blocking quality or security risk is demonstrated.
