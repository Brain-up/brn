import { applyTransform } from 'jscodeshift/src/testUtils';
import * as path from 'path';
import * as fs from 'fs';

const transformPath = path.resolve(__dirname, '../src/phase-2a-consumer-migration.ts');
// eslint-disable-next-line @typescript-eslint/no-var-requires
const transform = require(transformPath);

function readFixture(name: string, suffix: 'input' | 'output'): string {
  return fs.readFileSync(
    path.join(__dirname, 'fixtures', 'phase-2a', `${name}.${suffix}.ts`),
    'utf-8',
  );
}

function runTransform(input: string, filePath = 'app/routes/exercises.ts'): string {
  return applyTransform(
    transform,
    { appName: 'brn' },
    { source: input, path: filePath },
    { parser: 'ts' },
  );
}

describe('Phase 2a: Consumer Migration', () => {
  describe('full consumer file transformation', () => {
    it('should migrate store import, model type import, and toArray', () => {
      const input = readFixture('consumer-migration', 'input');
      const expected = readFixture('consumer-migration', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });
  });

  describe('store import', () => {
    it('should convert @ember-data/store to type import', () => {
      const input = `import Store from '@ember-data/store';
export default class MyRoute { store!: Store; }`;
      const result = runTransform(input);
      expect(result).toContain("import type Store from 'brn/services/store'");
    });

    it('should convert existing app store import to type import', () => {
      const input = `import Store from 'brn/services/store';
export default class MyRoute { store!: Store; }`;
      const result = runTransform(input);
      expect(result).toContain('import type Store');
    });

    it('should NOT convert @ember-data/store to type import when Store is used as a value', () => {
      const input = `import Store from '@ember-data/store';
const instance = Store.create();`;
      const result = runTransform(input);
      expect(result).toContain("from 'brn/services/store'");
      expect(result).not.toContain('import type Store');
    });

    it('should NOT convert app store import to type import when Store is used as a value', () => {
      const input = `import Store from 'brn/services/store';
const instance = Store.create();`;
      const result = runTransform(input);
      expect(result).not.toContain('import type Store');
    });
  });

  describe('value-to-type import conversion', () => {
    it('should convert model imports used only as types to import type', () => {
      const input = `import Exercise from 'brn/models/exercise';
export default class MyRoute {
  model(): Exercise[] {
    return [];
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('import type Exercise');
    });

    it('should NOT convert model imports used as values', () => {
      const input = `import Exercise from 'brn/models/exercise';
export default class MyRoute {
  model() {
    const x = new Exercise();
    return x;
  }
}`;
      const result = runTransform(input);
      expect(result).not.toContain('import type Exercise');
    });

    it('should convert model imports used only in type annotations', () => {
      const input = `import Task from 'brn/models/task';
export default class MyComponent {
  task: Task | null = null;
}`;
      const result = runTransform(input);
      expect(result).toContain('import type Task');
    });

    it('should NOT convert model imports used in typeof expressions', () => {
      const input = `import Exercise from 'brn/models/exercise';
export default class MyRoute {
  model() {
    console.log(Exercise);
    return null;
  }
}`;
      const result = runTransform(input);
      expect(result).not.toContain('import type Exercise');
    });
  });

  describe('toArray transform', () => {
    it('should convert .toArray() to Array.from()', () => {
      const input = `const items = data.toArray();`;
      const result = runTransform(input);
      expect(result).toContain('Array.from(data)');
    });
  });

  describe('sortBy transform', () => {
    it('should convert .sortBy("key") to Array.from().sort() with safe comparator', () => {
      const input = `const sorted = items.sortBy('order');`;
      const result = runTransform(input);
      expect(result).toContain('Array.from(items).sort');
      expect(result).toContain('a.order < b.order');
    });
  });

  describe('.gts file support', () => {
    it('should transform store import and toArray in .gts files while preserving <template> blocks', () => {
      const input = `import Store from '@ember-data/store';
import Exercise from 'brn/models/exercise';

export default class ExercisesRoute {
  store!: Store;

  model(): Exercise[] {
    const data = this.store.peekAll('exercise');
    return data.toArray();
  }

  <template>
    <div>{{this.model}}</div>
  </template>
}`;
      const result = applyTransform(
        transform,
        { appName: 'brn' },
        { source: input, path: 'app/routes/exercises.gts' },
        { parser: 'ts' },
      );
      // Store import should be migrated to type import
      expect(result).toContain("import type Store from 'brn/services/store'");
      // Model import should become type import
      expect(result).toContain('import type Exercise');
      // toArray should be converted
      expect(result).toContain('Array.from(data)');
      // Template should be preserved
      expect(result).toContain('<template>');
      expect(result).toContain('{{this.model}}');
      expect(result).toContain('</template>');
    });
  });

  describe('sortBy multi-key transform', () => {
    it('should convert .sortBy("k1", "k2") to Array.from().sort() with multi-key comparator', () => {
      const input = `const sorted = items.sortBy('name', 'order');`;
      const result = runTransform(input);
      expect(result).toContain('Array.from(items).sort');
      expect(result).toContain('a.name');
      expect(result).toContain('b.name');
      expect(result).toContain('a.order');
      expect(result).toContain('b.order');
    });

    it('should skip sortBy with invalid identifier keys like nested.path', () => {
      const input = `const sorted = items.sortBy('nested.path');`;
      const result = runTransform(input);
      // Should not transform since 'nested.path' is not a valid identifier
      expect(result).not.toContain('Array.from');
    });

    it('should skip multi-key sortBy when any key is invalid', () => {
      const input = `const sorted = items.sortBy('name', 'nested.path');`;
      const result = runTransform(input);
      // Should not transform since 'nested.path' is not a valid identifier
      expect(result).not.toContain('Array.from');
    });
  });

  describe('no changes', () => {
    it('should return undefined when no transformations apply', () => {
      const input = `import Component from '@glimmer/component';
export default class Foo extends Component {}`;
      const result = applyTransform(
        transform,
        { appName: 'brn' },
        { source: input, path: 'app/components/foo.ts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });
  });
});
