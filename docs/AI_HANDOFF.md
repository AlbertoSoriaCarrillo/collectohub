# CollectoHub - AI Handoff

Última actualización verificada: 2026-08-26
Repositorio: `AlbertoSoriaCarrillo/collectohub`
Rama de integracion efectiva: `dev` en
`SUPERVISED_ACTIVE_NO_ENFORCEMENT`.

Estado de entrega: 45B esta historicamente integrada, pero una revision tardia
reabrio su cierre mediante EPIC 45B-FIX. La rama
`codex/45b-fix-post-review` corrige la exposicion publica de stock fisico, la
incoherencia de referencias reservables y la prioridad legacy de
`ReservationResponse.productName`; requiere integracion humana en `dev`.
Las EPIC 45C-A a 45C-D estan integradas, pero 45C permanece abierta y no se
reconcilia en esta FIX.

## 1. Propósito de este documento

Este archivo permite continuar el proyecto en un chat nuevo sin depender del historial completo de conversaciones.

Antes de proponer una nueva tarea, la IA debe revisar siempre GitHub y confirmar:

1. El último commit real de `main`.
2. Los archivos modificados en la última EPIC.
3. Que los tests y la documentación estén completos.
4. Que no existan huecos que obliguen a una EPIC de cierre o corrección.
5. La siguiente tarea indicada por el backlog y los documentos de diseño.

No debe asumir que la salida local de Codex ya está publicada hasta verla en GitHub.

## 2. Forma de trabajo acordada

El flujo habitual es:

1. Alberto ejecuta en Codex el prompt de una EPIC.
2. Codex modifica el repositorio, valida, hace commit y push.
3. En un chat nuevo, Alberto pide: `revisa GitHub y dame el siguiente prompt`.
4. ChatGPT revisa el último commit y su diff.
5. Si la EPIC está incompleta, propone una EPIC `FIX` antes de avanzar.
6. Si está cerrada, genera el prompt completo de la siguiente EPIC.

Cada respuesta debe incluir:

- último commit detectado;
- valoración breve de si la EPIC está bien cerrada;
- huecos o riesgos reales;
- modelo recomendado para Codex;
- nivel de razonamiento;
- prompt completo y acotado;
- archivos a leer;
- archivos o módulos que no deben tocarse;
- comandos de validación;
- commit esperado;
- siguiente tarea prevista.

## 3. Preferencias para los prompts de Codex

Usar siempre:

```text
Continúa CollectoHub.

Modo ahorro de tokens.

No leas todo el repositorio. Lee únicamente los archivos y coincidencias indicados.
```

Modelos recomendados:

- `GPT-5.6 Terra + razonamiento Alto`: opción normal para la mayoría de EPICs.
- `GPT-5.6 Sol + razonamiento Alto`: seguridad, migraciones, concurrencia, errores complejos o refactors delicados.
- `GPT-5.6 Luna + razonamiento Medio`: documentación o cambios pequeños y claramente acotados.

No usar razonamiento extra alto salvo bloqueo técnico real.

## 4. Restricciones permanentes

No hacer salvo petición expresa:

- E2E.
- Playwright.
- cambios fuera de la EPIC actual;
- refactors generales no necesarios;
- nuevas dependencias sin justificación;
- `npm audit fix`;
- `npm audit fix --force`;
- `docker compose down -v`;
- `docker volume prune`;
- `docker system prune --volumes`;
- borrado de datos o volúmenes;
- backfills silenciosos;
- migraciones destructivas;
- cambios de contratos no documentados;
- sustituciones globales de seguridad;
- implementar varias EPICs en un mismo commit.

Los E2E/Playwright están pospuestos hasta que los recorridos completos de usuario estén maduros.

## 5. Comandos habituales de validación

### Backend

```powershell
cd C:\Users\Alber\Desktop\collectohub\backend
.\mvnw.cmd clean verify
```

### Frontend

```powershell
cd C:\Users\Alber\Desktop\collectohub\frontend
npm.cmd ci
npm.cmd test -- --watch=false
npm.cmd run build
```

### Revisión final

```powershell
cd C:\Users\Alber\Desktop\collectohub
git diff --check
git status
git log -3 --oneline
```

### Docker, solo cuando sea necesario

