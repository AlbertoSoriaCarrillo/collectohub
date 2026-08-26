# Modelo de ramas dev -> pre -> main

Fecha de reconciliacion: 2026-08-26

Modo documentado: `AUTONOMOUS_DEV_AUTO_MERGE_GUARDED`

Horario programado: `PAUSED`

Working copy canonico local: variable `COLLECTOHUB_WORKTREE`. Its value belongs
to local automation configuration and must not be committed because workstation
paths can contain personal identifiers.

GitHub no aplica branch protection ni rulesets enforced en el plan actual. Las
garantias descritas aqui son procedimentales; no equivalen a
`PROTECTED_ACTIVE`.

Este documento define el modo futuro, no lo activa. Mientras el horario siga
`PAUSED` y la politica raiz de ejecucion conserve el flujo humano supervisado,
toda fusion sigue siendo humana. La adaptacion de esa politica y de la
automatizacion es una precondicion de la primera prueba manual protegida.
Ademas, `expected_head_sha` solo fija el head: el modo no puede activarse hasta
disponer de un mecanismo atomico que rechace el merge si cambia el base SHA.

## Ramas y metodos de integracion

El flujo normativo es:

```text
codex/* o quality/*
        |
        v
       dev
        |
        v
       pre
        |
        v
      main
```

- `codex/* -> dev` y `quality/* -> dev`: entrega temporal, exclusivamente
  **Squash and merge**, con el guard completo de este documento.
- `dev -> pre`: promocion `HUMAN ONLY`, exclusivamente **Create a merge
  commit**.
- `pre -> main`: promocion `HUMAN ONLY`, exclusivamente **Create a merge
  commit**, con autorizacion humana explicita.
- Rebase, force push, push directo a ramas permanentes, auto-merge nativo de
  GitHub y borrado automatico de la rama fuente estan prohibidos.
- Una ejecucion procesa como maximo una EPIC o una PR pendiente y nunca empieza
  otra EPIC despues de entregar o bloquear la actual.

## Estado real de las ramas tras PR #21

La auditoria del 2026-08-26 obtuvo:

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

PR #21 promovio `dev -> main` mediante squash y omitio `pre`. El commit
`b3876ad39c20b7d49047bea4768fa82cc8890c82` tiene como unico padre
`5f5c45c6cec89e442c246508eb421ac641f8a967`; por eso el contenido promovido de
`dev` no figura como ancestro de `main`. La comprobacion de arboles demuestra que
el arbol de `main@b3876ad` coincide exactamente con `dev@42c8998`, por lo que
`main` no aporta cambios de contenido exclusivos que deban invertirse o
sincronizarse hacia atras. PR #22 avanzo despues `dev` a `1c3b26e`.

## Reparacion de ascendencia pendiente

No se reescribe historia, no se revierte PR #21 y no se hace reverse merge.

1. Integrar esta reconciliacion exclusivamente en `dev`.
2. Abrir `dev -> pre` con head/base exactos, siete checks `SUCCESS`, diff
   esperado, validacion funcional humana, cero conversaciones pendientes y refs
   sin cambios. Fusionar manualmente con **Create a merge commit** y comprobar:

   ```powershell
   git fetch origin --prune
   git merge-base --is-ancestor origin/dev origin/pre
   ```

3. Abrir `pre -> main` con head/base exactos, siete checks `SUCCESS`, diff
   esperado, revision, autorizacion humana explicita, cero conversaciones y refs
   sin cambios. Fusionar manualmente con **Create a merge commit** y comprobar:

   ```powershell
   git fetch origin --prune
   git merge-base --is-ancestor origin/pre origin/main
   ```

La topologia y la igualdad de arbol comprobadas indican que esta secuencia puede
restaurar la ascendencia sin reescribir historia. Antes de cada merge deben
repetirse las comprobaciones porque las refs pueden cambiar. Si Git detecta un
conflicto o contenido exclusivo nuevo, detenerse con
`BRANCH_RECOVERY_REQUIRES_SEPARATE_DECISION`.

## Preflight secuencial y sincronizacion de dev

Antes de una EPIC y despues de un squash merge protegido:

```powershell
Set-Location $env:COLLECTOHUB_WORKTREE
git switch dev
git fetch origin --prune
git pull --ff-only origin dev
git status --short
git rev-parse HEAD
git rev-parse origin/dev
```

Se exige worktree limpio y `HEAD == origin/dev`. Antes de trabajo nuevo se
consulta GitHub. Debe existir cero PR abierta con base `dev` y head `codex/*` o
`quality/*`; cualquier coincidencia produce `PENDING_DELIVERY_EXISTS` y bloquea
otra EPIC.

