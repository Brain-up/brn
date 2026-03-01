import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import {
  walkDir,
  checkStoreService,
  checkWarpDriveInstall,
  checkRemainingEmberDataImports,
  checkRemainingBarrelImports,
  checkRemainingEmberUtils,
  checkRemainingEmberArray,
  checkCodemodTodos,
  checkInverseNull,
  checkExtensionThisToSelf,
  checkRemainingDeprecatedApis,
  checkRemainingTransitionMethods,
  checkRemainingGetSet,
  checkRemainingSetProperties,
  checkRemainingAdapters,
  checkRemainingSerializers,
  checkRemainingOldTransforms,
  checkModelImportsNotRewrittenToSchemas,
  printCheckResults,
  formatCheckResultsJson,
  parsePostCheckArgs,
  runAllChecks,
} from '../src/post-check';

function createTempDir(): string {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'post-check-test-'));
}

describe('Post-check: walkDir', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should return empty array for empty directory', () => {
    expect(walkDir(tempDir)).toEqual([]);
  });

  it('should return empty array for non-existent directory', () => {
    expect(walkDir('/nonexistent/path')).toEqual([]);
  });

  it('should find files recursively', () => {
    fs.writeFileSync(path.join(tempDir, 'a.ts'), '', 'utf-8');
    fs.mkdirSync(path.join(tempDir, 'sub'));
    fs.writeFileSync(path.join(tempDir, 'sub', 'b.ts'), '', 'utf-8');

    const files = walkDir(tempDir);
    expect(files).toHaveLength(2);
    expect(files.some((f) => f.endsWith('a.ts'))).toBe(true);
    expect(files.some((f) => f.endsWith('b.ts'))).toBe(true);
  });
});

describe('Post-check: checkStoreService', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when services/store.ts exists', () => {
    fs.mkdirSync(path.join(tempDir, 'services'), { recursive: true });
    fs.writeFileSync(
      path.join(tempDir, 'services', 'store.ts'),
      'export default class Store {}',
      'utf-8',
    );
    const result = checkStoreService(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when services/store.js exists', () => {
    fs.mkdirSync(path.join(tempDir, 'services'), { recursive: true });
    fs.writeFileSync(
      path.join(tempDir, 'services', 'store.js'),
      'export default class Store {}',
      'utf-8',
    );
    const result = checkStoreService(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should fail when store service is missing', () => {
    const result = checkStoreService(tempDir);
    expect(result.status).toBe('fail');
    expect(result.message).toContain('create app/services/store.ts');
  });
});

describe('Post-check: checkWarpDriveInstall', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when import exists in app.ts', () => {
    fs.writeFileSync(
      path.join(tempDir, 'app.ts'),
      "import '@warp-drive/ember/install';\n",
      'utf-8',
    );
    const result = checkWarpDriveInstall(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when import exists in app.gts', () => {
    fs.writeFileSync(
      path.join(tempDir, 'app.gts'),
      "import '@warp-drive/ember/install';\n",
      'utf-8',
    );
    const result = checkWarpDriveInstall(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should fail when no app entry file has the import', () => {
    fs.writeFileSync(
      path.join(tempDir, 'app.ts'),
      "import Application from '@ember/application';\n",
      'utf-8',
    );
    const result = checkWarpDriveInstall(tempDir);
    expect(result.status).toBe('fail');
    expect(result.message).toContain("import '@warp-drive/ember/install'");
  });

  it('should fail when no app entry file exists', () => {
    const result = checkWarpDriveInstall(tempDir);
    expect(result.status).toBe('fail');
  });
});

describe('Post-check: checkRemainingEmberDataImports', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no @ember-data/ imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import Store from '@warp-drive/store';\n",
      'utf-8',
    );
    const result = checkRemainingEmberDataImports(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when @ember-data/ imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'adapter.ts'),
      "import RESTAdapter from '@ember-data/adapter/rest';\n",
      'utf-8',
    );
    const result = checkRemainingEmberDataImports(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.locations![0]).toBe('adapter.ts');
  });

  it('should pass for empty directory', () => {
    const result = checkRemainingEmberDataImports(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should only check .ts and .gts files', () => {
    fs.writeFileSync(
      path.join(tempDir, 'readme.md'),
      "Uses @ember-data/model for models.\n",
      'utf-8',
    );
    const result = checkRemainingEmberDataImports(tempDir);
    expect(result.status).toBe('pass');
  });
});

