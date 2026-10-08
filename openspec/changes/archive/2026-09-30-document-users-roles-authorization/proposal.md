## Why

The platform authenticates every request with a Firebase bearer token, provisions
a local user on first sight, and gates endpoints by role (`ADMIN` / `USER` /
`SPECIALIST`). This behaviour is spread across a security filter, a security
config, `UserDetails` mapping, and `@RolesAllowed` annotations on controllers, with
no written contract. As a volunteer project without a BA, we want the
authentication and authorization rules captured as an OpenSpec capability so
newcomers can reason about "who can do what" and changes to security are made
against a documented baseline rather than by reading scattered code.

This change is **documentation only** — it records existing behaviour as a new
`users-roles-authorization` capability, in the same style as the specs already
under `openspec/specs/`. No production code changes.

## What Changes

- Add a new capability `users-roles-authorization` describing:
  - **Firebase token authentication** — how a bearer token becomes an
    authenticated security context, token verification and caching, and the
    unauthenticated fallthrough on a missing/invalid token.
  - **Just-in-time user provisioning** — creating a local account from the
    Firebase record on first authenticated request, with the default `USER` role.
  - **Role model** — the three roles, the user↔role assignment, and how roles
    become granted authorities (`ROLE_<name>`); a disabled account is not enabled.
  - **Authorization enforcement** — method-level `@RolesAllowed` gating, the
    role matrix for the user and role endpoints, and the 401-vs-403 distinction.
- Back-fill characterization tests only for authorization/authentication scenarios
  not already covered by existing tests; no behaviour changes.

## Impact

- Affected specs: **new** `users-roles-authorization`.
- Affected code: none (documentation); at most new characterization tests under
  `src/test/kotlin/com/epam/brn/`.
- Boundary: this capability owns authentication and role-based authorization only.
  Profile editing, headphones, doctor/patient links, statistics, and last-visit
  tracking stay owned by their own capabilities and are out of scope.
