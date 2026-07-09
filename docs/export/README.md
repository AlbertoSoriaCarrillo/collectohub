# CollectoHub technical exports

This directory is a downloadable snapshot of the architecture implemented in
the repository. It is derived from Liquibase changelogs, Spring controllers and
DTOs, Angular routes, services and feature components as of 2026-07-07.
EPIC 38 closure updates the documented snapshot to 2026-07-08 for creators and
item relationships.

## Files

| File | Contents |
| --- | --- |
| `database-schema.md` | Detailed relational schema, constraints, indexes and ownership by domain. |
| `database-schema.mmd` | Mermaid `erDiagram` for the application tables and their foreign keys. |
| `database-tables.csv` | One filterable row per application table. |
| `backend-endpoints.md` | Complete application REST API grouped by backend module. |
| `backend-endpoints.csv` | One filterable row per REST endpoint. |
| `frontend-routes.md` | Angular route inventory, guards and navigation exposure. |
| `frontend-routes.csv` | One filterable row per child route in `app.routes.ts`. |
| `frontend-backend-map.md` | Mapping from screens and Angular services to API operations. |
| `frontend-backend-map.csv` | One row per direct screen-to-API relationship. |

GitHub renders the Markdown files directly. To download a source file, open it
on GitHub and use **Download raw file**. CSV files can then be opened in a
spreadsheet; `database-schema.mmd` can be opened in Mermaid-compatible tools.

## Status vocabulary

- `MVP1_VISIBLE`: part of the closed MVP 1 collector journey or directly
  supports it.
- `MVP2_FOUNDATION`: implemented MVP 2 backend/data foundation without a
  collector-facing editorial UI yet.
- `MVP2_VISIBLE`: public MVP 2 editorial reading flow exposed in the Angular
  application.
- `LEGACY_FUTURE`: implemented and retained for compatibility or a later
  product phase, but not promoted in the current main journey.
- `TECHNICAL`: infrastructure or operational support rather than a product
  screen or business capability.
- `REDIRECT`: frontend compatibility route that redirects without rendering a
  feature component.

For frontend navigation, `PRIMARY` means main navigation, `HEADER` means an
anonymous or user-menu entry, `CONTEXTUAL` means a link/action inside a screen,
and `MANUAL_ONLY` means the route remains addressable but is not promoted.

## Updating the exports

This snapshot is maintained manually. When the implementation changes:

1. Compare Liquibase changelogs and JPA mappings with the database files.
2. Compare Spring controllers, security rules and DTOs with the endpoint files.
3. Compare `frontend/src/app/app.routes.ts`, Angular services and feature
   components with the route and frontend-backend mapping files.
4. Keep the Markdown and CSV counts aligned and render the Mermaid diagram.
5. Run the repository documentation checks and `git diff --check`.

## Limitations

- The files describe repository code and migrations, not a live database
  introspection result.
- They can become stale when a migration, endpoint or route changes without a
  matching documentation update.
- They do not replace Swagger/OpenAPI for interactive API exploration.
- Liquibase's own `databasechangelog` and `databasechangeloglock` tables are
  runtime infrastructure and are intentionally excluded from the 22
  application-table count.
- Recommendations have no table: the backend matches collection and inventory
  references by edition, item or legacy master product.
- EPIC 38 raises the inventory to 11 Liquibase migrations, 22 application
  tables, 81 endpoints and 32 routes. The frontend-backend map remains at 61
  relationships because creators and relationships are consumed through the
  existing editorial item-detail endpoint.
