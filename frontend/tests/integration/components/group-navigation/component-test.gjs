import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { run } from '@ember/runloop';
import GroupNavigation from 'brn/components/group-navigation';

module('Integration | Component | group-navigation', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    const store = this.owner.lookup('service:store');
    this.owner.setupRouter();
    const group = run(() => {
      const group = store.createRecord('group');
      store.createRecord('series', { group, name: 1 });
      store.createRecord('series', { group });
      store.createRecord('series', { group });
      return group;
    });
    this.setProperties({ group });
    const self = this;




    await render(<template><GroupNavigation @group={{self.group}} /></template>);
    assert.equal(
      this.element.querySelectorAll('a').length,
      3,
      '3 links',
    );
  });
});
