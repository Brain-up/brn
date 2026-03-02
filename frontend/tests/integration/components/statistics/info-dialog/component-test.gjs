import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import sinon from 'sinon';
import click from '@ember/test-helpers/dom/click';

module('Integration | Component | statistics/info-dialog', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('it renders', async function (assert) {
    const closeModalAction = sinon.stub();
    this.set('closeModalAction', closeModalAction);

    const self = this;




    await render(
        <template><Statistics::InfoDialog @closeModalAction={{self.closeModalAction}} /></template>
    );
    assert.dom('[data-test-info-dialog]').exists();
    await click('[data-test-button-ok]');
    assert.ok(closeModalAction.calledOnce);
    await click('[data-test-button-close]');
    assert.ok(closeModalAction.calledTwice);
  });

  test('it shows English image for en-us locale', async function (assert) {
    const closeModalAction = sinon.stub();
    this.set('closeModalAction', closeModalAction);

    const self = this;




    await render(
        <template><Statistics::InfoDialog @closeModalAction={{self.closeModalAction}} /></template>
    );
    assert
      .dom('[data-test-info-image]')
      .hasAttribute('src', '/ui/statistics-info-dialog-en.svg');
  });

  module('with ru-ru locale', function (hooks) {
    setupIntl(hooks, 'ru-ru');

    test('it shows Russian image for ru-ru locale', async function (assert) {
      const closeModalAction = sinon.stub();
      this.set('closeModalAction', closeModalAction);

      const self = this;




      await render(
          <template><Statistics::InfoDialog @closeModalAction={{self.closeModalAction}} /></template>
      );
      assert
        .dom('[data-test-info-image]')
        .hasAttribute('src', '/ui/statistics-info-dialog.svg');
    });
  });
});
