# HORT Backend Agent Guide

## Scope and stack

This repository owns the Java 21 Spring Boot API, Flyway migrations, PostgreSQL
persistence, Spring Security resource-server configuration, domain behavior,
and the canonical HTTP contract.

Read `../AGENTS.md` and the cross-repository roadmap before changing public API
or security behavior.

## Architecture direction

- Prefer feature-oriented modules with `api`, `application`, `domain`, and
  `infrastructure` boundaries.
- Migrate existing technical packages incrementally when a feature is already
  being changed. Do not perform broad package moves without an explicit task.
- Controllers translate HTTP input/output and delegate behavior.
- Application services coordinate use cases and transaction boundaries.
- Domain code owns business rules.
- Repositories and external adapters belong to infrastructure.
- Shared code must represent a genuine cross-feature concern; do not use
  `shared` as a miscellaneous folder.
- Add architecture tests when module boundaries are introduced.

## API and DTO rules

- Do not expose JPA entities directly through controllers.
- Request and response models are distinct when their validation, optionality,
  or lifecycle differs.
- Use typed enums rather than unchecked string protocols.
- Define nullability and validation explicitly.
- Use one standard error response shape.
- Public contract changes require tests and, once available, regenerated
  OpenAPI and compatibility checks.
- The UUID alignment described in the roadmap is pending. Do not begin it
  without an explicit user request.

## Security and tenancy

- Authentication alone is not authorization. State operation-level access
  requirements explicitly.
- Treat Keycloak claims as untrusted input until validated by Spring Security.
- Derive tenant/hort context from authenticated identity or trusted mapping,
  never from an unrestricted request field.
- Changes to converters, audience, client roles, realm roles, or
  `@PreAuthorize` require coordination with `hort-keycloak-realm` and affected
  clients.
- Never weaken security merely to make generated documentation or tests start.
  Use a dedicated test/contract configuration.

## Persistence

- Change schema only through new forward Flyway migrations.
- Never rewrite an already-applied migration unless the user explicitly
  confirms that no persistent environment depends on it.
- Keep JPA mappings, migration SQL, constraints, and API semantics aligned.
- Test tenant isolation and authorization for affected data access.

## Verification

Before handoff, run the relevant tests and normally:

```bash
./mvnw verify
```

Run formatting checks configured by the repository. Do not apply formatting to
unrelated files. Report any check that could not be run and why.
