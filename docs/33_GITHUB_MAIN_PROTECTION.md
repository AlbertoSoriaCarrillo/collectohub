# Flujo protegido por procedimiento en GitHub Free

Fecha: 2026-08-26
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Visibilidad: privado
Modo documentado: `AUTONOMOUS_DEV_AUTO_MERGE_GUARDED`
Horario programado: `PAUSED`

Este documento describe controles operativos, no protecciones tecnicas. Con el
plan actual, GitHub no aplica branch protection ni rulesets enforced a este
repositorio privado. Las garantias dependen del procedimiento de Codex y de la
  verificacion de Codex. Las promociones permanentes siguen siendo humanas.

La consulta fiable de la API de GitHub realizada el 2026-08-02 confirmo:

- `Allow squash merging`: activado;
- `Allow merge commits`: activado;
- `Allow rebase merging`: desactivado;
- auto-merge nativo de GitHub: desactivado y prohibido para este modo;
- los endpoints de rulesets y branch protection responden `403` e indican que
  se requiere GitHub Pro o que el repositorio sea publico.

No se ha configurado ni modificado ningun ajuste remoto en esta tarea.

## Estados operativos

`AUTONOMOUS_DEV_AUTO_MERGE_GUARDED` significa exactamente que:

- `dev` es la rama de integracion efectiva;
- GitHub no aplica tecnicamente branch protection ni rulesets;
- los controles se aplican mediante el procedimiento de Codex;
- no se puede afirmar que `main`, `dev` o `pre` esten tecnicamente protegidas;
- la automatizacion solo puede fusionar `codex/* -> dev` o
  `quality/* -> dev` cuando pasan todas las condiciones normativas;
- `dev -> pre` y `pre -> main` siempre requieren intervencion humana;
- `main` y `pre` siguen siendo ramas permanentes;
- el push directo esta prohibido por politica, aunque GitHub no pueda impedirlo.

La transicion queda completada. El commit de activacion
`5f5c45c6cec89e442c246508eb421ac641f8a967` esta presente simultaneamente en
`origin/main`, `origin/dev` y `origin/pre`, y `dev` es la rama de integracion
efectiva.

La primera ejecucion bajo supervision humana tambien se completo correctamente:

- remoto y arbol local correctos;
- `dev` local actualizado exclusivamente mediante fast-forward;
- cero PR abiertas desde `codex/*` o `quality/*` hacia `dev`;
- cero cambios de producto;
- detencion segura porque la siguiente EPIC aun no estaba seleccionada;
- cero commit, push, pull request o fusion.

El modo queda documentado, pero el horario permanece `PAUSED`. Esta
reconciliacion de las propias reglas termina en `HUMAN_MERGE_REQUIRED` y no se
auto-fusiona. La activacion exige integrar esta politica en `dev`, reparar la
ascendencia `dev -> pre -> main`, configurar el working copy canonico y validar
manualmente una ejecucion completa del nuevo contrato.

## Siete checks obligatorios

Toda PR de entrega o promocion debe ejecutar:

1. `Validate repository structure`;
2. `Backend build and tests`;
3. `Frontend build and tests`;
4. `quality-policy`;
5. `backend-verify`;
6. `frontend-verify`;
7. `powershell-parse`.

Los checks pueden ejecutarse en GitHub, pero el plan actual no garantiza que un
ruleset bloquee una fusion con resultados rojos, pendientes o ausentes. Su
cumplimiento se comprueba de forma procedimental. Un check que no este en
`SUCCESS` bloquea la autorizacion humana para fusionar.

## Entregas temporales hacia `dev`

Despues de la activacion:

1. La rama `codex/<epic>` o `quality/<epic>` parte de `origin/dev` actualizado.
2. Una ejecucion procesa como maximo una EPIC o una PR pendiente.
3. Se abre una PR con base exacta `dev`.
4. Se ejecutan los siete checks.
5. Codex realiza autorrevision y registra `expected_head_sha`.
6. Codex espera al menos diez minutos desde ready-for-review, reconsulta GitHub
   y aplica el contrato completo de `docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md`.
