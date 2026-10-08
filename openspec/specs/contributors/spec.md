# contributors Specification

## Purpose

Describe how the platform maintains and serves the list of project contributors:
the public read API that powers the "Contributors" page, the admin-only editing
API, and the background job that reconciles contributor data from the GitHub API
into local records. This capability owns the contributor read/edit API and the
GitHub-sync reconciliation; role enforcement is owned by the
users-roles-authorization capability and only referenced here, and the GitHub HTTP
client wire details and media/picture hosting belong elsewhere.

## Requirements

### Requirement: List All Contributors

The system SHALL expose a read endpoint that, when no contributor type is requested,
returns every contributor — active and inactive — ordered by contribution count
descending. Each returned contributor SHALL carry both its Russian and English
fields (name, description, company) so the client can render either language.

#### Scenario: No type requested

- **WHEN** the contributors endpoint is called without a type
- **THEN** all contributors are returned, ordered by contribution count descending

#### Scenario: Both languages present

- **WHEN** a contributor is returned
- **THEN** the response carries both its Russian and its English fields

### Requirement: List Contributors By Type

The system SHALL, when a contributor type is requested, return only the active
contributors of that type, ordered by contribution count descending. The supported
types are developer, specialist, QA, autotester, designer, and other. A locale MAY
be supplied (default `ru-ru`); it does not remove the other language's fields from
the response.

#### Scenario: Type requested

- **WHEN** the contributors endpoint is called with a type
- **THEN** only the active contributors of that type are returned, ordered by contribution count descending

#### Scenario: Inactive contributor excluded from a typed list

- **WHEN** a contributor of the requested type is inactive
- **THEN** that contributor is not included in the typed result

### Requirement: Public Read Access, Admin-Only Editing

The system SHALL allow any caller to read the contributor list, and SHALL restrict
creating and updating contributors to users with the `ADMIN` role. The role check is
the one owned by the users-roles-authorization capability.

#### Scenario: Anyone may read

- **WHEN** any caller requests the contributor list
- **THEN** the request is permitted without a role requirement

#### Scenario: Only admin may edit

- **WHEN** a caller without the `ADMIN` role attempts to create or update a contributor
- **THEN** the request is rejected as forbidden

### Requirement: Add A Contributor

The system SHALL let an administrator add a new contributor from a validated request
and SHALL report the creation with a "created" status.

#### Scenario: Create a contributor

- **WHEN** an administrator submits a valid new contributor
- **THEN** the contributor is stored and the response reports a created status with the new contributor

### Requirement: Update A Contributor

The system SHALL let an administrator update an existing contributor by id, replacing
its editable fields — including its contacts — with the submitted values. When no
contributor exists for the id the system SHALL report a not-found error and SHALL NOT
create one.

#### Scenario: Update an existing contributor

- **WHEN** an administrator submits valid data for an existing contributor id
- **THEN** that contributor's fields and contacts are replaced with the submitted values

#### Scenario: Update a missing contributor

- **WHEN** an administrator submits data for an id that has no contributor
- **THEN** the system reports a not-found error and nothing is created

### Requirement: Validate Contributor Input

The system SHALL validate a contributor edit request before applying it: name,
description, English name, and English description SHALL be non-blank; text fields
SHALL be at most 255 characters; the contribution count SHALL be present and
positive; and the contributor type SHALL be present.

#### Scenario: Invalid request rejected

- **WHEN** a contributor edit request is missing a required field or violates a field constraint
- **THEN** the request is rejected as invalid and no contributor is created or updated

### Requirement: Invalidate Read Caches On Change

The contributor read results are cached for performance. The system SHALL invalidate
the cached contributor lists whenever a contributor is created or updated, so that
later reads reflect the change.

#### Scenario: Edit invalidates the cache

- **WHEN** a contributor is created or updated
- **THEN** the cached contributor lists are cleared so the next read reflects the change

### Requirement: Synchronize Contributors From GitHub

The system SHALL periodically synchronize contributors from GitHub on a configured
schedule, and SHALL also run the synchronization once at startup when no GitHub
contributor data exists yet. Synchronization SHALL be disabled in the development
profile. For each configured repository the system SHALL fetch that repository's
contributors from the GitHub API, SHALL skip configured bot logins, and SHALL upsert
each contributor's GitHub user record before reconciling the local contributor.

#### Scenario: Scheduled synchronization

- **WHEN** the configured synchronization schedule fires outside the development profile
- **THEN** contributors are fetched from each configured repository and reconciled into local records

#### Scenario: One-time startup synchronization

- **WHEN** the application starts and no GitHub contributor data exists yet
- **THEN** the synchronization runs once to populate contributors

#### Scenario: Bot login skipped

- **WHEN** a fetched GitHub contributor's login is in the configured bot list
- **THEN** that contributor is skipped and not stored

### Requirement: Classify And Back-Fill A Contributor From GitHub

When reconciling a GitHub user into a local contributor, the system SHALL create a
new contributor if none is linked to that GitHub user, classifying it as an
autotester when it comes from the auto-tests repository and as a developer
otherwise. When a contributor already exists for that GitHub user, the system SHALL
refresh its contribution count and SHALL back-fill only the fields that are still
empty, so administrator-entered values are not overwritten.

#### Scenario: New GitHub contributor

- **WHEN** a GitHub user has no linked contributor
- **THEN** a contributor is created, typed autotester for the auto-tests repository and developer otherwise

#### Scenario: Existing GitHub contributor refreshed

- **WHEN** a GitHub user already has a linked contributor
- **THEN** the contribution count is refreshed and only still-empty fields are back-filled, preserving existing values

### Requirement: Tolerate GitHub API Failures

The system SHALL treat a failed GitHub API call as empty data rather than letting the
synchronization crash, and the startup synchronization SHALL not prevent the
application from starting if it fails.

#### Scenario: GitHub API call fails

- **WHEN** a call to the GitHub API fails
- **THEN** the synchronization treats the result as empty and continues without crashing
