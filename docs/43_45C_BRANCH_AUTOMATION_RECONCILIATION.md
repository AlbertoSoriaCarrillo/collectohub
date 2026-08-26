# 45C, branch and automation reconciliation evidence

Task: `45C / branch / automation reconciliation`

Date: 2026-08-26

Base: `origin/dev@1c3b26ed00d7d82c5145388b0ee228992644485b`

Branch: `quality/reconcile-45c-branch-automation`

Scope: documentation and process policy only. Backend, frontend, Liquibase,
workflows, dependencies and Docker are excluded.

## Canonical repository

```text
CANONICAL_WORKING_COPY=COLLECTOHUB_WORKTREE local setting
REMOTE=AlbertoSoriaCarrillo/collectohub
```

The exact workstation path belongs in local automation configuration, not in
versioned documentation. The obsolete checkout from the previous installation
must not be used. `COLLECTOHUB_WORKTREE` must point to the canonical checkout
before the schedule can be reactivated.

## 45B and 45B-FIX closure

PR #22 is merged into `dev`. Its validated head was
`27ede189518d451b83a7af121c31cb4e03ea8cd8`; the squash result is
`1c3b26ed00d7d82c5145388b0ee228992644485b`. The seven required checks were in
`SUCCESS` on the validated head.

- 45B: `CLOSED_AFTER_45B_FIX`.
- 45B-FIX: `INTEGRATED_IN_DEV`.
- `PublicShopProductResponse` no longer exposes physical `stockQuantity`.
- Inventory and reservations share the public-reference rule.
- `ReservationResponse.productName` prefers edition, then item, then legacy.
- The current npm measurement is 23 vulnerabilities. The previous 16 are
  historical evidence. No `npm audit fix` was run.
- `availableQuantity`, holds, PostgreSQL locks, idempotency, expiration,
  transactional availability, overbooking protection and reservation cutover
  remain in 45G.

## Exact 45C status

- 45C-A: integrated active-membership listing for OWNER/MANAGER.
- 45C-B: integrated OWNER-only member creation for an existing active account,
  with MANAGER/EMPLOYEE roles and privacy-safe responses.
- 45C-C: integrated OWNER-only MANAGER/EMPLOYEE role change.
- 45C-D: integrated OWNER-only audited deactivation of MANAGER/EMPLOYEE; OWNER
  cannot be deactivated through this contract.

45C remains `OPEN`. The code audit found that shop profile fields and backend
editing already exist, but public and managed reads share `ShopResponse`. The
schema stores membership role/status as unconstrained `VARCHAR`; the code enum
contains only OWNER, MANAGER and EMPLOYEE. No versioned persistence evidence of
`STAFF` was found; the legacy label appeared in docs/exports/i18n.

Remaining independent units:

1. **45C-E - Backend shop profile and contracts.** Separate public and managed
   projections (for example `PublicShopResponse` and `ManagedShopResponse`),
   harden public/contact sanitization, preserve legacy clients where required,
   and validate existing OWNER/MANAGER editing, authorization, validation and
   privacy. No frontend or schema work.
2. **45C-F - Membership compatibility and additive schema.** Inspect real data
   before deciding whether `STAFF -> EMPLOYEE` migration is required. If the
   evidence requires it, use an idempotent additive migration and PostgreSQL
   upgrade tests that preserve shop, user, status and permissions; add role and
   status constraints only with the same upgrade proof.
3. **45C-G - Backend closure.** Revalidate remaining membership/profile
   invariants, authorization, privacy, compatibility and API/export evidence,
   then close 45C without absorbing frontend 45D.

```text
NEXT_EPIC=45C-E
TRANSFER_OWNERSHIP=FUTURE / OUT_OF_MVP5
QUALITY-B=DEFINED_NOT_IMPLEMENTED
```

## Branch topology and recovery

```text
DEV_SHA=1c3b26ed00d7d82c5145388b0ee228992644485b
PRE_SHA=5f5c45c6cec89e442c246508eb421ac641f8a967
MAIN_SHA=b3876ad39c20b7d49047bea4768fa82cc8890c82
MERGE_BASE_DEV_PRE=5f5c45c6cec89e442c246508eb421ac641f8a967
MERGE_BASE_PRE_MAIN=5f5c45c6cec89e442c246508eb421ac641f8a967
MERGE_BASE_DEV_MAIN=5f5c45c6cec89e442c246508eb421ac641f8a967
DEV_IS_ANCESTOR_OF_PRE=false
PRE_IS_ANCESTOR_OF_MAIN=true
```

PR #21 squashed `dev` directly into `main`, skipped `pre`, and produced a
single-parent commit whose tree equals `dev@42c8998`. Therefore the content can
be reconciled without history rewriting: after this documentation is integrated
in `dev`, promote `dev -> pre`, then `pre -> main`, both through human PRs using
**Create a merge commit**. Recheck exact refs, diff, seven checks, reviews and
conversations immediately before each merge. If current Git reports a conflict
or new exclusive content, stop with
`BRANCH_RECOVERY_REQUIRES_SEPARATE_DECISION`.

## Automation contract

`AUTONOMOUS_DEV_AUTO_MERGE_GUARDED` applies only to `codex/* -> dev` and
`quality/* -> dev`. The normative 31-condition gate and states are in
`docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`. GitHub native auto-merge, automatic
source-branch deletion, rebase, force push, direct permanent-branch pushes and
automatic promotions are forbidden.

The current merge API protects the head SHA but does not atomically pin the base
SHA. Therefore the future mode cannot activate until an atomic base guard is
available and demonstrated fail-closed. Until then the root human-only policy
remains authoritative.

The schedule remains `PAUSED`. This process-policy PR is explicitly excluded
from automatic merge and must end in `HUMAN_MERGE_REQUIRED`.

## Review corrections

The first Codex review found four valid issues. The same branch was corrected
to keep the active loop human-only while root policy remains unchanged, require
an atomic base-SHA guard before future activation, keep the workstation path in
local configuration, and identify the remaining legacy `STAFF` references
instead of claiming they were already reconciled.

The second review found three valid issues. The future/manual merge wording was
made activation-aware, the clean-worktree check now precedes branch switching
or pulling, and the complete validation matrix was executed because this diff
changes an API contract document and executable command examples.

## Validation

- `git diff --check`: `PASS`.
- `scripts/quality/verify.ps1 -BaseRef origin/dev -DocumentationOnly`: `PASS`.
- `scripts/quality/verify.ps1 -BaseRef origin/dev`: `PASS` after one
  sandbox-permission retry for the Maven cache.
- Backend: 466 tests, 0 failures, 0 errors, 4
  `SKIPPED_WITH_REASON: Docker unavailable; no migration diff`.
- Frontend: 59 files and 244 tests, production build `PASS`, 23 known
  vulnerabilities, no `npm audit fix`.
- Warning: initial bundle 631.54 kB, 131.54 kB over budget.
- E2E/Playwright: `SKIPPED_WITH_REASON: explicitly excluded`.
