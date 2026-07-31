# CollectoHub agent policy

This policy applies to every automated agent working in this repository. A task-specific instruction may make the policy stricter, but it must not silently weaken it.

## Working principles

- Make the smallest change that closes one EPIC and preserve the existing architecture and responsibilities.
- Avoid duplication where a clear existing abstraction applies, but do not introduce premature abstractions.
- Do not perform opportunistic refactors or hide errors.
- Do not introduce secrets, tokens, credentials, personal data, or production-like identities.
- Preserve privacy, ownership, authorization, legacy compatibility, and documented contracts.
- Do not add or update dependencies unless the EPIC explicitly authorizes it.
- Never reduce validation or security merely to make a check pass.
- Keep one EPIC in one logical commit. Do not implement multiple EPICs together.
- End after reporting the current EPIC. Do not begin the next EPIC in the same execution.

## Behavior-oriented tests

Every feature or fix must include tests that demonstrate observable behavior and the regression being prevented, not merely execute lines. When practical, establish that the new test fails before the production change and passes afterwards. Cover positive, negative, boundary, authorization, privacy, and compatibility behavior that applies to the change.

## Prohibited validation shortcuts

Do not:

- delete tests to make a build pass;
- weaken assertions without an explicit, reviewed justification;
- ignore or quarantine a failing test;
- introduce `@Disabled`, `@Ignore`, `.skip`, `xit`, `xdescribe`, `test.todo`, or `it.todo`;
- use `-DskipTests`, `maven.test.skip`, `--passWithNoTests`, or an equivalent bypass;
- catch a test or build failure and convert it into success;
- update snapshots without reviewing the behavioral change;
- present historical counts or results as current evidence;
- report `PASS` for a required validation that was not executed.

A permitted omission must be recorded as `SKIPPED_WITH_REASON`, never as `PASS`.

## Universal blockers

Do not commit or push when a validation applicable at that stage detects any of the following:

- the initial worktree is dirty or the starting branch diverges from `origin/main`;
- another task is concurrently changing the repository;
- a merge conflict or contradictory documentation exists;
- a required test or build fails or cannot run;
- a required migration cannot be validated;
- privacy, ownership, or authorization is not demonstrated;
- a change is outside the EPIC scope;
- a manifest, lockfile, or dependency changes without authorization;
- a test is deleted, ignored, or weakened;
- a secret is detected;
- critical QA is not demonstrated;
- a remote check required for closure is red, pending, or absent;
- an acceptance criterion cannot be demonstrated.

Use this report and stop:

```text
EPIC BLOQUEADA
Motivo:
Validacion fallida o no ejecutada:
Cambios realizados:
Commit: NO
Push: NO
```

An initial branch push that is necessary to start remote checks is allowed only after all required local checks pass. Pending or absent remote checks still block merge and final closure.

## Sequential delivery gate

Before beginning any EPIC, query GitHub for every open pull request targeting `main` whose head branch starts with `codex/` or `quality/`. Any matching pull request blocks a new EPIC, including a draft or one with green, pending, red, or absent checks.

When a matching pull request exists, do not create a branch, modify files, execute another EPIC, commit, or push. Stop with:

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

Scheduled automation is strictly sequential: one EPIC cannot begin until the prior delivery pull request has been reviewed and merged or closed. After that pull request is merged or closed, the next execution must return to `main`, run `git fetch origin`, require a clean worktree, update `main` only by fast-forward, verify `HEAD == origin/main`, and only then determine the next EPIC.

## Git and delivery

1. Start from a clean, fetched, up-to-date `main`.
2. Create one branch per EPIC named `codex/<epic>` unless the EPIC specifies an approved exception.
3. Keep one logical commit per EPIC and never push directly to `main`.
4. Open a pull request and wait for all required checks.
5. Do not merge while a check is red, pending, or absent.
6. Do not rewrite `main`, force-push, or use destructive Git commands.
7. After merge, verify the expected SHA and a clean worktree before another task begins.

## Required execution loop

For every future Codex EPIC: first query GitHub and stop if an open `codex/` or `quality/` delivery pull request targets `main`; otherwise return to `main`, fetch, require a clean tree, fast-forward to `origin/main`, and verify the SHAs match; create `codex/<epic>`; implement one EPIC; run the applicable test matrix and `scripts/quality/verify.ps1`; do not commit on failure; commit and push only the EPIC branch after local success; open a pull request; wait for checks; do not merge red, pending, or missing checks; report the SHA, PR, checks, and evidence; then stop.
