import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | answer-correctness-widget', function (hooks) {
  setupRenderingTest(hooks);

  test('gets victory image if isCorrect', async function (assert) {
    await render(hbs`<AnswerCorrectnessWidget @isCorrect={{true}} />`);

    assert.dom('[data-test-answer-correctness-widget]').exists();
  });

  test('gets regret image if isCorrect is false', async function (assert) {
    await render(hbs`<AnswerCorrectnessWidget @isCorrect={{false}} />`);

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
