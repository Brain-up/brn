import TaskPlayerSingleSimpleWordsOption from 'brn/components/task-player/single-simple-words/option';

/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
module(
  'Integration | Component | task-player/single-simple-words/option',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');
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
      const self = this;




      await render(
        <template><TaskPlayerSingleSimpleWordsOption @activeWord={{self.activeWord}} @answerOption={{self.answerOption}} @checkMaybe={{self.checkMaybe}} @disableAnswers={{self.disableAnswers}} @mode={{self.mode}} @onPlayText={{self.onPlayText}} /></template>
      );
      assert.equal(this.element.textContent.trim(), 'any'); // Template block usage:
    });
  },
);
