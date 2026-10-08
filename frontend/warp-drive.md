# Migration Plan: ember-data 4.12 to WarpDrive

## Context

The BRN frontend is an Ember 4.12 Octane app using `ember-data@4.12.3` with the classic adapter/serializer/model pattern. The backend serves a **custom JSON REST API** (not JSON:API) with payloads wrapped in `{ data: [...] }`. WarpDrive is the official successor to ember-data, offering a schema-driven, request-centric architecture.

**Key insight:** WarpDrive 5.x supports ember-source 4.12 — no Ember framework upgrade is required. The Ember upgrade is an independent, optional effort.

**Key insight:** `@warp-drive/json-api` is required even for non-JSON:API backends — it is the only production cache implementation. REST responses must be normalized into JSON:API format before entering the cache.

---

## Current State Summary

| Area | Count | Details |
|------|-------|---------|
| Models | 15 | Group, Series, Subgroup, Exercise, Task (base + 3 polymorphic subtypes), Signal, Contributor, Headphone, 3 stats models, CompletionDependent base |
| Adapters | 5 | Application (RESTAdapter) + 4 per-model |
| Serializers | 13 | Application (JSONSerializer) + 12 per-model |
| Transforms | 2 | `array`, `full-date` |
| Routes using store | 8 | query, findRecord, findAll, peekRecord |
| Key patterns | — | Polymorphic tasks, custom primaryKeys (date, seriesName), service injection in models, sideloaded signals via `store.push()`, `this.get()`/`this.set()` on models |

## Deprecations Inventory (must fix before or during migration)

| Category | Count | Key Files |
|----------|-------|-----------|
| `this.get()` / `this.set()` | 50+ instances | exercise.ts, task.ts, series.ts, completion-dependent.ts, routes, components |
| `import DS from 'ember-data'` | 1 | transforms/full-date.ts |
| `.toArray()` on relationships | 9 instances | group.ts, routes, controllers, components |
| `.hasMany().ids()` / `.value()` / `.load()` | 5 instances | exercise.ts, subgroup.ts, routes, controllers |
| `AsyncHasMany`/`AsyncBelongsTo`/`SyncHasMany` types | 6 files | model files, series-navigation component |
| Route mixins (`.extend(AuthenticatedRouteMixin)`) | 4 routes | registration, groups, group, login |
| `this.transitionTo()` (should be `this.router.transitionTo()`) | 6 instances | exercise route, group route, index route, application route |
| `store.adapterFor()` outside adapters | 2 instances | services/network.ts |
| `coalesceFindRequests` / `shouldReloadRecord` | 1 file | adapters/application.ts |
| `ember-data/types/registries/*` declarations | 30+ files | models, adapters, serializers, transforms |
| Classic `.extend()` transforms | 2 files | transforms/array.ts, transforms/full-date.ts |
| Silenced deprecations in workflow | 11 items | config/deprecation-workflow.js |

---

## Test Coverage Gap (Critical Risk)

The current test suite is **dangerously thin** for this migration:
- **Serializers:** All 6 test files are skeleton (only test instantiation). Zero normalization logic tested. The `ExerciseSerializer` (polymorphic mapping, signal sideloading, cross-serializer delegation) is completely untested.
- **Adapters:** 4 of 5 are skeleton. No tests for `pathForType()`, `sortQueryParams()`, `urlForFindRecord()`.
- **Models:** 11 of 14 are skeleton. Exercise's `postHistory()`, `calcStats()` untested. Subgroup's `parent` getter untested.
- **Meaningful coverage exists for:** `completion-dependent` model, `task/words-sequences` model, `full-date` transform, `tasks-manager` service, 3 acceptance tests.

---

## Migration Phases

### Phase 0: Pre-migration Cleanup & Safety Net (on ember-data 4.12)

**Goal:** Fix critical deprecations, write regression tests, and decouple adapter dependencies — all while still on ember-data 4.12.

#### 0a. Write Serializer Snapshot Tests (HIGHEST PRIORITY)