## Siete checks obligatorios

Los siete resultados deben pertenecer al head actual y estar en `SUCCESS`:

1. `Validate repository structure`;
2. `Backend build and tests`;
3. `Frontend build and tests`;
4. `quality-policy`;
5. `backend-verify`;
6. `frontend-verify`;
7. `powershell-parse`.

## Guard de auto-merge hacia dev

Una PR de entrega puede fusionarse automaticamente hacia `dev` solo cuando se
cumplen simultaneamente las 31 condiciones siguientes:

1. la base exacta es `dev`;
2. el head empieza exactamente por `codex/` o `quality/`;
3. existe exactamente una PR de entrega abierta hacia `dev`;
4. la PR esta abierta;
5. la PR no es draft;
6. la PR es mergeable;
7. no existen conflictos;
8. el base SHA actual coincide con la base usada para validacion;
9. el head SHA actual coincide exactamente con `expected_head_sha`;
10. el diff actual coincide con el revisado;
11. los siete checks obligatorios corresponden al head actual y estan en
    `SUCCESS`;
12. la review automatica ha finalizado;
13. han transcurrido al menos diez minutos desde ready-for-review;
14. despues de esos diez minutos se ha vuelto a consultar GitHub;
15. conversaciones sin resolver = 0;
16. reviews `CHANGES_REQUESTED` = 0;
17. self-review = `PASS`;
18. archivos fuera de alcance = 0;
19. tests eliminados = 0;
20. nuevos tests ignorados = 0;
21. secretos introducidos = 0;
22. no existe modificacion posterior a la revision final;
23. `origin/dev` no se ha movido desde la validacion y un guard atomico rechaza
    la operacion si el base SHA cambia antes de crear el merge;
24. el metodo es exclusivamente **Squash and merge**;
25. la operacion esta protegida mediante `expected_head_sha`;
26. no se utiliza el auto-merge nativo de GitHub;
27. no se borra automaticamente la rama fuente;
28. no se usa rebase;
29. no se usa force push;
30. no se modifica `pre`;
31. no se modifica `main`.

Si cualquiera falla, la automatizacion no fusiona. No resuelve conversaciones
solo para obtener permiso: corrige un defecto valido en la misma rama, repite
validacion/checks/review y vuelve a comprobar todas las condiciones.

La API de merge disponible protege `expected_head_sha`, pero no fija
atomicamente el base SHA. Mientras no exista branch protection, merge queue u
otro compare-and-swap verificable sobre la base, la condicion 23 no puede
demostrarse y el resultado obligatorio es
`BASE_MOVED_HUMAN_ACTION_REQUIRED`, incluso si una consulta previa vio el mismo
SHA.

## Estados de automatizacion

- `EPIC_MERGED_TO_DEV`: guard completo superado, squash merge realizado y
  `dev` local nuevamente limpia y alineada.
- `REVIEW_THREADS_PENDING`: existe al menos una conversacion sin resolver.
- `LOCAL_PASS_REMOTE_PENDING`: validacion local correcta y checks remotos aun no
  concluidos en `SUCCESS`.
- `BASE_MOVED_HUMAN_ACTION_REQUIRED`: `origin/dev` cambio desde la validacion.
- `EPIC_BLOCKED`: fallo de alcance, validacion, seguridad o entrega.
- `PENDING_DELIVERY_EXISTS`: otra PR temporal hacia `dev` bloquea trabajo nuevo.

## Activacion del horario

El horario permanece `PAUSED`. Solo puede reactivarse despues de:

1. integrar esta reconciliacion en `dev`;
2. confirmar coherencia documental;
3. configurar `COLLECTOHUB_WORKTREE` en la automatizacion local con el checkout
   canonico acordado, sin versionar la ruta personal;
4. adaptar la politica raiz de ejecucion y la automatizacion al guard aqui
   definido, sin debilitar el verificador;
5. completar la reparacion `dev -> pre -> main`;
6. disponer de un guard atomico del base SHA y demostrar que falla cerrado;
7. ejecutar manualmente un ciclo completo
   `AUTONOMOUS_DEV_AUTO_MERGE_GUARDED` que sincronice `dev`, implemente una unica
   EPIC, abra la PR, espere checks y review, reconsulte conversaciones, fusione
   solo si procede, vuelva a sincronizar `dev` y termine en
   `EPIC_MERGED_TO_DEV`.

Esta PR de reconciliacion modifica las propias reglas del proceso: no se
auto-fusiona y termina en `HUMAN_MERGE_REQUIRED`.
