# EPIC 44H-B-FIX quality evidence

EPIC: 44H-B-FIX - SummaryPath compatible con Windows PowerShell 5.1
Commit base: `f31ab7d4a43c1786fe7068b6fe01c931cf389b86`
Rama: `codex/44h-b-fix`
Fecha real: 2026-08-01
Archivos modificados: `scripts/demo/create-mvp4-integral-demo-data.ps1`,
`scripts/demo/test-mvp4-integral-demo-data.ps1`, `docs/12_TASK_LOG.md`,
`docs/AI_HANDOFF.md`, `docs/08_NEXT_BACKLOG.md`, `docs/19_MVP_STATUS.md` y este
informe
Archivos fuera de alcance: ninguno
Criterios: resolver el `SummaryPath` predeterminado despues del bloque `param`,
conservar la ruta explicita y demostrar el comportamiento en Windows PowerShell
5.1 mediante `-File` y `-WhatIf` sin efectos
Pruebas nuevas: 0
Pruebas modificadas: 1 script PowerShell de comportamiento
Pruebas eliminadas: 0
Pruebas ignoradas nuevas: 0

Defecto y causa:

- reproduccion previa: FAIL en Windows PowerShell `5.1.19041.7548` antes de
  cualquier llamada HTTP
- causa: `Join-Path $PSScriptRoot` se evaluaba como valor predeterminado dentro
  de `param`, donde `$PSScriptRoot` podia estar vacio con la invocacion `-File`
- correccion: `SummaryPath` queda sin expresion en `param` y se resuelve despues
  de `Set-StrictMode` solo cuando esta vacio

Backend:

- comando: `.\mvnw.cmd clean verify`, ejecutado por el verificador
- tests: 424
- fallos: 0
- errores: 0
- omitidos: 0
- Testcontainers: PASS; Docker disponible

Frontend:

- npm ci: PASS; 16 vulnerabilidades conocidas, sin `npm audit fix`
- archivos: 59
- tests: 244 PASS
- build: PASS
- warnings: bundle inicial 631.54 kB, 131.54 kB sobre el budget de 500 kB

Scripts:

- parser: PASS para todos los `.ps1`
- Windows PowerShell: PASS, version `5.1.19041.7548`
- ruta predeterminada: PASS mediante proceso `powershell.exe -File` sin
  `-SummaryPath`; salida con la ruta esperada
- ruta explicita: PASS; las pruebas existentes permanecen activas
- WhatIf: PASS; sin HTTP, psql ni creacion/modificacion del resumen
- prueba especifica: PASS

Migraciones:

- base vacia: NOT_RUN: sin cambios de migracion
- actualizacion: NOT_RUN: sin cambios de migracion
- datos preservados: NOT_RUN: esta FIX no toca Docker ni datos

Seguridad/privacidad:

- revision de secretos: PASS; no se anaden credenciales, tokens ni hashes
- cuentas locales: NOT_RUN: fuera de alcance y sin cambios
- API/DB/UI real de 44H-C: NOT_RUN: prohibida en esta FIX

QA:

- automatizado: parser global, regresion predeterminada, ruta explicita,
  WhatIf sin efectos, backend, frontend, build y politica de diff
- manual: revision del diff y de los archivos permitidos/prohibidos
- no ejecutado y motivo: escenario integral, API, DB y UI pertenecen a 44H-C

Dependencias: sin cambios
Verificador local: PASS con
`powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\quality\verify.ps1 -BaseRef origin/main`;
la invocacion directa inicial fue bloqueada por la Execution Policy del host y
no se conto como PASS
Resultado: LOCAL_PASS
Commit: pendiente de crear tras la revision final
Push: pendiente
PR: pendiente
Checks remotos: pendientes
Riesgos: warnings npm/bundle conocidos; no se cambia el alcance funcional
Siguiente tarea: EPIC 44H-C - Validacion integral y cierre parcial de MVP4