Before any migration, capture the exact output of every serializer so the new handlers can be verified against them:

- `ExerciseSerializer` — test `normalizeResponse()` for each `exerciseMechanism` (WORDS, MATRIX, SIGNALS), verify polymorphic type mapping, verify signal sideloading via `store.push()`
- `TaskSerializer` + subtypes — test `normalize()` for answerOptions flattening
- `ContributorSerializer` — test DTO field remapping
- `SeriesSerializer` — test `type → kind` mapping
- `SignalSerializer` — test `normalize()` output
- `ApplicationSerializer` — test `{ data: [...] }` unwrapping
- Stats serializers — test custom primaryKey handling

#### 0b. Fix Deprecations

1. **Replace 50+ `this.get()`/`this.set()` calls** with native property access
   - Files: `exercise.ts`, `task.ts`, `series.ts`, `completion-dependent.ts`, routes, controllers, components, services
2. **Replace `.toArray()` calls** (9 instances) with `Array.from()` or spread
3. **Fix route transition methods** — `this.transitionTo()` → `this.router.transitionTo()`
4. **Fix `import DS from 'ember-data'`** in `transforms/full-date.ts` → specific `@ember-data/*` import
5. **Refactor classic `.extend()` transforms** to native class syntax

#### 0c. Decouple NetworkService from Adapter

Extract auth token logic from adapter into a standalone service:

```ts
// NEW: app/services/auth-token.ts
// Reads Firebase token from ember-simple-auth session
// Both ApplicationAdapter and NetworkService consume this
```

**Files:**
- `app/services/network.ts` — remove `store.adapterFor('application')` access
- `app/adapters/application.ts` — use AuthTokenService instead of embedding token logic
- New: `app/services/auth-token.ts`

#### 0d. Relocate ExerciseMechanism Enum

The `ExerciseMechanism` enum is defined in `app/serializers/application.ts` but imported by 4+ model files. Move it to a shared location before serializers are deleted in Phase 2:

- New: `app/utils/exercise-types.ts` (enum + ExerciseDTOType + DTO types)

#### 0e. Remove DefinitelyTyped Packages

Delete from `package.json`:
- `@types/ember-data`, `@types/ember-data__adapter`, `@types/ember-data__model`, `@types/ember-data__serializer`, `@types/ember-data__store`

#### 0f. Brand Models with `[Type]` Symbol

Add type declarations for TypeScript generics support:
```ts
import { Type } from '@warp-drive/core-types/symbols';
class GroupModel extends Model {
  declare [Type]: 'group';
}
```

**Rollback:** All Phase 0 changes are backward-compatible on ember-data 4.12. Git revert if needed.

---

### Phase 1: Install WarpDrive via Mirror Packages (Incremental, Safe)

**Goal:** Install WarpDrive alongside existing ember-data 4.12 using mirror packages, enabling incremental migration with two coexisting data layers.

**Why mirror packages?** The `@warp-drive-mirror/*` packages can be installed alongside `ember-data@4.12.3` without conflicts. This avoids a big-bang replacement and lets you convert routes/features one at a time.

#### 1a. Install Mirror Packages

```bash
npm install -E @warp-drive-mirror/core@5.7.0 @warp-drive-mirror/json-api@5.7.0 \
  @warp-drive-mirror/ember@5.7.0 @warp-drive-mirror/legacy@5.7.0 \
  @warp-drive-mirror/utilities@5.7.0 @warp-drive-mirror/build-config@5.7.0
```

**Important:** Use exact versions (not `^`). All WarpDrive packages are versioned in lockstep.

#### 1b. Configure Build (ember-cli-build.js must become async)

```js
// ember-cli-build.js
module.exports = async function (defaults) {
  const { setConfig } = await import('@warp-drive-mirror/build-config');
  const app = new EmberApp(defaults, { /* existing config */ });
  setConfig(app, __dirname, {
    compatWith: '4.12',
    deprecations: {
      // list resolved deprecations as you fix them
    }
  });
  return app.toTree();
};
```

#### 1c. Configure Reactivity

