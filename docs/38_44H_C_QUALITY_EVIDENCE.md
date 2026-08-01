# EPIC 44H-C quality evidence

EPIC: 44H-C - Validacion integral y cierre parcial de MVP4
Commit base: `309d5db6069354577ffc606620dd33307063ad19`
Rama: `codex/44h-c`
Fecha real: 2026-08-01
Archivos modificados: informe de cierre, evidencia y documentos de estado de
44H-C
Archivos fuera de alcance: ninguno
Criterios: doble ejecucion idempotente, API/DB, privacidad, propiedad, filtros,
ordenaciones, progreso, restauracion, UI humana, rotacion final y matriz de
calidad completa
Pruebas nuevas: 0
Pruebas modificadas: 0
Pruebas eliminadas: 0
Pruebas ignoradas nuevas: 0

## Dataset y scripts

- escenario: `44hc3`
- doble ejecucion real: PASS, COMPLETE durante 44H-C
- segunda ejecucion y revalidaciones: PASS, cero datos creados
- ejecucion del dataset en este cierre posterior a UI: `NOT_RUN: prohibida para
  conservar exactamente el escenario ya validado`
- IDs conservados: personas 49-51, colecciones 17-18 e items 15-20
- cuentas 47 y 48: sin cambios
- prueba especifica:
  `powershell.exe -NoProfile -ExecutionPolicy Bypass -File
  .\scripts\demo\test-mvp4-integral-demo-data.ps1`: PASS
- parser: 56 archivos `.ps1`, PASS
- WhatIf: PASS dentro de la prueba especifica, sin efectos
- idempotencia: PASS

## Backend y persistencia

- comando: `cd backend; .\mvnw.cmd clean verify`
- resultado: PASS
- tests: 424
- fallos: 0
- errores: 0
- omitidos: 0
- Testcontainers/PostgreSQL 17: PASS
- API/DB funcional de 44H-C: PASS
- filtros y ordenaciones: PASS
- progreso y restauracion D2: PASS
- D3 calculado, filas persistidas: 0
- bridge VERIFIED y compatibilidad legacy: PASS
- migraciones: sin cambios; la suite Liquibase/Testcontainers paso
- datos preservados: PASS

## Frontend y QA

- `npm.cmd ci`: PASS
- primer intento de test dentro del sandbox: FAIL por acceso denegado del host,
  sin fallo de codigo; reintento autorizado fuera del sandbox: PASS
- archivos de test: 59
- tests: 244 PASS
- build: PASS
- UI humana owner ID 50: PASS
- UI humana reader ID 51: PASS
- privacidad, propiedad, coleccion publica y coleccion privada: PASS
- vacio filtrado, error, retry, busqueda y ordenaciones: PASS
- warnings: bundle inicial 631.54 kB frente a budget 500 kB;
  `@angular/animations` deprecado y cuatro scripts npm pendientes de
  `allowScripts`
- vulnerabilidades npm: `NOT_RUN: no se ejecuto auditoria nueva ni se autorizo
  mantenimiento de dependencias`; el baseline historico conocido permanece sin
  `npm audit fix`

## Seguridad y rotacion local

- actores: operador ADMIN/USER 49, owner USER 50, reader USER 51 y visitante
- positivas: logins 49-51 y `/api/users/me`, PASS
- negativa: credencial deliberadamente incorrecta, HTTP 401 PASS
- rotacion: PASS, tres hashes BCrypt independientes generados solo en memoria
- transaccion: exactamente 3 filas; solo `password_hash`; 0 inserciones y 0
  eliminaciones
- campos no relacionados, roles, propiedad y datos: sin cambios
- variables de credenciales demo en Process y User: ausentes, PASS
- secretos, hashes y tokens persistidos o documentados: 0

## Controles de entrega

- dependencias: sin cambios
- migraciones: sin cambios
- manifests y lockfiles: sin cambios
- workflows: sin cambios
- archivos eliminados: 0
- tests eliminados: 0
- tests ignorados nuevos: 0
- verificador local:
  `powershell.exe -NoProfile -ExecutionPolicy Bypass -File
  .\scripts\quality\verify.ps1 -BaseRef origin/main`: PASS
- verificador backend: 424 tests, 0 fallos, 0 errores y 0 omitidos
- verificador frontend: 59 archivos, 244 tests y build PASS
- verificador PowerShell y politica de alcance: PASS
- `git diff --check`: PASS

Resultado local: PASS
Commit: un unico commit de `codex/44h-c`
Push: pendiente
PR: pendiente; debe abrirse en borrador
Checks remotos: NOT_RUN, se ejecutan despues de publicar la rama
Riesgos: warnings npm/bundle conocidos; limites de MVP4 enumerados en el informe
Siguiente tarea: revision e integracion humana de 44H-C; no iniciar otra EPIC
