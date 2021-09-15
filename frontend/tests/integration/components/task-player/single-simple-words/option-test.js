/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';
module(
  'Integration | Component | task-player/single-simple-words/option',
  function (hooks) {
    setupRenderingTest(hooks);
    test('it renders', async function (assert) {
      this.set('activeWord', 'activeword');
      this.set('answerOption', { word: 'any' });
      this.set('checkMaybe', () => {});
      this.set('disableAnswers', [
        { id: 1, name: 'one' },
        { id: 2, name: 'two' },
      ]);
      this.set('mode', 'mode');
      this.set('onPlayText', () => assert.ok('event:onPlayText'));
      await render(
        hbs`<TaskPlayer::SingleSimpleWords::Option @activeWord={{this.activeWord}} @answerOption={{this.answerOption}} @checkMaybe={{this.checkMaybe}} @disableAnswers={{this.disableAnswers}} @mode={{this.mode}} @onPlayText={{this.onPlayText}} />`,
      );
      assert.equal(this.element.textContent.trim(), 'any'); // Template block usage:
    });
  },
);
