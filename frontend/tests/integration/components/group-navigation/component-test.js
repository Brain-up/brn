import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { run } from '@ember/runloop';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | group-navigation', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    const store = this.owner.lookup('service:store');
    const group = run(() => {
      const group = store.createRecord('group');
      store.createRecord('series', { group, name: 1 });
      store.createRecord('series', { group });
      store.createRecord('series', { group });
      return group;
    });
    this.setProperties({ group });

    await render(hbs`<GroupNavigation @group={{this.group}}/>`);

    assert.equal(this.element.querySelectorAll('a').length, 3, '3 links');
  });
});