```powershell
cd C:\Users\Alber\Desktop\collectohub\infra
docker compose down
docker compose up --build -d
docker compose ps
curl.exe http://localhost:8080/api/health
docker compose down
```

Nunca borrar volúmenes salvo petición explícita.

### 5.1 Politica permanente de calidad y entrega

Desde EPIC QUALITY-A, cualquier agente debe leer `AGENTS.md` y aplicar la matriz
de `docs/32_QUALITY_GATES.md`. El modelo normativo de ramas esta en
`docs/39_BRANCH_MODEL_DEV_PRE_MAIN.md` y el procedimiento supervisado en
`docs/33_GITHUB_MAIN_PROTECTION.md`.

El estado actual es `SUPERVISED_ACTIVE_NO_ENFORCEMENT`: `dev` es la rama de
integracion efectiva, GitHub no aplica branch protection ni rulesets enforced,
y todos los controles dependen del procedimiento de Codex y revision humana.
No se debe describir ninguna rama como tecnicamente protegida. El push directo
esta prohibido por politica aunque GitHub no pueda impedirlo.

El commit de activacion `5f5c45c6cec89e442c246508eb421ac641f8a967` esta
presente simultaneamente en `origin/main`, `origin/dev` y `origin/pre`. La
primera ejecucion supervisada se completo correctamente con arbol limpio,
fast-forward exclusivo de `dev`, cero PR de entrega, cero cambios de producto y
cero commit, push, PR o fusion. Se detuvo porque la siguiente EPIC aun no estaba
seleccionada.

La automatizacion nunca fusiona. El horario automatico no se activa en este
cierre y requiere autorizacion separada.

Despues de activar, `codex/<epic>` y `quality/<epic>` parten de `origin/dev`
actualizado y apuntan a `dev`. Los siete checks, la autorrevision y
`expected_head_sha` son obligatorios. Codex informa `HUMAN_MERGE_REQUIRED` y
termina; una persona vuelve a comprobar head, base `dev`, diff, checks,
conversaciones y cambios posteriores antes de usar **Squash and merge**.

Las promociones `dev -> pre` y `pre -> main` conservan los siete checks y
requieren head/base exactos, validacion funcional o autorizacion humana segun
corresponda, y **Create a merge commit**. Squash y rebase estan prohibidos entre
ramas permanentes. Cualquier cambio posterior obliga a repetir la revision y,
para `pre -> main`, a obtener nueva autorizacion.

Despues de promocionar se demuestra la ascendencia con
`git merge-base --is-ancestor origin/dev origin/pre` o
`git merge-base --is-ancestor origin/pre origin/main`. No se fusiona la rama
destino de vuelta hacia la fuente para actualizar una PR.

Antes de otra EPIC se consultan PR abiertas de entrega hacia la rama efectiva.
Una coincidencia impide iniciar trabajo nuevo. Una ejecucion procesa como
maximo una EPIC o una PR pendiente, nunca fusiona y termina despues de informar.

## 6. Resumen funcional del producto

CollectoHub es una plataforma de coleccionismo con:

- usuarios normales;
- creadores de contenido;
- tiendas profesionales;
- colecciones personales;
- catálogo editorial común;
- items, ediciones, creadores y relaciones;
- componente social futuro;
- marketplace, pagos y móvil en fases posteriores.

Tipos de usuario de negocio:

- `USER`;
- `CONTENT_CREATOR`;
- `SHOP_OWNER`.

Roles administrativos:

- `ADMIN`: superusuario global;
- `EDITORIAL_ADMIN`: administración del catálogo editorial sin privilegios globales.

`CONTENT_CREATOR`, `SHOP_OWNER` y `USER` no equivalen a administrador editorial.

## 7. Estado global del roadmap

### MVP1

Cerrado. Base funcional de usuarios, colecciones, tiendas, inventario, reservas y recomendaciones.

### MVP2

Cerrado con limitaciones. Catálogo editorial común, items, ediciones, creadores, créditos, relaciones y enlaces con productos legacy.

### MVP3

Admin editorial avanzado implementado de forma parcial pero utilizable:

- shell y CRUD editorial;
- publishers;
- franchises;
- series;
- items;
- editions;
- creators;
- credits;
- relationships;
- master product links;
- datos demo;
- validaciones;
- reporte de calidad editorial;
- rol `EDITORIAL_ADMIN`;
- guard y navegación frontend;
- provisionamiento controlado por script.

