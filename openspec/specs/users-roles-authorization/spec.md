# users-roles-authorization Specification

## Purpose

Describe how the platform authenticates a request from a Firebase bearer token,
provisions a local account on first sight, models roles, and authorizes access by
role. This capability owns authentication and role-based authorization only;
profile editing, headphones, doctor/patient links, statistics, and last-visit
tracking belong to other capabilities.

## Requirements

### Requirement: Roles And Granted Authorities

The system SHALL support exactly three roles — `ADMIN`, `USER`, and `SPECIALIST` —
each identified by a unique name. A user MAY hold any set of these roles. When a
user is authenticated, each of the user's roles SHALL become a granted authority
named `ROLE_<role name>` (for example, role `ADMIN` becomes authority `ROLE_ADMIN`).

#### Scenario: Roles map to prefixed authorities

- **WHEN** an authenticated user holds one or more roles
- **THEN** the user's granted authorities are those role names each prefixed with `ROLE_`

#### Scenario: A user may hold several roles

- **WHEN** a user is assigned more than one role
- **THEN** the user is granted every corresponding `ROLE_<name>` authority

### Requirement: Authenticate A Request From A Firebase Bearer Token

The system SHALL derive the authenticated principal from a Firebase ID token sent
as an `Authorization: Bearer <token>` header. When a valid token is present the
system SHALL verify it with Firebase, resolve the local user by the token's email,
and place an authenticated principal — carrying that user's granted authorities —
into the security context for the request. Authentication SHALL be stateless: no
server-side session is created, and each request is authenticated on its own.

#### Scenario: Valid token for an existing user

- **WHEN** a request carries a valid Firebase bearer token whose email matches an existing local user
- **THEN** the request is authenticated as that user with the user's role authorities

#### Scenario: No bearer token

- **WHEN** a request carries no `Authorization: Bearer` token
- **THEN** the request proceeds unauthenticated and no principal is placed in the security context

#### Scenario: Invalid or unverifiable token

- **WHEN** a request carries a bearer token that Firebase fails to verify
- **THEN** the request proceeds unauthenticated and no principal is placed in the security context

#### Scenario: Security context already populated

- **WHEN** the security context already holds an authentication for the request
- **THEN** token verification is skipped and the existing authentication is kept

### Requirement: Cache Verified Tokens

To avoid re-verifying the same token on every request, the system SHALL cache the
result of a successful token verification, keyed by a hash of the token, for a
bounded time that never exceeds the token's own expiry. A cached verification
SHALL be reused only while it is unexpired; an expired entry SHALL be discarded and
the token re-verified.

#### Scenario: Repeated request with the same token

- **WHEN** a second request arrives with a token that was recently verified and is not yet expired
- **THEN** the cached verification is reused instead of calling Firebase again

#### Scenario: Cached verification expired

- **WHEN** a request arrives with a token whose cached verification has expired
- **THEN** the cached entry is discarded and the token is verified again

### Requirement: Provision A Local User On First Authentication

When a request presents a valid Firebase token whose email has no local user yet,
the system SHALL create a local account from the Firebase user record (email,
display name, Firebase uid) and assign it the default role `USER`, then authenticate
the request as the newly created user. Provisioning SHALL NOT create a duplicate
when an account with that email already exists.

#### Scenario: First authenticated request for a new user

- **WHEN** a valid token's email has no local user and a matching Firebase user record exists
- **THEN** a local account is created with the default `USER` role and the request is authenticated as that user

#### Scenario: No matching Firebase record

- **WHEN** a valid token's email has no local user and no matching Firebase user record can be found
- **THEN** no local account is created and the request proceeds unauthenticated

#### Scenario: Account for the email already exists

- **WHEN** provisioning is attempted for an email that already has a local account
- **THEN** no duplicate account is created

### Requirement: A Disabled Account Is Not Enabled

The system SHALL expose an account's `active` flag as its enabled state, so that a
deactivated user is treated as not enabled for authentication purposes.

#### Scenario: Inactive account

- **WHEN** the authenticated user's account is inactive
- **THEN** the user is reported as not enabled

### Requirement: Authorize Access By Role

The system SHALL enforce access to endpoints by role using method-level role
checks. A request without a valid authenticated principal SHALL be rejected as
unauthorized (HTTP 401). An authenticated request whose roles do not satisfy an
endpoint's required roles SHALL be rejected as forbidden (HTTP 403). Endpoints that
declare no role requirement SHALL be reachable by any authenticated user.

#### Scenario: Unauthenticated caller

- **WHEN** an unauthenticated request reaches a role-protected endpoint
- **THEN** the response is 401 Unauthorized

#### Scenario: Authenticated caller missing the required role

- **WHEN** an authenticated caller lacks a role required by the endpoint
- **THEN** the response is 403 Forbidden

#### Scenario: Authenticated caller with a sufficient role

- **WHEN** an authenticated caller holds a role the endpoint allows
- **THEN** the request is permitted

### Requirement: Role Matrix For User And Role Endpoints

The system SHALL apply the following role requirements:

| Area | Access |
| --- | --- |
| List users / get a user by id / list a user's headphones | `ADMIN` or `SPECIALIST` |
| Add headphones to another user by id; delete auto-test users | `ADMIN` |
| Read/update the current user and the current user's own headphones/avatar | any authenticated user |
| Read the list of roles | `ADMIN` |

#### Scenario: Only admin or specialist can list users

- **WHEN** a caller requests the user list or another user by id
- **THEN** the request is permitted only if the caller has the `ADMIN` or `SPECIALIST` role

#### Scenario: Any authenticated user manages their own account

- **WHEN** an authenticated caller reads or updates their own current-user data
- **THEN** the request is permitted regardless of which of the three roles they hold

#### Scenario: Only admin can list roles

- **WHEN** a caller requests the list of roles
- **THEN** the request is permitted only if the caller has the `ADMIN` role

### Requirement: Swagger UI Restricted To Admins

The system SHALL restrict the Swagger UI and its API-docs resources to users with
the `ADMIN` role.

#### Scenario: Non-admin opens Swagger

- **WHEN** a caller without the `ADMIN` role requests the Swagger UI or API-docs resources
- **THEN** access is denied

### Requirement: Resolve The Current User From The Security Context

The system SHALL resolve the current user from the authenticated security context
(by the principal's email) to serve current-user reads and to expose the current
user's roles and id. When no user can be resolved for the context the system SHALL
report a not-found error.

#### Scenario: Current user resolved

- **WHEN** current-user data is requested within an authenticated request
- **THEN** the account matching the context's email is returned, including its roles

#### Scenario: Context has no resolvable user

- **WHEN** current-user data is requested but no account matches the context
- **THEN** the system reports a not-found error
