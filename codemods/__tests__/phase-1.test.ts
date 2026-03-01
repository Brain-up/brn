import { applyTransform } from 'jscodeshift/src/testUtils';
import * as path from 'path';
import * as fs from 'fs';

const transformPath = path.resolve(__dirname, '../src/phase-1-import-migration.ts');
// eslint-disable-next-line @typescript-eslint/no-var-requires
const transform = require(transformPath);

function readFixture(name: string, suffix: 'input' | 'output'): string {
  return fs.readFileSync(
    path.join(__dirname, 'fixtures', 'phase-1', `${name}.${suffix}.ts`),
    'utf-8',
  );
}

function runTransform(input: string, filePath = 'app/models/group.ts'): string {
  return applyTransform(
    transform,
    { appName: 'brn' },
    { source: input, path: filePath },
    { parser: 'ts' },
  );
}

describe('Phase 1: Import Migration', () => {
  describe('import rewrites', () => {
    it('should rewrite ember-data imports and add [Type] brand', () => {
      const input = readFixture('import-rewrite', 'input');
      const expected = readFixture('import-rewrite', 'output');
      const result = runTransform(input);
      expect(result.trim()).toBe(expected.trim());
    });
  });

  describe('import source replacement', () => {
    it('should rewrite @ember-data/model to @warp-drive/legacy/model', () => {
      const input = `import Model from '@ember-data/model';
export default class Foo extends Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain("'@warp-drive/legacy/model'");
    });

    it('should rewrite @ember-data/model named imports', () => {
      const input = `import Model, { attr, belongsTo, hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @attr('string') name!: string;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain("from '@warp-drive/legacy/model'");
    });

    it('should rewrite @ember-data/adapter/rest to @warp-drive/legacy/adapter/rest', () => {
      const input = `import RESTAdapter from '@ember-data/adapter/rest';
export default class AppAdapter extends RESTAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).toContain("'@warp-drive/legacy/adapter/rest'");
    });

    it('should rewrite @ember-data/serializer/json to @warp-drive/legacy/serializer/json', () => {
      const input = `import JSONSerializer from '@ember-data/serializer/json';
export default class AppSerializer extends JSONSerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).toContain("'@warp-drive/legacy/serializer/json'");
    });

    it('should rewrite @ember-data/serializer/json-api', () => {
      const input = `import JSONAPISerializer from '@ember-data/serializer/json-api';
export default class AppSerializer extends JSONAPISerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).toContain("'@warp-drive/legacy/serializer/json-api'");
    });

    it('should rewrite @ember-data/serializer/transform', () => {
      const input = `import Transform from '@ember-data/serializer/transform';
export default class DateTransform extends Transform {}`;
      const result = runTransform(input, 'app/transforms/date.ts');
      expect(result).toContain("'@warp-drive/legacy/serializer/transform'");
    });

    it('should rewrite @ember-data/adapter (base)', () => {
      const input = `import Adapter from '@ember-data/adapter';
export default class AppAdapter extends Adapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).toContain("'@warp-drive/legacy/adapter'");
    });

    it('should rewrite @ember-data/serializer (base)', () => {
      const input = `import Serializer from '@ember-data/serializer';
export default class AppSerializer extends Serializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).toContain("'@warp-drive/legacy/serializer'");
    });
  });

  describe('DS barrel import', () => {
    it('should handle import DS from "ember-data" with destructuring', () => {
      const input = `import DS from 'ember-data';
const { Model, attr } = DS;
export default class Foo extends Model {
  @attr('string') name!: string;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const { Model, attr } = DS');
      expect(result).toContain("'@warp-drive/legacy/model'");
    });

    // H6: DS barrel destructuring routes to correct packages
    it('should route DS.RESTAdapter to @warp-drive/legacy/adapter/rest via destructuring', () => {
      const input = `import DS from 'ember-data';
const { RESTAdapter } = DS;
export default class AppAdapter extends RESTAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const { RESTAdapter } = DS');
      expect(result).toContain("from '@warp-drive/legacy/adapter/rest'");
    });

    it('should route DS.JSONSerializer to @warp-drive/legacy/serializer/json via destructuring', () => {
      const input = `import DS from 'ember-data';
const { JSONSerializer } = DS;
export default class AppSerializer extends JSONSerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const { JSONSerializer } = DS');
      expect(result).toContain("from '@warp-drive/legacy/serializer/json'");
    });

    it('should route DS.JSONAPIAdapter to @warp-drive/legacy/adapter/json-api via destructuring', () => {
      const input = `import DS from 'ember-data';
const { JSONAPIAdapter } = DS;
export default class AppAdapter extends JSONAPIAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/adapter/json-api'");
    });

    it('should route DS.JSONAPISerializer to @warp-drive/legacy/serializer/json-api via destructuring', () => {
      const input = `import DS from 'ember-data';
const { JSONAPISerializer } = DS;
export default class AppSerializer extends JSONAPISerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/serializer/json-api'");
    });

    it('should route DS.RESTSerializer to @warp-drive/legacy/serializer/rest via destructuring', () => {
      const input = `import DS from 'ember-data';
const { RESTSerializer } = DS;
export default class AppSerializer extends RESTSerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/serializer/rest'");
    });

    it('should route DS.Transform to @warp-drive/legacy/serializer/transform via destructuring', () => {
      const input = `import DS from 'ember-data';
const { Transform } = DS;
export default class DateTransform extends Transform {}`;
      const result = runTransform(input, 'app/transforms/date.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/serializer/transform'");
    });

    it('should handle mixed destructuring with items from different packages', () => {
      const input = `import DS from 'ember-data';
const { RESTAdapter, JSONSerializer, attr, belongsTo } = DS;
export default class AppAdapter extends RESTAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const {');
      expect(result).toContain("from '@warp-drive/legacy/adapter/rest'");
      expect(result).toContain("from '@warp-drive/legacy/serializer/json'");
      expect(result).toContain("from '@warp-drive/legacy/model'");
    });

    it('should handle DS.Model member access pattern', () => {
      const input = `import DS from 'ember-data';
export default class Foo extends DS.Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('DS.Model');
      expect(result).toContain("from '@warp-drive/legacy/model'");
      expect(result).toContain('extends Model');
    });

    it('should handle DS.RESTAdapter member access pattern', () => {
      const input = `import DS from 'ember-data';
export default class AppAdapter extends DS.RESTAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('DS.RESTAdapter');
      expect(result).toContain("from '@warp-drive/legacy/adapter/rest'");
      expect(result).toContain('extends RESTAdapter');
    });

    it('should handle DS.JSONSerializer member access pattern', () => {
      const input = `import DS from 'ember-data';
export default class AppSerializer extends DS.JSONSerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('DS.JSONSerializer');
      expect(result).toContain("from '@warp-drive/legacy/serializer/json'");
      expect(result).toContain('extends JSONSerializer');
    });

    it('should add TODO comment for unknown DS member via destructuring', () => {
      const input = `import DS from 'ember-data';
const { EmbeddedRecordsMixin } = DS;
export default class AppSerializer {}`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const { EmbeddedRecordsMixin } = DS');
      expect(result).toContain('TODO');
      expect(result).toContain('EmbeddedRecordsMixin');
    });

    it('should add TODO comment for unknown DS.X member access', () => {
      const input = `import DS from 'ember-data';
const mixin = DS.EmbeddedRecordsMixin;`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain('TODO');
      expect(result).toContain('EmbeddedRecordsMixin');
    });

    // L5: Named imports from 'ember-data'
    it('should handle named imports from ember-data barrel', () => {
      const input = `import { Model, attr } from 'ember-data';
export default class Foo extends Model {
  @attr('string') name!: string;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/model'");
    });

    it('should route named imports from ember-data to correct packages', () => {
      const input = `import { RESTAdapter } from 'ember-data';
export default class AppAdapter extends RESTAdapter {}`;
      const result = runTransform(input, 'app/adapters/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/adapter/rest'");
    });

    it('should handle mixed named imports from ember-data to different packages', () => {
      const input = `import { Model, attr, RESTAdapter } from 'ember-data';
export default class Foo extends Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain("from '@warp-drive/legacy/model'");
      expect(result).toContain("from '@warp-drive/legacy/adapter/rest'");
    });

    it('should add TODO for unknown named imports from ember-data', () => {
      const input = `import { EmbeddedRecordsMixin } from 'ember-data';
const mixin = EmbeddedRecordsMixin;`;
      const result = runTransform(input, 'app/serializers/application.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).toContain('TODO');
      expect(result).toContain('EmbeddedRecordsMixin');
    });

    // L5: Namespace import from 'ember-data'
    it('should handle namespace import: import * as DS from ember-data', () => {
      const input = `import * as DS from 'ember-data';
export default class Foo extends DS.Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('DS.Model');
      expect(result).toContain("from '@warp-drive/legacy/model'");
      expect(result).toContain('extends Model');
    });

    it('should handle namespace import with destructuring', () => {
      const input = `import * as DS from 'ember-data';
const { Model, attr } = DS;
export default class Foo extends Model {
  @attr('string') name!: string;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain("from 'ember-data'");
      expect(result).not.toContain('const { Model, attr } = DS');
      expect(result).toContain("from '@warp-drive/legacy/model'");
    });
  });

  describe('store import', () => {
    it('should convert @ember-data/store to type import from app services', () => {
      const input = `import Store from '@ember-data/store';
export default class MyRoute { store!: Store; }`;
      const result = runTransform(input, 'app/routes/my.ts');
      expect(result).toContain("import type Store from 'brn/services/store'");
    });

    // H7: Named value exports should not become type-only
    it('should split default and named imports from @ember-data/store', () => {
      const input = `import Store, { normalizeModelName } from '@ember-data/store';
export default class MyRoute {
  store!: Store;
  doSomething() { return normalizeModelName('foo'); }
}`;
      const result = runTransform(input, 'app/routes/my.ts');
      // Default import should be type-only pointing to app services
      expect(result).toContain("import type Store from 'brn/services/store'");
      // Named import should stay as value import from @ember-data/store
      expect(result).toContain("import { normalizeModelName } from '@ember-data/store'");
      // Should have TODO comment for named imports
      expect(result).toContain('TODO');
    });

    it('should keep named-only @ember-data/store imports as value imports', () => {
      const input = `import { normalizeModelName } from '@ember-data/store';
const name = normalizeModelName('foo');`;
      const result = runTransform(input, 'app/utils/helper.ts');
      // Should NOT be a type import
      expect(result).not.toContain('import type');
      // Should keep the source as @ember-data/store
      expect(result).toContain("from '@ember-data/store'");
      // Should have TODO comment
      expect(result).toContain('TODO');
    });

    it('should handle multiple named imports from @ember-data/store alongside default', () => {
      const input = `import Store, { normalizeModelName, recordIdentifierFor } from '@ember-data/store';
export default class MyRoute {
  store!: Store;
  doSomething() {
    return normalizeModelName('foo');
  }
}`;
      const result = runTransform(input, 'app/routes/my.ts');
      // Default should become type-only
      expect(result).toContain("import type Store from 'brn/services/store'");
      // Named should stay as value imports
      expect(result).toContain('normalizeModelName');
      expect(result).toContain('recordIdentifierFor');
      expect(result).toContain("from '@ember-data/store'");
    });
  });

  describe('registry removal', () => {
    it('should remove ember-data type registry declarations', () => {
      const input = `import Model from '@ember-data/model';
export default class Foo extends Model {}
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry { foo: Foo; }
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('ember-data/types/registries');
    });
  });

  describe('[Type] brand', () => {
    it('should add [Type] brand to model classes', () => {
      const input = `import Model from '@ember-data/model';
export default class Foo extends Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'foo'");
      expect(result).toContain("'@warp-drive/core/types/symbols'");
    });

    it('should derive nested model names from path', () => {
      const input = `import Model from '@ember-data/model';
export default class TaskSignal extends Model {}`;
      const result = runTransform(input, 'app/models/task/signal.ts');
      expect(result).toContain("'task/signal'");
    });

    it('should not add [Type] brand if already present', () => {
      const input = `import Model from '@ember-data/model';
import { Type } from '@warp-drive/core/types/symbols';
export default class Foo extends Model {
  declare [Type]: 'foo';
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      const typeCount = (result.match(/\[Type\]/g) || []).length;
      expect(typeCount).toBe(1);
    });

    it('should detect relative imports as model parents', () => {
      const input = `import BaseTask from '../task';
export default class TaskSignal extends BaseTask {}`;
      const result = runTransform(input, 'app/models/task/signal.ts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'task/signal'");
    });

    // M6: Relative import exclusions for non-model directories
    it('should not treat imports from /mixins/ as model parents', () => {
      const input = `import Saveable from '../mixins/saveable';
export default class Foo extends Saveable {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /helpers/ as model parents', () => {
      const input = `import Helper from '../helpers/format';
export default class Foo extends Helper {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /adapters/ as model parents', () => {
      const input = `import AppAdapter from '../adapters/application';
export default class Foo extends AppAdapter {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /serializers/ as model parents', () => {
      const input = `import AppSerializer from '../serializers/application';
export default class Foo extends AppSerializer {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /transforms/ as model parents', () => {
      const input = `import DateTransform from '../transforms/date';
export default class Foo extends DateTransform {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /config/ as model parents', () => {
      const input = `import Config from '../config/environment';
export default class Foo extends Config {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /initializers/ as model parents', () => {
      const input = `import Init from '../initializers/setup';
export default class Foo extends Init {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should not treat imports from /instance-initializers/ as model parents', () => {
      const input = `import Init from '../instance-initializers/setup';
export default class Foo extends Init {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('[Type]');
    });

    it('should handle class Foo extends Model with separate export default', () => {
      const input = `import Model from '@ember-data/model';
class Foo extends Model {}
export default Foo;`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('[Type]');
    });

    it('should derive model name from .gts file extension', () => {
      const input = `import Model from '@ember-data/model';
export default class Foo extends Model {}`;
      const result = runTransform(input, 'app/models/foo.gts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'foo'");
    });

    // L6: Mixin call pattern detection
    it('should detect Model inside mixin call: SortableMixin(Model)', () => {
      const input = `import Model from '@ember-data/model';
import SortableMixin from '../mixins/sortable';
export default class Foo extends SortableMixin(Model) {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'foo'");
    });

    it('should detect Model inside nested mixin calls: A(B(Model))', () => {
      const input = `import Model from '@ember-data/model';
import A from '../mixins/a';
import B from '../mixins/b';
export default class Foo extends A(B(Model)) {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'foo'");
    });

    it('should detect imported base model inside mixin call', () => {
      const input = `import BaseTask from '../task';
import Trackable from '../mixins/trackable';
export default class TaskSignal extends Trackable(BaseTask) {}`;
      const result = runTransform(input, 'app/models/task/signal.ts');
      expect(result).toContain('[Type]');
      expect(result).toContain("'task/signal'");
    });
  });

  describe('relationship fixes', () => {
    it('should add inverse: null when missing on hasMany', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany('bar', { async: false }) bars!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('inverse: null');
    });

    it('should add inverse: null when missing on belongsTo', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
export default class Foo extends Model {
  @belongsTo('bar', { async: false }) bar!: any;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('inverse: null');
    });

    it('should convert async: true to async: false on hasMany', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany('bar', { async: true, inverse: 'foo' }) bars!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).not.toContain('async: true');
    });

    it('should convert async: true to async: false on belongsTo', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
export default class Foo extends Model {
  @belongsTo('bar', { async: true, inverse: 'foo' }) bar!: any;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).not.toContain('async: true');
    });

    it('should add async: false when 2-arg relationship has no async key', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany('bar', { inverse: 'foo' }) bars!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).toContain("inverse: 'foo'");
    });

    it('should add async: false when belongsTo 2-arg has no async key', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
export default class Foo extends Model {
  @belongsTo('bar', { inverse: 'foo' }) bar!: any;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).toContain("inverse: 'foo'");
    });

    it('should not add inverse if already present', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany('bar', { async: false, inverse: 'foo' }) bars!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      const inverseCount = (result.match(/inverse/g) || []).length;
      expect(inverseCount).toBe(1);
    });

    it('should add options object when @hasMany has only 1 arg (no options)', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany('bar') bars!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).toContain('inverse: null');
      // Issue #6: inverse: null should have a TODO comment
      expect(result).toContain('TODO: verify inverse value');
    });

    it('should add options object when @belongsTo has only 1 arg (no options)', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
export default class Foo extends Model {
  @belongsTo('bar') bar!: any;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('async: false');
      expect(result).toContain('inverse: null');
      // Issue #6: inverse: null should have a TODO comment
      expect(result).toContain('TODO: verify inverse value');
    });

    // M7: Zero-arg @hasMany()/@belongsTo() should get a TODO comment
    it('should add TODO comment for zero-arg @hasMany()', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
export default class Foo extends Model {
  @hasMany() items!: any[];
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('TODO');
      expect(result).toContain('@hasMany()');
    });

    it('should add TODO comment for zero-arg @belongsTo()', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
export default class Foo extends Model {
  @belongsTo() parent!: any;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('TODO');
      expect(result).toContain('@belongsTo()');
    });
  });

  describe('belongsTo().load() chain', () => {
    it('should handle model file that uses this.belongsTo("user").load()', () => {
      const input = `import Model, { belongsTo, attr } from '@ember-data/model';
export default class Post extends Model {
  @attr('string') title!: string;
  @belongsTo('user') author!: any;

  async loadAuthor() {
    const ref = this.belongsTo('user');
    return ref.load();
  }
}`;
      const result = runTransform(input, 'app/models/post.ts');
      // Import should be rewritten
      expect(result).toContain("from '@warp-drive/legacy/model'");
      // [Type] brand should be added
      expect(result).toContain('[Type]');
      expect(result).toContain("'post'");
      // The belongsTo decorator should get options with async: false and inverse: null
      expect(result).toContain('async: false');
      expect(result).toContain('inverse: null');
      // The this.belongsTo('user').load() call should be preserved (not a decorator)
      expect(result).toContain("this.belongsTo('user')");
      expect(result).toContain('.load()');
    });
  });

  describe('type replacements', () => {
    it('should replace SyncHasMany<X> with X[]', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
import { SyncHasMany } from '@ember-data/model/-private';
export default class Foo extends Model {
  @hasMany('bar', { async: false, inverse: null }) bars!: SyncHasMany<Bar>;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('Bar[]');
      expect(result).not.toContain('SyncHasMany');
    });

    it('should replace AsyncHasMany<X> with X[]', () => {
      const input = `import Model, { hasMany } from '@ember-data/model';
import { AsyncHasMany } from '@ember-data/model/-private';
export default class Foo extends Model {
  @hasMany('bar', { async: false, inverse: null }) bars!: AsyncHasMany<Bar>;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).toContain('Bar[]');
      expect(result).not.toContain('AsyncHasMany');
    });

    it('should replace AsyncBelongsTo<X> with X', () => {
      const input = `import Model, { belongsTo } from '@ember-data/model';
import { AsyncBelongsTo } from '@ember-data/model/-private';
export default class Foo extends Model {
  @belongsTo('bar', { async: false, inverse: null }) bar!: AsyncBelongsTo<Bar>;
}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('AsyncBelongsTo');
      // Should just be Bar (not Bar[])
      expect(result).toContain('bar!: Bar');
    });

    it('should remove @ember-data/model/-private import when all specifiers handled', () => {
      const input = `import Model from '@ember-data/model';
import { SyncHasMany, AsyncHasMany, AsyncBelongsTo } from '@ember-data/model/-private';
export default class Foo extends Model {}`;
      const result = runTransform(input, 'app/models/foo.ts');
      expect(result).not.toContain('@ember-data/model/-private');
    });
  });

  // L8: .gts file support
  describe('.gts file support', () => {
    it('should transform imports in .gts files while preserving <template> blocks', () => {
      const input = `import Model, { attr } from '@ember-data/model';

export default class Foo extends Model {
  @attr('string') name!: string;

  <template>
    <div>{{this.name}}</div>
  </template>
}`;
      const result = runTransform(input, 'app/models/foo.gts');
      // Import should be rewritten
      expect(result).toContain("from '@warp-drive/legacy/model'");
      expect(result).not.toContain("from '@ember-data/model'");
      // [Type] brand should be added
      expect(result).toContain('[Type]');
      // Template should be preserved
      expect(result).toContain('<template>');
      expect(result).toContain('{{this.name}}');
      expect(result).toContain('</template>');
    });

    it('should handle .gts file with no transformable content', () => {
      const input = `import Component from '@glimmer/component';

export default class Hello extends Component {
  <template>
    <div>Hello</div>
  </template>
}`;
      const result = applyTransform(
        transform,
        { appName: 'brn' },
        { source: input, path: 'app/components/hello.gts' },
        { parser: 'ts' },
      );
      // No ember-data imports, so no changes
      expect(result).toBe('');
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