Los E2E continúan pospuestos.

### MVP4

Cerrado con limitaciones: alta editorial, edicion opcional, items manuales,
enlace posterior, WANTED/OWNED, missing calculado, privacidad, propiedad,
filtros, ordenaciones, progreso, compatibilidad legacy y recorrido integral
demostrados. Estado: `MVP4_CLOSED_WITH_LIMITATIONS`; no equivale a producto
final completo.

### MVP5

EPIC 45A esta integrada. 45B esta historicamente integrada, pero no debe
considerarse correctamente cerrada hasta integrar 45B-FIX. Las fases 45C-A a
45C-D incorporan lectura
backend protegida de miembros activos para OWNER/MANAGER, alta acotada, cambio
seguro de rol y desactivacion auditada solo para OWNER, sin datos personales.
Perfil profesional, compatibilidad `STAFF` y cierre del esquema aditivo deben
definirse como EPICs independientes antes de continuar.

## 8. EPICs recientes completadas

Resumen relevante:

- EPIC 42C + FIX + FIX2: reporte avanzado de calidad editorial.
- EPIC 43A: diseño de roles editoriales.
- EPIC 43B: backend `EDITORIAL_ADMIN`.
- EPIC 43C: guard y navegación frontend.
- EPIC 43D: provisionamiento controlado por PowerShell y `psql`.
- EPIC 44A: diseño y auditoría de MVP4.
- EPIC 44B: contrato backend de `collection_items` y compatibilidad editorial.
- EPIC 44B-FIX: cierre de tests de privacidad, `referenceKind`, documentación y exports.
- EPIC 44C: flujo frontend de creación y edición de colecciones.
- EPIC 44D a 44G-D-FIX: alta editorial, items manuales, progreso, detalle final
  y regresion.
- EPIC 44H-A a 44H-C: dataset, validacion integral y evidencia de cierre MVP4.
- Activacion supervisada: alineacion de `main`/`dev`/`pre` y primera ejecucion
  supervisada completadas.

## 9. Estado actual verificado

Fecha de verificacion: 2026-08-02

`origin/main`, `origin/dev` y `origin/pre` contienen el commit de activacion
`5f5c45c6cec89e442c246508eb421ac641f8a967`. El estado operativo es
`SUPERVISED_ACTIVE_NO_ENFORCEMENT` y `dev` es la rama de integracion efectiva.

La primera ejecucion supervisada comprobo remoto correcto, arbol limpio,
alineacion de refs, fast-forward exclusivo de `dev` y cero PR abiertas desde
`codex/*` o `quality/*` hacia `dev`. No hizo cambios de producto, commit, push,
PR ni fusion; se detuvo porque la siguiente EPIC no estaba seleccionada.

MVP4 queda `MVP4_CLOSED_WITH_LIMITATIONS`, con evidencia en
`docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md` y
`docs/38_44H_C_QUALITY_EVIDENCE.md`. Se conserva como limitaciones
E2E/Playwright, imagenes y almacenamiento real, `quantity` frente a ejemplares
separados, paginacion avanzada, decisiones de taxonomia y MISSING
legacy/persistido, produccion, social, marketplace y pagos.

EPIC 45A deja la auditoria y el diseno ejecutable de MVP5. 45B esta integrada
historicamente, pero queda reabierta hasta integrar 45B-FIX: el DTO publico aun
exponia `stockQuantity`, reservas no compartia la regla de referencia publica y
el nombre de reserva priorizaba legacy sobre edicion/item.
Las fases 45C-A a 45C-D estan integradas en `dev`; la PR #19 fue fusionada
manualmente como `0395b6d8ab6786b406ad9c0e772b09571b252f12` y su arbol coincide
con el head validado `dc8b1cabdef1d674513ababc13a696ae28393d4f`. Al iniciar
45B-FIX no habia otra entrega abierta hacia `dev`. Antes de continuar se debe elegir y definir como
EPIC independiente uno de los alcances restantes de 45C. El horario automatico
no se activa en este cierre.

La API de GitHub confirmo repositorio privado, Squash merging y merge commits
activados, Rebase merging y auto-merge desactivados. Los endpoints de rulesets
y branch protection responden que se requiere GitHub Pro o repositorio publico;
por tanto no existe garantia tecnica de proteccion en el plan actual.

