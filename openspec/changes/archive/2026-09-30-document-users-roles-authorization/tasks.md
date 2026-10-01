## 1. Validate the planning artifacts

- [x] 1.1 Run `openspec validate document-users-roles-authorization --strict` and verify it passes with no errors

## 2. Confirm each requirement against the current code

- [x] 2.1 Verify "Roles And Granted Authorities" against `BrnRole` (ADMIN/USER/SPECIALIST), `Role` (unique name), `UserAccount.roleSet` (`user_roles` M2M), and `CustomUserDetails` (`ROLE_<name>` authorities)
- [x] 2.2 Verify "Authenticate A Request From A Firebase Bearer Token" against `FirebaseTokenAuthenticationFilter` + `TokenHelperUtils.getBearerToken` + `WebSecurityBasicConfiguration` — bearer extraction, `verifyIdToken`, stateless session, unauthenticated fallthrough on missing/invalid token, and the skip when the context is already populated
- [x] 2.3 Verify "Cache Verified Tokens" against the verified-tokens cache in `FirebaseTokenAuthenticationFilter` — hash key, TTL bounded by token `exp`, reuse-while-unexpired, discard-on-expiry (reuse path covered by `FirebaseTokenAuthenticationFilterTest`; expiry is the same Caffeine TTL boundary, clock is not injectable so no brittle expiry test added)
- [x] 2.4 Verify "Provision A Local User On First Authentication" against the `UsernameNotFoundException` branch in the filter and `UserAccountServiceImpl.createUser` — Firebase record lookup by uid, default `USER` role, duplicate-email guard, unauthenticated when no record
- [x] 2.5 Verify "A Disabled Account Is Not Enabled" against `CustomUserDetails.isEnabled` / `UserAccount.active`
- [x] 2.6 Verify "Authorize Access By Role" and "Role Matrix For User And Role Endpoints" against `@EnableMethodSecurity`, the `@RolesAllowed` annotations on `UserDetailController`/`RoleController`, and the 401/403 handlers in `WebSecurityBasicConfiguration`
- [x] 2.7 Verify "Swagger UI Restricted To Admins" against the `hasRole(ADMIN)` request matchers in `WebSecurityBasicConfiguration`
- [x] 2.8 Verify "Resolve The Current User From The Security Context" against `UserAccountServiceImpl.getCurrentUser` / `getCurrentUserDto` / `getCurrentUserRoles` and the `EntityNotFoundException` on an unresolvable context

## 3. Back-fill characterization tests for uncovered scenarios

- [x] 3.1 For any scenario in section 2 not already covered by an existing unit/integration test, add a characterization test in the standard stack (JUnit5 + MockK + kotest) and verify `gradlew verify` passes — added `CustomUserDetailsTest` (roles → `ROLE_<name>` authorities, `isEnabled` = `active`, username/password); authentication, provisioning, token-reuse, default-role, and duplicate-guard scenarios already covered by `FirebaseTokenAuthenticationFilterTest`, `BrainUpUserDetailsServiceTest`, and `UserAccountServiceTest`
- [x] 3.2 If any scenario does not match the code, stop and report the mismatch (spec draft vs. code) rather than editing code silently — record the resolution in the change before proceeding — no mismatches found; spec matches current behaviour

## 4. Finalize

- [x] 4.1 Run `gradlew verify` and confirm it exits 0 (ktlint + unit tests green)
- [x] 4.2 Re-run `openspec validate document-users-roles-authorization --strict` and confirm the change is ready to archive
