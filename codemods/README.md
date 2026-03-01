# ember-data → WarpDrive Codemod Suite

> jscodeshift codemod suite for migrating **ember-data@4.12** to **@warp-drive/*@5.8.1**

Automates the bulk of a multi-phase migration that typically touches 90+ files. Each phase is an independent jscodeshift transform (or standalone script) that can be run separately.

## Table of Contents

- [Quick Start](#quick-start)
- [CLI Wrapper](#cli-wrapper)
- [Post-Migration Checker](#post-migration-checker)
- [Migration Pipeline](#migration-pipeline)
- [Phase 0 — Deprecation Cleanup](#phase-0-deprecation-cleanup)
- [Phase 1 — Import Migration](#phase-1-import-migration)
- [Phase 2a — Consumer Migration](#phase-2a-consumer-migration)
- [Phase 3a — Model to Schema](#phase-3a-model-to-schema)
- [Phase 3b — Schema Index](#phase-3b-schema-index)
- [Phase 4 — Mirror to Official](#phase-4-mirror-to-official)
- [Manual Steps After Codemods](#manual-steps-after-codemods)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Technical Notes](#technical-notes)

---

## Quick Start

```bash
cd codemods && npm install

# Run all phases in order (replace "myapp" with your app name):
npx jscodeshift -t codemods/src/phase-0-deprecation-cleanup.ts  frontend/app/ --parser=ts --extensions=ts,gts
npx jscodeshift -t codemods/src/phase-1-import-migration.ts     frontend/app/ --parser=ts --extensions=ts,gts --appName=myapp
npx jscodeshift -t codemods/src/phase-3a-model-to-schema.ts     frontend/app/models/ --parser=ts --extensions=ts,gts --appName=myapp
npx jscodeshift -t codemods/src/phase-2a-consumer-migration.ts  frontend/app/ --parser=ts --extensions=ts,gts --appName=myapp --ignore-pattern='**/models/**'
npx tsx codemods/src/phase-3b-schema-index.ts --schemasDir=frontend/app/schemas

# Then: manual steps (store, handlers, extensions, inverse values)
```

All phases support `.ts`, `.gts`, and `.gjs` files.

---

## CLI Wrapper

Instead of running 6 separate commands, use the unified CLI wrapper:

```bash
# Run all default phases (0, 1, 3a, 2a, 3b):
npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app

# Run specific phases:
npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app --phases=0,1

# Dry run (preview changes without writing):
npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app --dry-run

# Phase 4 is opt-in (only needed for mirror packages):
npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app --phases=0,1,3a,2a,3b,4

# Or use the npm script:
cd codemods && npm run migrate -- --appName=myapp --target=../frontend/app
```

### CLI Options

| Option | Description | Default |
|--------|-------------|---------|
| `--target` | Target directory to transform | **required** |
| `--appName` | Application name for import paths | **required** for phases 1/2a/3a |
| `--phases` | Comma-separated list of phase IDs | `0,1,3a,2a,3b` |
| `--dry-run` | Preview changes without writing files | `false` |
| `--extensions` | File extensions to process | `ts,gts` |
| `--modelsDir` | Models directory override | `<target>/models` |
| `--schemasDir` | Schemas directory override | `<target>/schemas` |
| `--baseOnlyClasses` | Comma-separated base-only class names | `[]` |
| `--verbose` | Show file-level detail from jscodeshift | `false` |
| `--quiet` | Summary only, suppress per-phase output | `false` |
| `--strict` | Exit with error if any phase has errors | `false` |
| `--json` | Machine-readable JSON output (for CI) | `false` |

### Config File (`.codemodrc.json`)

Create a `.codemodrc.json` in your working directory to avoid repeating options:

```json
{
  "appName": "myapp",
  "target": "frontend/app",
  "modelsDir": "frontend/app/models",
  "schemasDir": "frontend/app/schemas",
  "extensions": "ts,gts",
  "baseOnlyClasses": ["CompletionDependent"]
}
```

CLI arguments override config file values. `dryRun` is CLI-only (never read from config).

### Validation

The CLI validates options before running:
- **Fatal**: `--target` missing or directory doesn't exist, `--appName` missing when needed
- **Warning**: `appName === 'app'`, phase ordering issues, missing `schemasDir` for phase 3b

---

## Post-Migration Checker

After running the codemods, scan for common issues that need manual attention:

```bash
npx tsx codemods/src/post-check.ts --target=frontend/app

# Verbose mode (show all file locations):
npx tsx codemods/src/post-check.ts --target=frontend/app --verbose

# Strict mode (warnings treated as failures, exits non-zero):
npx tsx codemods/src/post-check.ts --target=frontend/app --strict

# JSON output (for CI integration):
npx tsx codemods/src/post-check.ts --target=frontend/app --json

# Or use the npm script:
cd codemods && npm run post-check -- --target=../frontend/app
```

### Checks Performed

| # | Check | Status | What it scans |
|---|-------|--------|---------------|
| 1 | Store service exists | pass/fail | `target/services/store.{ts,js}` |
| 2 | `@warp-drive/ember/install` | pass/fail | `target/app.{ts,js,gts}` |
| 3 | Remaining `@ember-data/` imports | pass/warn | All `.ts`/`.gts` files |
| 4 | Remaining `ember-data` barrel imports | pass/warn | All `.ts`/`.gts` files |
| 5 | Codemod TODO comments | pass/warn | All `.ts`/`.gts` files |
| 6 | `inverse: null` relationships | pass/warn | Schema files |
| 7 | Extension `this.` -> `self.` TODOs | pass/warn | Schema files |
| 8 | Remaining deprecated array APIs | pass/warn | `.toArray()`, `.sortBy()`, `.filterBy()`, `.mapBy()`, etc. |
| 9 | Remaining `this.transitionTo/replaceWith` | pass/warn | Deprecated route/controller methods |
| 10 | Remaining `this.get()`/`this.set()` | pass/warn | Ember computed property access |
| 11 | Remaining `.setProperties()` | pass/warn | Incompatible with SchemaRecord |
| 12 | Remaining adapter files | pass/warn | `target/adapters/` directory |
| 13 | Remaining serializer files | pass/warn | `target/serializers/` directory |
| 14 | Remaining legacy transforms | pass/warn | `target/transforms/` (should be `transformations/`) |
| 15 | Model imports not rewritten to schemas | pass/warn | Consumer files still importing from `models/` instead of `schemas/` |

---

## Migration Pipeline

```
                          YOUR EMBER APP
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│  Phase 0: Deprecation Cleanup                                    │
│  ─────────────────────────────                                   │
│  this.get('x')  →  this.x                                       │
│  .toArray()     →  Array.from(...)                               │
│  .sortBy('k')   →  .sort(...)                                    │
│  isEmpty(x)     →  x == null || ...                              │
│  + 28 more transforms                                            │
│                                                        ~95% auto │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│  Phase 1: Import Migration                                       │
│  ─────────────────────────                                       │
│  @ember-data/model     →  @warp-drive/legacy/model               │
│  @ember-data/store     →  myapp/services/store (type)            │
│  import DS from 'ember-data'  →  individual @warp-drive imports  │
│  + [Type] brand, relationship fixes, async→sync                  │
│                                                        ~90% auto │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
┌───────────────────────────┐ ┌────────────────────────────────────┐
│  Phase 3a: Model→Schema   │ │  Phase 2a: Consumer Migration      │
│  ──────────────────────── │ │  ──────────────────────────         │
│                           │ │                                     │
│  app/models/user.ts       │ │  import Store → import type Store   │
│       ↓                   │ │  import Model → import type Model   │
│  app/schemas/user.ts      │ │  .toArray() → Array.from(...)       │
│  + app/models/user.ts     │ │                                     │
│    (re-export stub)       │ │                           ~80% auto │
│                 ~70% auto │ └────────────────────────────────────┘
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│  Phase 3b: Schema Index   │
│  ──────────────────────── │
│                           │
│  Generates:               │
│  schemas/index.ts         │
│  ALL_SCHEMAS = [...]      │
│  ALL_EXTENSIONS = [...]   │
│                 100% auto │
└─────────────┬─────────────┘
              │
              ▼
┌──────────────────────────────────────────────────────────────────┐
│  ✋ Manual Steps                                                  │
│  ──────────────                                                  │
│  • Create app/services/store.ts                                  │
│  • Create request handlers                                       │
│  • Rewrite extension bodies (this. → self.)                      │
│  • Fix relationship inverse values                               │
│  • Fix TypeScript errors                                         │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                               ▼  (optional)
┌──────────────────────────────────────────────────────────────────┐
│  Phase 4: Mirror → Official                                      │
│  ──────────────────────────                                      │
│  @warp-drive-mirror/*  →  @warp-drive/*                          │
│  (Only if mirror packages were used as intermediate step)        │
│                                                       100% auto  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Phase 0: Deprecation Cleanup

Remove deprecated Ember/ember-data APIs before the actual migration.

```bash
npx jscodeshift -t codemods/src/phase-0-deprecation-cleanup.ts frontend/app/ --parser=ts --extensions=ts,gts
```

### Before / After Examples

```ts
// ─── Property access ────────────────────────────────
this.get('name')                    → this.name
this.get('user.email')              → this.user?.email
this.set('name', val)               → this.name = val
obj.get('prop')                     → obj.prop
obj.set('prop', val)                → obj.prop = val
get(obj, 'prop')                    → obj.prop        // from @ember/object
set(obj, 'prop', val)               → obj.prop = val  // from @ember/object
obj.setProperties({ a: 1, b: 2 })  → obj.a = 1; obj.b = 2;

// ─── Array helpers ──────────────────────────────────
items.toArray()                     → Array.from(items)
items.sortBy('name')                → items.sort((a, b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0)
items.sortBy('last', 'first')       → items.sort(/* chained ternary for each key */)
items.mapBy('name')                 → items.map(item => item.name)
items.filterBy('active', true)      → items.filter(item => item.active === true)
items.findBy('id', 42)              → items.find(item => item.id === 42)
items.uniq()                        → [...new Set(items)]
items.firstObject                   → items[0]
items.lastObject                    → items.at(-1)