### Registro historico conservado

Fecha: 2026-07-15

Último commit previo a publicar EPIC 44E-B2A-FIX:

```text
f5e870cad152ab7589d3245f8f19f44098e85043
test: complete manual collection item frontend coverage
```

Base verificada para EPIC 44H-C-FIX:

```text
2cb6a68c2c95dc707f201b99eb6d05798ea285ba
fix: resolve mvp4 demo summary path on powershell 5
```

EPIC 44G-D-FIX, EPIC 44H-A y EPIC QUALITY-A estan publicadas. EPIC
QUALITY-A-FIX2 esta integrada en la base anterior. La PR #2 de
`quality/quality-gates` fue fusionada en `main` como `3cb789b7`; los jobs
`quality-policy`, `backend-verify`, `frontend-verify` y `powershell-parse`, junto
con el workflow CI anterior, terminaron correctamente sobre el head
`ea71a5918f917c4b7fd2e5a9eba9759044cd4c19`. La configuracion remota de
proteccion de `main` sigue siendo una accion administrativa no verificada desde
este repositorio.

EPIC 44H-B y EPIC 44H-B-FIX estan integradas en `main`. La primera ejecucion
real de 44H-C detecto que Windows PowerShell 5.1 convertia una pagina con
`content=[]` en `null`, bloqueando la busqueda-creacion antes del catalogo. EPIC
44H-C-FIX corrige en `codex/44h-c-fix` el contrato de colecciones vacias y
conserva la deteccion de ambiguedad. No ejecuta el escenario real, API, DB ni UI.

EPIC 44H-C-FIX fue integrada en `main` como
`309d5db6069354577ffc606620dd33307063ad19`. EPIC 44H-C se ejecuto sobre esa
base y el escenario local `44hc3`: doble ejecucion idempotente, API/DB,
privacidad, propiedad, filtros, ordenaciones, progreso, restauracion y recorrido
UI humano estan en PASS. La credencial temporal se roto al terminar y las
variables demo quedaron ausentes en Process y User.

Estado funcional al publicar 44H-C:

```text
MVP4_PARTIALLY_CLOSED
```

Evidencia: `docs/31_MVP4_PARTIAL_CLOSURE_REVIEW.md` y
`docs/38_44H_C_QUALITY_EVIDENCE.md`. La siguiente tarea funcional no queda
seleccionada automaticamente: primero debe revisarse e integrarse la PR de
44H-C y despues reconciliar GitHub, `main` y el backlog. La IA debe volver a
comprobar GitHub antes de asumir que este estado sigue vigente.

## 10. Decisiones vigentes de MVP4

Documento principal:

```text
docs/26_MVP4_COLLECTIONS_DESIGN.md
```

Decisiones cerradas:

1. `catalogItemId` es la identidad canónica de nuevas altas catalogadas.
2. `catalogItemEditionId` es opcional.
3. Una edición debe pertenecer al item seleccionado.
4. `masterProductId` se mantiene temporalmente como compatibilidad legacy.
5. Los bridges `VERIFIED` pueden enriquecer o validar referencias legacy.
6. No se hace backfill silencioso al leer.
7. Los datos editoriales permanecen en el catálogo global.
8. Los datos personales permanecen en `collection_items`.
9. `notes` y `acquiredAt` son privados y solo los recibe el propietario.
10. Los roles administrativos no sustituyen la propiedad para acceder a datos privados.
11. `referenceKind` se calcula y no se persiste.
12. Los items manuales se implementarán en una EPIC posterior.
13. `WANTED` será intención explícita.
14. `OWNED` será posesión real.
15. `MISSING` será un resultado calculado futuro, no equivalente a `WANTED`.
16. El estado persistido legacy `MISSING` se mantiene hasta una transición explícita.
17. Durante MVP4, las copias físicas se representan inicialmente como entradas separadas; `quantity` sigue abierto.
18. La edición frontend compara siempre `collection.userId` con el usuario autenticado; los roles administrativos no sustituyen la propiedad.
19. Crear usa `null` para opcionales vacíos; editar usa `""` para solicitar el vaciado de `description` o `categoryCode`.
20. La alta catalogada usa `catalogItemId` y una edición opcional obtenida solo del detalle del item; no envía enlaces legacy implícitos.
21. EPIC 44E-A1 añade el contrato de item manual en esquema, entidad y DTOs;
    la API seguirá rechazando altas manuales hasta EPIC 44E-A2.