Add to **both** files:
- `app/app.ts`: `import '@warp-drive-mirror/ember/install';`
- `tests/test-helper.ts`: `import '@warp-drive-mirror/ember/install';`

#### 1d. Configure Legacy Store

```ts
// app/services/store.ts
import { useLegacyStore } from '@warp-drive-mirror/legacy';

export default useLegacyStore({
  legacyRequests: true,  // REQUIRED: keeps store.query/findRecord/findAll working
});
```

`legacyRequests: true` enables the `LegacyNetworkHandler` which bridges existing adapters/serializers through the new RequestManager automatically.

#### 1e. Update All Imports

Across the entire codebase, update imports from `ember-data`/`@ember-data/*` to `@warp-drive-mirror/*`:

| Old Import | New Import |
|------------|-----------|
| `import Model, { attr, belongsTo, hasMany } from '@ember-data/model'` | `from '@warp-drive-mirror/legacy/model'` |
| `import RESTAdapter from '@ember-data/adapter/rest'` | `from '@warp-drive-mirror/legacy/adapter/rest'` |
| `import JSONSerializer from '@ember-data/serializer/json'` | `from '@warp-drive-mirror/legacy/serializer/json'` |
| `import Transform from '@ember-data/serializer/transform'` | `from '@warp-drive-mirror/legacy/serializer/transform'` |
| `import Store from '@ember-data/store'` | `import { Store } from '@warp-drive-mirror/core'` (named export) |
| `import DS from 'ember-data'` | specific imports from `@warp-drive-mirror/legacy` |
| `declare module 'ember-data/types/registries/*'` | Update to WarpDrive type registry format |

#### 1f. Fix Mirage Integration (Do This Now, Not Phase 4)

`discoverEmberDataModels(config.store)` in `mirage/config.js` will break immediately. Replace with explicit model definitions or remove the call.

#### 1g. Uninstall ember-data

```bash
npm uninstall ember-data
```

#### 1h. Verify

- `yarn lint` — no import/type errors
- `yarn test:ember` — full test suite passes
- Manual smoke test of all routes

**Rollback:** `git revert` the commit and re-install `ember-data@4.12.3`.

**Files to modify:**
- `package.json`, `ember-cli-build.js`, `app/app.ts`, `tests/test-helper.ts`
- New: `app/services/store.ts`
- All 15 model files, 5 adapter files, 13 serializer files, 2 transform files (import changes)
- All routes, services, components importing from `@ember-data/*`
- `mirage/config.js`
- 30+ files with `ember-data/types/registries/*` declarations

---

### Phase 2: Migrate Adapters + Serializers to RequestManager + Handlers

**Risk: HIGH** — This phase replaces 18 files containing all data transformation logic with near-zero test coverage.

**Goal:** Replace the adapter/serializer layer with the RequestManager + Handler + Builder pattern.

**Critical detail:** The BRN API handler must normalize REST responses into **JSON:API format** before they enter the `@warp-drive/json-api` cache. Example:

```
// Backend returns:
{ data: [{ id: 1, name: "Group A", locale: "en" }] }

// Handler must produce:
{ data: [{ id: "1", type: "group", attributes: { name: "Group A", locale: "en" } }] }
```

#### 2a. Investigate Built-in REST Builders

Before writing custom builders, check if `@warp-drive-mirror/utilities` provides REST builders:
```ts
import { query, findRecord } from '@warp-drive-mirror/utilities/rest';
```
These may handle standard REST URL patterns out of the box, reducing custom code.

#### 2b. Create Auth Request Handler

```ts
// app/handlers/auth-handler.ts
// Reads token from AuthTokenService, adds Authorization header, passes to next handler
```

#### 2c. Create BRN API Normalization Handler

This is the most complex new file. It must replicate all serializer logic:
- Unwrap `{ data: [...] }` envelope
- Stringify all IDs (WarpDrive enforces string-only IDs)
- Map attributes (e.g., `type` → `kind`, `order` → `level`, `serialNumber` → `order`)
- Wire up relationships in JSON:API format
- Handle polymorphic task type resolution (`exerciseMechanism` → task subtype)
- Handle signal sideloading (produce `included` array in JSON:API response)
- Handle custom primaryKeys (date, seriesName for stats models)

