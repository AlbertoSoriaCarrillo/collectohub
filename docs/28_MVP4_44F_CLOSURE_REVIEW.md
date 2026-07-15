# MVP4 EPIC 44F Closure Review

## 1. Resumen ejecutivo

Estado final: `EPIC_44F_CLOSED`. OWNED y WANTED son intenciones persistidas;
MISSING es calculado por coleccion y serie.

## 2. Alcance y decisiones

44F cubre progreso owner-only, precedencia OWNED sobre WANTED sobre MISSING,
compatibilidad con MISSING legacy y transicion sobre la misma fila. No cubre
filtros, ordenacion, progreso publico ni quantity.

## 3. Backend, frontend, seguridad y legacy

El backend devuelve progreso calculado sin notas ni fechas personales y exige
propiedad. El frontend agrupa estados, protege la ruta y recarga el estado
canonico tras la transicion. Una fila MISSING legacy sigue siendo legible, pero
las nuevas escrituras MISSING son rechazadas. Referencias directas y VERIFIED
bridge participan; manual sin enlace y legacy sin bridge no participan.

## 4. Datos demo y validaciones

El demo legacy no persiste MISSING. El nuevo script MVP4 crea un escenario de
tres items: OWNED, WANTED y MISSING calculado, valida 1/1/1 y 33%, no guarda
secretos y tiene `-WhatIf`. La validacion real depende de backend, PostgreSQL y
ADMIN locales; el parser y WhatIf se validaron sin llamadas HTTP.

## 5. Matriz de aceptacion

| Requisito | Estado | Evidencia | Limitacion |
| --- | --- | --- | --- |
| OWNED y WANTED explicitos | OK | API y UI existentes | Ninguna |
| MISSING calculado / no nuevas filas | OK | Servicio, validaciones y demos | Legacy permanece |
| Precedencia y legacy | OK | Tests backend | MISSING legacy solo lectura inicial |
| Referencias directas y bridge | OK | Tests de progreso | Manual/legacy sin bridge excluidos |
| Progreso owner-only y privacidad | OK | Endpoint y tests MVC | No publico |
| Transicion, multiples WANTED y errores | OK | UI y tests Angular | Sin selector complejo |
| Ruta protegida y exports | OK | Exports auditados | Ninguna |
| Datos demo | OK | Scripts y WhatIf | `VALIDATED_WITH_WHATIF_ONLY` |
| Tests y build | OK | Regresion de esta EPIC | Docker puede omitir Testcontainers |

## 6. Deuda, fuera de alcance y siguiente tarea

Permanece el warning de bundle y las vulnerabilidades npm conocidas. No se
ejecutaron E2E ni Playwright. Siguiente tarea: EPIC 44G-A - Auditoria y diseno
del detalle final, filtros y ordenacion.
