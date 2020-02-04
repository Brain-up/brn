import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import pageObject from './test-support/page-object';

module('Integration | Component | answer-correctness-widget', function(hooks) {
  setupRenderingTest(hooks);

  test('gets victory image if isCorrect', async function(assert) {
    await render(
      hbs`<AnswerCorrectnessWidget @isCorrect={{true}} @maxImagesNumber=1/>`,
    );

    assert.equal(
      pageObject.widgetStyleAttribute,
      `background-image: url('/pictures/victory/victory1.jpg'), url('/pictures/victory/victory1.png'), url('/pictures/victory/victory1.jpeg'), url('/pictures/victory/victory1.svg')`,
      'has right image paths',
    );
  });

  test('gets regret image if isCorrect is false', async function(assert) {
    await render(
      hbs`<AnswerCorrectnessWidget @isCorrect={{false}} @maxImagesNumber=1/>`,
    );

    assert.equal(
      pageObject.widgetStyleAttribute,
      `background-image: url('/pictures/regret/regret1.jpg'), url('/pictures/regret/regret1.png'), url('/pictures/regret/regret1.jpeg'), url('/pictures/regret/regret1.svg')`,
      'has right image paths',
    );
  });
});
