# ADR-001: Versioned student onboarding and paginated catalog

- Status: Accepted
- Date: 2026-08-02

## Context

The legacy student onboarding request embeds `StudentDto`, accepts only inline
collector data, and does not fully validate its aggregate. The student catalog
returns every student in the current Hort. A Hort can contain hundreds of
students even though each group normally contains no more than 30.

The multitenant model already supports one collector receiving pickup rights
for multiple students. Tenant isolation is enforced by the authenticated
`hort_id`, transaction-scoped database context, explicit tenant relationships,
and PostgreSQL RLS.

## Decision

### HTTP contract

- Introduce `GET /api/v1/students`, `GET /api/v1/students/{id}`, and
  `POST /api/v1/students`.
- Temporarily preserve `/api/students` for compatibility.
- Use UUID values represented as JSON strings.
- Return RFC 9457 Problem Details using the Phase 1 shared error contract.
- `POST` returns `201`, the created student resource, and a versioned
  `Location` header.

### Authorization

- `HORT_ADMIN` may list, read, and onboard students.
- `ASSISTANT` may list and read students but cannot onboard them.
- `PARENT` cannot access the administrative student catalog. A future
  parent-facing resource must be scoped to that parent's children.
- The same role matrix is applied to the temporary legacy endpoints.

### Pagination

- The versioned list uses zero-based offset pagination.
- Defaults are `page=0`, `size=20`, and `sort=lastName,asc`.
- Page size is limited to 100.
- Supported sort fields are `lastName` and `firstName`, in `asc` or `desc`
  direction.
- The response is the application-owned shape `items`, `page`, `size`,
  `totalElements`, and `totalPages`; Spring's `Page` is not exposed publicly.
- The legacy endpoint remains unpaged until its consumers migrate.

### Onboarding aggregate

- A student must be assigned to a group in the authenticated Hort.
- At least one collector assignment is required.
- Exactly one assignment must have `mainCollector=true`.
- Each assignment explicitly selects `NEW` or `EXISTING` as its source.
- `NEW` requires `newCollector` and forbids `existingCollectorId`.
- `EXISTING` requires `existingCollectorId` and forbids `newCollector`.
- A new collector is created with domain type `COLLECTOR`; clients cannot
  choose an internal collector type during onboarding.
- Every assignment creates an active pickup right. A collector may therefore
  receive pickup rights for multiple students, including siblings or another
  student for whom a Vollmacht was granted.
- Existing collectors, groups, and students are resolved inside the current
  tenant. Cross-tenant identifiers are indistinguishable from unknown IDs.
- Student person, student, new collector people, collectors, and pickup rights
  are created in one transaction. Any failure rolls the aggregate back.
- `hortId`, entity IDs, and audit fields are always derived by the backend.

## Consequences

- The web must use UUID strings and render server-driven pagination.
- The onboarding UI must allow choosing an existing collector or entering a
  new one and must enforce exactly one main collector.
- Reusing a collector does not duplicate its person record.
- Offset pagination is adequate for the expected hundreds of students and is
  simpler for an administrative table than cursor pagination.
- Legacy DTOs remain migration inventory and can be removed only after all
  consumers use `/api/v1/students`.
