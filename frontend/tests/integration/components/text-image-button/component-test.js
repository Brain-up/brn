import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | text-image-button', function (hooks) {
  setupRenderingTest(hooks);

  test('sets right classes and action', async function (assert) {
    this.set('clickAction', () => {
      assert.ok(true, 'calls clickAction');
    });

    await render(hbs`<TextImageButton
      @disabled={{true}}
      @isSelected={{true}}
      @clickAction={{action this.clickAction}}
      @pictureFileUrl="/image"
      @word="word"
    />`);

    assert
      .dom('[data-test-task-answer]')
      .hasAttribute('data-test-task-answer-option', 'word');
    assert.dom('[data-test-task-answer]').isDisabled();
    assert.dom('[data-test-task-answer]').hasClass('selected');
  });
});
