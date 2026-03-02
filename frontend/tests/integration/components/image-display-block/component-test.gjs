import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import pageObject from './test-support/page-object';

module('Integration | Component | image-display-block', function (hooks) {
  setupRenderingTest(hooks);setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    this.set('fileUrl', '/pictures/вить.jpg');
    this.set('label', 'вить');

    const controller = this.owner.lookup('controller:group.series.subgroup.exercise.task');
    controller.set('model', {
      shouldBeWithPictures: true,
    });

    const self = this;




    await render(<template><ImageDisplayBlock
    data-test-component-container
    @pictureFileUrl={{self.fileUrl}}
    @label={{self.label}}
    /></template>);

    assert.equal(
      pageObject.ariaLabel,
      'изображение вить',
      'shows a given label',
    );
    assert.equal(
      pageObject.imageAttribute,
      '--word-picture-url: url(/pictures/вить.jpg);',
      'has a right image variable',
    );
  });
});