// ─── Array mutations ────────────────────────────────
arr.pushObject(item)                → arr.push(item)
arr.pushObjects(items)              → arr.push(...items)
arr.removeObject(item)              → const _idx = arr.indexOf(item); if (_idx !== -1) arr.splice(_idx, 1);
arr.removeObjects(items)            → items.forEach(_item => { const _idx = arr.indexOf(_item); if (_idx !== -1) arr.splice(_idx, 1); })

// ─── Ember utils (import-gated) ─────────────────────
isEmpty(x)                          → x == null || x === '' || (Array.isArray(x) && x.length === 0)
isPresent(x)                        → x != null && x !== '' && (!Array.isArray(x) || x.length > 0)
isNone(x)                           → x == null
A(arr)                              → arr         // from @ember/array
isArray(x)                          → Array.isArray(x)

// ─── Routing (Route/Controller classes only) ────────
this.transitionTo('route')          → this.router.transitionTo('route')
this.replaceWith('route')           → this.router.replaceWith('route')

// ─── Relationship access ────────────────────────────
model.hasMany('items').value()      → model.items
model.hasMany('items').ids()        → (model.items || []).map(r => r.id)
model.belongsTo('user').value()     → model.user
```

**32 transforms total.** Automatically removes unused `@ember/utils`, `@ember/object`, and `@ember/array` imports. Supports aliased imports (e.g., `import { get as emberGet }`).

> **Note**: `filterBy`/`findBy` emit `===` (strict equality), while Ember used `==` (loose). A `// NOTE` comment is added to flag this for review.

