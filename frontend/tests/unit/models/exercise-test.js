import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Model | exercise', function(hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('isCompleted when all tasks completed', function(assert) {
    let store = this.owner.lookup('service:store');
    let model = store.createRecord('exercise', {});

    [{ isCompleted: true }, { isCompleted: true }].map((data) =>
      store.createRecord('task', { ...data, exercise: model }),
    );

    assert.ok(model.isCompleted, 'works fine');
  });

  test('isCompleted when has no tasks, but can interact', function(assert) {
    let store = this.owner.lookup('service:store');
    let model = store.createRecord('exercise', { tasks: [] });
    store.createRecord('series', { exercises: [model] });

    assert.ok(model.isCompleted, 'works fine');
  });
});