describe('Post-check: checkRemainingBarrelImports', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no barrel imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'model.ts'),
      "import Model from '@warp-drive/legacy/model';\n",
      'utf-8',
    );
    const result = checkRemainingBarrelImports(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when barrel import remains', () => {
    fs.writeFileSync(
      path.join(tempDir, 'old.ts'),
      "import DS from 'ember-data';\n",
      'utf-8',
    );
    const result = checkRemainingBarrelImports(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });
});

describe('Post-check: checkRemainingEmberUtils', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should warn when @ember/utils imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'util.ts'),
      "import { isEmpty } from '@ember/utils';\n",
      'utf-8',
    );
    const result = checkRemainingEmberUtils(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should pass when no @ember/utils imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'util.ts'),
      'const isEmpty = (v: unknown) => !v;\n',
      'utf-8',
    );
    const result = checkRemainingEmberUtils(tempDir);
    expect(result.status).toBe('pass');
  });
});

describe('Post-check: checkRemainingEmberArray', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should warn when @ember/array imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "import { A } from '@ember/array';\n",
      'utf-8',
    );
    const result = checkRemainingEmberArray(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should pass when no @ember/array imports remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'const items: string[] = [];\n',
      'utf-8',
    );
    const result = checkRemainingEmberArray(tempDir);
    expect(result.status).toBe('pass');
  });
});

describe('Post-check: checkCodemodTodos', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no codemod TODOs exist', () => {
    fs.writeFileSync(
      path.join(tempDir, 'clean.ts'),
      'const x = 1;\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when TODO codemod comments exist', () => {
    fs.writeFileSync(
      path.join(tempDir, 'model.ts'),
      '// TODO: codemod could not convert this\nconst x = 1;\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.locations![0]).toContain('model.ts:1');
  });

  it('should detect CODEMOD TODO variant', () => {
    fs.writeFileSync(
      path.join(tempDir, 'model.ts'),
      '// CODEMOD TODO: fix this\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should catch TODO about @warp-drive imports', () => {
    fs.writeFileSync(
      path.join(tempDir, 'model.ts'),
      '// TODO: verify @ember-data/store named imports exist in @warp-drive\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should catch TODO about self. rewrite', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schema.ts'),
      '// TODO: Rewrite this.propName -> self.propName\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should NOT match a regular TODO comment', () => {
    fs.writeFileSync(
      path.join(tempDir, 'service.ts'),
      '// TODO: fix later\n',
      'utf-8',
    );
    const result = checkCodemodTodos(tempDir);
    expect(result.status).toBe('pass');
  });
});

describe('Post-check: checkInverseNull', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
    fs.mkdirSync(path.join(tempDir, 'schemas'), { recursive: true });
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no inverse: null exists', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'user.ts'),
      "export const UserSchema = { fields: [{ inverse: 'posts' }] };\n",
      'utf-8',
    );
    const result = checkInverseNull(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when inverse: null found in schemas', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'user.ts'),
      "export const UserSchema = { fields: [{ inverse: null }] };\n",
      'utf-8',
    );
    const result = checkInverseNull(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.locations![0]).toContain('schemas/user.ts:1');
  });

  it('should pass when schemas directory does not exist', () => {
    const emptyDir = createTempDir();
    try {
      const result = checkInverseNull(emptyDir);
      expect(result.status).toBe('pass');
    } finally {
      fs.rmSync(emptyDir, { recursive: true, force: true });
    }
  });
});

