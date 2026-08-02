# ADR-004: Self-dismissal is the sole source of truth

- Status: Accepted
- Date: 2026-08-03

## Context

`student.can_leave_alone` represented a general capability while
`self_dismissal` represented the effective authorization, including lifecycle,
validity dates, allowed times and weekly rules. The two values could diverge: a
student could appear as allowed to leave alone in the student list without any
authorization that checkout could legally accept.

## Decision

- `self_dismissal` is the only persisted source of truth for autonomous
  departure.
- The legacy `student.can_leave_alone` and `student.allowed_time_to_leave`
  columns are removed.
- Student onboarding neither accepts nor returns autonomous-departure data.
- Autonomous departure is configured only through the student authorization
  (Vollmacht) workflow.
- Attendance may expose `canLeaveAloneNow`, `allowedToLeaveFromTime` and the
  authorization ID as derived checkout data. Those values must be calculated
  exclusively from an active `self_dismissal` and its applicable time rule.

## Consequences

- The student list no longer displays an ambiguous “may leave alone” flag.
- Creating a student cannot silently create an incomplete authorization.
- Existing values in the removed student columns are intentionally discarded;
  they were not sufficient legal authorization and must not be converted into
  unrestricted self-dismissals.
- Users create a new Vollmacht when a student needs permission to leave alone.

