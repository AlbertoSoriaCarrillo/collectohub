# EPIC quality evidence

Use real results from the current execution. Use `unknown` when a tool does not expose a reliable count, `SKIPPED_WITH_REASON` for an approved omission, and `NOT_RUN` when validation has not happened. Never copy historical figures as current evidence.

```text
EPIC:
Commit base:
Rama:
Fecha real:
Archivos modificados:
Archivos fuera de alcance:
Criterios:
Pruebas nuevas:
Pruebas modificadas:
Pruebas eliminadas: 0
Pruebas ignoradas nuevas: 0

Backend:
- comando:
- tests:
- fallos:
- errores:
- omitidos:
- Testcontainers:

Frontend:
- npm ci:
- archivos:
- tests:
- build:
- warnings:

Scripts:
- parser:
- WhatIf:
- idempotencia:
- ejecucion:

Migraciones:
- base vacia:
- actualizacion:
- datos preservados:

Seguridad/privacidad:
- actores:
- positivas:
- negativas:

QA:
- automatizado:
- manual:
- no ejecutado y motivo:

Dependencias:
Resultado: PASS / FAIL / BLOCKED
Commit:
Push:
PR:
Checks remotos:
Riesgos:
Siguiente tarea:
```

Only `PASS` permits a commit and push to the EPIC branch. Merge requires green CI and the protected-branch conditions described in `docs/33_GITHUB_MAIN_PROTECTION.md`.
