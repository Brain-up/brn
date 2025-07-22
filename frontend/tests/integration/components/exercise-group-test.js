import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
module('Integration | Component | exercise-group', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');
  test('it renders', async function (assert) {
    this.set('group', {
      picture: 'any',
      name: 'any',
      description: 'any',
      count: 'any',
    });
    await render(hbs`<ExerciseGroup @group={{this.group}} />`);
    assert.dom('div').exists();
  });
});
