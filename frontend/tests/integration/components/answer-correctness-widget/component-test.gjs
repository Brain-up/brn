import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import AnswerCorrectnessWidget from 'brn/components/answer-correctness-widget';

module('Integration | Component | answer-correctness-widget', function (hooks) {
  setupRenderingTest(hooks);
  setupIntl(hooks, 'en-us');

  test('gets victory image if isCorrect', async function (assert) {
    await render(<template><AnswerCorrectnessWidget @isCorrect={{true}} /></template>);

    assert.dom('[data-test-answer-correctness-widget]').exists();
  });

  test('gets regret image if isCorrect is false', async function (assert) {
    await render(<template><AnswerCorrectnessWidget @isCorrect={{false}} /></template>);

    const styleNode = getComputedStyle(
      document.querySelector('[data-test-answer-correctness-widget]'),
    ).backgroundImage;

    assert.equal(
      styleNode.split(',').every((e) => e.includes('regret/regret')),
      true,
      'has regret/ in every image path',
    );
    const exts = ['jpg', 'png', 'svg', 'jpeg'].map((e) => `.${e}`);
    exts.forEach((ext) => {
      assert.equal(
        styleNode.includes(ext),
        true,
        `has ${ext} image in background`,
      );
    });
  });
});