```ts
// app/handlers/brn-api-handler.ts
// Chain-of-responsibility handler that normalizes BRN REST responses to JSON:API cache format
```

#### 2d. Create Request Builders

Only for URLs that don't match standard REST patterns:

| Builder | Custom URL | Source Adapter |
|---------|-----------|----------------|
| `headphone.ts` | `/api/users/current/headphones` | HeadphoneAdapter |
| `statistics.ts` | `/api/v2/statistics/study/week`, `/year`, `/day` | Stats adapters |
| `task.ts` | `/api/tasks/:id` (polymorphic rewrite) | ApplicationAdapter.urlForFindRecord |

Standard builders (if not using built-in REST builders):
- `group.ts`, `series.ts`, `subgroup.ts`, `exercise.ts`, `contributor.ts`

#### 2e. Configure Store RequestManager

```ts
// app/services/store.ts
import { RequestManager, Fetch, Store as BaseStore } from '@warp-drive-mirror/core';
import { CacheHandler } from '@warp-drive-mirror/core/store';
import { JSONAPICache } from '@warp-drive-mirror/json-api';
import { AuthHandler } from '../handlers/auth-handler';
import { BrnApiHandler } from '../handlers/brn-api-handler';

export default class Store extends BaseStore {
  constructor(args) {
    super(args);
    this.requestManager = new RequestManager()
      .use([AuthHandler, BrnApiHandler, Fetch])
      .useCache(CacheHandler);
    this.cache = new JSONAPICache(this);
  }
}
```

#### 2f. Update Route Model Hooks

```ts
// Before:
model() { return this.store.query('group', { locale: 'en-us' }); }

// After:
model() { return this.store.request(queryGroups('en-us')); }
```

#### 2g. Handle store.push() for Signal Sideloading

The ExerciseSerializer currently calls `store.push()` to sideload signals. In the new handler, include signals in the JSON:API `included` array of the exercise response — the cache processes these automatically.

#### 2h. Verify Against Serializer Snapshot Tests

Run the snapshot tests written in Phase 0a against the new handlers to verify identical output.

#### 2i. Delete Adapter and Serializer Files

After full verification:
- Delete `app/adapters/` (5 files)
- Delete `app/serializers/` (13 files) — ensure `ExerciseMechanism` enum was already relocated in Phase 0d

**Rollback:** Keep adapter/serializer files in a git branch. Revert store.ts to `useLegacyStore({ legacyRequests: true })` to re-enable them.

**Files to create:** `app/handlers/auth-handler.ts`, `app/handlers/brn-api-handler.ts`, 3-8 builder files
**Files to modify:** 8 routes, 3 components, `app/services/store.ts`
**Files to delete:** 5 adapters, 13 serializers

---

### Phase 3: Migrate Models to SchemaRecord (LegacyMode)

