# Flujo supervisado de ramas en GitHub Free

Fecha: 2026-08-02
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Visibilidad: privado
Estado de esta entrega: `SUPERVISED_ACTIVATION_PENDING_ALIGNMENT`

Este documento describe controles operativos, no protecciones tecnicas. Con el
plan actual, GitHub no aplica branch protection ni rulesets enforced a este
repositorio privado. Las garantias dependen del procedimiento de Codex y de la
revision y fusion humanas.

La consulta fiable de la API de GitHub realizada el 2026-08-02 confirmo:

- `Allow squash merging`: activado;
- `Allow merge commits`: activado;
- `Allow rebase merging`: desactivado;
- auto-merge nativo de GitHub: desactivado y no necesario para este modo;
- los endpoints de rulesets y branch protection responden `403` e indican que
  se requiere GitHub Pro o que el repositorio sea publico.

No se ha configurado ni modificado ningun ajuste remoto en esta tarea.

## Estados operativos

`SUPERVISED_ACTIVE_NO_ENFORCEMENT` significa exactamente que:

- `dev` es la rama de integracion efectiva;
- GitHub no aplica tecnicamente branch protection ni rulesets;
- los controles se aplican mediante el procedimiento de Codex y revision
  humana;
- no se puede afirmar que `main`, `dev` o `pre` esten tecnicamente protegidas;
- ninguna automatizacion tiene permiso para fusionar pull requests;
- toda fusion requiere intervencion humana;
- `main` y `pre` siguen siendo ramas permanentes;
- el push directo esta prohibido por politica, aunque GitHub no pueda impedirlo.

La activacion es condicional. Hasta que el commit que contiene esta politica
este presente simultaneamente en `origin/main`, `origin/dev` y `origin/pre`, el
estado es `SUPERVISED_ACTIVATION_PENDING_ALIGNMENT`, `main` sigue siendo la rama
de integracion efectiva y la automatizacion permanece `PAUSED`.

La igualdad de refs es necesaria, pero la activacion operativa completa tambien
requiere:

1. fusion manual de la PR de esta tarea en `main`;
2. fast-forward manual de `dev` y `pre` al commit integrado;
3. confirmacion de que `origin/main`, `origin/dev` y `origin/pre` coinciden;
4. adaptacion separada de la automatizacion al modo supervisado, sin permiso de
   fusion;
5. primera ejecucion bajo supervision humana.

Si las refs ya coinciden pero los pasos 4 o 5 siguen pendientes, la alineacion
esta completa pero la activacion operativa no: la automatizacion continua
`PAUSED`. Esta tarea no fusiona la PR, no alinea ramas y no activa ni modifica
la automatizacion.

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
6. Codex informa `HUMAN_MERGE_REQUIRED` y termina sin fusionar ni iniciar otra
   EPIC.

Antes de fusionar, una persona debe verificar:

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
- `SUPERVISED_ACTIVE_NO_ENFORCEMENT` no debe confundirse con
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

La automatizacion es estrictamente secuencial y nunca fusiona. Una entrega
verde termina en `HUMAN_MERGE_REQUIRED`.

## Verificacion posterior humana

Despues de cualquier fusion, obtener de nuevo las refs, comprobar el SHA
esperado, la ascendencia aplicable y el arbol limpio antes de permitir otra
tarea. La eliminacion de una rama temporal no es automatica y solo puede
considerarse despues de confirmar su fusion; esta tarea no elimina ramas.
