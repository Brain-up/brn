## ADDED Requirements

### Requirement: Consistent Availability Across Access Paths

The system SHALL compute the same exercise availability for a given user and
subgroup regardless of which access path is used to obtain it — the subgroup
exercise listing or the available-exercise-ids lookup. Both paths SHALL apply the
availability rules defined in this capability identically.

#### Scenario: Both paths agree for a regular user

- **WHEN** a regular user's availability for a subgroup is obtained via the subgroup exercise listing
- **AND** the available-exercise-ids lookup is obtained for the same user and subgroup
- **THEN** the set of exercises marked available by the listing equals the set of exercise ids returned by the lookup

#### Scenario: Both paths agree for a privileged role

- **WHEN** an `ADMIN` or `SPECIALIST` obtains availability for a subgroup via either access path
- **THEN** both paths report every exercise in the subgroup as available