---

## Phase 1: Import Migration

Rewrite ember-data imports to WarpDrive, add `[Type]` brand, fix relationship specs.

```bash
npx jscodeshift -t codemods/src/phase-1-import-migration.ts frontend/app/ --parser=ts --extensions=ts,gts --appName=myapp
```

**Options:** `--appName=myapp` (default: `app`)

### Before / After Examples

```ts
// ─── Import rewrites ────────────────────────────────
import Model, { attr, hasMany } from '@ember-data/model';
→ import Model, { attr, hasMany } from '@warp-drive/legacy/model';

import Store from '@ember-data/store';
→ import type Store from 'myapp/services/store';

import RESTAdapter from '@ember-data/adapter/rest';
→ import RESTAdapter from '@warp-drive/legacy/adapter/rest';

// ─── DS barrel (routed per member) ──────────────────
import DS from 'ember-data';
class Foo extends DS.Model { ... }
const { RESTAdapter } = DS;
→ import Model from '@warp-drive/legacy/model';
  import RESTAdapter from '@warp-drive/legacy/adapter/rest';
  class Foo extends Model { ... }

// ─── [Type] brand injection ─────────────────────────
// app/models/user.ts
class User extends Model {               class User extends Model {
  @attr name!: string;           →         declare [Type]: 'user';
}                                          @attr name!: string;
                                         }

// ─── Relationship type fixes ────────────────────────
@hasMany('comment') comments!: AsyncHasMany<Comment>;
→ @hasMany('comment', { async: false, inverse: null }) comments!: Comment[];

@belongsTo('user') author!: AsyncBelongsTo<User>;
→ @belongsTo('user', { async: false, inverse: null }) author!: User;

// ─── Registry module removal ────────────────────────
declare module 'ember-data/types/registries/model' { ... }
→ // (removed entirely)
```

