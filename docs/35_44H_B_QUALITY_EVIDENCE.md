# EPIC 44H-B quality evidence

EPIC: 44H-B - Datos demo y scripts idempotentes
Commit base: `6862d251f5ee9453bab7179d0e36a6566d284bfc`
Rama: `codex/44h-b`
Fecha de validacion actual: 2026-08-01
Archivos modificados: `.gitignore`, `scripts/demo/`, `docs/08_NEXT_BACKLOG.md`,
`docs/12_TASK_LOG.md`, `docs/19_MVP_STATUS.md`, `docs/25_DEMO_DATA.md`,
`docs/30_MVP4_DEMO_VALIDATION_DESIGN.md`, `docs/35_44H_B_QUALITY_EVIDENCE.md`,
`docs/AI_HANDOFF.md`
Archivos fuera de alcance: ninguno
Criterios: orquestador unico, dataset D1-D3/M1/B1/L1/P1, claves deterministas,
find-validate-create, incompatibilidad segura, WhatIf sin efectos, resumen
atomico sin secretos y prueba offline repetible
Pruebas nuevas: 1 script PowerShell de comportamiento offline
Pruebas modificadas: 0
Pruebas eliminadas: 0
Pruebas ignoradas nuevas: 0

Backend:

- comando: `backend\.\mvnw.cmd clean verify`
- tests: 424
- fallos: 0
- errores: 0
- omitidos: 0
- Testcontainers: PASS; Docker disponible en la ejecucion fuera del sandbox

Frontend:

- npm ci: PASS; 474 paquetes; warning de `@angular/animations` y cuatro scripts
  de instalacion pendientes de aprobacion; 16 vulnerabilidades informadas por
  el verificador, sin ejecutar `npm audit fix`
- archivos: 59
- tests: 244 PASS
- build: PASS
- warnings: bundle inicial 631.54 kB, 131.54 kB sobre el budget de 500 kB

Scripts:

- parser: PASS para todos los `.ps1`
- WhatIf: PASS dos veces; salida determinista, sin HTTP, psql ni resumen
- idempotencia: PASS offline para plan determinista y primitivas
  find-validate-create; `NOT_RUN` para doble ejecucion API real, reservada a
  44H-C con servicios y credenciales locales
- fallo seguro: PASS con backend inalcanzable, salida distinta de cero y sin
  resumen

Migraciones:

- base vacia: NOT_RUN: sin cambios de migracion
- actualizacion: NOT_RUN: sin cambios de migracion
- datos preservados: NOT_RUN: la ejecucion API/DB corresponde a 44H-C

Seguridad/privacidad:

- actores: operador ADMIN, owner, reader y visitante definidos en el script
- positivas: validaciones implementadas en el orquestador; NOT_RUN contra API
  real hasta 44H-C
- negativas: duplicados ambiguos/incompatibles, PRIVATE y progreso ajeno
  implementados; NOT_RUN contra API real hasta 44H-C
- secretos: credenciales solo SecureString/entorno de proceso; resumen y logs no
  contienen password, token, header, hash, PGPASSWORD ni JWT secret

QA:

- automatizado: parser, WhatIf doble, ausencia de efectos, fallo seguro,
  primitivas idempotentes, comandos prohibidos, backend, frontend y build
- manual: revision de contratos DTO/controller y alcance del diff
- no ejecutado y motivo: API real doble, SELECT DB y UI manual pertenecen a
  44H-C; backend local y credenciales no estaban disponibles

Dependencias: sin cambios
Verificador local reejecutado el 2026-08-01: PASS con
`scripts/quality/verify.ps1 -BaseRef origin/main`; diff, conflictos, tests
eliminados, marcadores ignorados y parser correctos
Resultado: LOCAL_PASS_REMOTE_PENDING
Commit: pendiente de publicacion tras revision final del diff
Push: NO
PR: pendiente
Checks remotos: pendientes
Riesgos: la idempotencia real y la matriz API/DB/manual deben demostrarse en
44H-C; warning de bundle y 16 vulnerabilidades npm conservados; no hay cambios
de dependencias ni se ejecuto ningun arreglo automatico
Siguiente tarea: EPIC 44H-C - Validacion integral y cierre parcial de MVP4
