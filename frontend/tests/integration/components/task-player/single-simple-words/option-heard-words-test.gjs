import TaskPlayerSingleSimpleWordsOption from 'brn/components/task-player/single-simple-words/option';

/* eslint-disable @typescript-eslint/no-empty-function */
import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, rerender } from '@ember/test-helpers';

module(
  'Integration | Component | task-player/single-simple-words/option | heard words green fill',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    hooks.beforeEach(function () {
      this.set('activeWord', '');
      this.set('answerOption', { word: 'cat' });
      this.set('checkMaybe', () => {});
      this.set('disableAnswers', false);
      this.set('mode', 'interact');
      this.set('onPlayText', () => {});
      this.set('heardWords', new Set());
    });

    async function renderOption(self) {
      await render(
        <template><TaskPlayerSingleSimpleWordsOption @activeWord={{self.activeWord}} @answerOption={{self.answerOption}} @checkMaybe={{self.checkMaybe}} @disableAnswers={{self.disableAnswers}} @mode={{self.mode}} @onPlayText={{self.onPlayText}} @heardWords={{self.heardWords}} /></template>
      );
    }

    test('applies green fill classes in interact mode when word has been heard', async function (assert) {
      this.set('heardWords', new Set(['cat']));

      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasClass('border-green-400', 'has green border class when heard in interact mode');
      assert.dom('[data-test-task-answer]').hasClass('text-green-700', 'has green text class when heard in interact mode');
      assert.dom('[data-test-task-answer]').hasClass('bg-green-50', 'has green background class when heard in interact mode');
      assert.dom('[data-test-task-answer]').hasNoClass('border-purple-primary/25', 'green border replaces default purple border');
    });

    test('does NOT apply green fill classes when word has NOT been heard in interact mode', async function (assert) {
      this.set('heardWords', new Set(['dog']));

      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'no green border when word not heard');
      assert.dom('[data-test-task-answer]').hasNoClass('text-green-700', 'no green text when word not heard');
      assert.dom('[data-test-task-answer]').hasNoClass('bg-green-50', 'no green bg when word not heard');
      assert.dom('[data-test-task-answer]').hasClass('border-purple-primary/25', 'has default purple border');
    });

    test('does NOT apply green fill classes when NOT in interact mode even if word was heard', async function (assert) {
      this.set('mode', 'task');
      this.set('heardWords', new Set(['cat']));

      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'no green border in task mode');
      assert.dom('[data-test-task-answer]').hasNoClass('text-green-700', 'no green text in task mode');
      assert.dom('[data-test-task-answer]').hasNoClass('bg-green-50', 'no green bg in task mode');
      assert.dom('[data-test-task-answer]').hasClass('border-purple-primary/25', 'has default purple border in task mode');
    });

    test('does NOT apply green fill classes in listen mode even if word was heard', async function (assert) {
      this.set('mode', 'listen');
      this.set('heardWords', new Set(['cat']));

      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'no green border in listen mode');
      assert.dom('[data-test-task-answer]').hasNoClass('text-green-700', 'no green text in listen mode');
      assert.dom('[data-test-task-answer]').hasNoClass('bg-green-50', 'no green bg in listen mode');
    });

    test('does NOT apply green fill classes in interact mode when heardWords is undefined', async function (assert) {
      this.set('heardWords', undefined);

      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'no green border when heardWords is undefined');
      assert.dom('[data-test-task-answer]').hasNoClass('text-green-700', 'no green text when heardWords is undefined');
      assert.dom('[data-test-task-answer]').hasNoClass('bg-green-50', 'no green bg when heardWords is undefined');
    });

    test('applies active word styling instead of green fill when word is both active and heard', async function (assert) {
      this.set('activeWord', 'cat');
      this.set('heardWords', new Set(['cat']));

      await renderOption(this);

      // When the word is the active word, the active styling takes precedence
      assert.dom('[data-test-task-answer]').hasClass('bg-purple-primary', 'active word has purple bg');
      assert.dom('[data-test-task-answer]').hasClass('text-white', 'active word has white text');
      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'active word does not show green border');
    });

    test('green fill updates when heardWords set changes', async function (assert) {
      await renderOption(this);

      assert.dom('[data-test-task-answer]').hasNoClass('border-green-400', 'no green border initially');
      assert.dom('[data-test-task-answer]').hasClass('border-purple-primary/25', 'has default purple border initially');

      // Simulate parent component updating heardWords with a new Set
      this.set('heardWords', new Set(['cat']));
      await rerender();

      assert.dom('[data-test-task-answer]').hasClass('border-green-400', 'green border appears after hearing word');
      assert.dom('[data-test-task-answer]').hasClass('text-green-700', 'green text appears after hearing word');
      assert.dom('[data-test-task-answer]').hasClass('bg-green-50', 'green bg appears after hearing word');
      assert.dom('[data-test-task-answer]').hasNoClass('border-purple-primary/25', 'default purple border removed after hearing word');
    });
  },
);
