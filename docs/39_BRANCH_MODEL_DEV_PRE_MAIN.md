# Modelo de ramas dev -> pre -> main

Fecha: 2026-08-01
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Estado: `DOCUMENTED_NOT_ACTIVE`

Este documento define la estrategia normativa de ramas. No crea ni configura
ramas o reglas remotas por si mismo. El modelo no esta activo hasta que existan
simultaneamente `origin/dev` y `origin/pre`.

## Ramas permanentes

### `main`

- Es la rama estable y de produccion.
- No admite push directo ni fusion automatica.
- Una vez activo el modelo, recibe unicamente promociones desde `pre`.
- Cada promocion requiere una PR de release, validacion humana y autorizacion
  explicita para fusionar.
- Se prepara un tag cuando corresponda al release.

### `pre`

- Es la rama de preproduccion.
- No admite push directo ni fusion automatica.
- Recibe promociones desde `dev` mediante PR.
- Requiere validacion funcional humana antes de la fusion manual.
- No se desarrolla funcionalidad directamente en `pre` y una PR de promocion
  no incorpora cambios funcionales nuevos.

### `dev`

- Es la rama de integracion continua una vez activo el modelo.
- No admite push directo.
- Recibe PR desde `codex/<epic>` y `quality/<epic>`.
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

### `dev` -> `pre`

- PR de promocion sin cambios funcionales nuevos.
- Validacion funcional humana.
- Fusion manual; nunca automatica.

### `pre` -> `main`

- PR de release.
- Autorizacion humana explicita.
- Fusion manual; nunca automatica.
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

Mientras `origin/dev` o `origin/pre` no exista:

- el modelo permanece en `DOCUMENTED_NOT_ACTIVE`;
- `main` sigue siendo la rama de integracion efectiva;
- las ramas temporales siguen partiendo de `main` y sus PR siguen apuntando a
  `main` bajo las puertas secuenciales existentes;
- no se declara que `dev` o `pre` existan.

Despues de integrar esta configuracion, una persona administradora crea
manualmente `dev` y `pre` desde el mismo commit de `main`. La activacion exige
comprobar ambos refs remotos y aplicar las protecciones descritas en
`docs/33_GITHUB_MAIN_PROTECTION.md`. Desde ese momento `dev` pasa a ser la rama
de integracion efectiva. La PR que integra esta documentacion en `main` es parte
del bootstrap previo a la activacion y no autoriza ninguna fusion automatica.

## Configuracion fuera del repositorio

La creacion inicial de ramas, los rulesets, las restricciones de push y las
opciones de fusion se configuran manualmente en GitHub. Ningun workflow o script
del repositorio debe crear ramas permanentes, cambiar protecciones, fusionar en
`pre` o `main`, ni declarar el modelo activo sin verificar los refs remotos.
