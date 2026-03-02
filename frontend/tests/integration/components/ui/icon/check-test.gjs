import UiIconCheck from 'brn/components/ui/icon/check';

import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';

module('Integration | Component | ui/icon/check', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(<template><UiIconCheck /></template>);

    assert.dom('svg').doesNotExist();

    await render(<template><UiIconCheck @isCompleted={{true}} /></template>);
    assert.dom('svg').exists({ count: 1 });

    await render(<template><UiIconCheck @isActive={{true}} /></template>);
    assert.dom('svg').exists({ count: 1 });

    await render(
        <template><UiIconCheck @isLocked={{true}} @isCompleted={{true}} /></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isCompleted={{true}} @isActive={{true}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isLocked={{true}} @isActive={{true}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });

    await render(
        <template><UiIconCheck @isLocked={{true}} @isCompleted={{true}} @isActive={{true}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isLocked={{true}} @isCompleted={{false}} @isActive={{true}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isLocked={{true}} @isCompleted={{true}} @isActive={{false}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isLocked={{true}} @isCompleted={{false}} @isActive={{false}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });
    await render(
        <template><UiIconCheck @isLocked={{false}} @isCompleted={{true}} @isActive={{false}}/></template>
    );
    assert.dom('svg').exists({ count: 1 });

    await render(<template><UiIconCheck @isDisabled={{true}} /></template>);
    assert.dom('svg').exists({ count: 1 });
  });
});
