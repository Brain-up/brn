## Why

BrainUp is a volunteer project, and the "Contributors" page is how it credits the
people who build it. Behind that page sits a small domain: a public read API
(`GET /contributors`, optionally filtered by type), an admin-only editing API
(create / update), and a background job that periodically pulls contributor data
from the GitHub API and reconciles it into local `Contributor` records. None of
this is written down, so the rules for "who shows up on the page, in what order,
and where the data comes from" live only in the controller, service and job code.

As a volunteer project without a BA, we want this behaviour captured as an OpenSpec
capability so the read contract, the admin edit rules, and the GitHub-sync
reconciliation are explicit and safe to change against.

This change is **documentation only** — it records existing behaviour as a new
`contributors` capability, in the same style as the specs already under
`openspec/specs/`. No production code changes.

## What Changes

- Add a new capability `contributors` describing:
  - **The read API** — `GET /contributors` open to any caller: all contributors
    (active and inactive) when no type is given, or only the active contributors of
    a given type, both ordered by contribution descending; responses carry both the
    Russian and English fields and are cached.
  - **The admin editing API** — `POST /contributors` (create, `201`) and
    `PUT /contributors/{id}` (update, `404` when missing), both restricted to
    `ADMIN`, with request validation; edits invalidate the read caches.
  - **The GitHub synchronization job** — a scheduled (and run-once-at-startup) job,
    disabled in the `dev` profile, that fetches contributors per configured
    repository from the GitHub API, skips bot logins, upserts the GitHub user
    record, and creates or back-fills the local `Contributor` (classifying the
    repository and filling only empty fields on update), tolerating API failures.
- Back-fill characterization tests only for scenarios not already covered; no
  behaviour changes.

## Impact

- Affected specs: **new** `contributors`.
- Affected code: none (documentation); at most new characterization tests under
  `src/test/kotlin/com/epam/brn/`.
- Boundary: this capability owns the contributor read/edit API and the GitHub
  contributor-sync reconciliation. Role enforcement itself is owned by
  `users-roles-authorization` and only referenced here; the GitHub HTTP client
  wire details and media/picture hosting are separate concerns.
