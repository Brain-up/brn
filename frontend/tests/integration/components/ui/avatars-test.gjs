import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import UiAvatars from 'brn/components/ui/avatars';

module('Integration | Component | ui/avatars', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders avatars', async function (assert) {
    this.setProperties({
      selectedAvatar: '1',
      onCancel: () => void 0,
      onAvatarSubmit: () => void 0,
    });
    const self = this;




    await render(
      <template><UiAvatars @selectedAvatar={{self.selectedAvatar}} @onCancel={{self.onCancel}} @onSubmit={{self.onAvatarSubmit}} /></template>
    );

    assert.dom('img').exists({ count: 20 });
    assert.dom('[data-test-avatar-btn="1"]').hasClass('activeTab');

    await click(`[data-test-avatar-btn="2"]`);

    assert.dom('[data-test-avatar-btn="1"]').hasNoClass('activeTab');

    assert.dom('[data-test-avatar-btn="2"]').hasClass('activeTab');
  });
});