### DS Barrel Routing Map

| `DS.X` Member | Target Package |
|---------------|----------------|
| `Model`, `attr`, `belongsTo`, `hasMany` | `@warp-drive/legacy/model` |
| `RESTAdapter` | `@warp-drive/legacy/adapter/rest` |
| `JSONAPIAdapter` | `@warp-drive/legacy/adapter/json-api` |
| `JSONSerializer` | `@warp-drive/legacy/serializer/json` |
| `JSONAPISerializer` | `@warp-drive/legacy/serializer/json-api` |
| `RESTSerializer` | `@warp-drive/legacy/serializer/rest` |
| `Transform` | `@warp-drive/legacy/serializer/transform` |
| Unknown member | TODO comment added |

**`[Type]` brand detection** works with: direct `extends Model`, relative model imports (`../task`), and mixin patterns (`SortableMixin(Model)`).

---

## Phase 2a: Consumer Migration

Update consumer files (routes, controllers, components) that reference ember-data APIs.

```bash
npx jscodeshift -t codemods/src/phase-2a-consumer-migration.ts frontend/app/ --parser=ts --extensions=ts,gts --appName=myapp --ignore-pattern='**/models/**'
```

**Options:** `--appName=myapp` (default: `app`)

### Before / After Examples

```ts
// ─── Store import → type-only (when used only as type) ───
import Store from '@ember-data/store';
@service declare store: Store;
→ import type Store from 'myapp/services/store';

// ─── Model import → type-only (when used only as type) ───
import User from 'myapp/models/user';
async model(): Promise<User[]> { ... }
→ import type User from 'myapp/models/user';
```

Type-only detection covers: annotations, type references, generics, interfaces, `as` expressions, mapped types, conditional types, and indexed access types.

---

## Phase 3a: Model to Schema

Extract model field definitions into WarpDrive schema scaffolds.

```bash
npx jscodeshift -t codemods/src/phase-3a-model-to-schema.ts frontend/app/models/ --parser=ts --extensions=ts,gts --appName=myapp
```

**Options:** `--appName=myapp` | `--dryRun=true` | `--schemasDir=path` | `--baseOnlyClasses=Foo,Bar`

### What Happens to Each Model

