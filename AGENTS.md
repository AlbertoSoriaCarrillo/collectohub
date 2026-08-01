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
- One execution may implement at most one EPIC or complete one pending delivery pull request.
- End after reporting the current EPIC or pending pull request. Do not begin the next EPIC in the same execution.

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

- the initial worktree is dirty or the starting branch diverges from the effective integration branch;
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

## Branch model and activation

The normative branch model is defined in `docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`.

- `main` is the stable production branch. It accepts only manual, explicitly authorized release promotions from `pre` after the model is active. Releases must use **Create a merge commit**; squash and rebase are forbidden, and `main` must not require linear history.
- `pre` is the preproduction branch. It accepts only manual promotions from `dev` after human functional validation. Promotions must use **Create a merge commit**; squash and rebase are forbidden, and `pre` must not require linear history. Do not develop features directly on `pre`.
- `dev` is the continuous integration branch. After activation, temporary `codex/<epic>` and `quality/<epic>` branches start from `dev` and target `dev` using squash and merge. `dev` may require linear history.
- Direct pushes are forbidden on `main`, `pre`, and `dev`.
- Automatic merge is forbidden on `main` and `pre`. A `codex/*` or `quality/*` pull request into `dev` may use squash and merge automatically only after all seven required checks pass, self-review is recorded, and the current head equals the recorded `expected_head_sha`.

The existence of `origin/dev` and `origin/pre` is necessary but not sufficient for activation. The model remains `DOCUMENTED_NOT_ACTIVE`, and `main` remains the effective integration branch, until all three branch protections and merge methods are configured, tested, evidenced, and explicitly declared active. Do not infer activation from branch existence or documentation alone.

## Sequential delivery gate

Before beginning any EPIC, determine the effective integration branch from the activation rule above and query GitHub for every open pull request targeting it whose head branch starts with `codex/` or `quality/`. Any matching pull request blocks a new EPIC, including a draft or one with green, pending, red, or absent checks.

When a matching pull request exists, do not create a branch or execute another EPIC. The execution may only validate, review, report, or complete that pending pull request under the merge rules above. If it cannot be completed safely, stop with:

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

Scheduled automation is strictly sequential: one EPIC cannot begin until the prior delivery pull request has been reviewed and merged or closed. After that pull request is merged or closed, the next execution must return to the effective integration branch, run `git fetch origin`, require a clean worktree, update only by fast-forward, verify local `HEAD` equals the corresponding `origin/*` ref, and only then determine the next EPIC.

## Git and delivery

1. Start from a clean, fetched, up-to-date effective integration branch: `main` during transition and `dev` after activation.
2. Create one branch per EPIC named `codex/<epic>` or `quality/<epic>` as appropriate.
3. Keep one logical commit per EPIC and never push directly to `main`, `pre`, or `dev`.
4. Open a pull request to the effective integration branch and wait for all seven required checks.
5. For a pull request into `dev`, recheck self-review and `expected_head_sha` immediately before automatic squash and merge. Do not merge while a check is red, pending, or absent.
6. Promote `dev` to `pre` only through a manual pull request whose exact head is `dev` and exact base is `pre`, with human functional validation, all seven checks successful, and no new functional changes. Use **Create a merge commit**; never squash or rebase. After merge, require `git merge-base --is-ancestor origin/dev origin/pre` to succeed.
7. Promote `pre` to `main` only through a manually authorized release pull request whose exact head is `pre` and exact base is `main`, with all seven checks successful. Use **Create a merge commit**; never squash or rebase. After merge, require `git merge-base --is-ancestor origin/pre origin/main` to succeed and prepare a tag when appropriate.
8. Do not rewrite protected branches, force-push, or use destructive Git commands.
9. After merge, verify the expected SHA, required ancestry, and a clean worktree before another task begins. Delete a temporary branch only after its merge is confirmed. Do not merge `pre` or `main` back into `dev` after a normal ancestry-preserving promotion; reverse synchronization is only for an exceptional correction present in `main` but absent from `dev`.

## Required execution loop

For every future Codex EPIC: determine whether the model is active; query GitHub and do not start a new EPIC if an open `codex/` or `quality/` delivery pull request targets the effective integration branch; otherwise return to that branch, fetch, require a clean tree, fast-forward to its `origin/*` ref, and verify the SHAs match; create `codex/<epic>` or `quality/<epic>`; implement one EPIC; run the applicable test matrix and `scripts/quality/verify.ps1`; do not commit on failure; commit and push only the temporary branch after local success; open a pull request; wait for all seven checks; apply the merge rules for the target branch; report the SHA, PR, checks, and evidence; then stop.
