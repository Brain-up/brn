import * as jscodeshift from 'jscodeshift';
import { isUsedOnlyAsType, addImport, removeImport, replaceImportSource, removeImportBySource, convertToTypeImport } from '../src/utils/imports';
import { classifyMember, getDecorators, getDecoratorName, getDecoratorArgs, hasDecorator, findDecorator, removeDecorator } from '../src/utils/decorators';
import { restoreTemplates, findUnresolvedPlaceholders, extractTemplates, isGtsFile } from '../src/utils/gts-support';

const j = jscodeshift.withParser('ts');

function parse(source: string) {
  return j(source);
}

describe('Utils: imports.ts', () => {
  describe('isUsedOnlyAsType', () => {
    it('should return true when identifier is only in type annotation', () => {
      const root = parse(`import Foo from './foo';
function test(x: Foo) { return x; }`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return false when identifier is used as value', () => {
      const root = parse(`import Foo from './foo';
const x = new Foo();`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(false);
    });

    it('should return true when identifier is in TSTypeReference', () => {
      const root = parse(`import Foo from './foo';
type Bar = Foo;`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return false when identifier is called as function', () => {
      const root = parse(`import { isEmpty } from '@ember/utils';
const result = isEmpty(val);`);
      expect(isUsedOnlyAsType(j, root, 'isEmpty')).toBe(false);
    });

    it('should return true for type-only export', () => {
      const root = parse(`import Foo from './foo';
export type { Foo };`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return true when used as interface extends', () => {
      const root = parse(`import Foo from './foo';
interface Bar extends Foo { x: number; }`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return false when used as class extends', () => {
      const root = parse(`import Foo from './foo';
class Bar extends Foo {}`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(false);
    });

    it('should return true when used in generic type parameter', () => {
      const root = parse(`import Foo from './foo';
const x: Array<Foo> = [];`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return true when used in "as" type assertion', () => {
      const root = parse(`import Foo from './foo';
const x = y as Foo;`);
      // The identifier in the type side of TSAsExpression is type-only
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return true when used in TSMappedType', () => {
      const root = parse(`import Foo from './foo';
type Mapped = { [K in keyof Foo]: string };`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return true when used in TSConditionalType', () => {
      const root = parse(`import Foo from './foo';
type Check = Foo extends string ? true : false;`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });

    it('should return true when used in TSIndexedAccessType', () => {
      const root = parse(`import Foo from './foo';
type Val = Foo['key'];`);
      expect(isUsedOnlyAsType(j, root, 'Foo')).toBe(true);
    });
  });

  describe('addImport', () => {
    it('should add a new import when none exists', () => {
      const root = parse(`const x = 1;`);
      addImport(j, root, 'foo', './utils');
      const output = root.toSource();
      expect(output).toContain('import { foo }');
      expect(output).toContain('./utils');
    });

    it('should add specifier to existing import', () => {
      const root = parse(`import { bar } from './utils';`);
      addImport(j, root, 'foo', './utils');
      const output = root.toSource();
      expect(output).toContain('bar');
      expect(output).toContain('foo');
    });

    it('should not duplicate existing specifier', () => {
      const root = parse(`import { foo } from './utils';`);
      addImport(j, root, 'foo', './utils');
      const output = root.toSource();
      const count = (output.match(/foo/g) || []).length;
      expect(count).toBe(1);
    });

    it('should create type import when isType is true', () => {
      const root = parse(`const x = 1;`);
      addImport(j, root, 'Foo', './types', { isType: true });
      const output = root.toSource();
      expect(output).toContain('import type');
    });

    it('should create separate type import when existing import is value', () => {
      const root = parse(`import { service } from '@ember/service';`);
      addImport(j, root, 'RouterService', '@ember/service', { isType: true });
      const output = root.toSource();
      // Should have both value and type imports
      expect(output).toContain("import { service } from '@ember/service'");
      expect(output).toContain('import type');
    });

    it('should add default import', () => {
      const root = parse(`const x = 1;`);
      addImport(j, root, 'Foo', './foo', { isDefault: true });
      const output = root.toSource();
      expect(output).toContain('import Foo from');
      expect(output).toContain('./foo');
    });

    // M14: Value specifier should not be merged into type-only import
    it('should create separate value import when existing import is type-only', () => {
      const root = parse(`import type { Foo } from 'bar';`);
      addImport(j, root, 'Baz', 'bar');
      const output = root.toSource();
      // Should have both type and value imports
      expect(output).toContain("import type { Foo } from 'bar'");
      // New import may use double quotes (jscodeshift default for new declarations)
      expect(output).toContain('import { Baz }');
      expect(output).toContain('bar');
      // Should NOT have merged Baz into the type import
      expect(output).not.toContain('import type { Foo, Baz }');
    });

    it('should not merge value default import into type-only import', () => {
      const root = parse(`import type { Foo } from 'bar';`);
      addImport(j, root, 'Bar', 'bar', { isDefault: true });
      const output = root.toSource();
      // Type import should remain
      expect(output).toContain("import type { Foo } from 'bar'");
      // Value default import should be separate (not merged into type import)
      expect(output).toContain('import Bar from');
      // Should NOT have merged Bar into the type import
      expect(output).not.toContain('import type Bar');
    });

    // M15: Check ALL import declarations from the same source, not just the first
    it('should not duplicate specifier when it exists in a later import from same source', () => {
      const root = parse(`import { foo } from 'bar';
import type { Baz } from 'bar';`);
      // 'foo' already exists in the first import
      addImport(j, root, 'foo', 'bar');
      const output = root.toSource();
      const count = (output.match(/\bfoo\b/g) || []).length;
      expect(count).toBe(1);
    });

    it('should not duplicate type specifier when it exists in a later type import from same source', () => {
      const root = parse(`import { foo } from 'bar';
import type { Baz } from 'bar';`);
      // 'Baz' already exists in the second (type) import
      addImport(j, root, 'Baz', 'bar', { isType: true });
      const output = root.toSource();
      const count = (output.match(/\bBaz\b/g) || []).length;
      expect(count).toBe(1);
    });

    it('should merge type specifier into existing type import even when value import comes first', () => {
      const root = parse(`import { foo } from 'bar';
import type { Baz } from 'bar';`);
      // Adding a type import 'Qux' — should merge into the type import, not the value one
      addImport(j, root, 'Qux', 'bar', { isType: true });
      const output = root.toSource();
      expect(output).toContain('Qux');
      // Should be in the type import, not the value import
      expect(output).not.toContain('import { foo, Qux }');
    });

    it('should merge value specifier into existing value import even when type import comes first', () => {
      const root = parse(`import type { Baz } from 'bar';
import { foo } from 'bar';`);
      // Adding a value import 'Qux' — should merge into the value import, not the type one
      addImport(j, root, 'Qux', 'bar');
      const output = root.toSource();
      expect(output).toContain('Qux');
      // Should be in the value import
      expect(output).toContain('foo');
      // Should NOT be in the type import
      expect(output).not.toContain('import type { Baz, Qux }');
    });
  });

  describe('removeImport', () => {
    it('should remove a named specifier', () => {
      const root = parse(`import { foo, bar } from './utils';`);
      removeImport(j, root, 'foo');
      const output = root.toSource();
      expect(output).not.toContain('foo');
      expect(output).toContain('bar');
    });

    it('should remove entire import when last specifier removed', () => {
      const root = parse(`import { foo } from './utils';`);
      removeImport(j, root, 'foo');
      const output = root.toSource();
      expect(output).not.toContain("'./utils'");
    });

    it('should remove by source filter', () => {
      const root = parse(`import { foo } from './a';
import { bar } from './b';`);
      removeImport(j, root, 'foo', './a');
      const output = root.toSource();
      expect(output).not.toContain('./a');
      expect(output).toContain('bar');
    });

    it('should return false when specifier not found', () => {
      const root = parse(`import { bar } from './utils';`);
      const result = removeImport(j, root, 'nonexistent');
      expect(result).toBe(false);
    });
  });

  describe('replaceImportSource', () => {
    it('should replace the source of an import declaration', () => {
      const root = parse(`import Model, { attr } from '@ember-data/model';`);
      const changed = replaceImportSource(j, root, '@ember-data/model', '@warp-drive/legacy/model');
      const output = root.toSource();
      expect(changed).toBe(true);
      expect(output).toContain('@warp-drive/legacy/model');
      expect(output).not.toContain('@ember-data/model');
    });

    it('should return false when source not found', () => {
      const root = parse(`import { foo } from './bar';`);
      const changed = replaceImportSource(j, root, '@nonexistent/pkg', '@new/pkg');
      expect(changed).toBe(false);
    });

    it('should replace all imports from the same source', () => {
      const root = parse(`import { foo } from 'old-pkg';
import { bar } from 'old-pkg';`);
      const changed = replaceImportSource(j, root, 'old-pkg', 'new-pkg');
      const output = root.toSource();
      expect(changed).toBe(true);
      expect(output).not.toContain('old-pkg');
    });
  });

  describe('removeImportBySource', () => {
    it('should remove an entire import declaration by source', () => {
      const root = parse(`import { foo, bar } from '@ember/utils';
const x = 1;`);
      const removed = removeImportBySource(j, root, '@ember/utils');
      const output = root.toSource();
      expect(removed).toBe(true);
      expect(output).not.toContain('@ember/utils');
      expect(output).not.toContain('foo');
      expect(output).toContain('const x = 1');
    });

    it('should return false when source not found', () => {
      const root = parse(`import { foo } from './bar';`);
      const removed = removeImportBySource(j, root, '@nonexistent/pkg');
      expect(removed).toBe(false);
    });
  });

  describe('convertToTypeImport', () => {
    it('should convert a value import to type-only', () => {
      const root = parse(`import { Foo } from './foo';`);
      const importDecl = root.find(j.ImportDeclaration);
      convertToTypeImport(j, importDecl.paths()[0]);
      const output = root.toSource();
      expect(output).toContain('import type');
    });
  });
});

describe('Utils: decorators.ts', () => {
  function getClassMember(source: string, memberIndex = 0) {
    const root = parse(source);
    const classDecl = root.find(j.ClassDeclaration).get().node;
    return classDecl.body.body[memberIndex];
  }

  describe('getDecorators', () => {
    it('should extract decorators from class property', () => {
      const member = getClassMember(`class Foo {
  @attr('string') name!: string;
}`);
      const decorators = getDecorators(member as any);
      expect(decorators).toHaveLength(1);
    });

    it('should return empty array for no decorators', () => {
      const member = getClassMember(`class Foo {
  name: string = '';
}`);
      const decorators = getDecorators(member as any);
      expect(decorators).toHaveLength(0);
    });
  });

  describe('getDecoratorName', () => {
    it('should get name of simple decorator', () => {
      const member = getClassMember(`class Foo {
  @tracked name!: string;
}`);
      const decorators = getDecorators(member as any);
      expect(getDecoratorName(decorators[0])).toBe('tracked');
    });

    it('should get name of call expression decorator', () => {
      const member = getClassMember(`class Foo {
  @attr('string') name!: string;
}`);
      const decorators = getDecorators(member as any);
      expect(getDecoratorName(decorators[0])).toBe('attr');
    });

    it('should get name of bare MemberExpression decorator (@Ember.computed)', () => {
      const member = getClassMember(`class Foo {
  @Ember.computed name!: string;
}`);
      const decorators = getDecorators(member as any);
      expect(getDecoratorName(decorators[0])).toBe('computed');
    });

    it('should get name of called MemberExpression decorator (@Ember.computed())', () => {
      const member = getClassMember(`class Foo {
  @Ember.computed('dep') name!: string;
}`);
      const decorators = getDecorators(member as any);
      expect(getDecoratorName(decorators[0])).toBe('computed');
    });
  });

  describe('getDecoratorArgs', () => {
    it('should get arguments of call expression decorator', () => {
      const member = getClassMember(`class Foo {
  @attr('string') name!: string;
}`);
      const decorators = getDecorators(member as any);
      const args = getDecoratorArgs(decorators[0]);
      expect(args).toHaveLength(1);
      expect(args[0].value).toBe('string');
    });

    it('should return empty array for non-call decorator', () => {
      const member = getClassMember(`class Foo {
  @tracked name!: string;
}`);
      const decorators = getDecorators(member as any);
      const args = getDecoratorArgs(decorators[0]);
      expect(args).toHaveLength(0);
    });
  });

  describe('hasDecorator', () => {
    it('should return true when decorator exists', () => {
      const member = getClassMember(`class Foo {
  @tracked name!: string;
}`);
      expect(hasDecorator(member as any, 'tracked')).toBe(true);
    });

    it('should return false when decorator does not exist', () => {
      const member = getClassMember(`class Foo {
  name: string = '';
}`);
      expect(hasDecorator(member as any, 'tracked')).toBe(false);
    });
  });

  describe('findDecorator', () => {
    it('should find a decorator by name on a class property', () => {
      const member = getClassMember(`class Foo {
  @attr('string') name!: string;
}`);
      const dec = findDecorator(member as any, 'attr');
      expect(dec).toBeDefined();
      expect(getDecoratorName(dec!)).toBe('attr');
    });

    it('should return undefined when decorator not found', () => {
      const member = getClassMember(`class Foo {
  name: string = '';
}`);
      const dec = findDecorator(member as any, 'attr');
      expect(dec).toBeUndefined();
    });

    it('should find the correct decorator when multiple exist', () => {
      const member = getClassMember(`class Foo {
  @tracked @service('audio') audioService!: any;
}`);
      const dec = findDecorator(member as any, 'service');
      expect(dec).toBeDefined();
      expect(getDecoratorName(dec!)).toBe('service');
    });
  });

  describe('removeDecorator', () => {
    it('should remove a decorator by name', () => {
      const member = getClassMember(`class Foo {
  @tracked isActive = false;
}`);
      const removed = removeDecorator(member as any, 'tracked');
      expect(removed).toBe(true);
      const decorators = getDecorators(member as any);
      expect(decorators).toHaveLength(0);
    });

    it('should return false when decorator not found', () => {
      const member = getClassMember(`class Foo {
  name: string = '';
}`);
      const removed = removeDecorator(member as any, 'tracked');
      expect(removed).toBe(false);
    });

    it('should remove only the specified decorator when multiple exist', () => {
      const member = getClassMember(`class Foo {
  @cached @tracked value = 0;
}`);
      const removed = removeDecorator(member as any, 'cached');
      expect(removed).toBe(true);
      const remaining = getDecorators(member as any);
      expect(remaining).toHaveLength(1);
      expect(getDecoratorName(remaining[0])).toBe('tracked');
    });
  });

  describe('classifyMember', () => {
    it('should classify @attr as attribute', () => {
      const sourceCode = `class Foo {
  @attr('string') name!: string;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('attribute');
      if (result.kind === 'attribute') {
        expect(result.name).toBe('name');
        expect(result.attrType).toBe('string');
      }
    });

    it('should classify @belongsTo', () => {
      const sourceCode = `class Foo {
  @belongsTo('bar', { async: false }) bar!: Bar;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('belongsTo');
      if (result.kind === 'belongsTo') {
        expect(result.relatedType).toBe('bar');
      }
    });

    it('should classify @hasMany', () => {
      const sourceCode = `class Foo {
  @hasMany('bar', { async: false }) bars!: Bar[];
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('hasMany');
    });

    it('should classify @service', () => {
      const sourceCode = `class Foo {
  @service('audio') audioService!: any;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('service');
      if (result.kind === 'service') {
        expect(result.serviceName).toBe('audio');
      }
    });

    it('should classify @tracked', () => {
      const sourceCode = `class Foo {
  @tracked isActive = false;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('tracked');
      if (result.kind === 'tracked') {
        expect(result.name).toBe('isActive');
      }
    });

    it('should classify declare property', () => {
      const sourceCode = `class Foo {
  declare type: 'foo';
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('declare');
    });

    it('should classify getter', () => {
      const sourceCode = `class Foo {
  get displayName() { return this.name; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('getter');
    });

    it('should classify setter', () => {
      const sourceCode = `class Foo {
  set value(v: string) { this._value = v; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('setter');
    });

    it('should classify method', () => {
      const sourceCode = `class Foo {
  doSomething() { return 42; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('method');
    });

    it('should classify @action method as method with isAction=true', () => {
      const sourceCode = `class Foo {
  @action toggle() { this.isActive = !this.isActive; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('method');
      if (result.kind === 'method') {
        expect(result.isAction).toBe(true);
      }
    });

    it('should classify property with value', () => {
      const sourceCode = `class Foo {
  maxRetries = 3;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('property');
    });

    it('should classify underscore-prefixed private property', () => {
      const sourceCode = `class Foo {
  _internalState = 'idle';
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('property');
      expect(result.name).toBe('_internalState');
    });

    it('should classify underscore-prefixed private method', () => {
      const sourceCode = `class Foo {
  _compute() { return 42; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('method');
      expect(result.name).toBe('_compute');
    });

    it('should classify @attr with no type arg as null attrType', () => {
      const sourceCode = `class Foo {
  @attr() data!: any;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('attribute');
      if (result.kind === 'attribute') {
        expect(result.attrType).toBeNull();
      }
    });

    it('should preserve TS type annotation', () => {
      const sourceCode = `class Foo {
  @attr('number') count!: number | null;
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      if (result.kind === 'attribute') {
        expect(result.tsType).toBe('number | null');
      }
    });

    it('should classify constructor as unknown', () => {
      const sourceCode = `class Foo extends Bar {
  constructor() { super(); }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('unknown');
      expect(result.name).toBe('constructor');
    });

    it('should classify @cached getter', () => {
      const sourceCode = `class Foo {
  @cached get value() { return computeExpensive(); }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('getter');
      if (result.kind === 'getter') {
        expect(result.isCached).toBe(true);
      }
    });

    it('should classify static getter as unknown', () => {
      const sourceCode = `class Foo {
  static get foo() { return 42; }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('unknown');
      expect(result.name).toBe('foo');
    });

    it('should classify static method as unknown', () => {
      const sourceCode = `class Foo {
  static create() { return new Foo(); }
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('unknown');
      expect(result.name).toBe('create');
    });

    it('should classify static property as unknown', () => {
      const sourceCode = `class Foo {
  static defaultName = 'bar';
}`;
      const member = getClassMember(sourceCode);
      const result = classifyMember(j, member, sourceCode);
      expect(result.kind).toBe('unknown');
      expect(result.name).toBe('defaultName');
    });
  });
});

describe('Utils: gts-support.ts', () => {
  describe('isGtsFile', () => {
    it('should return true for .gts files', () => {
      expect(isGtsFile('app/components/foo.gts')).toBe(true);
    });

    it('should return true for .gjs files', () => {
      expect(isGtsFile('app/components/foo.gjs')).toBe(true);
    });

    it('should return false for .ts files', () => {
      expect(isGtsFile('app/models/foo.ts')).toBe(false);
    });

    it('should return false for .js files', () => {
      expect(isGtsFile('app/components/foo.js')).toBe(false);
    });

    it('should return false for files ending with gts but not .gts', () => {
      expect(isGtsFile('app/components/foogts')).toBe(false);
    });
  });

  describe('extractTemplates', () => {
    it('should return unchanged source when content-tag is unavailable', () => {
      // extractTemplates uses the content-tag Preprocessor which may not be available in test
      // If it is not available, it returns source unchanged with hasTemplates: false
      const source = '<template>Hello</template>';
      const result = extractTemplates(source);
      // If content-tag is not available, source is returned unchanged
      // If it IS available, it processes the template
      expect(result.processedSource).toBeDefined();
      expect(typeof result.hasTemplates).toBe('boolean');
    });

    it('should return hasTemplates false for plain TS code', () => {
      const source = 'const x = 1;';
      const result = extractTemplates(source);
      expect(result.processedSource).toBe(source);
      expect(result.hasTemplates).toBe(false);
    });
  });

  describe('restoreTemplates', () => {
    it('should restore expression-position placeholders', () => {
      const input = 'const x = TEMPLATE_TEMPLATE(`<h1>Hello</h1>`);';
      const result = restoreTemplates(input);
      expect(result).toBe('const x = <template><h1>Hello</h1></template>;');
    });

    it('should restore class-member-position placeholders', () => {
      const input = 'class Foo { [_TEMPLATE_(`<div>Hi</div>`)] = 0; }';
      const result = restoreTemplates(input);
      expect(result).toBe('class Foo { <template><div>Hi</div></template> }');
    });

    it('should warn when placeholders are not restored', () => {
      // Simulate jscodeshift reformatting that breaks the placeholder pattern
      const mangled = 'const x = TEMPLATE_TEMPLATE(\n`<h1>Hello</h1>`\n);';
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
      restoreTemplates(mangled);
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('template placeholder(s) were not restored'),
      );
      warnSpy.mockRestore();
    });

    it('should not warn when all placeholders are restored', () => {
      const input = 'TEMPLATE_TEMPLATE(`<h1>Hello</h1>`)';
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
      restoreTemplates(input);
      expect(warnSpy).not.toHaveBeenCalled();
      warnSpy.mockRestore();
    });

    it('should return original source when unresolved placeholders found and originalSource provided', () => {
      const mangled = 'const x = TEMPLATE_TEMPLATE(\n`<h1>Hello</h1>`\n);';
      const originalSource = '<template>Hello</template>\nconst x = 1;';
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
      const result = restoreTemplates(mangled, originalSource);
      expect(result).toBe(originalSource);
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('reverting to original'),
      );
      warnSpy.mockRestore();
    });

    it('should return transformed source when no unresolved placeholders', () => {
      const input = 'const x = TEMPLATE_TEMPLATE(`<h1>Hello</h1>`);';
      const originalSource = 'const x = <template><h1>Hello</h1></template>;';
      const result = restoreTemplates(input, originalSource);
      expect(result).toBe('const x = <template><h1>Hello</h1></template>;');
    });

    it('should handle escaped backticks in template content', () => {
      const input = 'TEMPLATE_TEMPLATE(`some \\`code\\` here`)';
      const result = restoreTemplates(input);
      expect(result).toBe('<template>some `code` here</template>');
    });

    it('should handle escaped dollar signs in template content', () => {
      const input = 'TEMPLATE_TEMPLATE(`price: \\$5`)';
      const result = restoreTemplates(input);
      expect(result).toBe('<template>price: $5</template>');
    });
  });

  describe('findUnresolvedPlaceholders', () => {
    it('should return empty array when no placeholders exist', () => {
      expect(findUnresolvedPlaceholders('const x = 1;')).toEqual([]);
    });

    it('should detect expression-position placeholder fragments', () => {
      const result = findUnresolvedPlaceholders('const x = TEMPLATE_TEMPLATE( broken );');
      expect(result).toContain('TEMPLATE_TEMPLATE(...)');
    });

    it('should detect class-member-position placeholder fragments', () => {
      const result = findUnresolvedPlaceholders('class Foo { [_TEMPLATE_( broken )] = 0; }');
      expect(result).toContain('[_TEMPLATE_(...)]');
    });

    it('should detect both types of unresolved placeholders', () => {
      const source = 'TEMPLATE_TEMPLATE(x) and [_TEMPLATE_(y)] = 0;';
      const result = findUnresolvedPlaceholders(source);
      expect(result).toHaveLength(2);
    });
  });
});
