import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

module('Integration | Component | text-image-button', function(hooks) {
  setupRenderingTest(hooks);

  test('sets right classes and action', async function(assert) {
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

    assert.equal(
      pageObject.buttonText,
      'word',
      'has an option with the text: "word"',
    );
    assert.ok(pageObject.buttonIsDisabled, 'answer option button is disabled');
    assert.ok(pageObject.buttonIsSelected, 'answer is selected');
  });
});
