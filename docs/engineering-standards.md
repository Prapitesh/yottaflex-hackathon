# Engineering standards and verification

## Backend boundaries

- Controllers handle HTTP mapping, request binding, response status and validation entry points. They do not contain workflow rules.
- Request and response DTOs isolate the API contract from JPA entities.
- Services coordinate use cases and own transaction boundaries.
- Service implementations enforce lifecycle policy, cross-entity rules and audit/history writes.
- Repositories contain persistence queries only.
- Entities protect their own invariants; enums constrain workflow vocabulary.
- Cross-cutting policy and scheduling decisions live outside controllers.
- Exceptions are translated centrally by GlobalExceptionHandler.

## Data and workflow safety

- Use Bean Validation at request boundaries and repeat critical business checks in services.
- Use database foreign keys, unique constraints, CHECK constraints and indexes as a second line of defence.
- Keep lifecycle changes explicit in application_stage_history.
- Keep operational decisions traceable in audit_event.
- Keep audit records append-only; apply database/migrations/V2__immutable_audit_events.sql to an existing database.
- Keep synthetic ground-truth anomaly labels separate from normal feature tables.
- Never log passwords, tokens, full resume contents or unnecessary candidate personal data.

## Frontend boundaries

- Keep the application shell in app and workflow screens in features.
- Keep network transport and common response/error handling in shared/api.
- Prefer controlled inputs, semantic labels and server-side validation.
- Every asynchronous page should represent loading, error and empty states.
- Avoid duplicating server state in derived component state.
- Use feature-local styles and shared global tokens.

## Test strategy

Backend:
- Entity tests cover domain invariants.
- Policy/service tests cover business rules, lifecycle transitions, scheduling and approval constraints.
- Controller tests cover endpoint-to-service delegation and HTTP response contracts.
- Run all backend tests with ./mvnw.cmd test.

Frontend:
- API client tests cover JSON success, API error mapping and 204 responses.
- Run npm run lint, npm test and npm run build in frontend.

## Known scope boundaries

- Actor fields are supplied by workflow requests or SYSTEM defaults; without authentication they are not verified identities.
- ResumeArtifact currently stores metadata and a storage URI; binary file upload/scanning is not implemented.
- Current frontend is a single-workspace demo, not a multi-tenant secured product.
