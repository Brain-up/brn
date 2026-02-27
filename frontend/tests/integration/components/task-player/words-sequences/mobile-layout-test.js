import { module, test } from 'qunit';
import { setupIntl } from 'ember-intl/test-support';
import { setupRenderingTest } from 'ember-qunit';
import { render, find, findAll } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';
import AudioService from 'brn/services/audio';

module(
  'Integration | Component | words-sequences | mobile layout (#2717)',
  function (hooks) {
    setupRenderingTest(hooks);
    setupIntl(hooks, 'en-us');

    hooks.beforeEach(function () {
      class MockAudio extends AudioService {
        startPlayTask() {}
        audioUrlForText() {
          return '';
        }
      }
      this.owner.register('service:audio', MockAudio);

      const store = this.owner.lookup('service:store');
      const model = store.createRecord('task/words-sequences');
      model.setProperties({
        exerciseMechanism: 'MATRIX',
        type: 'task/MATRIX',
        name: '',
        wrongAnswers: [],
        template: '<OBJECT OBJECT_ACTION>',
        answerOptions: {
          OBJECT_ACTION: [
            {
              id: 345,
              audioFileUrl: '',
              word: 'runs',
              wordType: 'OBJECT_ACTION',
              pictureFileUrl: '',
              soundsCount: 0,
            },
            {
              id: 346,
              audioFileUrl: '',
              word: 'sits',
              wordType: 'OBJECT_ACTION',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          OBJECT: [
            {
              id: 344,
              audioFileUrl: '',
              word: 'cat',
              wordType: 'OBJECT',
              pictureFileUrl: '',
              soundsCount: 0,
            },
            {
              id: 347,
              audioFileUrl: '',
              word: 'dog',
              wordType: 'OBJECT',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
        },
      });
      this.set('model', model);
      this.set('onRightAnswer', function () {});
      this.set('onWrongAnswer', function () {});
      this.set('onPlayText', function () {});
    });

    test('columns container has scrollable class for mobile support', async function (assert) {
      await render(hbs`
        <TaskPlayer::WordsSequences
          @task={{this.model}}
          @mode="task"
          @onRightAnswer={{this.onRightAnswer}}
          @onWrongAnswer={{this.onWrongAnswer}}
          @onPlayText={{this.onPlayText}}
        />
      `);

      const columnsContainer = find('.words-sequences-columns');
      assert.ok(
        columnsContainer,
        'words-sequences-columns container exists for horizontal scroll support',
      );
    });

    test('all word columns are rendered and visible', async function (assert) {
      await render(hbs`
        <TaskPlayer::WordsSequences
          @task={{this.model}}
          @mode="task"
          @onRightAnswer={{this.onRightAnswer}}
          @onWrongAnswer={{this.onWrongAnswer}}
          @onPlayText={{this.onPlayText}}
        />
      `);

      const columns = findAll('.type-column');
      assert.strictEqual(
        columns.length,
        2,
        'both word type columns are rendered',
      );

      columns.forEach((col) => {
        assert.notStrictEqual(
          window.getComputedStyle(col).display,
          'none',
          'column is not hidden',
        );
      });
    });

    test('all answer option buttons are rendered', async function (assert) {
      await render(hbs`
        <TaskPlayer::WordsSequences
          @task={{this.model}}
          @mode="task"
          @onRightAnswer={{this.onRightAnswer}}
          @onWrongAnswer={{this.onWrongAnswer}}
          @onPlayText={{this.onPlayText}}
        />
      `);

      const buttons = findAll('[data-test-task-answer]');
      assert.strictEqual(
        buttons.length,
        4,
        'all 4 answer option buttons are rendered',
      );

      const words = buttons.map((btn) =>
        btn.getAttribute('data-test-task-answer-option'),
      );
      assert.ok(words.includes('cat'), 'cat button is rendered');
      assert.ok(words.includes('dog'), 'dog button is rendered');
      assert.ok(words.includes('runs'), 'runs button is rendered');
      assert.ok(words.includes('sits'), 'sits button is rendered');
    });

    test('columns use flex layout allowing responsive sizing', async function (assert) {
      await render(hbs`
        <TaskPlayer::WordsSequences
          @task={{this.model}}
          @mode="task"
          @onRightAnswer={{this.onRightAnswer}}
          @onWrongAnswer={{this.onWrongAnswer}}
          @onPlayText={{this.onPlayText}}
        />
      `);

      const columns = findAll('.type-column');
      columns.forEach((col) => {
        const style = window.getComputedStyle(col);
        assert.strictEqual(
          style.display,
          'flex',
          'column uses flex layout',
        );
        assert.strictEqual(
          style.flexDirection,
          'column',
          'column items are stacked vertically',
        );
      });
    });

    test('renders with many columns (6 word types)', async function (assert) {
      const store = this.owner.lookup('service:store');
      const model = store.createRecord('task/words-sequences');
      model.setProperties({
        exerciseMechanism: 'MATRIX',
        type: 'task/MATRIX',
        name: '',
        wrongAnswers: [],
        template: '<TYPE1 TYPE2 TYPE3 TYPE4 TYPE5 TYPE6>',
        answerOptions: {
          TYPE1: [
            {
              id: 1,
              audioFileUrl: '',
              word: 'w1',
              wordType: 'TYPE1',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          TYPE2: [
            {
              id: 2,
              audioFileUrl: '',
              word: 'w2',
              wordType: 'TYPE2',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          TYPE3: [
            {
              id: 3,
              audioFileUrl: '',
              word: 'w3',
              wordType: 'TYPE3',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          TYPE4: [
            {
              id: 4,
              audioFileUrl: '',
              word: 'w4',
              wordType: 'TYPE4',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          TYPE5: [
            {
              id: 5,
              audioFileUrl: '',
              word: 'w5',
              wordType: 'TYPE5',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
          TYPE6: [
            {
              id: 6,
              audioFileUrl: '',
              word: 'w6',
              wordType: 'TYPE6',
              pictureFileUrl: '',
              soundsCount: 0,
            },
          ],
        },
      });
      this.set('model', model);

      await render(hbs`
        <TaskPlayer::WordsSequences
          @task={{this.model}}
          @mode="task"
          @onRightAnswer={{this.onRightAnswer}}
          @onWrongAnswer={{this.onWrongAnswer}}
          @onPlayText={{this.onPlayText}}
        />
      `);

      const columns = findAll('.type-column');
      assert.strictEqual(columns.length, 6, 'all 6 columns are rendered');

      const buttons = findAll('[data-test-task-answer]');
      assert.strictEqual(
        buttons.length,
        6,
        'all 6 answer buttons are rendered for 6-column layout',
      );

      const container = find('.words-sequences-columns');
      assert.ok(container, 'scrollable container wraps all columns');
    });
  },
);
