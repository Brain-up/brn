import { applyTransform } from 'jscodeshift/src/testUtils';
import * as path from 'path';
import * as fs from 'fs';

const transformPath = path.resolve(__dirname, '../src/phase-4-mirror-to-official.ts');
// eslint-disable-next-line @typescript-eslint/no-var-requires
const transform = require(transformPath);

function readFixture(name: string, suffix: 'input' | 'output'): string {
  return fs.readFileSync(
    path.join(__dirname, 'fixtures', 'phase-4', `${name}.${suffix}.ts`),
    'utf-8',
  );
}

function runTransform(input: string): string {
  return applyTransform(
    transform,
    {},
    { source: input, path: 'app/models/foo.ts' },
    { parser: 'ts' },
  );
}

describe('Phase 4: Mirror to Official', () => {
  describe('import source replacement', () => {
    it('should replace @warp-drive-mirror with @warp-drive', () => {
      const input = readFixture('mirror-to-official', 'input');
      const expected = readFixture('mirror-to-official', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });
  });

  describe('individual replacements', () => {
    it('should replace @warp-drive-mirror/core', () => {
      const input = `import { Type } from '@warp-drive-mirror/core/types/symbols';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/core/types/symbols'");
    });

    it('should replace @warp-drive-mirror/legacy', () => {
      const input = `import Model from '@warp-drive-mirror/legacy/model';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/legacy/model'");
    });

    it('should replace @warp-drive-mirror/json-api', () => {
      const input = `import JSONAPIAdapter from '@warp-drive-mirror/json-api/adapter';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/json-api/adapter'");
    });

    it('should replace @warp-drive-mirror/ember', () => {
      const input = `import '@warp-drive-mirror/ember/install';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/ember/install'");
    });

    it('should handle deep import paths', () => {
      const input = `import { withDefaults } from '@warp-drive-mirror/legacy/model/migration-support';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/legacy/model/migration-support'");
    });

    it('should handle type imports', () => {
      const input = `import type { LegacyResourceSchema } from '@warp-drive-mirror/core/types/schema/fields';`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/core/types/schema/fields'");
    });
  });

  describe('multiple imports in same file', () => {
    it('should replace all mirror imports', () => {
      const input = `import Model from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import { withDefaults } from '@warp-drive-mirror/legacy/model/migration-support';`;
      const result = runTransform(input);
      expect(result).not.toContain('@warp-drive-mirror');
      expect(result).toContain("'@warp-drive/legacy/model'");
      expect(result).toContain("'@warp-drive/core/types/symbols'");
      expect(result).toContain("'@warp-drive/legacy/model/migration-support'");
    });
  });

  describe('require() calls', () => {
    it('should replace @warp-drive-mirror in require()', () => {
      const input = `const Model = require('@warp-drive-mirror/legacy/model');`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/legacy/model'");
      expect(result).not.toContain('@warp-drive-mirror');
    });

    it('should replace @warp-drive-mirror in require() with deep path', () => {
      const input = `const { Type } = require('@warp-drive-mirror/core/types/symbols');`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/core/types/symbols'");
    });

    it('should not modify non-mirror require()', () => {
      const input = `const x = require('@warp-drive/legacy/model');`;
      const result = applyTransform(
        transform,
        {},
        { source: input, path: 'app/models/foo.ts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });
  });

  describe('dynamic import() calls', () => {
    it('should replace @warp-drive-mirror in dynamic import()', () => {
      const input = `const mod = import('@warp-drive-mirror/legacy/model');`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/legacy/model'");
      expect(result).not.toContain('@warp-drive-mirror');
    });

    it('should replace @warp-drive-mirror in async dynamic import()', () => {
      const input = `async function load() { const m = await import('@warp-drive-mirror/core/types/symbols'); }`;
      const result = runTransform(input);
      expect(result).toContain("'@warp-drive/core/types/symbols'");
      expect(result).not.toContain('@warp-drive-mirror');
    });

    it('should not modify non-mirror dynamic import()', () => {
      const input = `const mod = import('@warp-drive/legacy/model');`;
      const result = applyTransform(
        transform,
        {},
        { source: input, path: 'app/models/foo.ts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });
  });

  describe('no changes', () => {
    it('should return undefined when no mirror imports exist', () => {
      const input = `import Model from '@warp-drive/legacy/model';
export default class Foo extends Model {}`;
      const result = applyTransform(
        transform,
        {},
        { source: input, path: 'app/models/foo.ts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });

    it('should not modify non-warp-drive imports', () => {
      const input = `import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';`;
      const result = applyTransform(
        transform,
        {},
        { source: input, path: 'app/components/foo.ts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });
  });
});
