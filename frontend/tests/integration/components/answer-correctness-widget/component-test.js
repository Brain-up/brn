import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | answer-correctness-widget', function (hooks) {
  setupRenderingTest(hooks);

  test('gets victory image if isCorrect', async function (assert) {
    await render(
      hbs`<AnswerCorrectnessWidget @isCorrect={{true}} @maxImagesNumber={{1}}/>`,
    );

    assert.dom('[data-test-answer-correctness-widget]').exists();
  });

  test('gets regret image if isCorrect is false', async function (assert) {
    const host = window.location.origin;

    await render(
      hbs`<AnswerCorrectnessWidget @isCorrect={{false}} @maxImagesNumber={{1}} />`,
    );

    assert.dom('[data-test-answer-correctness-widget]').hasStyle({
      backgroundImage: `url("${host}/pictures/regret/regret1.jpg"), url("${host}/pictures/regret/regret1.png"), url("${host}/pictures/regret/regret1.jpeg"), url("${host}/pictures/regret/regret1.svg")`,
    });
  });
});
