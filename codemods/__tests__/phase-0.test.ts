import { applyTransform } from 'jscodeshift/src/testUtils';
import * as path from 'path';
import * as fs from 'fs';

const transformPath = path.resolve(__dirname, '../src/phase-0-deprecation-cleanup.ts');
// eslint-disable-next-line @typescript-eslint/no-var-requires
const transform = require(transformPath);

function readFixture(name: string, suffix: 'input' | 'output'): string {
  return fs.readFileSync(
    path.join(__dirname, 'fixtures', 'phase-0', `${name}.${suffix}.ts`),
    'utf-8',
  );
}

function runTransform(input: string, filePath = 'app/models/test.ts'): string {
  const result = applyTransform(transform, {}, { source: input, path: filePath }, { parser: 'ts' });
  return result;
}

describe('Phase 0: Deprecation Cleanup', () => {
  describe('this.get transforms', () => {
    it('should convert this.get("prop") to this.prop', () => {
      const input = readFixture('this-get', 'input');
      const expected = readFixture('this-get', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should convert this.get("nested.path") to this.nested?.path', () => {
      const input = `export default class Foo {
  get val() {
    return this.get('a.b.c');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.a?.b?.c');
      expect(result).not.toContain("this.get('a.b.c')");
    });
  });

  describe('this.set transforms', () => {
    it('should convert this.set("prop", val) to this.prop = val', () => {
      const input = readFixture('this-set', 'input');
      const expected = readFixture('this-set', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should handle this.set with nested path (e.g. "a.b.c")', () => {
      const input = `export default class Foo {
  update(value: string) {
    this.set('a.b.c', value);
  }
}`;
      const result = runTransform(input);
      // Nested dot-path is not a valid identifier, so it uses computed access
      expect(result).toContain("this['a.b.c'] = value");
      expect(result).not.toContain('this.set');
    });

    it('should handle this.set with template literal key', () => {
      const input = `export default class Foo {
  update(type: string) {
    this.set(\`\${type}Time\`, new Date());
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('[`${type}Time`] = new Date()');
      expect(result).not.toContain('this.set');
    });
  });

  describe('obj.set transforms (non-this receiver)', () => {
    it('should convert obj.set("prop", val) to obj.prop = val', () => {
      const input = `export default class Foo {
  update(model: any) {
    model.set('name', 'hello');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("model.name = 'hello'");
      expect(result).not.toContain("model.set('name'");
    });

    it('should NOT transform map.set() (false positive guard)', () => {
      const input = `map.set('key', 'value');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform headers.set() (false positive guard)', () => {
      const input = `headers.set('content-type', 'application/json');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });
  });

  describe('obj.get transforms (non-this receiver)', () => {
    it('should convert obj.get("prop") to obj.prop', () => {
      const input = `export default class Foo {
  update(model: any) {
    const name = model.get('name');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('model.name');
      expect(result).not.toContain("model.get('name')");
    });

    it('should convert obj.get("nested.path") to obj.a?.b', () => {
      const input = `export default class Foo {
  update(model: any) {
    const val = model.get('parent.name');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('model.parent?.name');
    });

    it('should NOT transform searchParams.get() (false positive guard)', () => {
      const input = `const text = url.searchParams.get('text');`;
      const result = runTransform(input);
      // searchParams.get is a URLSearchParams API, not Ember
      expect(result).toBe('');
    });

    it('should NOT transform headers.get() (false positive guard)', () => {
      const input = `const ct = headers.get('content-type');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform map.get() (false positive guard)', () => {
      const input = `const val = map.get('key');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform new Map().get() (false positive guard)', () => {
      const input = `const val = new Map().get('key');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform params.get() (false positive guard)', () => {
      const input = `const val = params.get('q');`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should still transform ember model.get() (not a false positive)', () => {
      const input = `const val = model.get('name');`;
      const result = runTransform(input);
      expect(result).toContain('model.name');
    });
  });

  describe('functional get/set from @ember/object', () => {
    it('should convert get(obj, "prop") to obj.prop', () => {
      const input = `import { get } from '@ember/object';
export default class Foo {
  read(model: any) {
    return get(model, 'name');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('model.name');
      expect(result).not.toContain("get(model, 'name')");
    });

    it('should convert set(obj, "prop", val) to obj.prop = val', () => {
      const input = `import { set } from '@ember/object';
export default class Foo {
  update(model: any) {
    set(model, 'name', 'hello');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("model.name = 'hello'");
      expect(result).not.toContain("set(model, 'name'");
    });

    it('should remove @ember/object get import when all usages transformed', () => {
      const input = `import { get } from '@ember/object';
export default class Foo {
  read(model: any) {
    return get(model, 'name');
  }
}`;
      const result = runTransform(input);
      expect(result).not.toContain("'@ember/object'");
    });

    it('should use computed access for get(this, "some-dashed-prop") with invalid identifier', () => {
      const input = `import { get } from '@ember/object';
const val = get(this, 'some-dashed-prop');`;
      const result = runTransform(input);
      expect(result).toContain("this['some-dashed-prop']");
      expect(result).not.toContain("get(this, 'some-dashed-prop')");
    });

    it('should use computed access for set(this, "some-dashed-prop", val) with invalid identifier', () => {
      const input = `import { set } from '@ember/object';
set(this, 'some-dashed-prop', 42);`;
      const result = runTransform(input);
      expect(result).toContain("this['some-dashed-prop'] = 42");
      expect(result).not.toContain("set(this, 'some-dashed-prop'");
    });
  });

  describe('array helper transforms', () => {
    it('should convert toArray, sortBy, mapBy, filterBy, uniq', () => {
      const input = readFixture('array-helpers', 'input');
      const expected = readFixture('array-helpers', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should use locale-safe sort comparator for sortBy', () => {
      const input = `const sorted = items.sortBy('name');`;
      const result = runTransform(input);
      expect(result).toContain('a.name < b.name ? -1 : a.name > b.name ? 1 : 0');
    });

    it('should handle filterBy with no value (truthy check)', () => {
      const input = `const active = items.filterBy('isActive');`;
      const result = runTransform(input);
      expect(result).toContain('filter(item => item.isActive)');
    });

    it('should handle filterBy with a value', () => {
      const input = `const active = items.filterBy('status', 'active');`;
      const result = runTransform(input);
      expect(result).toContain("item.status === 'active'");
      expect(result).toContain('NOTE: Ember filterBy used == (loose equality)');
    });
  });

  describe('findBy transform', () => {
    it('should convert .findBy("key", val) to .find(item => item.key === val)', () => {
      const input = `const found = items.findBy('id', targetId);`;
      const result = runTransform(input);
      expect(result).toContain('item.id === targetId');
      expect(result).toContain('NOTE: Ember findBy used == (loose equality)');
    });

    it('should convert .findBy("key") to .find(item => item.key) (truthy)', () => {
      const input = `const found = items.findBy('name');`;
      const result = runTransform(input);
      expect(result).toContain('find(item => item.name)');
    });
  });

  describe('pushObject/removeObject transforms', () => {
    it('should convert .pushObject(item) to .push(item)', () => {
      const input = `arr.pushObject(newItem);`;
      const result = runTransform(input);
      expect(result).toContain('arr.push(newItem)');
      expect(result).not.toContain('pushObject');
    });

    it('should convert .removeObject(item) to safe splice with indexOf guard', () => {
      const input = `arr.removeObject(oldItem);`;
      const result = runTransform(input);
      expect(result).toContain('const _idx = arr.indexOf(oldItem)');
      expect(result).toContain('if (_idx !== -1)');
      expect(result).toContain('arr.splice(_idx, 1)');
      expect(result).not.toContain('removeObject');
    });

    it('should hoist receiver to temp variable when removeObject receiver is a CallExpression', () => {
      const input = `getArray().removeObject(item);`;
      const result = runTransform(input);
      expect(result).toContain('const _arr = getArray()');
      expect(result).toContain('const _idx = _arr.indexOf(item)');
      expect(result).toContain('_arr.splice(_idx, 1)');
      expect(result).not.toContain('removeObject');
      // Should NOT call getArray() multiple times
      const getArrayCount = (result.match(/getArray\(\)/g) || []).length;
      expect(getArrayCount).toBe(1);
    });

    it('should use comma operator for pushObject when return value is used (L1)', () => {
      const input = `const added = arr.pushObject(item);`;
      const result = runTransform(input);
      expect(result).toContain('(arr.push(item), item)');
      expect(result).not.toContain('pushObject');
    });

    it('should keep simple push for pushObject in statement context (L1)', () => {
      const input = `arr.pushObject(item);`;
      const result = runTransform(input);
      expect(result).toContain('arr.push(item)');
      expect(result).not.toContain(',');
      expect(result).not.toContain('pushObject');
    });

    it('should convert .pushObjects(items) to .push(...items) in statement context (L2)', () => {
      const input = `arr.pushObjects(newItems);`;
      const result = runTransform(input);
      expect(result).toContain('arr.push(...newItems)');
      expect(result).not.toContain('pushObjects');
    });

    it('should use comma operator for pushObjects when return value is used (L2)', () => {
      const input = `const result = arr.pushObjects(newItems);`;
      const result = runTransform(input);
      expect(result).toContain('(arr.push(...newItems), arr)');
      expect(result).not.toContain('pushObjects');
    });

    it('should convert .removeObjects(items) to forEach-based removal in statement context (L2)', () => {
      const input = `arr.removeObjects(oldItems);`;
      const result = runTransform(input);
      expect(result).toContain('oldItems.forEach');
      expect(result).toContain('arr.indexOf(_item)');
      expect(result).toContain('if (_idx !== -1)');
      expect(result).toContain('arr.splice(_idx, 1)');
      expect(result).not.toContain('removeObjects');
    });

    it('should hoist receiver for removeObjects with complex receiver (L2)', () => {
      const input = `getArray().removeObjects(items);`;
      const result = runTransform(input);
      expect(result).toContain('const _arr = getArray()');
      expect(result).toContain('_arr.indexOf(_item)');
      expect(result).toContain('_arr.splice(_idx, 1)');
      expect(result).not.toContain('removeObjects');
      const getArrayCount = (result.match(/getArray\(\)/g) || []).length;
      expect(getArrayCount).toBe(1);
    });

    it('should use IIFE for removeObjects in expression context (L2)', () => {
      const input = `const result = arr.removeObjects(oldItems);`;
      const result = runTransform(input);
      expect(result).toContain('return arr');
      expect(result).toContain('oldItems.forEach');
      expect(result).toContain('arr.indexOf(_item)');
      expect(result).not.toContain('removeObjects');
    });
  });

  describe('setProperties transforms', () => {
    it('should convert this.setProperties({...}) to individual assignments', () => {
      const input = `export default class Foo {
  update() {
    this.setProperties({ name: 'hello', age: 5 });
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("this.name = 'hello'");
      expect(result).toContain('this.age = 5');
      expect(result).not.toContain('setProperties');
    });

    it('should convert model.setProperties({...}) to individual assignments', () => {
      const input = `export default class Foo {
  update(model: any) {
    model.setProperties({ name: 'hello', active: true });
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("model.name = 'hello'");
      expect(result).toContain('model.active = true');
      expect(result).not.toContain('setProperties');
    });

    it('should hoist receiver to temp variable when setProperties receiver is a CallExpression', () => {
      const input = `export default class Foo {
  update() {
    getModel().setProperties({ a: 1, b: 2 });
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('const _obj = getModel()');
      expect(result).toContain('_obj.a = 1');
      expect(result).toContain('_obj.b = 2');
      expect(result).not.toContain('setProperties');
      // Should NOT call getModel() multiple times
      const getModelCount = (result.match(/getModel\(\)/g) || []).length;
      expect(getModelCount).toBe(1);
    });

    it('should return the properties object when setProperties is used in expression context', () => {
      const input = `export default class Foo {
  update(model: any) {
    const result = model.setProperties({ a: 1, b: 2 });
  }
}`;
      const result = runTransform(input);
      // Expression context: IIFE should return the properties object
      expect(result).toContain('model.a = 1');
      expect(result).toContain('model.b = 2');
      expect(result).toContain('return {');
      expect(result).toContain('a: 1');
      expect(result).toContain('b: 2');
      expect(result).not.toContain('setProperties');
    });

    it('should not transform setProperties with computed keys', () => {
      const input = `export default class Foo {
  update() {
    this.setProperties({ [key]: value });
  }
}`;
      const result = runTransform(input);
      // Should not transform because of computed key
      expect(result).toBe('');
    });
  });

  describe('isEmpty/isPresent/isNone transforms', () => {
    it('should convert isEmpty(x) to full semantic equivalent', () => {
      const input = readFixture('is-empty', 'input');
      const expected = readFixture('is-empty', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should convert isPresent(x) and remove import', () => {
      const input = `import { isPresent } from '@ember/utils';
export default class Foo {
  get valid() {
    return isPresent(this.value);
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.value != null');
      expect(result).not.toContain('isPresent');
      expect(result).not.toContain('@ember/utils');
    });

    it('should convert isNone(x) to x == null', () => {
      const input = `import { isNone } from '@ember/utils';
export default class Foo {
  get empty() {
    return isNone(this.value);
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.value == null');
      expect(result).not.toContain('isNone');
    });

    it('should wrap isEmpty(getData()) in IIFE when argument is a CallExpression', () => {
      const input = `import { isEmpty } from '@ember/utils';
const val = isEmpty(getData());`;
      const result = runTransform(input);
      // Should use IIFE pattern to avoid multiple evaluation
      expect(result).toContain('x =>');
      expect(result).toContain('x == null');
      expect(result).toContain('getData()');
      // The isEmpty() call itself should be removed (the TODO comment may contain the word)
      expect(result).not.toContain('isEmpty(');
      // getData() should only appear once (as the IIFE argument)
      const getDataCount = (result.match(/getData\(\)/g) || []).length;
      expect(getDataCount).toBe(1);
    });

    it('should wrap isPresent(getData()) in IIFE when argument is a CallExpression', () => {
      const input = `import { isPresent } from '@ember/utils';
const val = isPresent(getData());`;
      const result = runTransform(input);
      // Should use IIFE pattern to avoid multiple evaluation
      expect(result).toContain('x =>');
      expect(result).toContain('x != null');
      expect(result).toContain('getData()');
      expect(result).not.toContain('isPresent');
      // getData() should only appear once (as the IIFE argument)
      const getDataCount = (result.match(/getData\(\)/g) || []).length;
      expect(getDataCount).toBe(1);
    });

    it('should not transform isEmpty without @ember/utils import', () => {
      const input = `function isEmpty(x: any) { return !x; }
const val = isEmpty(data);`;
      const result = runTransform(input);
      // No @ember/utils import, so isEmpty should NOT be transformed
      expect(result).toBe('');
    });
  });

  describe('A() and isArray() from @ember/array', () => {
    it('should convert A(arr) to arr', () => {
      const input = `import { A } from '@ember/array';
const items = A(rawItems);`;
      const result = runTransform(input);
      expect(result).toContain('const items = rawItems');
      expect(result).not.toContain('A(');
      expect(result).not.toContain('@ember/array');
    });

    it('should convert A() with no args to []', () => {
      const input = `import { A } from '@ember/array';
const items = A();`;
      const result = runTransform(input);
      expect(result).toContain('const items = []');
      expect(result).not.toContain('A(');
    });

    it('should convert isArray(x) to Array.isArray(x)', () => {
      const input = `import { isArray } from '@ember/array';
const check = isArray(data);`;
      const result = runTransform(input);
      expect(result).toContain('Array.isArray(data)');
      // Import should be removed
      expect(result).not.toContain('@ember/array');
    });

    it('should NOT transform A() without @ember/array import', () => {
      const input = `function A(x: any) { return [x]; }
const items = A(data);`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform isArray() without @ember/array import', () => {
      const input = `function isArray(x: any) { return Array.isArray(x); }
const check = isArray(data);`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should only remove A import after all A() calls are transformed', () => {
      // A(arr) with 1 arg should be transformed, but A(x, y) with 2 args should not
      // The import should be kept because there are remaining A() calls
      const input = `import { A } from '@ember/array';
const items = A(rawItems);
const other = A(x, y);`;
      const result = runTransform(input);
      // A(rawItems) should be transformed
      expect(result).toContain('const items = rawItems');
      // A(x, y) is non-standard, should be left as-is
      expect(result).toContain('A(x, y)');
      // Import should be kept because A(x, y) still references A
      expect(result).toContain('@ember/array');
    });
  });

  describe('hasMany().value() and hasMany().ids() transforms', () => {
    it('should convert hasMany value/ids patterns', () => {
      const input = readFixture('has-many-value-ids', 'input');
      const expected = readFixture('has-many-value-ids', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should convert belongsTo("rel").value() to model.rel', () => {
      const input = `const parent = model.belongsTo('group').value();`;
      const result = runTransform(input);
      expect(result).toContain('model.group');
      expect(result).not.toContain('belongsTo');
    });
  });

  describe('hasMany/belongsTo .load() transforms', () => {
    it('should convert hasMany("rel").load() to model.rel', () => {
      const input = `const tasks = await model.hasMany('tasks').load();`;
      const result = runTransform(input);
      expect(result).toContain('model.tasks');
      expect(result).not.toContain('load()');
    });
  });

  describe('firstObject/lastObject transforms', () => {
    it('should convert .firstObject to [0]', () => {
      const input = `const first = items.firstObject;`;
      const result = runTransform(input);
      expect(result).toContain('items[0]');
      expect(result).not.toContain('firstObject');
    });

    it('should convert .lastObject to .at(-1)', () => {
      const input = `const last = items.lastObject;`;
      const result = runTransform(input);
      expect(result).toContain('items.at(-1)');
      expect(result).not.toContain('lastObject');
    });
  });

  describe('transitionTo/replaceWith transforms', () => {
    it('should convert this.transitionTo to this.router.transitionTo', () => {
      const input = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

export default class MyRoute extends Route {
  model() {
    this.transitionTo('home');
  }
}`;
      const expected = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

import type { RouterService } from '@ember/routing';

export default class MyRoute extends Route {
  @service
  declare router: RouterService;

  model() {
    this.router.transitionTo('home');
  }
}`;
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });

    it('should convert this.replaceWith to this.router.replaceWith', () => {
      const input = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

export default class MyRoute extends Route {
  model() {
    this.replaceWith('home');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.router.replaceWith');
      expect(result).not.toContain("this.replaceWith('home')");
    });

    it('should not add duplicate router service declaration', () => {
      const input = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

export default class MyRoute extends Route {
  @service declare router: RouterService;

  model() {
    this.transitionTo('home');
  }
}`;
      const result = runTransform(input);
      const routerCount = (result.match(/declare router/g) || []).length;
      expect(routerCount).toBe(1);
    });
  });

  describe('.gts file support', () => {
    it('should transform JS in .gts files while preserving <template> blocks', () => {
      const input = `import Component from '@glimmer/component';

export default class MyComponent extends Component {
  get val() {
    return this.get('name');
  }

  <template>
    <div>{{this.val}}</div>
  </template>
}`;
      const result = runTransform(input, 'app/components/my-component.gts');
      // JS part should be transformed
      expect(result).toContain('this.name');
      expect(result).not.toContain("this.get('name')");
      // Template should be preserved
      expect(result).toContain('<template>');
      expect(result).toContain('{{this.val}}');
      expect(result).toContain('</template>');
    });

    it('should handle standalone template-only .gts components', () => {
      const input = `<template>
  <div>Hello world</div>
</template>`;
      // No transformable JS — should return empty (no changes)
      const result = applyTransform(
        transform,
        {},
        { source: input, path: 'app/components/hello.gts' },
        { parser: 'ts' },
      );
      expect(result).toBe('');
    });
  });

  describe('invalid identifier handling (hyphenated property names)', () => {
    it('should convert this.get("some-prop") to this["some-prop"] (computed access)', () => {
      const input = `export default class Foo {
  get val() {
    return this.get('some-prop');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("this['some-prop']");
      expect(result).not.toContain("this.get('some-prop')");
    });

    it('should convert obj.get("some-prop") to obj["some-prop"] (computed access)', () => {
      const input = `export default class Foo {
  read(model: any) {
    return model.get('some-prop');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("model['some-prop']");
      expect(result).not.toContain("model.get('some-prop')");
    });

    it('should convert this.set("some-prop", val) to this["some-prop"] = val', () => {
      const input = `export default class Foo {
  update() {
    this.set('some-prop', 42);
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("this['some-prop'] = 42");
      expect(result).not.toContain("this.set('some-prop'");
    });

    it('should NOT transform .sortBy("some-key") when key is not a valid identifier', () => {
      const input = `const sorted = items.sortBy('some-key');`;
      const result = runTransform(input);
      // Should leave the original code unchanged (no transform)
      expect(result).toBe('');
    });
  });

  describe('multi-key sortBy (H4)', () => {
    it('should transform .sortBy("lastName", "firstName") to chained comparator', () => {
      const input = `const sorted = items.sortBy('lastName', 'firstName');`;
      const result = runTransform(input);
      expect(result).toContain('.sort(');
      expect(result).toContain('a.lastName < b.lastName ? -1');
      expect(result).toContain('a.lastName > b.lastName ? 1');
      expect(result).toContain('a.firstName < b.firstName ? -1');
      expect(result).toContain('a.firstName > b.firstName ? 1');
      expect(result).toContain(': 0');
      expect(result).not.toContain('sortBy');
    });

    it('should transform .sortBy with three keys', () => {
      const input = `const sorted = items.sortBy('a', 'b', 'c');`;
      const result = runTransform(input);
      expect(result).toContain('.sort(');
      expect(result).toContain('a.a < b.a');
      expect(result).toContain('a.b < b.b');
      expect(result).toContain('a.c < b.c');
      expect(result).not.toContain('sortBy');
    });

    it('should skip multi-key sortBy when any key is not a valid identifier', () => {
      const input = `const sorted = items.sortBy('lastName', 'first-name');`;
      const result = runTransform(input);
      // Should not transform because 'first-name' is not a valid identifier
      expect(result).toBe('');
    });
  });

  describe('removeObject expression context (M2)', () => {
    it('should return the array when removeObject is used as expression value', () => {
      const input = `const result = arr.removeObject(x);`;
      const result = runTransform(input);
      // Should wrap in IIFE that returns arr
      expect(result).toContain('return arr');
      expect(result).toContain('arr.indexOf(x)');
      expect(result).toContain('arr.splice(_idx, 1)');
      expect(result).not.toContain('removeObject');
    });

    it('should return the array with complex receiver in expression context', () => {
      const input = `const result = getArray().removeObject(x);`;
      const result = runTransform(input);
      expect(result).toContain('const _arr = getArray()');
      expect(result).toContain('return _arr');
      expect(result).not.toContain('removeObject');
    });

    it('should NOT return array in statement context (existing behavior)', () => {
      const input = `arr.removeObject(oldItem);`;
      const result = runTransform(input);
      expect(result).not.toContain('return');
      expect(result).toContain('const _idx = arr.indexOf(oldItem)');
    });
  });

  describe('transitionTo/replaceWith scope guard (M3)', () => {
    it('should NOT transform this.transitionTo in a non-Route/Controller class', () => {
      const input = `export default class MyService {
  doSomething() {
    this.transitionTo('somewhere');
  }
}`;
      const result = runTransform(input);
      // Should not transform because MyService does not extend Route or Controller
      expect(result).toBe('');
    });

    it('should NOT transform this.replaceWith in a non-Route/Controller class', () => {
      const input = `export default class SomeHelper {
  doSomething() {
    this.replaceWith('somewhere');
  }
}`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should transform this.transitionTo in a class extending Route', () => {
      const input = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

export default class MyRoute extends Route {
  model() {
    this.transitionTo('home');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.router.transitionTo');
    });

    it('should transform this.transitionTo in a class extending Controller', () => {
      const input = `
import Controller from '@ember/controller';
import { service } from '@ember/service';

export default class MyController extends Controller {
  goHome() {
    this.transitionTo('home');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('this.router.transitionTo');
    });
  });

  describe('ensureRouterService with ClassExpression (M4)', () => {
    it('should inject @service router into ClassExpression (const MyRoute = class extends Route {})', () => {
      const input = `
import Route from '@ember/routing/route';
import { service } from '@ember/service';

const MyRoute = class extends Route {
  model() {
    this.transitionTo('home');
  }
};`;
      const result = runTransform(input);
      expect(result).toContain('this.router.transitionTo');
      expect(result).toContain('declare router');
    });
  });

  describe('aliased import handling (H2)', () => {
    it('should transform aliased get from @ember/object and remove import', () => {
      const input = `import { get as emberGet } from '@ember/object';
export default class Foo {
  read(model: any) {
    return emberGet(model, 'name');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain('model.name');
      expect(result).not.toContain('emberGet');
      expect(result).not.toContain("'@ember/object'");
    });

    it('should transform aliased set from @ember/object and remove import', () => {
      const input = `import { set as emberSet } from '@ember/object';
export default class Foo {
  update(model: any) {
    emberSet(model, 'name', 'hello');
  }
}`;
      const result = runTransform(input);
      expect(result).toContain("model.name = 'hello'");
      expect(result).not.toContain('emberSet');
      expect(result).not.toContain("'@ember/object'");
    });

    it('should transform aliased isEmpty from @ember/utils and remove import', () => {
      const input = `import { isEmpty as emberIsEmpty } from '@ember/utils';
const val = emberIsEmpty(data);`;
      const result = runTransform(input);
      expect(result).toContain('data == null');
      expect(result).not.toContain('emberIsEmpty');
      expect(result).not.toContain("'@ember/utils'");
    });

    it('should transform aliased isPresent from @ember/utils and remove import', () => {
      const input = `import { isPresent as checkPresent } from '@ember/utils';
const val = checkPresent(data);`;
      const result = runTransform(input);
      expect(result).toContain('data != null');
      expect(result).not.toContain('checkPresent');
      expect(result).not.toContain("'@ember/utils'");
    });

    it('should transform aliased isNone from @ember/utils and remove import', () => {
      const input = `import { isNone as checkNone } from '@ember/utils';
const val = checkNone(data);`;
      const result = runTransform(input);
      expect(result).toContain('data == null');
      expect(result).not.toContain('checkNone');
      expect(result).not.toContain("'@ember/utils'");
    });

    it('should transform aliased A from @ember/array and remove import', () => {
      const input = `import { A as emberA } from '@ember/array';
const items = emberA(rawItems);`;
      const result = runTransform(input);
      expect(result).toContain('const items = rawItems');
      expect(result).not.toContain('emberA');
      expect(result).not.toContain("'@ember/array'");
    });

    it('should transform aliased isArray from @ember/array and remove import', () => {
      const input = `import { isArray as emberIsArray } from '@ember/array';
const check = emberIsArray(data);`;
      const result = runTransform(input);
      expect(result).toContain('Array.isArray(data)');
      expect(result).not.toContain('emberIsArray');
      expect(result).not.toContain("'@ember/array'");
    });

    it('should not leave dangling references when aliased get is transformed', () => {
      // This is the core H2 bug: the import is removed but the alias reference remains
      const input = `import { get as emberGet } from '@ember/object';
const name = emberGet(model, 'name');
const age = emberGet(model, 'age');`;
      const result = runTransform(input);
      expect(result).toContain('model.name');
      expect(result).toContain('model.age');
      expect(result).not.toContain('emberGet');
      expect(result).not.toContain("'@ember/object'");
    });
  });

  describe('toArray false positive guard (H3)', () => {
    it('should NOT transform new expression receiver toArray()', () => {
      const input = `const arr = new Uint8Array(buffer).toArray();`;
      const result = runTransform(input);
      // Should not transform because receiver is a new expression
      expect(result).toBe('');
    });

    it('should NOT transform new Set().toArray()', () => {
      const input = `const arr = new Set([1, 2, 3]).toArray();`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should NOT transform new Immutable.List().toArray()', () => {
      const input = `const arr = new Immutable.List([1, 2]).toArray();`;
      const result = runTransform(input);
      expect(result).toBe('');
    });

    it('should still transform regular receiver toArray()', () => {
      const input = `const arr = items.toArray();`;
      const result = runTransform(input);
      expect(result).toContain('Array.from(items)');
      expect(result).not.toContain('toArray');
    });

    it('should still transform this.items.toArray()', () => {
      const input = `const arr = this.items.toArray();`;
      const result = runTransform(input);
      expect(result).toContain('Array.from(this.items)');
      expect(result).not.toContain('toArray');
    });
  });

  describe('chained transforms (L3)', () => {
    // Note: jscodeshift processes outer CallExpressions first. When the outer
    // call (e.g. sortBy) is replaced, its receiver (the inner call, e.g. toArray)
    // may not be re-visited in the same pass. Running the codemod twice resolves
    // all layers. These tests document the single-pass behavior.

    it('should transform the outer sortBy but leave inner toArray for a second pass', () => {
      const input = `const arr = items.toArray().sortBy('name');`;
      const result = runTransform(input);
      // sortBy is transformed to .sort(...)
      expect(result).toContain('.sort(');
      expect(result).toContain('a.name < b.name ? -1 : a.name > b.name ? 1 : 0');
      expect(result).not.toContain('sortBy');
      // toArray remains after first pass (outer-first traversal)
      expect(result).toContain('toArray()');
    });

    it('should transform the outer mapBy but leave inner filterBy for a second pass', () => {
      const input = `const names = items.filterBy('active', true).mapBy('name');`;
      const result = runTransform(input);
      // mapBy is transformed to .map(...)
      expect(result).toContain('.map(item => item.name)');
      expect(result).not.toContain('mapBy');
      // filterBy remains after first pass
      expect(result).toContain("filterBy('active', true)");
    });

    it('should fully resolve all chained transforms after two passes', () => {
      const input = `const sorted = items.filterBy('active').sortBy('name');`;
      // First pass: sortBy transformed, filterBy remains
      const firstPass = runTransform(input);
      expect(firstPass).toContain('.sort(');
      expect(firstPass).not.toContain('sortBy');
      expect(firstPass).toContain("filterBy('active')");

      // Second pass: filterBy is now the outermost deprecated call, so it gets transformed
      const secondPass = runTransform(firstPass);
      expect(secondPass).toContain('.filter(item => item.active)');
      expect(secondPass).toContain('.sort(');
      expect(secondPass).not.toContain('filterBy');
      expect(secondPass).not.toContain('sortBy');
    });
  });

  describe('no changes needed', () => {
    it('should return undefined when no transformations apply', () => {
      const input = `
import Component from '@glimmer/component';
export default class Foo extends Component {
  get name() { return 'hello'; }
}`;
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
