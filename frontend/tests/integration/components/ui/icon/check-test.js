import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | ui/icon/check', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<Ui::Icon::Check />`);

    assert.dom('svg').doesNotExist();

    await render(hbs`<Ui::Icon::Check @isCompleted={{true}} />`);
    assert.dom('svg').exists({ count: 1 });

    await render(hbs`<Ui::Icon::Check @isAvailable={{true}} />`);
    assert.dom('svg').exists({ count: 1 });

    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isCompleted={{true}} />`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isCompleted={{true}} @isAvailable={{true}}/>`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isAvailable={{true}}/>`);
    assert.dom('svg').exists({ count: 1 });

    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isCompleted={{true}} @isAvailable={{true}}/>`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isCompleted={{false}} @isAvailable={{true}}/>`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isCompleted={{true}} @isAvailable={{false}}/>`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isLocked={{true}} @isCompleted={{false}} @isAvailable={{false}}/>`);
    assert.dom('svg').exists({ count: 1 });
    await render(hbs`<Ui::Icon::Check @isLocked={{false}} @isCompleted={{true}} @isAvailable={{false}}/>`);
    assert.dom('svg').exists({ count: 1 });

  });
});
