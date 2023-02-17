import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render, click } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | ui/avatars', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders avatars', async function (assert) {
    this.setProperties({
      selectedAvatar: '1',
      onCancel: () => void 0,
      onAvatarSubmit: () => void 0,
    });
    await render(
      hbs`<Ui::Avatars @selectedAvatar={{this.selectedAvatar}} @onCancel={{this.onCancel}} @onSubmit={{this.onAvatarSubmit}} />`,
    );

    assert.dom('img').exists({ count: 20 });
    assert
      .dom('[data-test-avatar-btn="1"]')
      .hasAttribute('data-test-avatar-btn-selected');

    await click(`[data-test-avatar-btn="2"]`);

    assert
      .dom('[data-test-avatar-btn="1"]')
      .hasNoAttribute('data-test-avatar-btn-selected');

    assert
      .dom('[data-test-avatar-btn="2"]')
      .hasAttribute('data-test-avatar-btn-selected');
  });
});