describe('Post-check: checkExtensionThisToSelf', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
    fs.mkdirSync(path.join(tempDir, 'schemas'), { recursive: true });
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no this->self TODOs exist', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'user.ts'),
      'export const UserSchema = {};\n',
      'utf-8',
    );
    const result = checkExtensionThisToSelf(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when TODO rewrite this exists', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'user.ts'),
      '// TODO: rewrite this. to self.\nexport const UserExtension = {};\n',
      'utf-8',
    );
    const result = checkExtensionThisToSelf(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should pass when schemas directory does not exist', () => {
    const emptyDir = createTempDir();
    try {
      const result = checkExtensionThisToSelf(emptyDir);
      expect(result.status).toBe('pass');
    } finally {
      fs.rmSync(emptyDir, { recursive: true, force: true });
    }
  });
});

// ---------------------------------------------------------------------------
// New checks from PR #2809 review
// ---------------------------------------------------------------------------

describe('Post-check: checkRemainingDeprecatedApis', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no deprecated APIs remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      'const items = Array.from(records);\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn on .toArray()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      'const items = records.toArray();\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should warn on .sortBy()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "const sorted = items.sortBy('name');\n",
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .filterBy()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "const active = items.filterBy('active', true);\n",
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .mapBy()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "const names = items.mapBy('name');\n",
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .findBy()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "const item = items.findBy('id', 1);\n",
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .pushObject()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'items.pushObject(newItem);\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .uniq()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'const unique = items.uniq();\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .firstObject', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'const first = items.firstObject;\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on .lastObject', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'const last = items.lastObject;\n',
      'utf-8',
    );
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should pass for empty directory', () => {
    const result = checkRemainingDeprecatedApis(tempDir);
    expect(result.status).toBe('pass');
  });
});

describe('Post-check: checkRemainingTransitionMethods', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no transition methods remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "this.router.transitionTo('home');\n",
      'utf-8',
    );
    const result = checkRemainingTransitionMethods(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn on this.transitionTo()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "this.transitionTo('home');\n",
      'utf-8',
    );
    const result = checkRemainingTransitionMethods(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should warn on this.replaceWith()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "this.replaceWith('login');\n",
      'utf-8',
    );
    const result = checkRemainingTransitionMethods(tempDir);
    expect(result.status).toBe('warn');
  });
});

describe('Post-check: checkRemainingGetSet', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no this.get()/this.set() calls remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'const name = this.name;\n',
      'utf-8',
    );
    const result = checkRemainingGetSet(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn on this.get()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "const name = this.get('name');\n",
      'utf-8',
    );
    const result = checkRemainingGetSet(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });

  it('should warn on this.set()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "this.set('name', 'John');\n",
      'utf-8',
    );
    const result = checkRemainingGetSet(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should detect import { get } from @ember/object', () => {
    fs.writeFileSync(
      path.join(tempDir, 'helper.ts'),
      "import { get } from '@ember/object';\nconst val = get(obj, 'prop');\n",
      'utf-8',
    );
    const result = checkRemainingGetSet(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations!.length).toBeGreaterThanOrEqual(1);
  });

  it('should detect obj.get() on non-this receiver', () => {
    fs.writeFileSync(
      path.join(tempDir, 'service.ts'),
      "const val = obj.get('prop');\n",
      'utf-8',
    );
    const result = checkRemainingGetSet(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });
});

describe('Post-check: checkRemainingSetProperties', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no setProperties calls remain', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      'this.name = "John"; this.age = 30;\n',
      'utf-8',
    );
    const result = checkRemainingSetProperties(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn on .setProperties()', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "model.setProperties({ name: 'John', age: 30 });\n",
      'utf-8',
    );
    const result = checkRemainingSetProperties(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
  });
});

describe('Post-check: checkRemainingAdapters', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no adapters directory exists', () => {
    const result = checkRemainingAdapters(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when adapters directory is empty', () => {
    fs.mkdirSync(path.join(tempDir, 'adapters'));
    const result = checkRemainingAdapters(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when adapter files exist', () => {
    fs.mkdirSync(path.join(tempDir, 'adapters'));
    fs.writeFileSync(
      path.join(tempDir, 'adapters', 'application.ts'),
      'export default class ApplicationAdapter {}',
      'utf-8',
    );
    const result = checkRemainingAdapters(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.locations![0]).toContain('adapters/application.ts');
    expect(result.message).toContain('RequestManager handlers');
  });
});

