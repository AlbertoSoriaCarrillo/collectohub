# GitHub branch protection guide

Date: 2026-08-01
Repository: `AlbertoSoriaCarrillo/collectohub`
Status: remote configuration pending; this document does not change GitHub settings.

The branch roles and activation rule are normative in
`docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`. Do not apply the active-state rules to
`dev` or `pre` until both remote branches exist.

## Shared required controls

Pull requests targeting `dev`, `pre`, or `main` must run these seven checks:

1. `Validate repository structure`
2. `Backend build and tests`
3. `Frontend build and tests`
4. `quality-policy`
5. `backend-verify`
6. `frontend-verify`
7. `powershell-parse`

Configure each protected branch to require its branch to be up to date, block
force pushes and deletion, require resolved review conversations, and apply the
rules to administrators or prevent routine bypass. Do not permit direct pushes.

Do not apply a shared linear-history rule: `dev` may require linear history,
but `pre` and `main` must permit merge commits so promotions preserve Git
ancestry.

CI reports failures; protection prevents a red, pending, or missing result from
being integrated. Do not reduce validation or rename a check merely to satisfy
a ruleset.

## Ruleset for `dev`

After activation, create an active ruleset for `dev` that:

1. Requires pull requests from `codex/*` or `quality/*` for delivery work.
2. Requires all seven exact checks above.
3. Allows squash and merge automation only after a recorded self-review.
4. Requires the automation to compare the current PR head with its recorded
   `expected_head_sha` immediately before merge.
5. Does not require human approval for each EPIC.
6. Blocks merge when any required check is red, pending, or absent, when review
   finds a blocker, or when the expected SHA no longer matches.
7. May require linear history and uses squash and merge for deliveries from
   `codex/*` or `quality/*`.

## Ruleset for `pre`

After activation, create an active ruleset for `pre` that:

1. Accepts promotions only through a pull request whose exact head is `dev` and
   exact base is `pre`.
2. Requires all seven exact checks and human functional validation.
3. Requires manual merge and forbids automatic merge.
4. Rejects new functional changes added inside a promotion pull request.
5. Does not require linear history and permits merge commits.
6. Requires **Create a merge commit**; forbids Squash and merge and Rebase and
   merge.
7. After merge, requires
   `git merge-base --is-ancestor origin/dev origin/pre` to succeed.

## Ruleset for `main`

Keep `main` stable and production-ready. Configure an active ruleset that:

1. Requires a pull request before merging and rejects direct updates.
2. Requires all seven exact checks.
3. Once the model is active, accepts only a release pull request whose exact
   head is `pre` and exact base is `main`.
4. Requires explicit human authorization and manual merge.
5. Forbids automatic merge.
6. Does not require linear history and permits merge commits.
7. Requires **Create a merge commit**; forbids Squash and merge and Rebase and
   merge.
8. After merge, requires
   `git merge-base --is-ancestor origin/pre origin/main` to succeed.
9. Does not require an approval that the sole repository owner cannot obtain;
   explicit owner authorization after green checks and resolved conversations
   is sufficient.
10. Includes tag preparation in the release procedure when appropriate.

During `DOCUMENTED_NOT_ACTIVE`, `main` remains the integration branch and the
existing sequential delivery process continues even though `origin/dev` and
`origin/pre` exist. Branch existence alone does not activate the model.

## Repository merge methods

Configure GitHub to allow:

- Squash merging for temporary deliveries into `dev`;
- Merge commits for promotions `dev` -> `pre` and releases `pre` -> `main`.

Rebase merging is not part of the agreed flow. Do not apply a repository or
ruleset requirement for linear history to `pre` or `main`.

## Sequential automation preflight

Before starting any EPIC, determine the effective integration branch using the
activation rule and query GitHub for open pull requests targeting that branch
from `codex/*` or `quality/*`.

Any match blocks a new EPIC, including draft pull requests and pull requests
with green, pending, red, or absent checks. The execution may only validate,
review, report, or safely complete that pending pull request. One execution may
implement at most one EPIC or complete one pending pull request.

After the earlier pull request is merged or closed, use this recovery sequence
against the effective integration branch before selecting work:

```powershell
git switch <integration-branch>
git fetch origin
git status --short
git merge --ff-only origin/<integration-branch>
git rev-parse HEAD
git rev-parse origin/<integration-branch>
```

Continue only when the worktree is clean and both SHAs are identical.

## Activation procedure

Bootstrap is verified at
`b669a3fc2a9cd64346f400bbbfaf583cc184ab46`: `origin/main`, `origin/dev`, and
`origin/pre` resolve to the same commit and `origin/main` is an ancestor of both
permanent downstream branches.

To activate the model:

1. Apply and test the three rulesets above.
2. Configure and test the repository merge methods.
3. Confirm pull requests to `dev`, `pre`, and `main` expose all seven checks.
4. Demonstrate that `dev` accepts the guarded squash flow while `pre` and
   `main` require manual merge commits.
5. Record ruleset and merge-method evidence.
6. Declare `dev` the effective integration branch only after every step passes.

Until then, keep `DOCUMENTED_NOT_ACTIVE` and do not enable the scheduled
automation for `dev`.

## Protection test

Use disposable documentation-only pull requests where needed. Confirm:

- direct push to each protected branch is rejected;
- all seven exact checks appear and complete for each target branch;
- merge is unavailable while any required check is pending or red;
- an outdated branch must be updated;
- an unresolved conversation blocks merge;
- only `dev` delivery PRs can use the guarded automatic squash flow;
- `pre` and `main` always require manual human action and Create a merge commit;
- squash and rebase are unavailable for permanent-branch promotions;
- successful promotions satisfy their required `merge-base --is-ancestor`
  checks;
- force push and deletion remain blocked.

Record the test PR URL, commit SHA, date, check results, target branch, and
screenshots or settings export as administrative evidence. Delete only a
disposable or temporary branch after merge or closure and only through a normal
GitHub operation; do not rewrite history.

## Post-merge verification

Fetch `origin`, require a clean tree, update only by fast-forward, confirm local
`HEAD` equals the expected target ref, and query GitHub again before another
EPIC. A missing or altered required check blocks further delivery until the
ruleset is corrected.

An ancestry-preserving promotion does not require merging `pre` or `main` back
into `dev`. Reverse synchronization is reserved for an exceptional correction
present in `main` but absent from `dev`.
