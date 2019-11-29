import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Model | completion dependent', function(hooks) {
  setupTest(hooks);

  test('isCompleted if all children are completed', function(assert) {
    let store = this.owner.lookup('service:store');
    const children = [
      {
        isCompleted: true,
      },
      {
        isCompleted: true,
      },
    ];
    let model = store.createRecord('completion-dependent', { children });
    assert.ok(model.isCompleted, 'is true');
  });

  test('canInteract according to sibling models', function(assert) {
    let store = this.owner.lookup('service:store');
    let parent = store.createRecord('completion-dependent', {});
    const children = [
      {
        isCompleted: true,
      },
      {
        isCompleted: false,
      },
      {
        isCompleted: false,
      },
    ].map((childData) =>
      store.createRecord('completion-dependent', { ...childData, parent }),
    );

    parent.set('children', children);

    assert.ok(
      children[1].canInteract,
      'true if all previous sublings completed',
    );
    assert.notOk(
      children[2].canInteract,
      'false if some of previous sublings are not completed',
    );
  });

  test('has previous sibling models prop', function(assert) {
    let store = this.owner.lookup('service:store');
    let parent = store.createRecord('completion-dependent');
    const children = [
      {
        isCompleted: true,
        order: 1,
      },
      {
        isCompleted: false,
        order: 2,
      },
      {
        isCompleted: false,
        order: 3,
      },
    ].map((childData) =>
      store.createRecord('completion-dependent', { ...childData, parent }),
    );
    parent.children = children;
    assert.deepEqual(
      children[2].previousSiblings.mapBy('order'),
      [children[0], children[1]].mapBy('order'),
    );
  });
});
