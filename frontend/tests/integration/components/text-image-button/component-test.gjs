import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, settled } from '@ember/test-helpers';
import TextImageButton from 'brn/components/text-image-button';

module('Integration | Component | text-image-button', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('sets right classes and action', async function (assert) {
    this.set('clickAction', () => {
      assert.ok(true, 'calls clickAction');
    });

    const self = this;




    await render(<template><TextImageButton
    @disabled={{true}}
    @isSelected={{true}}
    @clickAction={{self.clickAction}}
    @pictureFileUrl="/image"
    @word="word"
    /></template>);

    assert
      .dom('[data-test-task-answer]')
      .hasAttribute('data-test-task-answer-option', 'word');
    assert.dom('[data-test-task-answer]').isDisabled();
    assert.dom('[data-test-task-answer]').hasClass('c-text-image-button__selected');
  });

  test('shows image by default when showImages is true', async function (assert) {
    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');
    controller.set('model', { shouldBeWithPictures: true });

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    this.set('noop', () => {});

    const self = this;




    await render(<template><TextImageButton
    @pictureFileUrl="/image"
    @word="word"
    @clickAction={{self.noop}}
    /></template>);

    assert.dom('[data-test-text-image-button]').doesNotHaveClass('text-mode');
    assert.dom('[data-test-task-answer]').exists();
  });

  test('hides image and shows text-mode when showImages is false', async function (assert) {
    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');
    controller.set('model', { shouldBeWithPictures: true });

    const studyConfig = this.owner.lookup('service:study-config');
    studyConfig.toggleImageVisibility();

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    this.set('noop', () => {});

    const self = this;




    await render(<template><TextImageButton
    @pictureFileUrl="/image"
    @word="word"
    @clickAction={{self.noop}}
    /></template>);

    assert.dom('[data-test-text-image-button]').hasClass('text-mode');
  });

  test('reactively toggles text-mode class when showImages changes', async function (assert) {
    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');
    controller.set('model', { shouldBeWithPictures: true });

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    this.set('noop', () => {});

    const self = this;




    await render(<template><TextImageButton
    @pictureFileUrl="/image"
    @word="word"
    @clickAction={{self.noop}}
    /></template>);

    assert.dom('[data-test-text-image-button]').doesNotHaveClass('text-mode', 'images shown by default');

    const studyConfig = this.owner.lookup('service:study-config');
    studyConfig.toggleImageVisibility();
    await settled();

    assert.dom('[data-test-text-image-button]').hasClass('text-mode', 'images hidden after toggle');

    studyConfig.toggleImageVisibility();
    await settled();

    assert.dom('[data-test-text-image-button]').doesNotHaveClass('text-mode', 'images shown again after second toggle');
  });
});
