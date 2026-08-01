# Modelo de ramas dev -> pre -> main

Fecha: 2026-08-01
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Estado: `DOCUMENTED_NOT_ACTIVE`

Este documento define la estrategia normativa de ramas. No crea ni configura
ramas o reglas remotas por si mismo. `origin/main`, `origin/dev` y `origin/pre`
existen desde el mismo commit, pero el modelo no esta activo hasta que sus
protecciones y metodos de fusion se configuren, prueben y documenten.

## Ramas permanentes

### `main`

- Es la rama estable y de produccion.
- No admite push directo ni fusion automatica.
- No exige historial lineal y debe permitir merge commits de release.
- Una vez activo el modelo, recibe unicamente promociones desde `pre`.
- Cada promocion requiere una PR de release, validacion humana y autorizacion
  explicita para fusionar mediante **Create a merge commit**.
- Squash and merge y Rebase and merge estan prohibidos para releases.
- Se prepara un tag cuando corresponda al release.

### `pre`

- Es la rama de preproduccion.
- No admite push directo ni fusion automatica.
- No exige historial lineal y debe permitir merge commits de promocion.
- Recibe promociones desde `dev` mediante PR.
- Requiere validacion funcional humana antes de la fusion manual.
- La promocion usa obligatoriamente **Create a merge commit**; Squash and merge
  y Rebase and merge estan prohibidos.
- No se desarrolla funcionalidad directamente en `pre` y una PR de promocion
  no incorpora cambios funcionales nuevos.

### `dev`

- Es la rama de integracion continua una vez activo el modelo.
- No admite push directo.
- Recibe PR desde `codex/<epic>` y `quality/<epic>`.
- Puede exigir historial lineal.
- Usa status checks en modo strict: **Require branches to be up to date before
  merging** esta activado para entregas temporales.
- No requiere aprobacion humana para cada EPIC.
- Autoriza squash and merge automatico exclusivamente cuando los siete
  controles obligatorios estan en `PASS`, la autorrevision esta registrada y
  el head actual coincide con el `expected_head_sha` registrado.
- Una ejecucion procesa como maximo una EPIC o completa una PR pendiente.

## Ramas temporales

`codex/<epic>` y `quality/<epic>`:

- parten de `dev` cuando el modelo esta activo;
- contienen una unica tarea y un unico commit logico;
- no admiten alcance funcional ajeno a la tarea;
- se eliminan solo despues de confirmar su fusion.

## Promociones y controles

### `codex/<epic>` o `quality/<epic>` -> `dev`

La PR requiere validacion tecnica automatica, autorrevision y comprobacion de
`expected_head_sha`. Los siete checks obligatorios son:

1. `Validate repository structure`;
2. `Backend build and tests`;
3. `Frontend build and tests`;
4. `quality-policy`;
5. `backend-verify`;
6. `frontend-verify`;
7. `powershell-parse`.

Solo si los siete concluyen en `SUCCESS`, la autorrevision no detecta un
bloqueo y el SHA esperado sigue siendo el head real se permite squash and merge
automatico. Un check rojo, pendiente o ausente bloquea la fusion.

**Require branches to be up to date before merging** esta activado. Si `dev`
cambia, la rama temporal debe actualizarse con `dev`, repetir los siete checks y
registrar su nuevo head como `expected_head_sha`.

El metodo de entrega es **Squash and merge**. Cada PR contiene una unica tarea y
la rama temporal solo se elimina despues de confirmar la fusion.

### `dev` -> `pre`

- PR de promocion con head exacto `dev` y base exacta `pre`.
- Validacion funcional humana y siete checks en `SUCCESS`.
- Status checks en modo loose: **Require branches to be up to date before
  merging** esta desactivado. Los siete checks siguen siendo obligatorios.
- No se fusiona `pre` de vuelta hacia `dev` para actualizar la PR.
- Ninguna funcionalidad nueva dentro de la promocion.
- Fusion manual mediante **Create a merge commit**.
- Squash and merge y Rebase and merge estan prohibidos.
- `pre` no exige historial lineal.
- Inmediatamente antes de fusionar se vuelven a comprobar el SHA actual de
  `dev`, el SHA actual de `pre`, el head de la PR sin cambios, la base de la PR
  sin cambios desde la revision final, el diff sin cambios inesperados y los
  siete checks en `SUCCESS`.
