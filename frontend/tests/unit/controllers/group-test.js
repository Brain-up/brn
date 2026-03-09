import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Controller | group', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let controller = this.owner.lookup('controller:group');
    assert.ok(controller);
  });

  module('series getter', function () {
    test('returns series array from composite { group, series } model', function (assert) {
      const controller = this.owner.lookup('controller:group');
      const seriesData = [
        { id: '1', name: 'Words' },
        { id: '2', name: 'Phrases' },
      ];
      controller.model = {
        group: { id: '10', name: 'Test Group' },
        series: seriesData,
      };

      assert.strictEqual(controller.series.length, 2, 'returns both series');
      assert.strictEqual(controller.series[0].id, '1');
      assert.strictEqual(controller.series[1].id, '2');
      assert.strictEqual(controller.series, seriesData, 'returns the exact same array reference');
    });

    test('returns empty array when model is a bare GroupModel (regression for tabs visibility bug)', function (assert) {
      const controller = this.owner.lookup('controller:group');
      // When Ember skips model() and passes a bare GroupModel via <LinkTo @model=...>,
      // the controller.model is just the group record. Before the fix in redirect(),
      // the series getter would return [] and GroupNavigation would render zero tabs.
      controller.model = { id: '10', name: 'Bare Group' };

      assert.deepEqual(controller.series, [], 'returns empty array for bare GroupModel');
    });

    test('returns empty array when composite model has empty series', function (assert) {
      const controller = this.owner.lookup('controller:group');
      controller.model = {
        group: { id: '10', name: 'Empty Group' },
        series: [],
      };

      assert.deepEqual(controller.series, [], 'returns empty array');
    });
  });

  module('group getter', function () {
    test('returns group from composite { group, series } model', function (assert) {
      const controller = this.owner.lookup('controller:group');
      const groupData = { id: '10', name: 'Test Group' };
      controller.model = {
        group: groupData,
        series: [{ id: '1', name: 'Words' }],
      };

      assert.strictEqual(controller.group.id, '10');
      assert.strictEqual(controller.group.name, 'Test Group');
      assert.strictEqual(controller.group, groupData, 'returns the exact same object reference');
    });

    test('returns the model itself when model is a bare GroupModel', function (assert) {
      const controller = this.owner.lookup('controller:group');
      const bareGroup = { id: '10', name: 'Bare Group' };
      controller.model = bareGroup;

      assert.strictEqual(controller.group.id, '10');
      assert.strictEqual(controller.group.name, 'Bare Group');
      assert.strictEqual(controller.group, bareGroup, 'returns the bare model directly');
    });
  });

  module('model update flow (integration with redirect fix)', function () {
    test('series getter returns data after model is updated from bare to composite', function (assert) {
      const controller = this.owner.lookup('controller:group');

      // Simulate initial state: Ember passes bare GroupModel
      controller.model = { id: '10', name: 'My Group' };
      assert.deepEqual(controller.series, [], 'before fix: series is empty with bare model');

      // Simulate what redirect() does after the fix: update to composite format
      const seriesData = [
        { id: '1', name: 'Series A' },
        { id: '2', name: 'Series B' },
      ];
      controller.model = {
        group: { id: '10', name: 'My Group' },
        series: seriesData,
      };
      assert.strictEqual(controller.series.length, 2, 'after fix: series is populated');
      assert.strictEqual(controller.series[0].id, '1');
      assert.strictEqual(controller.group.id, '10', 'group is still accessible');
    });
  });
});