describe('Post-check: checkRemainingSerializers', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no serializers directory exists', () => {
    const result = checkRemainingSerializers(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when serializers directory is empty', () => {
    fs.mkdirSync(path.join(tempDir, 'serializers'));
    const result = checkRemainingSerializers(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when serializer files exist', () => {
    fs.mkdirSync(path.join(tempDir, 'serializers'));
    fs.writeFileSync(
      path.join(tempDir, 'serializers', 'application.ts'),
      'export default class ApplicationSerializer {}',
      'utf-8',
    );
    const result = checkRemainingSerializers(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.message).toContain('RequestManager handlers');
  });
});

describe('Post-check: checkRemainingOldTransforms', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no transforms directory exists', () => {
    const result = checkRemainingOldTransforms(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when transforms directory is empty', () => {
    fs.mkdirSync(path.join(tempDir, 'transforms'));
    const result = checkRemainingOldTransforms(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when transform files exist', () => {
    fs.mkdirSync(path.join(tempDir, 'transforms'));
    fs.writeFileSync(
      path.join(tempDir, 'transforms', 'array.ts'),
      'export default class ArrayTransform {}',
      'utf-8',
    );
    const result = checkRemainingOldTransforms(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.message).toContain('WarpDrive Transformations');
  });
});

// ---------------------------------------------------------------------------
// parsePostCheckArgs
// ---------------------------------------------------------------------------

describe('Post-check: parsePostCheckArgs', () => {
  it('should parse --target', () => {
    const opts = parsePostCheckArgs(['--target=frontend/app']);
    expect(opts.target).toBe('frontend/app');
  });

  it('should parse --strict flag', () => {
    const opts = parsePostCheckArgs(['--strict']);
    expect(opts.strict).toBe(true);
  });

  it('should parse --json flag', () => {
    const opts = parsePostCheckArgs(['--json']);
    expect(opts.json).toBe(true);
  });

  it('should parse --verbose flag', () => {
    const opts = parsePostCheckArgs(['--verbose']);
    expect(opts.verbose).toBe(true);
  });

  it('should default all flags to false', () => {
    const opts = parsePostCheckArgs(['--target=app']);
    expect(opts.strict).toBe(false);
    expect(opts.json).toBe(false);
    expect(opts.verbose).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// runAllChecks
// ---------------------------------------------------------------------------

describe('Post-check: runAllChecks', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should return results for all 17 checks', () => {
    const results = runAllChecks(tempDir);
    expect(results).toHaveLength(17);
  });

  it('should include new checks in results', () => {
    const results = runAllChecks(tempDir);
    const names = results.map((r) => r.name);
    expect(names.some((n) => n.includes('deprecated array APIs'))).toBe(true);
    expect(names.some((n) => n.includes('transitionTo'))).toBe(true);
    expect(names.some((n) => n.includes('get()/set()'))).toBe(true);
    expect(names.some((n) => n.includes('setProperties'))).toBe(true);
    expect(names.some((n) => n.includes('adapter'))).toBe(true);
    expect(names.some((n) => n.includes('serializer'))).toBe(true);
    expect(names.some((n) => n.includes('legacy transforms'))).toBe(true);
    expect(names.some((n) => n.toLowerCase().includes('model imports') || n.toLowerCase().includes('schema'))).toBe(true);
    expect(names.some((n) => n.includes('@ember/utils'))).toBe(true);
    expect(names.some((n) => n.includes('@ember/array'))).toBe(true);
  });

  it('should not return stale cached data between runs', () => {
    // Create a file with an @ember-data import
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import Model from '@ember-data/model';\n",
      'utf-8',
    );
    const results1 = runAllChecks(tempDir);
    const emberDataCheck1 = results1.find((r) =>
      r.name.includes('@ember-data/'),
    );
    expect(emberDataCheck1!.status).toBe('warn');

    // Remove the file and run again — cache should be cleared
    fs.unlinkSync(path.join(tempDir, 'route.ts'));
    const results2 = runAllChecks(tempDir);
    const emberDataCheck2 = results2.find((r) =>
      r.name.includes('@ember-data/'),
    );
    expect(emberDataCheck2!.status).toBe('pass');
  });
});