```
app/models/user.ts                     app/schemas/user.ts  (NEW)
┌──────────────────────┐               ┌──────────────────────────────────┐
│ import Model, {      │               │ import { withDefaults } from     │
│   attr, hasMany      │               │   '@warp-drive/legacy/model/...' │
│ } from '...'         │               │                                  │
│                      │               │ interface UserSelf {             │
│ export default class │   ────────►   │   [Type]: 'user';               │
│   User extends Model │               │   name: string;                 │
│ {                    │               │   posts: Post[];                 │
│   @attr name!: string│               │ }                               │
│   @hasMany posts!:.. │               │                                  │
│   @tracked isEditing │               │ export const UserSchema =       │
│                      │               │   withDefaults({                 │
│   get displayName() {│               │     type: 'user',               │
│     return this.name │               │     fields: [                   │
│   }                  │               │       { kind: 'attribute', ... },│
│ }                    │               │       { kind: 'hasMany', ... },  │
│                      │               │       { kind: '@local', ... },   │
│ export interface     │               │     ]                           │
│   IUserStats { ... } │               │   }) as LegacyResourceSchema;   │
└──────────────────────┘               │                                  │
                                       │ export const UserExtension = {   │
app/models/user.ts  (REPLACED)         │   get displayName() {           │
┌──────────────────────┐               │     // TODO: rewrite this. →    │
│ export type {        │               │     //       self.              │
│   User as default    │               │   },                            │
│ } from               │               │ };                              │
│   'myapp/schemas/...'│               │                                  │
│                      │               │ export interface IUserStats {..} │
│ export type {        │               └──────────────────────────────────┘
│   IUserStats         │
│ } from               │
│   'myapp/schemas/...'│
└──────────────────────┘
```

### Member Classification

```
Model Class Member                    Schema Output
─────────────────                     ─────────────
@attr('string') name               →  { kind: 'attribute', name: 'name', type: 'string' }
@attr() name                       →  { kind: 'attribute', name: 'name' }
@attr('string', { defaultValue })  →  { kind: 'attribute', ... } + TODO comment
@belongsTo('user', opts)           →  { kind: 'belongsTo', name, type: 'user', options }
@hasMany('post', opts)             →  { kind: 'hasMany', name, type: 'post', options }
@tracked isEditing = false         →  { kind: '@local', name, options: { defaultValue: false } }

@service('store') store            →  Service record + TODO comment
get displayName() { ... }          →  Extension getter scaffold
set name(v) { ... }                →  Extension setter scaffold
doSomething() { ... }              →  Extension method scaffold
@action save() { ... }             →  Extension getter-closure scaffold
@cached get sorted() { ... }       →  Extension getter + "Was @cached" note

static modelName = 'user'          →  (skipped — not in extensions)
constructor()                       →  (skipped)
```

---

## Phase 3b: Schema Index

Generate `schemas/index.ts` barrel file collecting all schemas and extensions.

```bash
npx tsx codemods/src/phase-3b-schema-index.ts --schemasDir=frontend/app/schemas
```

**Output:**

```ts
// This file is auto-generated by phase-3b-schema-index.ts. Do not edit manually.

import { UserSchema, UserExtension } from './user';
import { PostSchema } from './post';

export const ALL_SCHEMAS = [UserSchema, PostSchema];
export const ALL_EXTENSIONS = [UserExtension];
```

---

## Phase 4: Mirror to Official

Replace `@warp-drive-mirror/*` with `@warp-drive/*`. Only needed if mirror packages were used as an intermediate step.

```bash
npx jscodeshift -t codemods/src/phase-4-mirror-to-official.ts frontend/app/ --parser=ts --extensions=ts,gts
```

Handles static `import`, `require()`, and dynamic `import()`.

---

## Manual Steps After Codemods

After running all phases, these tasks require manual work:

### Must Do

- [ ] **Create `app/services/store.ts`** with `useLegacyStore()` and RequestManager pipeline
- [ ] **Create request handlers** (`app/handlers/`) for your API endpoints
- [ ] **Create WarpDrive transformations** (`app/transformations/`) for custom attribute types
- [ ] **Add** `import '@warp-drive/ember/install'` to `app/app.js`
- [ ] **Fix relationship `inverse` values** — codemods use `null` as placeholder, correct values require domain knowledge
- [ ] **Rewrite extension bodies** — change `this.propName` → `self.propName`, replace `@service` injections with `getService(self, 'service-name')`

### Should Do

- [ ] **Audit `filterBy`/`findBy`** for loose vs strict equality edge cases (look for `// NOTE` comments)
- [ ] **Handle `@attr` defaultValue** TODOs at the handler/transform layer
- [ ] **Review extension Self interface types** for accuracy

### Environment

- [ ] **Upgrade TypeScript** to 5.x for `moduleResolution: "bundler"` support
- [ ] **Fix TypeScript errors** from WarpDrive generics (id nullability, relationship types)

