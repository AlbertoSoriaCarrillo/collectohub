# Guía de uso para Codex

Este directorio contiene la documentación funcional y técnica que debe guiar el desarrollo de CollectoHub.

## Lectura obligatoria antes de programar

Codex debe leer en este orden:

1. `PROMPT_FOR_CODEX.md`
2. `docs/01_PRODUCT_SPEC.md`
3. `docs/02_ARCHITECTURE_SPEC.md`
4. `docs/03_BACKEND_SPEC.md`
5. `docs/04_DATABASE_SPEC.md`
6. `docs/05_API_SPEC.md`
7. `docs/06_SECURITY_PRIVACY_SPEC.md`
8. `docs/07_FRONTEND_SPEC.md`
9. `docs/08_TESTING_QUALITY_SPEC.md`
10. `docs/09_DEVOPS_CI_CD_SPEC.md`
11. `docs/10_ROADMAP.md`
12. `docs/11_CODEX_TASKS.md`
13. `docs/12_TASK_LOG.md`
14. `docs/13_DECISIONS.md`

## Principios

- Primero utilidad, después comunidad.
- Primero backend, después frontend.
- Primero reservas sin pago, después marketplace completo.
- Primero búsqueda SQL, después OpenSearch.
- Primero almacenamiento local, después S3/Blob/Cloud Storage.
- Primero email/password, después OAuth/Google.
- Primero auditoría y seguridad básica, después 2FA.

## No hacer en MVP

No implementar en la primera versión:

- Pagos online.
- Chat.
- Feed social.
- Comentarios multimedia.
- Directos.
- App móvil nativa.
- Marketplace entre usuarios.
- Venta/intercambio real entre usuarios.
- IA avanzada.
- Recomendaciones geográficas.
- OpenSearch.

Todo eso debe quedar documentado como evolución futura.

## Registro obligatorio

Cada vez que Codex implemente algo, debe actualizar:

- `docs/12_TASK_LOG.md`
- `docs/13_DECISIONS.md`, si toma una decisión técnica nueva.