// ---------------------------------------------------------------------------
// checkModelImportsNotRewrittenToSchemas
// ---------------------------------------------------------------------------

describe('Post-check: checkModelImportsNotRewrittenToSchemas', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = createTempDir();
    // Create schemas dir so the check activates
    fs.mkdirSync(path.join(tempDir, 'schemas'), { recursive: true });
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'user.ts'),
      'export type User = {};\n',
      'utf-8',
    );
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should pass when no consumer imports from models/', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import type { User } from 'myapp/schemas/user';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should warn when consumer still imports from models/', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import type User from 'myapp/models/user';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(1);
    expect(result.locations![0]).toContain('route.ts:1');
  });

  it('should warn on value imports from models/', () => {
    fs.writeFileSync(
      path.join(tempDir, 'component.ts'),
      "import User from 'myapp/models/user';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should warn on named imports from models/', () => {
    fs.writeFileSync(
      path.join(tempDir, 'util.ts'),
      "import { IUser } from 'myapp/models/user';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('warn');
  });

  it('should not flag files inside models/ directory itself', () => {
    fs.mkdirSync(path.join(tempDir, 'models'), { recursive: true });
    fs.writeFileSync(
      path.join(tempDir, 'models', 'user.ts'),
      "export type { User as default } from 'myapp/schemas/user';\n// This is the stub — it references schemas/ but lives in models/\n",
      'utf-8',
    );
    // No consumer files — only the stub in models/
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should not flag files inside schemas/ directory', () => {
    fs.writeFileSync(
      path.join(tempDir, 'schemas', 'exercise.ts'),
      "import type { User } from 'myapp/models/user';\n", // schema cross-ref (unlikely but test coverage)
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('pass');
  });

  it('should pass when no schemas directory exists', () => {
    const noSchemasDir = createTempDir();
    try {
      fs.writeFileSync(
        path.join(noSchemasDir, 'route.ts'),
        "import User from 'myapp/models/user';\n",
        'utf-8',
      );
      const result = checkModelImportsNotRewrittenToSchemas(noSchemasDir);
      expect(result.status).toBe('pass');
    } finally {
      fs.rmSync(noSchemasDir, { recursive: true, force: true });
    }
  });

  it('should detect multiple imports across files', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import type User from 'myapp/models/user';\n",
      'utf-8',
    );
    fs.writeFileSync(
      path.join(tempDir, 'controller.ts'),
      "import type Exercise from 'myapp/models/exercise';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.status).toBe('warn');
    expect(result.locations).toHaveLength(2);
  });

  it('should include helpful message about stubs', () => {
    fs.writeFileSync(
      path.join(tempDir, 'route.ts'),
      "import User from 'myapp/models/user';\n",
      'utf-8',
    );
    const result = checkModelImportsNotRewrittenToSchemas(tempDir);
    expect(result.message).toContain('schemas/');
    expect(result.message).toContain('stubs');
  });
});

// ---------------------------------------------------------------------------
// printCheckResults
// ---------------------------------------------------------------------------