---

## Testing

```bash
cd codemods && npm test
```

**453+ tests** across 9 test suites covering all phases, utilities, CLI wrapper, and post-migration checker.

> **Tip**: Some chained patterns like `items.filterBy('active').sortBy('name')` may require running Phase 0 twice, since jscodeshift processes outer call expressions first.

---

## Project Structure

```
codemods/
├── src/
│   ├── utils/
│   │   ├── imports.ts              addImport, removeImport, isUsedOnlyAsType
│   │   ├── decorators.ts           getDecorators, classifyMember
│   │   ├── schema-builder.ts       buildSchemaFile, buildModelStub
│   │   ├── ember-apis.ts           Pattern matchers (isSortBy, isMapBy, ...)
│   │   ├── gts-support.ts          .gts/.gjs <template> extraction
│   │   └── reporter.ts             Phase summary + grand summary reporting
│   │
│   ├── cli.ts                            CLI wrapper — single command migration
│   ├── post-check.ts                     Post-migration diagnostic scanner
│   ├── phase-0-deprecation-cleanup.ts    32 deprecated API transforms
│   ├── phase-1-import-migration.ts       Import rewrites + [Type] brand
│   ├── phase-2a-consumer-migration.ts    value→type imports, toArray, sortBy
│   ├── phase-3a-model-to-schema.ts       Model→Schema+Extension extraction
│   ├── phase-3b-schema-index.ts          Barrel file generator (standalone)
│   └── phase-4-mirror-to-official.ts     @warp-drive-mirror→@warp-drive
│
└── __tests__/
    ├── fixtures/                   Before/after .input.ts/.output.ts pairs
    ├── cli.test.ts                 CLI wrapper + reporter tests
    ├── post-check.test.ts          Post-migration checker tests
    ├── phase-0.test.ts             69 tests
    ├── phase-1.test.ts             73 tests
    ├── phase-2a.test.ts            17 tests
    ├── phase-3a.test.ts            51 tests
    ├── phase-3b.test.ts            10 tests
    ├── phase-4.test.ts             12 tests
    └── utils.test.ts               97 tests
```

### Architecture

```
                    ┌─────────────┐
                    │ ember-apis  │  Pattern matchers:
                    │             │  isSortBy, isMapBy, isFilterBy,
                    │             │  isEmberGet, isEmberSet, ...
                    └──────┬──────┘
                           │ used by
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ Phase 0  │ │ Phase 2a │ │ Phase 1  │
        │ Deprec.  │ │ Consumer │ │ Imports  │
        │ cleanup  │ │ migration│ │ migration│
        └──────────┘ └──────────┘ └─────┬────┘
                                        │ feeds into
              ┌─────────────┐           │
              │ decorators  │           │
              │             │  classifyMember,      ┌──────────┐
              │             │  getDecorators         │ Phase 3a │
              └──────┬──────┘           │            │ Model →  │
                     │ used by          └───────────►│ Schema   │
                     └──────────────────────────────►│          │
              ┌─────────────┐                        └──────────┘
              │ schema-     │  buildSchemaFile,            │
              │ builder     │  buildModelStub               │
              └──────┬──────┘                               │
                     │ used by                              │
                     └──────────────────────────────────────┘

              ┌─────────────┐      ┌─────────────┐
              │  imports    │      │ gts-support  │
              │             │      │              │
              │ addImport,  │      │ withGts-     │
              │ removeImport│      │ Support()    │
              └──────┬──────┘      └──────┬───────┘
                     │ used by all        │ wraps all
                     └────────────────────┘ phase transforms
```

---

## Technical Notes

<details>
<summary><strong>Parser & Decorator Handling</strong></summary>

