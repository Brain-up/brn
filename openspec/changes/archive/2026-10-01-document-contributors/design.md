## Context

See proposal.md — Why. This change documents the existing `contributors` domain; no
code changes. The behaviour is realized by `ContributorController`
(`GET /contributors` open to all, `POST` / `PUT /{id}` `@RolesAllowed(ADMIN)`),
`ContributorServiceImpl` (cached reads via `@Cacheable("allContributors")` /
`@Cacheable("contributorsByType", key = "#type")`, cache eviction on write,
`EntityNotFoundException` on update of a missing id, and the GitHub reconciliation
in `createOrUpdateByGitHubUser`), `ContributorRepository`
(`findAllWithAssociations` ordered by contribution desc, `findAllByType` filtered to
`active = true`), the `ContributorRequest` bean-validation constraints, and
`GitHubContributorRefreshJob` (`@Scheduled(cron)` + `@EventListener(ApplicationReadyEvent)`,
`@Profile("!dev")`) driving `GitHubApiClient` to page the GitHub REST API.

## Goals / Non-Goals

**Goals:**
- Capture the read contract (all vs. by-type, ordering, active filtering,
  both-languages payload) as testable scenarios.
- Capture the admin edit contract (create/update, validation, not-found, cache
  invalidation).
- Capture the GitHub-sync reconciliation behaviour (schedule + startup, profile
  gating, bot skipping, classify-on-create, back-fill-on-update, failure tolerance)
  without pinning the GitHub HTTP/JSON wire format.

**Non-Goals:**
- No new endpoints, parameters, or behaviour changes.
- No re-specification of role enforcement itself (owned by
  users-roles-authorization); it is referenced at the admin-only boundary.
- No GitHub HTTP client wire details, pagination mechanics, or auth-token handling
  beyond "failures are tolerated".
- No media/picture hosting for `pictureUrl` (separate concern).

## Decisions

- **One capability spans read, edit, and sync.** They share the same `Contributor`
  model and caches; splitting them would scatter the "who appears on the page and
  why" contract. The GitHub HTTP client is treated as an external boundary, not a
  sub-capability.
  - *Alternative considered*: a separate `github-sync` capability — rejected, the
    sync exists only to populate the contributor list and shares its caches/model.

- **Ordering and active-filtering are part of the contract.** `findAllByType`
  filters to active and both queries order by contribution descending; these are
  observable in the response and specified. Caching is specified only as an
  observable invariant (edits are reflected on the next read), not by cache name.

- **Back-fill, not overwrite.** The update-from-GitHub path deliberately fills only
  empty fields and refreshes the contribution count, so administrator-curated values
  (English names, descriptions) survive a sync. This is specified as behaviour.

- **Document-only, verify by reading.** Confirm each scenario against the controller,
  service, repository, request DTO and job; back-fill a characterization test only
  where a scenario is not already covered. No production code is touched.

## Risks / Trade-offs

- [The written contract may not match runtime behaviour] → Ground every requirement
  in the actual controller/service/job/repository code during apply; treat any
  mismatch as a bug report against the spec draft or the code, not a silent edit.
- [Over-specifying GitHub wire details] → Specify only observable sync behaviour
  (schedule/startup, bot skip, classify, back-fill, failure tolerance); leave HTTP
  paging and token handling out.
- [Scope creep into roles/media] → Explicit Non-Goals above.
