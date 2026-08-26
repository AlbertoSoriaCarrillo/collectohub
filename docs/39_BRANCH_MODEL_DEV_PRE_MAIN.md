# Modelo supervisado de ramas dev -> pre -> main

Fecha: 2026-08-02
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Estado operativo actual: `SUPERVISED_ACTIVE_NO_ENFORCEMENT`

Este documento define la estrategia normativa de ramas. No crea ramas, no
configura rulesets o branch protection, no fusiona pull requests y no activa
automatizaciones.

## Significado del estado objetivo

`SUPERVISED_ACTIVE_NO_ENFORCEMENT` significa:

- `dev` es la rama de integracion efectiva;
- GitHub no aplica tecnicamente branch protection ni rulesets enforced;
- los controles se aplican mediante el procedimiento de Codex y revision
  humana;
- no se afirma que ninguna rama este tecnicamente protegida;
- la automatizacion no puede fusionar pull requests;
- toda fusion requiere intervencion humana;
- `main`, `dev` y `pre` son ramas permanentes;
- el push directo esta prohibido por politica, aunque GitHub no lo bloquee.

Este estado no equivale a `PROTECTED_ACTIVE`.

## Activacion completada

La activacion operativa se completo el 2026-08-02. El commit
`5f5c45c6cec89e442c246508eb421ac641f8a967` esta presente simultaneamente en
`origin/main`, `origin/dev` y `origin/pre`; por tanto `dev` es la rama de
integracion efectiva.

La primera ejecucion supervisada verifico remoto correcto, arbol limpio,
alineacion de las tres ramas, actualizacion local de `dev` solo por fast-forward
y cero PR abiertas de entrega hacia `dev`. No hizo cambios de producto, commit,
push, pull request ni fusion, y se detuvo de forma segura porque la siguiente
EPIC no estaba seleccionada.

El modo activo sigue siendo procedimental: la automatizacion no tiene permiso
de fusion, toda entrega termina en `HUMAN_MERGE_REQUIRED` y toda fusion es
humana. El horario automatico no se activa en este cierre y requiere una
autorizacion separada.

## Ramas permanentes

### `main`

- Es la rama estable y de produccion.
- No admite push directo por politica ni fusion automatica.
- Recibe unicamente promociones desde `pre` despues de la activacion.
- Cada release requiere PR, siete checks en `SUCCESS`, autorizacion humana
  explicita y fusion manual mediante **Create a merge commit**.
- Squash and merge y Rebase and merge estan prohibidos para releases.

### `pre`

- Es la rama de preproduccion.
- No admite push directo por politica ni fusion automatica.
- Recibe promociones desde `dev` mediante PR.
- Requiere siete checks en `SUCCESS` y validacion funcional humana.
- La promocion se fusiona manualmente mediante **Create a merge commit**.
- Squash and merge y Rebase and merge estan prohibidos.
- No se desarrolla funcionalidad directamente en `pre`.

### `dev`

- Es la rama de integracion efectiva solo en
  `SUPERVISED_ACTIVE_NO_ENFORCEMENT`.
- No admite push directo por politica.
- Recibe PR desde `codex/<epic>` y `quality/<epic>`.
- La entrega se fusiona manualmente mediante **Squash and merge**.
- La automatizacion valida e informa, pero no fusiona.

## Ramas temporales

Despues de la activacion, `codex/<epic>` y `quality/<epic>`:

- parten siempre de `origin/dev` actualizado;
- apuntan a `dev`;
- contienen una unica tarea y un unico commit logico;
- no incorporan alcance ajeno a la tarea;
- no se eliminan automaticamente.

Una ejecucion procesa como maximo una EPIC o una PR pendiente.

## Siete checks obligatorios

1. `Validate repository structure`;
2. `Backend build and tests`;
3. `Frontend build and tests`;
4. `quality-policy`;
5. `backend-verify`;
6. `frontend-verify`;
7. `powershell-parse`.

Los checks se ejecutan en GitHub, pero su obligatoriedad no esta enforced por
rulesets en el plan actual. Codex y la persona que fusiona deben comprobar que
los siete estan en `SUCCESS`. Un resultado rojo, pendiente o ausente bloquea la
fusion humana.

