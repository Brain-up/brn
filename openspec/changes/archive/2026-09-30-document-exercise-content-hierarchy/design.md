## Context

See proposal.md — Why. This change documents existing read behaviour of the
content tree; no code changes. The one decision worth recording is where this
capability ends and `exercise-availability` begins, because the two overlap at the
"exercises for a subgroup" endpoint.

## Goals / Non-Goals

**Goals:**
- Capture the current read/navigation contract of Group → Series → SubGroup →
  Exercise → Task as testable scenarios.
- Draw a clean, non-overlapping boundary with the existing `exercise-availability`
  capability.

**Non-Goals:**
- No new endpoints, DTO changes, ordering changes, or behaviour changes.
- No write/admin flows (create/update/delete subgroup, create exercise, CSV upload,
  active-status toggle) — those are separate capabilities and out of scope here.
- No re-specification of availability rules.

## Decisions

- **Boundary with `exercise-availability`.** The "read exercises for a subgroup"
  endpoint is dual-mode: admins get all exercises, regular users get an
  availability-filtered view. This capability owns the *shape and role branching* of
  the read; the *availability computation* stays owned by `exercise-availability`.
  The spec references availability at the boundary rather than restating its rules.
  - *Alternative considered*: fold availability scenarios in here — rejected, it would
    duplicate and risk diverging from the existing spec.

- **Document-only, verify by reading.** Because nothing changes in code, the
  implementation phase is: confirm each scenario against the current controllers/
  services and, where a scenario is not already covered by an existing test, note it —
  optionally back-filling a characterization test. No production code is touched.
  - *Alternative considered*: add tests for every scenario now — deferred; the existing
    availability/service tests already cover the hard logic, and this change's value is
    the written contract, not new coverage.

## Risks / Trade-offs

- [The written contract may not exactly match runtime behaviour] → Ground every
  requirement in the actual controller/service code during apply; treat any mismatch
  found as a bug report against either the spec draft or the code, not a silent edit.
- [Scope creep into admin/write flows] → Explicit Non-Goal above; those get their own
  capability later.
