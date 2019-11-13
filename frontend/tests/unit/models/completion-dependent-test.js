import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Model | completion dependent', function(hooks) {
  setupTest(hooks);

  test('canInteractWith method', function(assert) {
    let store = this.owner.lookup('service:store');
    const targetChild = {};
    const children = [
      {
        isCompleted: true,
      },
      {
        isCompleted: true,
      },
      targetChild,
    ];
    let model = store.createRecord('completion-dependent', { children });
    assert.ok(
      model.canInteractWith(targetChild),
      'true if all the previous children are completed',
    );

    const children2 = [
      {
        isCompleted: true,
      },
      {
        isCompleted: false,
      },
      targetChild,
    ];
    let model2 = store.createRecord('completion-dependent', {
      children: children2,
    });
    assert.notOk(
      model2.canInteractWith(targetChild),
      'false if all the previous children are completed',
    );
  });

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
});