**Goal:** Replace class-based models with JSON schemas using **LegacyMode** (not PolarisMode — that's preview-only, recommended for v6).

#### 3a. Register Schemas with `withDefaults` + `legacy: true`

```ts
import { withDefaults } from '@warp-drive-mirror/core/reactive';
import { registerDerivations } from '@warp-drive-mirror/legacy/model/migration-support';

const GroupSchema = withDefaults({
  type: 'group',
  legacy: true,  // REQUIRED for LegacyMode — enables mutable records
  fields: [
    { kind: 'field', name: 'name' },
    { kind: 'field', name: 'description' },
    { kind: 'field', name: 'locale' },
    { kind: 'collection', name: 'series', type: 'series', options: { inverse: 'group' } },
  ]
});

// REQUIRED when using withDefaults:
registerDerivations(store.schema);
```

#### 3b. Register Custom Transformations

Replace old transforms with WarpDrive Transformations (with `hydrate()`/`serialize()`):

```ts
// array transformation
{ hydrate(value) { return value ?? []; }, serialize(value) { return value ?? []; } }

// full-date transformation (Luxon DateTime)
{ hydrate(value) { return DateTime.fromISO(value, { zone: 'utc', locale: navigator.language }); },
  serialize(value) { return value?.toISO(); } }
```

#### 3c. Handle Service Injection (Models → Services/Components)

SchemaRecord does not support DI. Move logic out of models:

| Current Location | Method | Move To |
|-----------------|--------|---------|
| `Exercise` | `postHistory()`, `calcStats()`, `trackTime()` | New: `app/services/exercise-tracker.ts` |
| `Task` | `savePassed()` | Component action or service |
| `CompletionDependent` | `isCompleted`, `isFirst`, `canInteract`, tree traversal | New: `app/utils/completion-helpers.ts` or service |
| `Contributor` | `name`, `description`, `company` (locale-aware) | Derived fields or utility function |

#### 3d. Handle `pushObject()` on Model Arrays

`WordsSequencesComponent` directly mutates `task.wrongAnswers` via `pushObject()`. SchemaRecord arrays are plain JS arrays — use `push()` or create a new array.

#### 3e. Handle Polymorphic Tasks with Traits

```ts
// Shared fields via Trait (replaces CompletionDependent inheritance)
const CompletionTrait = {
  fields: [
    { kind: 'field', name: 'available', type: 'boolean' },
    // ... shared fields
  ]
};

// Each task subtype schema
const TaskSignalSchema = withDefaults({
  type: 'task/signal',
  legacy: true,
  traits: [CompletionTrait],
  fields: [
    { kind: 'resource', name: 'signal', type: 'signal' },
    // ... type-specific fields
  ]
});
```

Polymorphic collection on Exercise:
```ts
{ kind: 'collection', name: 'tasks', type: 'task', options: { polymorphic: true, inverse: 'exercise' } }
```

**Warning:** Code that uses `instanceof TaskModel` will break. Replace with type checks on `record.type` or `record.constructor.modelName`.

#### 3f. Handle Custom primaryKey for Stats Models

```ts
const UserWeeklyStatisticsSchema = withDefaults({
  type: 'user-weekly-statistics',
  legacy: true,
  identity: { kind: '@id', name: 'date' },  // overrides default 'id'
  fields: [...]
});
```

#### 3g. Migration Order (simplest → most complex)

1. `Signal` — pure data, no relationships
2. `Headphone` — standalone, minimal
3. `Contributor` — standalone, locale getters → utility
4. Stats models (3) — custom primaryKey, test identity override
5. `Group` — one hasMany
6. `Series` — relationships + computed getters → derived fields
7. `Subgroup` — `store.peekRecord` usage → move to route/component
8. `Exercise` — polymorphic tasks, signals, service injection → service
9. `Task` + subtypes — polymorphic, `savePassed()` → service, computed task logic → derived/utility

#### 3h. `store.peekRecord` / `store.peekAll` Replacement

These are deprecated but still work in LegacyMode. For eventual migration:
- `store.peekRecord('model', id)` → `store.cache.peek({ type: 'model', id })`
- `store.peekAll('model')` → track records from `store.request()` calls

**Rollback:** Keep model files alongside schemas. Remove model files only after schema versions are verified.

**Files to create:** `app/schemas/index.ts`, `app/services/exercise-tracker.ts`, `app/utils/completion-helpers.ts`
**Files to delete (gradually):** 15 model files, 2 transform files

---

### Phase 4: Migrate Mirage + Tests

**Goal:** Update test infrastructure for WarpDrive compatibility.

1. **Mirage integration points to fix:**
   - `discoverEmberDataModels()` — already handled in Phase 1f
   - `EmberDataSerializer` — may need updating
   - Acceptance tests that define manual `server.get()` handlers returning `{ data: [...] }` should still work

2. **Update unit/integration tests:**
   - Replace `ember-data` imports with `@warp-drive-mirror/*`
   - Update model instantiation in tests
   - Verify all 85 test files

3. **Optional:** Consider migrating from ember-cli-mirage to MSW (separate effort)

**Files:** `mirage/config.js`, `mirage/models/*.js`, `mirage/serializers/application.js`, test files

---

### Phase 5: Finalize — Mirror → Real Packages

**Goal:** Swap mirror packages for production packages and remove all legacy code.

1. **Rename all imports:** `@warp-drive-mirror/*` → `@warp-drive/*`
2. **Swap packages in package.json:**
   ```bash
   npm uninstall @warp-drive-mirror/core @warp-drive-mirror/json-api @warp-drive-mirror/ember \
     @warp-drive-mirror/legacy @warp-drive-mirror/utilities @warp-drive-mirror/build-config
   npm install -E @warp-drive/core @warp-drive/json-api @warp-drive/ember \
     @warp-drive/utilities @warp-drive/build-config
   ```
3. **Remove `@warp-drive/legacy`** once all models/adapters/serializers are gone
4. **Remove `compatWith: '4.12'`** from build config
5. **Remove legacy type declarations**
6. **Keep `@warp-drive/json-api`** — it is required (the only cache implementation)
7. **Update deprecation workflow** (`config/deprecation-workflow.js`) — remove resolved silences
8. **Full test suite + manual smoke test**

**Final package.json should have:** `@warp-drive/core`, `@warp-drive/ember`, `@warp-drive/json-api`, `@warp-drive/utilities`, `@warp-drive/build-config`

---

## Risk Assessment

| Phase | Risk | Justification |
|-------|------|---------------|
| Phase 0 | LOW | All changes are backward-compatible on ember-data 4.12 |
| Phase 1 | MEDIUM | Primarily import rewriting; mirage integration may break |
| Phase 2 | **HIGH** | Replaces 18 files of data transformation logic with near-zero test coverage. ExerciseSerializer polymorphic mapping + signal sideloading is the single riskiest piece. |
| Phase 3 | **HIGH** (Exercise/Task), MEDIUM (others) | Service injection removal, polymorphic type changes, `this.get()`/`this.set()` removal |
| Phase 4 | LOW | Test infrastructure updates |
| Phase 5 | LOW | Mechanical find-and-replace of import paths |

---

## Rollback Strategy

| Phase | Rollback Method |
|-------|----------------|
| Phase 0 | `git revert` — all changes are backward-compatible |
| Phase 1 | `git revert` + `npm install ember-data@4.12.3` |
| Phase 2 | Revert store.ts to `useLegacyStore({ legacyRequests: true })`, restore adapter/serializer files from git |
| Phase 3 | Restore model files from git, remove schema registrations |
| Phase 4 | `git revert` test changes |
| Phase 5 | Rename imports back to `@warp-drive-mirror/*` |

---

## Verification Checklist (Per Phase)

After each phase:
1. `yarn lint` — no import/type errors
2. `yarn test:ember` — full test suite passes
3. Manual smoke test:
   - Login/auth flow (token passed in requests)
   - Groups → Series → Subgroup → Exercise → Task navigation
   - All exercise types: WORDS (single-simple-words), MATRIX (words-sequences), SIGNALS
   - Statistics page (weekly, yearly, daily)
   - Contributors + Specialists pages
   - Breadcrumbs navigation with `peekRecord`
4. Browser DevTools Network tab — verify correct API URLs and auth headers
5. Ember Inspector — verify store contains expected records

---

## Estimated Scope

| Phase | Files Changed | New Files | Deleted Files |
|-------|--------------|-----------|---------------|
| Phase 0 | ~30 | 3 (auth-token service, exercise-types util, serializer tests) | 0 |
| Phase 1 | ~50+ | 1 (store service) | 0 |
| Phase 2 | ~15 | ~10 (handlers + builders) | 18 (adapters + serializers) |
| Phase 3 | ~20 | ~3 (schemas, exercise-tracker, completion-helpers) | 17 (models + transforms) |
| Phase 4 | ~10 | 0 | 0 |
| Phase 5 | ~50+ (import rename) | 0 | 0 |
