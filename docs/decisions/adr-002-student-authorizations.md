# ADR-002: Student authorizations, UTC validity and parent boundary

- Status: Accepted
- Date: 2026-08-02

## Context

The web uses the term Vollmacht and currently calls `/api/permissions`, but the
backend has no such endpoint. The domain is represented by two aggregates:
`pickup_right` for collection by an authorized person and `self_dismissal` for
a student leaving alone. The word `permissions` is ambiguous with application
roles and security permissions.

Validity is currently stored as timezone-free timestamps. Permanent
self-dismissal authorizations also require weekday-specific departure times,
which have no persistence model. A future parent experience must allow parents
to manage authorizations for their own children, but the application does not
yet have a verified user-to-child relationship.

## Decision

### Naming and API boundary

- Use `/api/v1/student-authorizations` as the administrative HTTP resource.
- Keep `PickupRight` and `SelfDismissal` as separate persistence models behind
  one discriminated API representation.
- Use `PICKUP_RIGHT` and `SELF_DISMISSAL` as authorization kinds.
- Do not introduce `/api/permissions`, because that name is reserved for
  application/security permissions and would be misleading.

### Authorization

- `HORT_ADMIN` and `ASSISTANT` may list, create, read and revoke student
  authorizations for their current Hort.
- `PARENT` has no access to the administrative resource.
- Parent self-service is a required future capability, but it must use a
  resource scoped through a verified relationship, such as
  `/api/v1/me/children/{studentId}/authorizations`.
- A realm role and tenant membership alone are insufficient: without a
  user-to-child relationship, a parent could otherwise access every student in
  the same Hort.

### Status semantics

The API exposes four derived states:

1. `REVOKED` when the persisted status is revoked.
2. `SCHEDULED` when the current instant precedes `validFrom`.
3. `EXPIRED` when `validUntil` exists and precedes the current instant.
4. `ACTIVE` otherwise.

Temporal transitions are calculated on reads. No scheduler mutates rows merely
because time passed. Explicit revocation remains persisted and idempotent.

### Time representation

- API v1 uses ISO-8601 values with offsets through `OffsetDateTime`.
- PostgreSQL validity columns use `TIMESTAMP WITH TIME ZONE` and normalize
  values to instants (UTC).
- Existing development data is interpreted as UTC during migration because the
  system is not in production and no reliable historical timezone metadata
  exists.

### Weekly self-dismissal rules

- A permanent self-dismissal authorization requires one or more weekday rules.
- Each rule contains a weekday and `allowedFromTime`.
- A weekday may occur only once per authorization.
- Daily self-dismissal uses the authorization's single `allowedFromTime` and
  cannot contain weekly rules.
- Pickup rights cannot contain weekly self-dismissal rules.
- Rules are tenant-owned, protected by RLS and linked with a tenant-composite
  foreign key.

### Collector selection

Pickup authorization creation supports either an existing collector in the
current Hort or a new collector/person created in the same transaction. The
two forms are mutually exclusive. Cross-tenant collector and student IDs are
indistinguishable from unknown IDs.

## Consequences

- Web and mobile contracts use `studentAuthorizations`, not generic
  `permissions`.
- The frontend must preserve Problem Details instead of replacing them with
  `Upstream error`.
- Checkout validity queries must compare UTC instants and ignore revoked rows.
- Parent access remains intentionally deferred until an explicit relationship
  model and authorization tests exist.