22. El enlace posterior de un item manual al catálogo queda reservado para
    EPIC 44E-A3; no hay backfill ni transición automática.
23. El PUT de un item manual envía solo metadata manual y datos personales;
    `""` solicita vaciar description o type, mientras que no enviar un campo
    conserva su valor.
24. La ruta frontend de edición exige que `collection.userId` coincida con el
    usuario autenticado antes de cargar o enviar items; los roles no conceden
    esta propiedad.
25. EPIC 44E-B2B debe limitarse al selector y la llamada visual de enlace; no
    debe cambiar el contrato ya validado ni iniciar E2E o Playwright.

## 11. Clasificación actual de referencias de collection items

Valores actuales de `referenceKind`:

- `DIRECT_CATALOG`;
- `VERIFIED_BRIDGE`;
- `LEGACY_UNRESOLVED`;
- `MANUAL`;
- `INVALID_REFERENCE`.

Reglas principales:

- referencia editorial directa: `catalogItemId` presente;
- edición opcional y perteneciente al item;
- referencia legacy: `masterProductId` sin catálogo directo;
- referencia manual: source `MANUAL`, title no vacío y sin referencias;
- referencia dual solo cuando no contradice un bridge `VERIFIED`;
- datos privados sanitizados en lecturas públicas;
- propietario recibe respuesta completa.

## 12. Cierre actual de MVP4

EPIC 44A a 44H-C estan completadas. MVP4 queda
`MVP4_CLOSED_WITH_LIMITATIONS`, no como producto final completo. Estan
demostrados alta editorial, edicion opcional, items manuales, enlace posterior,
WANTED/OWNED, missing calculado, privacidad, propiedad, filtros, ordenaciones,
progreso, compatibilidad legacy y recorrido integral con UI humana.

Permanecen como limitaciones E2E/Playwright, imagenes y almacenamiento real,
`quantity` frente a ejemplares separados, paginacion avanzada, decisiones de
taxonomia y MISSING legacy/persistido, produccion, social, marketplace y pagos.

## 13. EPIC 45C en curso

EPIC 45A audita la base real y deja el diseno ejecutable de MVP5 en
`docs/40_MVP5_SHOPS_INVENTORY_RESERVATIONS_DESIGN.md`. Identifica como
prerrequisitos la separacion de DTO publicos/internos, la sanitizacion de notas,
la compatibilidad de reservas con inventario editorial puro y la posterior
consistencia transaccional de stock. No implementa funcionalidad.

45B introdujo los contratos seguros de inventario y reservas editoriales, pero
su revision tardia detecto tres defectos que 45B-FIX corrige en la rama de
entrega actual: `PublicShopProductResponse` deja de exponer `stockQuantity`,
inventario y reservas comparten la misma regla OR de referencia publica y
`ReservationResponse.productName` usa el primer nombre no vacio en orden
edicion, item y legacy. `availableQuantity` permanece diferida hasta que 45G
implemente holds y su calculo real. 45C
anade `GET /api/shops/{shopId}/members`: devuelve memberships activas en
orden de id solo a OWNER/MANAGER activos. Tambien incorpora
`POST /api/shops/{shopId}/members` para que solo un OWNER activo agregue una
cuenta activa existente por email normalizado como MANAGER o EMPLOYEE. Las
respuestas no incluyen email, nombre ni otros campos personales; los duplicados
se rechazan con conflicto estable.

El cambio de rol mediante `PUT /api/shops/{shopId}/members/{memberId}/role` esta
integrado. Tambien esta integrada la desactivacion mediante
`DELETE /api/shops/{shopId}/members/{memberId}`: solo un OWNER activo puede
desactivar una membership MANAGER o EMPLOYEE activa de la misma tienda. Conserva
la fila como `INACTIVE`, registra `updatedBy` y devuelve `204` sin datos
personales; el OWNER no puede desactivarse por este contrato.

45C permanece abierta. No se han implementado todavia perfil profesional,
compatibilidad `STAFF`, transferencia de ownership ni el cierre del esquema
aditivo. El siguiente alcance no esta seleccionado: debe definirse como una EPIC
independiente antes de implementar cambios.

