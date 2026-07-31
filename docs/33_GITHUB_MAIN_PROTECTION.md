# GitHub main protection guide

Date: 2026-07-31
Repository: `AlbertoSoriaCarrillo/collectohub`
Status: remote configuration pending; this document does not change GitHub settings.

## Required ruleset

Create a branch ruleset targeting the default branch `main` and set it to active. Configure it to:

1. Restrict direct updates so changes enter through pull requests.
2. Require a pull request before merging.
3. Require these exact status checks:
   - `quality-policy`
   - `backend-verify`
   - `frontend-verify`
   - `powershell-parse`
4. Require the branch to be up to date before merge.
5. Block force pushes and branch deletion.
6. Require all review conversations to be resolved.
7. Require linear history.
8. Apply the rules to administrators or avoid a bypass that permits routine direct pushes.
9. Do not require an approval that the sole repository owner cannot obtain. The owner may merge after all required checks are green and conversations are resolved.

Do not enable an automatic merge or remote setting from a repository script. A repository administrator must review and apply the ruleset in GitHub settings.

## Why both CI and protection are required

The workflow detects test, build, policy, and parser failures. Branch protection enforces that a red, pending, or missing result cannot be integrated into `main`. CI alone reports; protection prevents the unsafe merge.

## Sequential automation preflight

Branch protection prevents an unsafe merge but does not, by itself, prevent automation from opening overlapping delivery branches. Before starting any EPIC, scheduled automation must query GitHub for open pull requests targeting `main` from `codex/*` or `quality/*`.

Any match blocks the new execution, including draft pull requests and pull requests with green, pending, red, or absent checks. The automation must not create a branch, modify files, run another EPIC, commit, or push until the previous delivery has been reviewed and merged or closed. This gate makes delivery strictly sequential.

After the earlier pull request is merged or closed, use this recovery sequence before selecting work:

```powershell
git switch main
git fetch origin
git status --short
git merge --ff-only origin/main
git rev-parse HEAD
git rev-parse origin/main
```

Continue only when the worktree is clean and both SHAs are identical. Otherwise, stop without starting an EPIC.

## Test pull request

After applying the ruleset, open a disposable documentation-only pull request from a branch. Confirm:

- direct push to `main` is rejected;
- all four exact checks appear and complete;
- merge is unavailable while any required check is pending or red;
- an outdated branch must be updated;
- an unresolved conversation blocks merge;
- after green checks and resolved conversations, the owner can merge without an impossible self-approval requirement;
- force push and deletion of `main` remain blocked.

Record the test PR URL, commit SHA, date, check results, and screenshots or settings export as administrative evidence. Delete only the disposable branch after merge/closure and only through a normal GitHub operation; do not rewrite history.

## Post-merge verification

Return to `main`, fetch `origin`, require a clean tree, update only by fast-forward, confirm `HEAD == origin/main` at the expected merge or squash SHA, and query GitHub again before starting another EPIC. A missing or altered required check blocks further delivery until the ruleset is corrected.