## Entrega `codex/<epic>` o `quality/<epic>` -> `dev`

1. Crear la rama desde `origin/dev` actualizado y limpio.
2. Implementar como maximo una EPIC.
3. Ejecutar validacion local y abrir una PR con base exacta `dev`.
4. Esperar los siete checks.
5. Realizar autorrevision y registrar `expected_head_sha`.
6. Informar `HUMAN_MERGE_REQUIRED` y terminar.

Codex y la automatizacion no tienen permiso para fusionar. Antes de la fusion,
una persona comprueba:

- head actual igual a `expected_head_sha`;
- base exacta `dev`;
- diff esperado;
- siete checks en `SUCCESS`;
- ausencia de conversaciones o bloqueos pendientes;
- ausencia de cambios posteriores a la revision.

La fusion humana usa **Squash and merge**. Si cambia `dev`, el head o el diff,
se actualiza lo necesario y se repiten la revision y los checks aplicables.

## Promocion `dev` -> `pre`

- PR con head exacto `dev` y base exacta `pre`;
- siete checks en `SUCCESS`;
- validacion funcional humana;
- ningun cambio funcional nuevo dentro de la promocion;
- comprobacion inmediatamente anterior de refs, head, base y diff sin cambios;
- fusion manual mediante **Create a merge commit**;
- nunca Squash and merge;
- nunca Rebase and merge.

Si `dev`, `pre` o la PR cambian despues de la validacion, se repite la revision.
No se fusiona `pre` de vuelta hacia `dev`. Despues de fusionar debe pasar:

```powershell
git merge-base --is-ancestor origin/dev origin/pre
```

## Promocion `pre` -> `main`

- PR con head exacto `pre` y base exacta `main`;
- siete checks en `SUCCESS`;
- autorizacion humana explicita;
- comprobacion inmediatamente anterior de refs, head, base y diff sin cambios;
- fusion manual mediante **Create a merge commit**;
- nunca Squash and merge;
- nunca Rebase and merge.

Si `pre`, `main` o la PR cambian despues de la autorizacion, se solicita una
nueva autorizacion explicita. No se fusiona `main` de vuelta hacia `pre`.
Despues de fusionar debe pasar:

```powershell
git merge-base --is-ancestor origin/pre origin/main
```

Se prepara un tag cuando corresponda.

## Entrega secuencial

Antes de iniciar una EPIC se consulta GitHub por PR abiertas desde `codex/*` o
`quality/*` hacia la rama de integracion efectiva. Cualquier coincidencia
bloquea una nueva EPIC, con independencia de que sea borrador o de que sus
checks esten verdes, pendientes, rojos o ausentes. La ejecucion solo puede
validar, revisar o informar esa PR pendiente; nunca fusionarla.

Despues de que una persona fusione o cierre la entrega anterior, la siguiente
ejecucion vuelve a la rama efectiva, ejecuta `git fetch origin`, exige arbol
limpio, actualiza solo por fast-forward y demuestra que `HEAD` coincide con el
ref remoto antes de seleccionar trabajo.

## Configuracion esperada del repositorio

- `Allow squash merging`: activado;
- `Allow merge commits`: activado;
- `Allow rebase merging`: desactivado;
- auto-merge nativo de GitHub: no necesario.

La API de GitHub confirmo esos cuatro valores el 2026-08-02. Esta observacion no
equivale a branch protection: GitHub no garantiza que se elija el metodo
correcto, por lo que la persona que fusiona debe aplicar el metodo asociado a
cada tipo de PR.

## Limitaciones y futura proteccion real

En el plan gratuito actual GitHub no bloquea tecnicamente push directo, force
push o borrado mediante rulesets enforced; tampoco garantiza los checks ni el
metodo de fusion. La politica prohibe esas acciones, pero la garantia es
procedimental.

Cuando exista GitHub Pro, Team o equivalente se debe crear una tarea separada
para configurar, probar y evidenciar branch protection y rulesets reales. No se
debe declarar `PROTECTED_ACTIVE` hasta completar esa tarea.

La guia operativa y la evidencia de los ajustes remotos estan en
`docs/33_GITHUB_MAIN_PROTECTION.md`.
