import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import ModelForRoute from 'brn/helpers/model-for-route';
import { getOwner } from '@ember/application';

module('Unit | Helper | model-for-route', function (hooks) {
  setupTest(hooks);

  test('returns model directly for non-group routes', function (assert) {
    const helper = new ModelForRoute(getOwner(this));
    const seriesModel = { id: '10', name: 'Series 1' };

    // Mock the route application's modelFor
    const owner = getOwner(this);
    const originalLookup = owner.lookup.bind(owner);
    owner.lookup = (name) => {
      if (name === 'route:application') {
        return { modelFor: () => seriesModel };
      }
      return originalLookup(name);
    };

    const result = helper.compute(['group.series']);
    assert.strictEqual(result, seriesModel);
  });

  test('unwraps composite group model', function (assert) {
    const helper = new ModelForRoute(getOwner(this));
    const groupRecord = { id: '1', name: 'Group 1' };
    const compositeModel = {
      group: groupRecord,
      series: [{ id: '10' }],
    };

    const owner = getOwner(this);
    const originalLookup = owner.lookup.bind(owner);
    owner.lookup = (name) => {
      if (name === 'route:application') {
        return { modelFor: () => compositeModel };
      }
      return originalLookup(name);
    };

    const result = helper.compute(['group']);
    assert.strictEqual(result, groupRecord, 'extracts group from composite model');
  });

  test('returns null model as-is', function (assert) {
    const helper = new ModelForRoute(getOwner(this));

    const owner = getOwner(this);
    const originalLookup = owner.lookup.bind(owner);
    owner.lookup = (name) => {
      if (name === 'route:application') {
        return { modelFor: () => null };
      }
      return originalLookup(name);
    };

    const result = helper.compute(['group']);
    assert.strictEqual(result, null);
  });
});