- Si `dev`, `pre` o la PR cambian despues de la validacion, se detiene la
  promocion y se repite la revision aplicable.
- Despues de fusionar debe cumplirse:

```powershell
git merge-base --is-ancestor origin/dev origin/pre
```

### `pre` -> `main`

- PR de release con head exacto `pre` y base exacta `main`.
- Autorizacion humana explicita y siete checks en `SUCCESS`.
- Status checks en modo loose: **Require branches to be up to date before
  merging** esta desactivado. Los siete checks siguen siendo obligatorios.
- No se fusiona `main` de vuelta hacia `pre` para actualizar la PR.
- Fusion manual mediante **Create a merge commit**.
- Squash and merge y Rebase and merge estan prohibidos.
- `main` no exige historial lineal.
- Inmediatamente antes de fusionar se vuelven a comprobar el SHA actual de
  `pre`, el SHA actual de `main`, el head de la PR sin cambios, la base de la PR
  sin cambios desde la autorizacion, el diff sin cambios inesperados y los
  siete checks en `SUCCESS`.
- Si `pre`, `main` o la PR cambian despues de la autorizacion, se detiene la
  promocion y se solicita una nueva autorizacion explicita.
- Despues de fusionar debe cumplirse:

```powershell
git merge-base --is-ancestor origin/pre origin/main
```

- Preparacion de tag cuando corresponda.

## Entrega secuencial

Antes de iniciar una EPIC se consulta GitHub por PR abiertas desde `codex/*` o
`quality/*` hacia la rama de integracion efectiva. Cualquier coincidencia impide
iniciar otra EPIC, con independencia de que sea borrador o de que sus checks
esten verdes, pendientes, rojos o ausentes. La ejecucion puede dedicarse
unicamente a validar, revisar, informar o completar esa PR pendiente.

Tras cerrar o fusionar la entrega anterior se vuelve a la rama de integracion
efectiva, se ejecuta `git fetch origin`, se exige arbol limpio, se actualiza solo
por fast-forward y se comprueba que `HEAD` coincide con el ref remoto antes de
seleccionar otra EPIC.

## Transicion y activacion

Estado verificado de bootstrap: `origin/main`, `origin/dev` y `origin/pre`
parten de `b669a3fc2a9cd64346f400bbbfaf583cc184ab46`. Esta igualdad no activa el
modelo por si sola.

Mientras las protecciones y metodos de fusion no esten configurados, probados y
documentados:

- el modelo permanece en `DOCUMENTED_NOT_ACTIVE`;
- `main` sigue siendo la rama de integracion efectiva;
- las ramas temporales siguen partiendo de `main` y apuntando a `main` bajo las
  puertas secuenciales existentes;
- no se activa la automatizacion programada para integrar en `dev`.

La activacion exige aplicar y probar las protecciones descritas en
`docs/33_GITHUB_MAIN_PROTECTION.md`, conservar evidencia y declarar el estado
explicitamente `ACTIVE`. Solo entonces `dev` pasa a ser la rama de integracion
efectiva.

## Configuracion fuera del repositorio

Los rulesets, las restricciones de push y las opciones de fusion se configuran
manualmente en GitHub. El repositorio debe permitir Squash merging para entregas
temporales hacia `dev` y Merge commits para promociones `dev` -> `pre` y
releases `pre` -> `main`. Rebase merging no forma parte del flujo acordado. No
debe existir una regla global de historial lineal que impida merge commits en
`pre` o `main`.

Los required status checks usan modo strict solo para entregas temporales hacia
`dev`. Las promociones hacia `pre` y `main` usan modo loose, con los mismos siete
checks obligatorios, para no forzar sincronizaciones inversas de los merge
commits historicos de la rama destino.

Ningun workflow o script del repositorio debe cambiar protecciones, fusionar en
`pre` o `main`, ni declarar el modelo activo sin verificar refs, reglas y
evidencia.

No es necesario fusionar `pre` o `main` de vuelta hacia `dev` cuando las
promociones preservan la ascendencia. Una sincronizacion inversa solo se realiza
si `main` recibe una correccion excepcional que `dev` todavia no contiene.