describe('Post-check: printCheckResults', () => {
  let logSpy: jest.SpyInstance;

  beforeEach(() => {
    logSpy = jest.spyOn(console, 'log').mockImplementation();
  });

  afterEach(() => {
    logSpy.mockRestore();
  });

  it('should print header', () => {
    printCheckResults([]);
    expect(logSpy).toHaveBeenCalledWith('=== Post-Migration Checklist ===');
  });

  it('should print summary counts', () => {
    printCheckResults([
      { name: 'Check 1', status: 'pass' },
      { name: 'Check 2', status: 'warn', locations: ['file.ts:1'] },
      { name: 'Check 3', status: 'fail', message: 'Missing something' },
    ]);

    const summaryCall = logSpy.mock.calls.find(
      (args: string[]) =>
        typeof args[0] === 'string' && args[0].includes('Summary:'),
    );
    expect(summaryCall).toBeDefined();
    expect(summaryCall![0]).toContain('1 passed');
    expect(summaryCall![0]).toContain('1 warnings');
    expect(summaryCall![0]).toContain('1 failures');
  });

  it('should print [PASS], [WARN], [FAIL] labels', () => {
    printCheckResults([
      { name: 'Pass check', status: 'pass' },
      { name: 'Warn check', status: 'warn' },
      { name: 'Fail check', status: 'fail', message: 'Fix it' },
    ]);

    expect(logSpy).toHaveBeenCalledWith('[PASS] Pass check');
    expect(logSpy).toHaveBeenCalledWith('[WARN] Warn check');
    expect(logSpy).toHaveBeenCalledWith('[FAIL] Fail check');
  });

  it('should print locations when verbose (default)', () => {
    printCheckResults([
      {
        name: 'Check with locations',
        status: 'warn',
        locations: ['file1.ts:10', 'file2.ts:20'],
      },
    ]);

    expect(logSpy).toHaveBeenCalledWith('       file1.ts:10');
    expect(logSpy).toHaveBeenCalledWith('       file2.ts:20');
  });

  it('should suppress locations when verbose=false', () => {
    printCheckResults(
      [
        {
          name: 'Check with locations',
          status: 'warn',
          locations: ['file1.ts:10', 'file2.ts:20'],
        },
      ],
      { verbose: false },
    );

    expect(logSpy).not.toHaveBeenCalledWith('       file1.ts:10');
    expect(logSpy).toHaveBeenCalledWith('       (use --verbose to see all locations)');
  });

  it('should print manual attention count', () => {
    printCheckResults([
      {
        name: 'Warnings',
        status: 'warn',
        locations: ['a.ts:1', 'b.ts:2', 'c.ts:3'],
      },
    ]);

    const manualCall = logSpy.mock.calls.find(
      (args: string[]) =>
        typeof args[0] === 'string' && args[0].includes('manual attention'),
    );
    expect(manualCall).toBeDefined();
    expect(manualCall![0]).toContain('3 items');
  });
});

// ---------------------------------------------------------------------------
// formatCheckResultsJson
// ---------------------------------------------------------------------------

describe('Post-check: formatCheckResultsJson', () => {
  it('should produce valid JSON', () => {
    const results = [
      { name: 'Check 1', status: 'pass' as const },
      { name: 'Check 2', status: 'warn' as const, locations: ['a.ts:1'] },
      { name: 'Check 3', status: 'fail' as const, message: 'Fix it' },
    ];
    const json = formatCheckResultsJson(results);
    const parsed = JSON.parse(json);
    expect(parsed.results).toHaveLength(3);
    expect(parsed.summary.passed).toBe(1);
    expect(parsed.summary.warnings).toBe(1);
    expect(parsed.summary.failures).toBe(1);
  });

  it('should include locations in JSON output', () => {
    const results = [
      { name: 'Check', status: 'warn' as const, locations: ['a.ts:1', 'b.ts:2'] },
    ];
    const parsed = JSON.parse(formatCheckResultsJson(results));
    expect(parsed.results[0].locations).toEqual(['a.ts:1', 'b.ts:2']);
  });

  it('should default message to null and locations to empty array', () => {
    const results = [{ name: 'Check', status: 'pass' as const }];
    const parsed = JSON.parse(formatCheckResultsJson(results));
    expect(parsed.results[0].message).toBeNull();
    expect(parsed.results[0].locations).toEqual([]);
  });

  it('should count manual items correctly', () => {
    const results = [
      { name: 'C1', status: 'warn' as const, locations: ['a.ts:1', 'b.ts:2'] },
      { name: 'C2', status: 'fail' as const, message: 'Missing' },
    ];
    const parsed = JSON.parse(formatCheckResultsJson(results));
    expect(parsed.summary.manualItems).toBe(3); // 2 locations + 1 fail
  });
});
