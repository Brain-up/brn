import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
module('Integration | Component | exercise-group', function (hooks) {
  setupRenderingTest(hooks);
  test('it renders', async function (assert) {
    this.set('group', { "picture": "any", "name": "any", "description": "any", "count": "any" });
    await render(hbs`<ExerciseGroup @group={{this.group}} />`);
    assert.dom('div').exists();
  });
});
