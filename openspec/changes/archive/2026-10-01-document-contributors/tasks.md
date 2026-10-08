## 1. Validate the planning artifacts

- [x] 1.1 Run `openspec validate document-contributors --strict` and verify it passes with no errors

## 2. Confirm each requirement against the current code

- [x] 2.1 Verify "List All Contributors" against `ContributorController.getContributors` (type == null branch), `ContributorServiceImpl.getAllContributors`, and `ContributorRepository.findAllWithAssociations` (ORDER BY contribution DESC); both-languages payload against `Contributor.toContributorResponse` / `ContributorResponse`
- [x] 2.2 Verify "List Contributors By Type" against the type != null branch, `getContributors(locale, type)`, and `findAllByType` (`WHERE type = ?1 AND active = true ORDER BY contribution DESC`); the six `ContributorType` values; locale default `ru-ru`
- [x] 2.3 Verify "Public Read Access, Admin-Only Editing" against the controller (no `@RolesAllowed` on class/GET; `@RolesAllowed(ADMIN)` on POST/PUT)
- [x] 2.4 Verify "Add A Contributor" against `createContributor` returning `201 CREATED` with the saved contributor
- [x] 2.5 Verify "Update A Contributor" against `updateContributor` (field + contacts replacement) and `EntityNotFoundException` when `findById` is empty
- [x] 2.6 Verify "Validate Contributor Input" against `ContributorRequest` constraints (`@NotBlank`/`@Length(max=255)` on name/description/nameEn/descriptionEn, `@NotNull @Positive` contribution, `@NotNull` type)
- [x] 2.7 Verify "Invalidate Read Caches On Change" against `@CacheEvict(["allContributors","contributorsByType"], allEntries = true)` on create/update
- [x] 2.8 Verify "Synchronize Contributors From GitHub" against `GitHubContributorRefreshJob` (`@Scheduled(cron)`, `@EventListener(ApplicationReadyEvent)` run-once-when-empty, `@Profile("!dev")`, per-repository fetch, bot-login skip, GitHub user upsert)
- [x] 2.9 Verify "Classify And Back-Fill A Contributor From GitHub" against `ContributorServiceImpl.createOrUpdateByGitHubUser` / `createContributor` (AUTOTESTER for `auto-tests-python`, else DEVELOPER) and `updateByGitHubUser` (refresh contribution, back-fill only empty fields)
- [x] 2.10 Verify "Tolerate GitHub API Failures" against `GitHubApiClient` `onErrorResume { Mono.empty() }` and the job's `runOnceAtStartup` try/catch

## 3. Back-fill characterization tests for uncovered scenarios

- [x] 3.1 For any scenario in section 2 not already covered by an existing unit/integration test (`ContributorServiceTest`, `ContributorControllerTest`, `GitHubContributorRefreshJobTest`, `ContributorControllerIT`), add a characterization test in the standard stack (JUnit5 + MockK + kotest) and verify `gradlew verify` passes — gap found and filled: the GitHub classification branch (`auto-tests-python` → AUTOTESTER vs. other repo → DEVELOPER) was not asserted; added two characterization tests in `ContributorServiceTest` capturing the saved entity's type. All other scenarios already covered.
- [x] 3.2 If any scenario does not match the code, stop and report the mismatch (spec draft vs. code) rather than editing code silently — record the resolution in the change before proceeding — no mismatch found

## 4. Finalize

- [x] 4.1 Run `gradlew verify` and confirm it exits 0 (ktlint + unit tests green)
- [x] 4.2 Re-run `openspec validate document-contributors --strict` and confirm the change is ready to archive
