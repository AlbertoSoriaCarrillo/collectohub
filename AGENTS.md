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

- The target operating state is `SUPERVISED_ACTIVE_NO_ENFORCEMENT`. In that
  state `dev` is the effective integration branch, but GitHub does not enforce
  branch protection or rulesets. Every guarantee below is a Codex procedure
  plus human review, never a claim of technical branch protection.
- Until the commit that defines this policy is present simultaneously in
  `origin/main`, `origin/dev`, and `origin/pre`, the state is
  `SUPERVISED_ACTIVATION_PENDING_ALIGNMENT`, `main` remains the effective
  integration branch, and scheduled automation remains `PAUSED`.
- Full activation also requires the policy pull request to be merged manually
  into `main`, `dev` and `pre` to be fast-forwarded to that integrated commit,
  the automation to be adapted without merge permission, and the first run to
  be performed under human supervision.
- `main` is the stable production branch. It accepts only manual, explicitly
  authorized release promotions from `pre`. Releases use **Create a merge
  commit**; squash and rebase are forbidden.
- `pre` is the preproduction branch. It accepts only manual promotions from
  `dev` after human functional validation. Promotions use **Create a merge
  commit**; squash and rebase are forbidden. Do not develop features directly
  on `pre`.
- After activation, temporary `codex/<epic>` and `quality/<epic>` branches
  start from an updated `origin/dev` and target `dev`. Their delivery method is
  manual **Squash and merge** after the seven checks, self-review, and
  `expected_head_sha` verification all pass.
- Direct pushes to `main`, `pre`, and `dev` are forbidden by policy even though
  GitHub cannot technically block them on the current plan.
- Automatic merge is forbidden on every branch. Every pull request merge
  requires human intervention.

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
5. For a pull request into `dev`, record a self-review and `expected_head_sha`. A person must recheck the current head, exact `dev` base, expected diff, seven successful checks, unresolved conversations, and absence of post-review changes before manual **Squash and merge**. If `dev`, the head, or the diff changes, repeat the review. End the automation with `HUMAN_MERGE_REQUIRED`; it must never merge the pull request.
6. Promote `dev` to `pre` only through a manual pull request whose exact head is `dev` and exact base is `pre`, with human functional validation, all seven checks successful, and no new functional changes. Do not merge `pre` back into `dev`. Immediately before **Create a merge commit**, recheck the current `dev` and `pre` SHAs, unchanged PR head, unchanged PR base since final review, expected diff, and seven successful checks. If `dev`, `pre`, or the PR changed after validation, stop and repeat the applicable review. Never squash or rebase. After merge, require `git merge-base --is-ancestor origin/dev origin/pre` to succeed.
7. Promote `pre` to `main` only through a manually authorized release pull request whose exact head is `pre` and exact base is `main`, with all seven checks successful. Do not merge `main` back into `pre`. Immediately before **Create a merge commit**, recheck the current `pre` and `main` SHAs, unchanged PR head, unchanged PR base since authorization, expected diff, and seven successful checks. If `pre`, `main`, or the PR changed after authorization, stop and request new authorization. Never squash or rebase. After merge, require `git merge-base --is-ancestor origin/pre origin/main` to succeed and prepare a tag when appropriate.
8. Do not rewrite permanent branches, force-push, or use destructive Git commands.
9. After merge, verify the expected SHA, required ancestry, and a clean worktree before another task begins. Delete a temporary branch only after its merge is confirmed. Do not merge `pre` or `main` back into `dev` after a normal ancestry-preserving promotion; reverse synchronization is only for an exceptional correction present in `main` but absent from `dev`.

## Required execution loop

For every future Codex EPIC: determine the current activation state; query GitHub and do not start a new EPIC if an open `codex/` or `quality/` delivery pull request targets the effective integration branch; otherwise return to that branch, fetch, require a clean tree, fast-forward to its `origin/*` ref, and verify the SHAs match; create `codex/<epic>` or `quality/<epic>`; implement one EPIC; run the applicable test matrix and `scripts/quality/verify.ps1`; do not commit on failure; commit and push only the temporary branch after local success; open a pull request; wait for all seven checks; record self-review and `expected_head_sha`; report `HUMAN_MERGE_REQUIRED` with the SHA, PR, individual checks, and evidence; then stop without merging or starting another EPIC.