Esta FIX no corrige la promocion `dev -> main` realizada mediante PR #21. La
reconciliacion de 45C y del flujo `dev -> pre -> main` es una tarea posterior e
independiente.

## 14. Documentos prioritarios para reconstruir contexto

Leer primero:

```text
docs/AI_HANDOFF.md
docs/08_NEXT_BACKLOG.md
docs/12_TASK_LOG.md
docs/13_DECISIONS.md
docs/16_MVP_API_ENDPOINTS.md
docs/19_MVP_STATUS.md
docs/26_MVP4_COLLECTIONS_DESIGN.md
```

Cuando la tarea afecte a exports:

```text
docs/export/README.md
docs/export/backend-endpoints.md
docs/export/backend-endpoints.csv
docs/export/frontend-routes.md
docs/export/frontend-routes.csv
docs/export/frontend-backend-map.md
docs/export/frontend-backend-map.csv
```

No actualizar exports que no hayan cambiado realmente.

## 15. Cómo revisar una EPIC antes de avanzar

La IA debe comprobar:

1. El commit existe en GitHub.
2. El diff corresponde al alcance solicitado.
3. No hay archivos inesperados.
4. Se añadieron los tests prometidos.
5. Los tests verifican la lógica real y no solo adaptan constructores.
6. La documentación y exports necesarios están actualizados.
7. No hay migraciones o endpoints fuera de alcance.
8. La siguiente EPIC está registrada en backlog.
9. No existen fallos de seguridad o privacidad.
10. No se debe crear una EPIC `FIX` salvo que haya un hueco real y concreto.

## 16. Formato recomendado para la respuesta de ChatGPT

```text
He revisado GitHub.

Último commit:
<sha corto> - <mensaje>

Valoración:
- qué quedó bien;
- huecos encontrados;
- si la EPIC puede cerrarse.

Modelo recomendado:
GPT-5.6 Terra/Sol/Luna
Razonamiento: Bajo/Medio/Alto/Extra alto

Siguiente prompt:
<prompt completo para Codex>
```

## 17. Actualización de este archivo

Al terminar cada EPIC, Codex debe actualizar como mínimo:

- fecha de última actualización;
- último commit verificado;
- EPICs completadas;
- siguiente tarea;
- decisiones nuevas;
- restricciones nuevas;
- cambios de roadmap.

No convertir este archivo en un registro histórico exhaustivo. Para el historial detallado ya existe:

```text
docs/12_TASK_LOG.md
```

Este documento debe mantenerse breve, operativo y centrado en el estado necesario para continuar.

## 18. Prompt inicial para un chat nuevo

```text
Continuamos el proyecto CollectoHub.

Repositorio:
AlbertoSoriaCarrillo/collectohub

Revisa GitHub y lee primero:

docs/AI_HANDOFF.md
docs/08_NEXT_BACKLOG.md
docs/12_TASK_LOG.md
docs/19_MVP_STATUS.md
docs/26_MVP4_COLLECTIONS_DESIGN.md

Trabajaremos igual que hasta ahora:

- revisa el último commit real;
- comprueba que la última EPIC está correctamente cerrada;
- detecta huecos antes de avanzar;
- dame el siguiente prompt completo para Codex;
- recomienda Terra, Sol o Luna;
- indica razonamiento bajo, medio, alto o extra alto;
- usa modo ahorro de tokens;
- no E2E/Playwright hasta que los recorridos sean maduros;
- no npm audit fix;
- no comandos Docker destructivos;
- no asumas que una salida local está publicada hasta verla en GitHub.

Estado esperado al continuar:

- modo `SUPERVISED_ACTIVE_NO_ENFORCEMENT`;
- `dev` como rama de integracion efectiva;
- MVP4 `MVP4_CLOSED_WITH_LIMITATIONS`;
- EPIC 45A integrada y 45B reabierta hasta integrar 45B-FIX;
- EPIC 45C-A a 45C-D integradas, con 45C aun abierta;
- reconciliacion de 45C y del flujo `dev -> pre -> main` pendiente y separada;
- horario automatico no activado;
- ninguna automatizacion puede fusionar.

Empieza revisando GitHub.
```
