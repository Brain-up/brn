import ExerciseGroup from 'brn/components/exercise-group';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
module('Integration | Component | exercise-group', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');
  test('it renders', async function (assert) {
    this.set('group', {
      picture: 'any',
      name: 'any',
      description: 'any',
      count: 'any',
    });
    const self = this;




    await render(<template><ExerciseGroup @group={{self.group}} /></template>);
    assert.dom('div').exists();
  });
});
