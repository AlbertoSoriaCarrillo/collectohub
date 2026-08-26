# EPIC 45B-FIX quality evidence

EPIC: 45B-FIX - Contratos publicos y coherencia de reservas
Commit base: `42c8998608d4bbcb8b241fdbf760ce59f4ab0712`
Rama: `codex/45b-fix-post-review`
Fecha real: 2026-08-26
Archivos modificados: inventario/reservas backend, regresiones, consumidores
frontend publicos, contrato API, exports y documentos de estado/evidencia
Archivos fuera de alcance: ninguno
Criterios: tres defectos tardios de 45B, privacidad publica, coherencia de
referencias, prioridad de nombre, compatibilidad legacy/editorial y entrega
supervisada
Pruebas nuevas: 9 casos backend
Pruebas modificadas: regresiones JSON/servicio y 2 consumidores frontend
Pruebas eliminadas: 0
Pruebas ignoradas nuevas: 0

## Defectos y resultado

1. `PublicShopProductResponse` exponia `stockQuantity`: eliminado del DTO, del
   mapping, del tipo frontend y de las dos vistas publicas. `notes`, auditoria y
   stock quedan ausentes en JSON MVC; el DTO gestionado conserva stock/notas.
2. Reservas priorizaba el master legacy al decidir la referencia publica: la
   regla se centraliza en `ShopProduct.hasPublicReference()` y se comparte con
   inventario. Master inactivo + editorial publica es reservable; referencias
   completamente no publicas y edicion no publica se rechazan.
3. `ReservationResponse.productName` priorizaba legacy: ahora usa el primer
   valor no vacio en orden edicion, item y master, con referencias nullable.

`availableQuantity` no se introduce: requiere `activeHeldQuantity` y holds
transaccionales todavia no implementados, reservados a 45G.

## Regresion y validacion dirigida

- Estado previo backend:
  `mvnw -Dtest=InventoryControllerSecurityTest,ReservationServiceTest test`
  demostro los tres defectos mediante fallos esperados.
- Estado previo frontend:
  `npm test -- --watch=false --include=...shop-product-detail.component.spec.ts`
  fallo porque la vista mostraba `3 in stock`.
- Backend dirigido final:
  `mvnw -Dtest=InventoryServiceTest,InventoryControllerSecurityTest,ReservationServiceTest,ReservationControllerSecurityTest test`.
  Resultado: 79 tests, 0 fallos, 0 errores, 0 omitidos.
- Frontend dirigido final: `npm test -- --watch=false` con los dos `--include`
  de `inventory.service.spec.ts` y `shop-product-detail.component.spec.ts`.
  Resultado: 2 archivos, 9 tests, 0 fallos.

## Matriz completa local

- backend: `backend\\mvnw.cmd clean verify`
- tests backend: 466
- fallos backend: 0
- errores backend: 0
- omitidos backend: 4 `SKIPPED_WITH_REASON`; Docker Desktop no estaba activo y
  los tests Testcontainers configurados para esa ausencia se omitieron. Esta FIX
  no modifica persistencia, consultas ni migraciones.
- Testcontainers: `SKIPPED_WITH_REASON: Docker Desktop no disponible`
- frontend `npm ci`: PASS; 474 paquetes instalados, 475 auditados
- vulnerabilidades npm conocidas: 23 (2 bajas, 2 moderadas, 18 altas, 1 critica);
  no se ejecuto `npm audit fix`
- frontend tests: 59 archivos, 244 tests, PASS
- frontend build: PASS
- warning frontend: bundle inicial 631.54 kB, 131.54 kB sobre budget
- E2E/Playwright: `SKIPPED_WITH_REASON: exclusion expresa de la EPIC`
- migraciones: 0; base vacia/upgrade `SKIPPED_WITH_REASON: sin diff de esquema`
- dependencias/manifests/lockfiles: 0
- scripts/workflows/Docker: 0
- QA manual: `SKIPPED_WITH_REASON: regresiones de contrato cubiertas en MVC,
  servicio y componentes; E2E excluido`

## Seguridad y privacidad

- actores: visitante publico, miembro activo de tienda y usuario autenticado
- positivas: inventario publico legacy/editorial, inventario gestionado con
  stock/notas, reservas legacy, item puro, item+edicion y bridge
- negativas: JSON publico sin stock/notas/auditoria; referencias no publicas y
  edicion no publica no son reservables
- secretos, tokens, credenciales o datos personales introducidos: 0

## Entrega

Resultado local previo al verificador: `PASS`
Verificador completo contra `origin/dev`: `PASS`; politica, parser, backend,
frontend tests y build correctos sobre el diff documental final
Commit: pendiente
Push: pendiente
PR: pendiente
Checks remotos: se registran en la PR sobre el SHA final; no se afirman dentro
de este commit
Riesgos: `availableQuantity`, holds, locks e idempotencia siguen pendientes de
45G. Esta FIX no corrige la promocion `dev -> main` realizada mediante PR #21.
Siguiente tarea: reconciliacion independiente del estado 45C y del flujo
`dev -> pre -> main`