7. Solo si todas las condiciones pasan, usa **Squash and merge**, sincroniza
   `dev`, informa `EPIC_MERGED_TO_DEV` y termina sin iniciar otra EPIC.

Antes de fusionar, la verificacion final debe confirmar:

- head actual de la PR igual a `expected_head_sha`;
- base exacta `dev`;
- diff esperado;
- siete checks en `SUCCESS`;
- ausencia de conversaciones o bloqueos pendientes;
- ausencia de cambios posteriores a la revision.

La fusion se realiza manualmente mediante **Squash and merge**. Si cambia
`dev`, el head o el diff, se repite la revision y, cuando corresponda, los siete
checks sobre el head actualizado.

Mientras exista una PR abierta hacia `dev` desde `codex/*` o `quality/*`, sea o
no borrador y con cualquier estado de checks, no se inicia otra EPIC.

## Promocion `dev` -> `pre`

- PR con head exacto `dev` y base exacta `pre`;
- siete checks en `SUCCESS`;
- validacion funcional humana;
- ningun cambio funcional nuevo dentro de la promocion;
- revision repetida si `dev`, `pre`, el head, la base o el diff cambian;
- fusion humana mediante **Create a merge commit**;
- nunca Squash and merge;
- nunca Rebase and merge.

Despues de la fusion debe pasar:

```powershell
git merge-base --is-ancestor origin/dev origin/pre
```

No se fusiona `pre` de vuelta hacia `dev` para actualizar la PR.

## Promocion `pre` -> `main`

- PR con head exacto `pre` y base exacta `main`;
- siete checks en `SUCCESS`;
- autorizacion humana explicita;
- nueva autorizacion si `pre`, `main`, el head, la base o el diff cambian;
- fusion humana mediante **Create a merge commit**;
- nunca Squash and merge;
- nunca Rebase and merge.

Despues de la fusion debe pasar:

```powershell
git merge-base --is-ancestor origin/pre origin/main
```

No se fusiona `main` de vuelta hacia `pre` para actualizar la PR.

## Limitaciones expresas del modo gratuito

- GitHub no bloquea tecnicamente pushes directos.
- GitHub no bloquea force push o borrado mediante rulesets enforced.
- GitHub no garantiza por si solo el metodo correcto de fusion.
- Los checks pueden ejecutarse, pero su cumplimiento es procedimental.
- `AUTONOMOUS_DEV_AUTO_MERGE_GUARDED` no debe confundirse con
  `PROTECTED_ACTIVE`.
- La politica prohibe push directo, force push y borrado de ramas permanentes,
  aunque el proveedor no lo impida tecnicamente.

Cuando exista GitHub Pro, Team o un plan equivalente, se debe abrir una tarea
separada para configurar, probar y evidenciar protecciones reales. Solo esa
tarea podra evaluar un futuro estado `PROTECTED_ACTIVE`.

## Preflight secuencial

Antes de cualquier EPIC:

1. Determinar el estado de activacion y la rama de integracion efectiva.
2. Consultar PR abiertas hacia esa rama desde `codex/*` o `quality/*`.
3. Detener trabajo nuevo ante cualquier coincidencia.
4. En ausencia de PR pendiente, volver a la rama efectiva, ejecutar
   `git fetch origin`, exigir arbol limpio, actualizar solo por fast-forward y
   demostrar que el `HEAD` local coincide con `origin/<rama>`.

La automatizacion es estrictamente secuencial. Una entrega verde solo termina
en `EPIC_MERGED_TO_DEV` si supera el guard completo; cualquier desviacion evita
el merge y produce el estado de bloqueo correspondiente.

## Verificacion posterior humana

Despues de cualquier fusion, obtener de nuevo las refs, comprobar el SHA
esperado, la ascendencia aplicable y el arbol limpio antes de permitir otra
tarea. La eliminacion de una rama temporal no es automatica y solo puede
considerarse despues de confirmar su fusion; esta tarea no elimina ramas.
