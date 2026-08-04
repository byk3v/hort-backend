# API Contract Phase 1 Inventory

## Scope

This document records the HTTP behavior found before implementing Phase 1 of
the API contract migration. It is an inventory, not a declaration that the
current behavior is the target contract.

## Approved first increment

The following decisions were approved for the first Phase 1 vertical slice:

- introduce the stable groups resource at `/api/v1/groups` while preserving
  `/api/groups` as a temporary compatibility path;
- use RFC 9457 Problem Details with the extensions `code`, `traceId`, and
  `fieldErrors` where validation applies;
- map expected failures consistently to `400`, `401`, `403`, `404`, `409`,
  and `500` without exposing internal exception details;
- allow `HORT_ADMIN` to read and write groups, allow `ASSISTANT` to read them,
  and deny group-catalog access to `PARENT`.

The initial implementation applies this matrix to both the versioned and
legacy group paths. The versioned create operation returns the created
`GroupDto` and a versioned `Location` header. The legacy create response remains
the UUID scalar expected by existing clients. Cross-tenant identifiers and
unknown identifiers intentionally share the same `404 resource_not_found`
response to avoid disclosing another tenant's data.

## Current HTTP surface

All current endpoints require an authenticated JWT at the filter-chain level.
There is no effective operation-level role matrix yet.

| Feature | Method and path | Request | Response | Current concerns |
| --- | --- | --- | --- | --- |
| Groups | `GET /api/groups` | none | `List<GroupDto>` | No pagination; no operation role |
| Groups | `GET /api/groups/{id}` | UUID path | `GroupDto` | Entity-not-found has no stable error contract |
| Groups | `POST /api/groups` | `GroupSaveRequest` | UUID body, `201` | Resource response shape is inconsistent with other operations |
| Groups | `PUT /api/groups/{id}` | `GroupUpdateRequest` | `204` | No operation role |
| Groups | `DELETE /api/groups/{id}` | UUID path | `204` | Referential conflicts have no stable error contract |
| People | `GET /api/persons` | none | `List<PersonDto>` | Public need and role are not established |
| People | `GET /api/persons/{id}` | UUID path | `PersonDto` | Same error issue as groups |
| People | `POST /api/persons` | `PersonSaveRequest` | UUID body, `201` | Exposes an independent person lifecycle that may bypass feature workflows |
| People | `PUT /api/persons/{id}` | `PersonUpdateRequest` | `204` | No operation role |
| People | `DELETE /api/persons/{id}` | UUID path | `204` | No operation role |
| Students | `GET /api/students` | optional `name`, `groupId` UUID | `List<StudentDto>` | Unpaged; frontend types still use numeric IDs |
| Students | `POST /api/students` | `StudentOnboardingRequest` | `StudentOnboardingResponse`, `201` | Reuses response `StudentDto` as nested request data; validation is incomplete |
| Collectors | `GET /api/collectors` | none | `List<CollectorDto>` | `collectorType` is an unchecked string in the DTO |
| Collectors | `GET /api/collectors/{id}` | UUID path | `CollectorDto` | No stable not-found error |
| Collectors | `POST /api/collectors` | `CollectorSaveWithPersonRequest` | UUID body, `201` | `collectorType` is accepted as string and parsed with `valueOf` |
| Collectors | `PUT /api/collectors/{id}` | `CollectorSaveRequest` | `204` | Allows relinking a collector to a person; intended semantics are unclear |
| Collectors | `DELETE /api/collectors/{id}` | UUID path | `204` | No operation role |
| Pickup rights | `POST /api/pickup-rights` | `PickupRightCreateRequest` | UUID body, `201` | Overlaps the missing unified permissions API |
| Pickup rights | `PUT /api/pickup-rights/{id}/revoke` | none | `204` | Action uses `PUT`; idempotency needs to be declared |
| Pickup rights | `GET /api/pickup-rights/by-student` | `studentId` UUID | `List<PickupRightDto>` | Query naming and missing parent resource boundary |
| Pickup rights | `GET /api/pickup-rights/by-collector` | `collectorId` UUID | `List<PickupRightDto>` | Same concern |
| Self-dismissals | `POST /api/self-dismissals` | `SelfDismissalCreateRequest` | UUID body, `201` | Overlaps the missing unified permissions API |
| Self-dismissals | `PUT /api/self-dismissals/{id}/revoke` | none | `204` | Action semantics need to align with pickup rights |
| Self-dismissals | `GET /api/self-dismissals` | `studentId` UUID | `List<SelfDismissalDto>` | Path does not express that the list is student-scoped |
| Attendance | `GET /api/v1/attendance/check-in-candidates` | `q`, `page`, `size` | paginated candidates | Tenant-scoped students that may start today's session |
| Attendance | `POST /api/v1/attendance/check-ins` | `studentId` UUID | attendance session, `201` | Creates the student's only session for the `Europe/Berlin` operational day |
| Attendance | `GET /api/v1/attendance/present-students` | `q`, `page`, `size` | paginated open sessions | Checkout search exposes only students checked in today |
| Attendance | `POST /api/v1/attendance/check-outs` | discriminated pickup-right or self-dismissal request | checkout result, `201` | Validates the authorization and closes the session atomically |
| Identity | `GET /api/v1/me` | authenticated JWT | username and resolved Hort | Tenant is derived from `hort_id`; the client cannot select it |

