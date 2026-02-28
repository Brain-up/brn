import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { getService } from 'brn/utils/schema-helpers';

module('Unit | Utility | schema-helpers', function (hooks) {
  setupTest(hooks);

  test('getService returns null for non-record objects', function (assert) {
    const result = getService({}, 'tasks-manager');
    assert.strictEqual(result, null, 'returns null for plain objects');
  });

  test('getService returns null for null input', function (assert) {
    const result = getService(null, 'tasks-manager');
    assert.strictEqual(result, null, 'returns null for null');
  });
});