- **Parser**: `@babel/parser` with TypeScript support in "legacy" decorator mode (default for `--parser=ts`)
- **Decorator access**: Uses `ClassProperty.decorators` directly — workaround for [jscodeshift#469](https://github.com/facebook/jscodeshift/issues/469) where `root.find(j.Decorator)` misses decorators on ClassProperty nodes
- **Bare decorators**: Handles `@Foo.bar` (MemberExpression without call parens)
</details>

<details>
<summary><strong>Safety Features</strong></summary>

| Feature | Description |
|---------|-------------|
| **False positive guards** | `obj.get()` / `obj.set()` skip non-Ember receivers (Map, URLSearchParams, Headers, FormData, etc.) and `new` expressions. `.toArray()` skips `new` expression receivers. |
| **Side-effect safety** | `isEmpty()`, `isPresent()`, `removeObject`, `removeObjects`, `setProperties` detect side-effectful arguments and wrap in IIFEs or hoist to temp variables. |
| **Invalid identifiers** | Property names like `some-prop` use computed access (`this['some-prop']`). `sortBy`/`mapBy`/`filterBy`/`findBy` skip transforms when key is not a valid JS identifier. |
| **Return values** | `pushObject` preserves return value (the item) via comma operator. `removeObject`/`removeObjects`/`pushObjects` return the array in expression context. |
| **Aliased imports** | `import { get as emberGet }` — resolves to local name before matching. |
| **Scoped transforms** | `transitionTo`/`replaceWith` only transform inside Route/Controller classes. Works with both `ClassDeclaration` and `ClassExpression`. |
| **Import-gated** | `A()`, `isArray()`, `isEmpty`, `isPresent`, `isNone`, `get`, `set` only transform when imported from their respective Ember packages. Import removal is deferred. |
</details>

<details>
<summary><strong>Schema Generation Details</strong></summary>

- **Extension bodies**: Copied as raw source text with TODO comments — `this.` → `self.` rewriting has too many unbounded patterns to automate safely
- **Relationship inverses**: Set to `null` as placeholder — correct values require domain knowledge
- **Relationship options**: Serialized as JS object literals with unquoted keys (`{ async: false, inverse: null }`). Complex values (identifiers, arrays, spreads) emit TODO comments.
- **`@attr` defaultValue**: Emits a TODO comment preserving the original options
- **`@cached` getters**: Annotated with `// NOTE: Was @cached in original model`
- **Static members / constructor**: Excluded from extension scaffolds
- **Private properties**: Both `_underscore` convention and `#private` fields classified correctly
- **Named exports**: Types/interfaces → `export type { }`. Enums/consts/functions/classes → `export { }` (value re-exports).
- **`[Type]` brand**: Self interfaces include `[Type]: 'model-name'` to satisfy `WithLegacy<T extends TypedRecordInstance>`
- **Field kinds**: All `@attr` use `kind: 'attribute'`. `@local` fields placed inline in `withDefaults()`.
- **Registration TODOs**:
  ```ts
  store.schema.registerResource(Schema);
  registerDerivations(store.schema);
  store.schema.CAUTION_MEGA_DANGER_ZONE_registerExtension(Extension);
  ```
</details>

<details>
<summary><strong>Import Handling</strong></summary>

- **Type-only detection**: Covers type annotations, type references, generics, interfaces, `as` expressions, mapped types, conditional types, indexed access types
- **Import merging**: `addImport` checks all declarations from the same source and respects `importKind` — value specifiers are never merged into type-only imports
- **Store imports**: Default import becomes type-only when used exclusively in type positions. Named value exports kept as value imports with TODO.
</details>

<details>
<summary><strong>GTS/GJS Support</strong></summary>

- Uses `content-tag` to extract `<template>` blocks, replace with valid JS placeholders, run transforms, then restore
- All phases wrapped via `withGtsSupport()`
- Placeholder restoration validated — warns if any placeholders survive in output
- Dollar signs in template content are safe (Glimmer uses `{{...}}` not `${...}`)
</details>

<details>
<summary><strong>WarpDrive Import Paths (v5.8.1)</strong></summary>

| Import | Package |
|--------|---------|
| `withDefaults` | `@warp-drive/legacy/model/migration-support` |
| `LegacyResourceSchema` | `@warp-drive/core/types/schema/fields` |
| `CAUTION_MEGA_DANGER_ZONE_Extension` (type) | `@warp-drive/core/reactive` |
| `Type` | `@warp-drive/core/types/symbols` |
| `WithLegacy` | `@warp-drive/legacy/model/migration-support` |
</details>
