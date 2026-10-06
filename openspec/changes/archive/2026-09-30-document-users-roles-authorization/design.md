## Context

See proposal.md — Why. This change documents the existing authentication and
authorization behaviour; no code changes. The behaviour is realized by
`FirebaseTokenAuthenticationFilter` (token → security context), `WebSecurityBasicConfiguration`
(`@EnableMethodSecurity`, stateless session, 401/403 handlers, Swagger rule),
`CustomUserDetails` / `BrainUpUserDetailsService` (roles → authorities, enabled
flag, user cache), the `Role` / `UserAccount` (`user_roles`) model, and
`@RolesAllowed` annotations on `UserDetailController` and `RoleController`.

## Goals / Non-Goals

**Goals:**
- Capture the current authentication contract (Firebase bearer token → authenticated
  context, verified-token caching, unauthenticated fallthrough).
- Capture just-in-time provisioning with the default `USER` role.
- Capture the role model, the role→authority mapping, and the role-based access
  matrix, including the 401-vs-403 distinction and the Swagger restriction.

**Non-Goals:**
- No new endpoints, security rules, roles, or behaviour changes.
- No profile-editing, headphones, doctor/patient, or statistics semantics — those
  are separate capabilities; this spec references them only where a role gate applies.
- No last-visit / activity-tracking behaviour (`RememberLastVisitFilter`), which is
  a separate concern layered after authentication.
- No admin user-management write flows beyond the role gates already in place.

## Decisions

- **Enforcement is method-level, not URL-level.** The security config permits all
  requests at the URL layer (except Swagger) and relies on `@RolesAllowed` on
  controllers for authorization. The spec therefore states role requirements per
  endpoint area (the matrix) rather than per URL pattern, and calls out Swagger as
  the one URL-level rule.
  - *Alternative considered*: describe an allow-list of URL patterns — rejected, it
    would misrepresent how access is actually enforced.

- **401 vs 403 is part of the contract.** Unauthenticated access yields 401 via the
  authentication entry point; authenticated-but-insufficient yields 403 via the
  access-denied handler. Both are specified because clients depend on the distinction.

- **Token/user caching is behaviour, not just optimization.** Verified-token caching
  (keyed by token hash, TTL bounded by token expiry) and the auth-user cache change
  observable timing and Firebase call counts, so the cache-reuse and expiry
  scenarios are specified — without pinning exact sizes/TTLs, which are tuning values.

- **Document-only, verify by reading.** Confirm each scenario against the filter,
  config, services, and controllers; back-fill a characterization test only where an
  authorization/authentication scenario is not already covered. No production code
  is touched.

## Risks / Trade-offs

- [The written contract may not match runtime behaviour] → Ground every requirement
  in the actual filter/config/controller code during apply; treat any mismatch as a
  bug report against the spec draft or the code, not a silent edit.
- [Scope creep into user-management/profile flows] → Explicit Non-Goals above; the
  spec references other capabilities at role boundaries instead of restating them.
- [Over-specifying cache internals] → Specify observable reuse/expiry only, leaving
  sizes and TTLs as configuration.
