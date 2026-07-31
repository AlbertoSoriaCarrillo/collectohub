# Local quality verification

Run from the repository root with Windows PowerShell 5.1+ or PowerShell 7+:

```powershell
.\scripts\quality\verify.ps1 -BaseRef origin/main
```

If the local execution policy blocks repository scripts, use the equivalent
non-persistent invocation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\quality\verify.ps1 -BaseRef origin/main
```

The verifier reviews the working tree against the base reference, checks whitespace and conflicts, detects deleted or newly ignored tests and skip flags, parses every PowerShell script, and runs backend and frontend validation unless an approved option applies.

```powershell
.\scripts\quality\verify.ps1 -BaseRef origin/main -SkipBackend
.\scripts\quality\verify.ps1 -BaseRef origin/main -SkipFrontend
.\scripts\quality\verify.ps1 -BaseRef origin/main -DocumentationOnly
```

Skip options fail when the changed paths affect the skipped layer. `-DocumentationOnly` fails unless every changed file is Markdown documentation. Without `-DocumentationOnly`, backend and frontend validation run by default so cross-cutting repository infrastructure receives full regression coverage.

The ignored summary `scripts/quality/.last-quality-verification.json` is overwritten after each run. It reports current commands, statuses, counts when reliably available, and `unknown` otherwise. It is local evidence, not proof of remote CI or merge readiness.

Do not use this script to bypass a required check. Do not edit the summary into a `PASS`, and never place secrets in command arguments or evidence.