## Missing backend surface used by web

The web currently calls `GET /api/permissions` and `POST /api/permissions`, but
the backend has no controller or service for those paths. The Java DTOs
`NewPermissionRequest` and `PermissionViewDto` appear to be unfinished contract
prototypes rather than an implemented API.

## Identifier mismatches

- Persistence and implemented backend DTOs use UUID.
- `NewPermissionRequest` and `PermissionViewDto` still use `Long` identifiers.
- The handwritten frontend types use `number` for groups, students,
  collectors, pickup rights, self-dismissals and permissions.
- `StudentOnboardingRequest.groupId` is UUID in Java and `number` in TypeScript.

The Phase 1 target is UUID in Java and JSON string in TypeScript. Existing
numeric permission DTO prototypes should not be published as a stable contract.

## Enum and vocabulary mismatches

| Concept | Persisted/backend enum | Other current vocabulary |
| --- | --- | --- |
| Permission duration | `PERMANENT`, `DAILY` | Web/prototype: `DAUER`, `TAGES` |
| Permission status | `ACTIVE`, `REVOKED`, `EXPIRED` | Prototype comment mentions `INACTIVE` |
| Permission subject | Separate `PickupRight` and `SelfDismissal` | `self_dismissal` is the sole source of truth for autonomous departure |
| Collector type | `COLLECTOR`, `STUDENT` | Some DTOs expose unchecked `String` |

The weekly recurring structure in `NewPermissionWeeklyAllowedFrom` has no
matching persistence model. It cannot be promised by `/api/v1` without an
explicit data-model decision.

## Nullability and validation gaps

- `StudentOnboardingRequest.student`, `groupId`, and `collectors` lack explicit
  validation and nested `@Valid` boundaries.
- The onboarding request embeds `StudentDto`, a response model containing ID,
  group and collectors fields that do not belong to new-student input.
- Collector and student string lengths are not validated consistently with the
  database schema.
- The stabilized attendance checkout request uses an explicit mode and validates
  the mutually exclusive pickup-right and self-dismissal variants.
- Validity intervals do not reject `validUntil < validFrom`.
- Attendance instants use offset-aware values stored in UTC; the operational
  date is calculated in `Europe/Berlin`.

## Error behavior

There is no application `@RestControllerAdvice`. Exceptions currently fall
through to Spring Boot's default error handling, so validation, not-found,
conflict and authorization responses do not have one documented shape.

Phase 1 needs stable mappings at least for:

- malformed JSON and bean validation;
- invalid path/query values;
- unauthenticated and unauthorized access;
- unknown or cross-tenant resources;
- state/data conflicts;
- unexpected internal failures without sensitive details.

## Authorization inventory

The realm roles are `HORT_ADMIN`, `ASSISTANT`, and `PARENT`. Spring converts
them to `ROLE_*`, but controllers currently rely only on `authenticated()`.
The commented student annotation is not an effective authorization rule.

An operation-level matrix must be approved before adding `@PreAuthorize`.

## Proposed incremental delivery

1. Approve API versioning, error representation, role matrix, permission model,
   and time representation.
2. Establish shared `/api/v1` error behavior and controller tests.
3. Stabilize groups as the first vertical feature while temporarily preserving
   the legacy path if compatibility is required.
4. Stabilize student onboarding with dedicated request models and UUIDs.
5. Stabilize collectors.
6. Implement one coherent permissions API over pickup rights and
   self-dismissals.
7. **Completed:** model daily attendance, stabilize check-in/checkout
   conditional requests and define time semantics.
8. **Completed for checkout:** migrate the web consumer and remove the legacy
   checkout HTTP paths. Continue removing other legacy paths only after their
   consumers migrate.
