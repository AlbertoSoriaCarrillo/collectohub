# MVP4 closure review - EPIC 44H-C and supervised closeout

Fecha: 2026-08-01

Estado actual: `MVP4_CLOSED_WITH_LIMITATIONS`

## Alcance demostrado

EPIC 44H-C completo la validacion integral definida en
`docs/30_MVP4_DEMO_VALIDATION_DESIGN.md` sobre el escenario local `44hc3`.
Su estado al publicarse fue `MVP4_PARTIALLY_CLOSED`. La decision humana de
2026-08-02 reconoce ahora ese alcance como `MVP4_CLOSED_WITH_LIMITATIONS`, sin
declarar el producto final completo ni incorporar funcionalidad nueva.

Base: `309d5db6069354577ffc606620dd33307063ad19`

Rama: `codex/44h-c`

Servicios: PostgreSQL, backend y frontend healthy en el Docker Compose local de
este repositorio.

## Decision de cierre

Queda demostrado:

- alta editorial con edicion opcional;
- items manuales y enlace posterior al catalogo;
- transicion WANTED/OWNED y missing calculado;
- privacidad y propiedad;
- filtros, ordenaciones y progreso;
- compatibilidad legacy;
- escenario integral y recorrido UI humano.

El cierre conserva como limitaciones no bloqueantes:

- E2E/Playwright pospuestos;
- imagenes y almacenamiento real;
- `quantity` frente a ejemplares separados;
- paginacion avanzada;
- decisiones restantes de taxonomia y MISSING legacy/persistido;
- produccion, social, marketplace y pagos fuera de MVP4.

## Dataset e idempotencia

- La misma ejecucion real del escenario `44hc3` se completo dos veces durante
  44H-C.
- La segunda pasada y las pasadas de revalidacion reutilizaron el dataset
  compatible y crearon cero usuarios, colecciones o items adicionales.
- En este cierre posterior al recorrido UI no se volvio a ejecutar el dataset,
  por prohibicion expresa.
- Se conservaron las personas 49, 50 y 51, las colecciones 17 y 18 y los items
  15 a 20. Las cuentas 47 y 48 no se modificaron.
- La comprobacion DB final confirma tres items editoriales ACTIVE, bridge
  VERIFIED, legacy sin resolver y cero filas persistidas para D3 calculado.

Resultado de idempotencia: PASS.

## API y base de datos

- ID 49: login PASS; `/api/users/me` devuelve ADMIN y USER.
- ID 50: login PASS; `/api/users/me` devuelve USER.
- ID 51: login PASS; `/api/users/me` devuelve USER.
- Credencial deliberadamente incorrecta: HTTP 401 PASS.
- Coleccion 17: PUBLIC, owner 50, cinco filas activas con identidades directa,
  manual, bridge verificado y legacy.
- Coleccion 18: PRIVATE, owner 50, una fila manual activa.
- Items 15 a 20 y datos de catalogo asociados permanecen activos.
- D3 aparece como MISSING calculado y no existe como fila de coleccion.
- Filtros, cinco ordenaciones, resumen, propiedad, privacidad y progreso se
  validaron en la ejecucion integral de 44H-C.

Resultado API/DB: PASS.

## Recorrido UI humano

El usuario completo el recorrido manual el 2026-08-01 y comunico PASS.

Owner ID 50:

- login y acceso a las colecciones publica 17 y privada 18;
- resumen de cinco entradas: dos OWNED, dos WANTED y una restante;
- busqueda con resultados, vacio filtrado y ordenaciones;
- cards directa, manual, bridge y legacy;
- progreso inicial 33 %, transicion de D2 a OWNED con 67 % y restauracion a
  WANTED con 33 %;
- vista `/wanted`.

Reader ID 51:

- login y lectura de la coleccion publica 17;
- notes, fecha de adquisicion y acciones de propietario ocultas;
- coleccion privada 18 ausente de listados y acceso directo denegado;
- progreso owner-only denegado;
- estado de error, reintento y cierre de sesion.

Resultado UI humana: PASS.

## Rotacion final local

Tras el recorrido UI se genero en memoria una credencial fuerte independiente
para el operador y otra credencial logica para ambos usuarios demo. El
`BCryptPasswordEncoder` real produjo tres hashes independientes. Una unica
transaccion local actualizo exclusivamente `password_hash` de los IDs 49, 50 y
51 y exigio exactamente tres filas afectadas.

La transaccion verifico cero inserciones, cero eliminaciones y ausencia de
cambios en identidades, roles, estado, timestamps, propiedad, colecciones,
items y cuentas 47/48. Los tres logins, `/api/users/me` y el negativo 401
pasaron despues del commit. Las variables locales de credenciales demo quedaron
ausentes en los ambitos Process y User. No se conservaron valores, hashes ni
tokens en archivos, logs o documentacion.

Resultado de rotacion: PASS.

## Regresion y calidad

- Prueba PowerShell especifica: PASS.
- Parser: 56 archivos `.ps1`, PASS.
- Backend: 424 tests, 0 fallos, 0 errores, 0 omitidos; Testcontainers/PostgreSQL
  PASS.
- Frontend: `npm ci` PASS; 59 archivos y 244 tests PASS; build PASS.
- Warning de build conocido: bundle inicial 631.54 kB, 131.54 kB por encima del
  budget de 500 kB.
- Warnings npm observados: `@angular/animations` deprecado y cuatro scripts de
  instalacion pendientes de revision en `allowScripts`. El baseline historico
  de vulnerabilidades no se reclasifico ni se ejecuto `npm audit fix`.
- Tests eliminados: 0. Tests ignorados nuevos: 0.
- Dependencias, lockfiles, migraciones y workflows: sin cambios.

## Limites del cierre

Permanecen fuera de MVP4: E2E/Playwright, produccion, `quantity`/copias
consolidadas, imagenes y almacenamiento real, paginacion avanzada, social,
marketplace, pagos y las decisiones abiertas de taxonomia y MISSING de
`docs/26_MVP4_COLLECTIONS_DESIGN.md`.

MVP4 queda `MVP4_CLOSED_WITH_LIMITATIONS`: se cierra la fase demostrada, no el
producto final completo. La siguiente tarea unica seleccionada es EPIC 45A,
exclusivamente documental; no se inicia en este cierre.
