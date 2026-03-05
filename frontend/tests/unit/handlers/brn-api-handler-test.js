import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { BrnApiHandler } from 'brn/handlers/brn-api-handler';

module('Unit | Handler | brn-api-handler', function (hooks) {
  setupTest(hooks);

  // ─── URL Building (tested via the handler's request method) ──────────────

  module('URL building', function () {
    function createContext(op, data) {
      return { request: { op, data } };
    }

    function createHandler() {
      return new BrnApiHandler({ peekRecord: () => null });
    }

    test('findRecord builds correct URL for standard type', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('findRecord', {
        record: { type: 'group', id: '5' },
        options: {},
      });
      let capturedUrl;
      await handler.request(ctx, async (req) => {
        capturedUrl = req.url;
        return { content: { data: { id: 5, name: 'Test' } } };
      });
      assert.strictEqual(capturedUrl, '/api/groups/5');
    });

    test('findRecord builds correct URL for headphone (path override)', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('findRecord', {
        record: { type: 'headphone', id: '1' },
        options: {},
      });
      let capturedUrl;
      await handler.request(ctx, async (req) => {
        capturedUrl = req.url;
        return { content: { data: { id: 1, name: 'Headphone' } } };
      });
      assert.strictEqual(capturedUrl, '/api/users/current/headphones/1');
    });

    test('query builds correct URL with query params', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('query', {
        type: 'series',
        query: { groupId: '5' },
        options: {},
      });
      let capturedUrl;
      await handler.request(ctx, async (req) => {
        capturedUrl = req.url;
        return { content: { data: [] } };
      });
      assert.strictEqual(capturedUrl, '/api/series?groupId=5');
    });

    test('query skips null/undefined params', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('query', {
        type: 'group',
        query: { locale: 'en-us', filter: null, extra: undefined },
        options: {},
      });
      let capturedUrl;
      await handler.request(ctx, async (req) => {
        capturedUrl = req.url;
        return { content: { data: [] } };
      });
      assert.strictEqual(capturedUrl, '/api/groups?locale=en-us');
    });

    test('findAll builds correct URL', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('findAll', {
        type: 'contributor',
        options: {},
      });
      let capturedUrl;
      await handler.request(ctx, async (req) => {
        capturedUrl = req.url;
        return { content: { data: [] } };
      });
      assert.strictEqual(capturedUrl, '/api/contributors');
    });

    test('task subtypes all use /tasks path', async function (assert) {
      const handler = createHandler();
      for (const subtype of [
        'task/signal',
        'task/single-simple-words',
        'task/words-sequences',
      ]) {
        const ctx = createContext('findRecord', {
          record: { type: subtype, id: '10' },
          options: {},
        });
        let capturedUrl;
        await handler.request(ctx, async (req) => {
          capturedUrl = req.url;
          return { content: { data: { id: 10 } } };
        });
        assert.strictEqual(capturedUrl, '/api/tasks/10', `${subtype} uses /tasks`);
      }
    });

    test('statistics types use correct path overrides', async function (assert) {
      const handler = createHandler();
      const expected = {
        'user-weekly-statistics': '/api/v2/statistics/study/week',
        'user-yearly-statistics': '/api/v2/statistics/study/year',
        'user-daily-time-table-statistics': '/api/v2/statistics/study/day',
      };
      for (const [type, expectedPath] of Object.entries(expected)) {
        const ctx = createContext('findAll', { type, options: {} });
        let capturedUrl;
        await handler.request(ctx, async (req) => {
          capturedUrl = req.url;
          return { content: { data: [] } };
        });
        assert.strictEqual(capturedUrl, expectedPath, `${type} path`);
      }
    });

    test('unknown ops are passed through to next handler', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('saveRecord', { record: { type: 'group' } });
      let passedThrough = false;
      await handler.request(ctx, async () => {
        passedThrough = true;
        return { content: null };
      });
      assert.ok(passedThrough, 'passes through unknown ops');
    });

    test('sets GET method by default', async function (assert) {
      const handler = createHandler();
      const ctx = createContext('findAll', {
        type: 'group',
        options: {},
      });
      await handler.request(ctx, async (req) => {
        assert.strictEqual(req.method, 'GET');
        return { content: { data: [] } };
      });
    });
  });

  // ─── Normalization: Group ──────────────────────────────────────────────────

  module('normalization - group', function () {
    test('normalizes a group record', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: {
            record: { type: 'group', id: '1' },
            options: {},
          },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 1,
            name: 'First Group',
            description: 'A group',
            locale: 'en-us',
            order: 1,
            series: [10, 20],
          },
        },
      }));
      assert.strictEqual(result.data.id, '1', 'stringifies id');
      assert.strictEqual(result.data.type, 'group');
      assert.strictEqual(result.data.attributes.name, 'First Group');
      assert.strictEqual(result.data.attributes.locale, 'en-us');
      assert.deepEqual(result.data.relationships.series.data, [
        { id: '10', type: 'series' },
        { id: '20', type: 'series' },
      ]);
      assert.strictEqual(
        result.data.attributes.series,
        undefined,
        'series excluded from attributes',
      );
    });
  });

  // ─── Normalization: Series ─────────────────────────────────────────────────

  module('normalization - series', function () {
    test('normalizes series with type→kind remapping and subgroups→subGroups', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'series', id: '10' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 10,
            name: 'Words',
            type: 'SINGLE_SIMPLE_WORDS',
            group: 1,
            subgroups: [100, 200],
            exercises: [50, 60],
          },
        },
      }));
      assert.strictEqual(result.data.attributes.kind, 'SINGLE_SIMPLE_WORDS', 'type remapped to kind');
      assert.deepEqual(
        result.data.relationships.group.data,
        { id: '1', type: 'group' },
        'group belongsTo',
      );
      assert.deepEqual(
        result.data.relationships.subGroups.data,
        [
          { id: '100', type: 'subgroup' },
          { id: '200', type: 'subgroup' },
        ],
        'subgroups remapped to subGroups',
      );
      assert.deepEqual(
        result.data.relationships.exercises.data,
        [
          { id: '50', type: 'exercise' },
          { id: '60', type: 'exercise' },
        ],
        'exercises hasMany',
      );
    });
  });

  // ─── Normalization: Exercise with embedded tasks ───────────────────────────

  module('normalization - exercise', function () {
    test('normalizes exercise with embedded WORDS tasks', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            name: 'Exercise 1',
            available: true,
            level: 3,
            exerciseMechanism: 'WORDS',
            series: 10,
            parent: 100,
            signals: [],
            tasks: [
              {
                id: 501,
                serialNumber: 1,
                exerciseMechanism: 'WORDS',
                answerOptions: [{ word: 'cat' }],
                correctAnswer: { word: 'dog' },
              },
            ],
          },
        },
      }));

      // Exercise attributes
      assert.strictEqual(result.data.id, '50');
      assert.strictEqual(result.data.type, 'exercise');
      assert.strictEqual(result.data.attributes.name, 'Exercise 1');
      assert.strictEqual(result.data.attributes.order, 3, 'level aliased to order');
      assert.strictEqual(result.data.attributes.level, 3, 'level preserved');

      // Exercise relationships
      assert.deepEqual(
        result.data.relationships.series.data,
        { id: '10', type: 'series' },
      );
      assert.deepEqual(
        result.data.relationships.parent.data,
        { id: '100', type: 'subgroup' },
      );

      // Embedded task in included
      assert.ok(result.included, 'has included array');
      assert.strictEqual(result.included.length, 1, 'one included task');
      const task = result.included[0];
      assert.strictEqual(task.id, '501');
      assert.strictEqual(task.type, 'task/single-simple-words');
      assert.strictEqual(task.attributes.order, 1, 'serialNumber remapped to order');
      assert.ok(task.attributes.normalizedAnswerOptions, 'has normalizedAnswerOptions');
      // answerOptions + correctAnswer merged: ['cat', 'dog']
      assert.strictEqual(
        task.attributes.normalizedAnswerOptions.length,
        2,
        'correctAnswer merged into options',
      );

      // Task linked to exercise
      assert.deepEqual(
        result.data.relationships.tasks.data,
        [{ id: '501', type: 'task/single-simple-words' }],
      );
      assert.deepEqual(task.relationships.exercise.data, {
        id: '50',
        type: 'exercise',
      });
    });

    test('normalizes exercise with SIGNALS (signal sideloading)', async function (assert) {
      const mockStore = {
        peekRecord: () => ({ duration: 500, frequency: 1000 }),
      };
      const handler = new BrnApiHandler(mockStore);
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '60' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 60,
            name: 'Signal Exercise',
            exerciseMechanism: 'SIGNALS',
            series: 10,
            parent: 100,
            signals: [
              { id: 1, frequency: 1000, length: 500 },
              { id: 2, frequency: 2000, length: 300 },
            ],
            tasks: [],
          },
        },
      }));

      // Should have 4 included records: 2 signals + 2 task/signal
      assert.strictEqual(result.included.length, 4);

      const signals = result.included.filter((r) => r.type === 'signal');
      assert.strictEqual(signals.length, 2, 'two signal records');
      assert.strictEqual(signals[0].attributes.duration, 500, 'signal length→duration');

      const taskSignals = result.included.filter((r) => r.type === 'task/signal');
      assert.strictEqual(taskSignals.length, 2, 'two task/signal records');
      assert.strictEqual(
        taskSignals[0].id,
        'signal-task-1',
        'signal task id format',
      );
      assert.ok(
        taskSignals[0].relationships.signal,
        'task/signal has signal relationship',
      );
      assert.ok(
        taskSignals[0].relationships.exercise,
        'task/signal has exercise relationship',
      );

      // Exercise's tasks relationship should point to task/signal records
      assert.deepEqual(result.data.relationships.tasks.data, [
        { id: 'signal-task-1', type: 'task/signal' },
        { id: 'signal-task-2', type: 'task/signal' },
      ]);
    });

    test('exercise level is aliased to order', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '70' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 70,
            name: 'Ex',
            level: 5,
            signals: [],
            tasks: [],
          },
        },
      }));
      assert.strictEqual(result.data.attributes.level, 5);
      assert.strictEqual(result.data.attributes.order, 5, 'level aliased to order');
    });
  });

  // ─── Normalization: Contributor ────────────────────────────────────────────

  module('normalization - contributor', function () {
    test('normalizes contributor with locale-specific fields', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'contributor', id: '1' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 1,
            name: 'Иван',
            nameEn: 'Ivan',
            description: 'Разработчик',
            descriptionEn: 'Developer',
            company: 'Компания',
            companyEn: 'Company',
            pictureUrl: 'avatar.png',
            gitHubLogin: 'ivan',
            contribution: 42,
            active: true,
            type: 'DEVELOPER',
            contacts: [{ type: 'email', value: 'ivan@test.com' }],
          },
        },
      }));

      assert.strictEqual(result.data.id, '1');
      assert.strictEqual(result.data.type, 'contributor');
      assert.deepEqual(result.data.attributes.rawName, {
        'ru-ru': 'Иван',
        'en-us': 'Ivan',
      });
      assert.deepEqual(result.data.attributes.rawDescription, {
        'ru-ru': 'Разработчик',
        'en-us': 'Developer',
      });
      assert.deepEqual(result.data.attributes.rawCompany, {
        'ru-ru': 'Компания',
        'en-us': 'Company',
      });
      assert.strictEqual(result.data.attributes.avatar, 'avatar.png');
      assert.strictEqual(result.data.attributes.login, 'ivan');
      assert.strictEqual(result.data.attributes.isActive, true);
      assert.strictEqual(result.data.attributes.kind, 'DEVELOPER');
    });

    test('contributor falls back for missing en fields', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'contributor', id: '2' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 2,
            name: 'Test',
            // nameEn missing
            description: null,
            company: undefined,
          },
        },
      }));
      assert.deepEqual(result.data.attributes.rawName, {
        'ru-ru': 'Test',
        'en-us': 'Test',
      });
      assert.strictEqual(result.data.attributes.rawDescription['ru-ru'], '');
      assert.strictEqual(result.data.attributes.rawCompany['ru-ru'], '');
    });
  });

  // ─── Normalization: Task ───────────────────────────────────────────────────

  module('normalization - task', function () {
    test('normalizes WORDS task with serialNumber→order remap', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 100,
                serialNumber: 3,
                exerciseMechanism: 'WORDS',
                answerOptions: [{ word: 'apple' }],
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      assert.strictEqual(task.type, 'task/single-simple-words');
      assert.strictEqual(task.attributes.order, 3, 'serialNumber→order');
      assert.strictEqual(
        task.attributes.serialNumber,
        undefined,
        'serialNumber not preserved as separate attr',
      );
    });

    test('normalizes MATRIX task and inits wrongAnswers', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 200,
                serialNumber: 1,
                exerciseMechanism: 'MATRIX',
                answerOptions: { group1: ['a', 'b'], group2: ['c'] },
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      assert.strictEqual(task.type, 'task/words-sequences');
      assert.deepEqual(
        task.attributes.wrongAnswers,
        [],
        'wrongAnswers initialized to empty array',
      );
      // Object answer options flattened
      assert.strictEqual(
        task.attributes.normalizedAnswerOptions.length,
        3,
        'flattened answer options: a, b, c',
      );
    });

    test('normalizes task with array answerOptions and correctAnswer', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 300,
                exerciseMechanism: 'WORDS',
                answerOptions: [{ word: 'a' }, { word: 'b' }],
                correctAnswer: { word: 'c' },
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      assert.strictEqual(
        task.attributes.normalizedAnswerOptions.length,
        3,
        'correctAnswer appended to answerOptions',
      );
    });
  });

  // ─── Normalization: Statistics (custom primary key) ────────────────────────

  module('normalization - statistics', function () {
    test('uses date as primary key for weekly statistics', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'query',
          data: {
            type: 'user-weekly-statistics',
            query: {},
            options: {},
          },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: [
            { date: '2024-01-15', exercisingTimeSeconds: 120 },
            { date: '2024-01-16', exercisingTimeSeconds: 180 },
          ],
        },
      }));
      assert.strictEqual(result.data[0].id, '2024-01-15', 'date used as id');
      assert.strictEqual(result.data[0].attributes.date, '2024-01-15', 'date also in attrs');
      assert.strictEqual(result.data[1].id, '2024-01-16');
    });
  });

  // ─── Normalization: Signal ─────────────────────────────────────────────────

  module('normalization - signal', function () {
    test('remaps length to duration', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'signal', id: '1' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: { id: 1, frequency: 1000, length: 500 },
        },
      }));
      assert.strictEqual(result.data.attributes.duration, 500, 'length→duration');
      assert.strictEqual(result.data.attributes.frequency, 1000);
    });
  });

  // ─── Normalization: Subgroup ───────────────────────────────────────────────

  module('normalization - subgroup', function () {
    test('normalizes subgroup with exercises hasMany', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'query',
          data: {
            type: 'subgroup',
            query: { seriesId: '10' },
            options: {},
          },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: [
            {
              id: 100,
              name: 'Level 1',
              exercises: [50, 60],
              seriesId: '10',
            },
          ],
        },
      }));
      assert.strictEqual(result.data[0].id, '100');
      assert.strictEqual(result.data[0].type, 'subgroup');
      assert.deepEqual(result.data[0].relationships.exercises.data, [
        { id: '50', type: 'exercise' },
        { id: '60', type: 'exercise' },
      ]);
    });
  });

  // ─── Normalization: belongsTo null ─────────────────────────────────────────

  module('normalization - belongsTo null', function () {
    test('sets null for null belongsTo values', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '1' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 1,
            series: null,
            parent: null,
            signals: [],
            tasks: [],
          },
        },
      }));
      assert.deepEqual(
        result.data.relationships.series.data,
        null,
        'null belongsTo',
      );
      assert.deepEqual(
        result.data.relationships.parent.data,
        null,
        'null belongsTo',
      );
    });
  });

  // ─── Array response ────────────────────────────────────────────────────────

  module('array responses', function () {
    test('normalizes array response (query)', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'query',
          data: {
            type: 'group',
            query: { locale: 'en-us' },
            options: {},
          },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: [
            { id: 1, name: 'Group A', series: [] },
            { id: 2, name: 'Group B', series: [] },
          ],
        },
      }));
      assert.ok(Array.isArray(result.data), 'data is array');
      assert.strictEqual(result.data.length, 2);
      assert.strictEqual(result.data[0].id, '1');
      assert.strictEqual(result.data[1].id, '2');
    });
  });

  // ─── BELONGS_TO_ID_MAP: "{relName}Id" → belongsTo relationship ───────────

  module('belongsTo ID mapping', function () {
    test('exercise seriesId becomes series belongsTo relationship', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'query',
          data: { type: 'exercise', query: { subGroupId: '5' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: [
            {
              id: 10,
              seriesId: 42,
              signals: [],
              tasks: [],
            },
          ],
        },
      }));
      assert.deepEqual(
        result.data[0].relationships.series.data,
        { id: '42', type: 'series' },
        'seriesId mapped to series belongsTo',
      );
      assert.strictEqual(
        result.data[0].attributes.seriesId,
        undefined,
        'seriesId excluded from attributes',
      );
    });

    test('seriesId does not overwrite explicit series relationship', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '10' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 10,
            series: 99,
            seriesId: 42,
            signals: [],
            tasks: [],
          },
        },
      }));
      // series key is processed as a relationship directly; seriesId should not overwrite
      assert.deepEqual(
        result.data.relationships.series.data,
        { id: '99', type: 'series' },
        'explicit series relationship takes precedence over seriesId',
      );
    });
  });

  // ─── MATRIX answerOptions grouping by wordType ─────────────────────────────

  module('MATRIX answerOptions grouping', function () {
    test('groups flat answerOptions by wordType for words-sequences tasks', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 300,
                exerciseMechanism: 'MATRIX',
                answerOptions: [
                  { id: 1, word: 'мама', wordType: 'OBJECT' },
                  { id: 2, word: 'бабушка', wordType: 'OBJECT' },
                  { id: 3, word: 'танцует', wordType: 'OBJECT_ACTION' },
                  { id: 4, word: 'вяжет', wordType: 'OBJECT_ACTION' },
                ],
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      assert.strictEqual(task.type, 'task/words-sequences');

      const opts = task.attributes.answerOptions;
      assert.ok(!Array.isArray(opts), 'answerOptions is grouped object, not array');
      assert.strictEqual(Object.keys(opts).length, 2, 'two word type groups');
      assert.strictEqual(opts.OBJECT.length, 2, 'OBJECT has 2 entries');
      assert.strictEqual(opts.OBJECT_ACTION.length, 2, 'OBJECT_ACTION has 2 entries');
      assert.strictEqual(opts.OBJECT[0].word, 'мама');
      assert.strictEqual(opts.OBJECT_ACTION[0].word, 'танцует');
    });

    test('keeps already-grouped object answerOptions for MATRIX tasks', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 400,
                exerciseMechanism: 'MATRIX',
                answerOptions: {
                  SUBJECT: ['cat', 'dog'],
                  VERB: ['runs', 'sits'],
                },
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      const opts = task.attributes.answerOptions;
      assert.ok(!Array.isArray(opts), 'remains as object');
      assert.deepEqual(opts.SUBJECT, ['cat', 'dog']);
      assert.deepEqual(opts.VERB, ['runs', 'sits']);
    });

    test('WORDS tasks keep answerOptions as flat array', async function (assert) {
      const handler = new BrnApiHandler({ peekRecord: () => null });
      const ctx = {
        request: {
          op: 'findRecord',
          data: { record: { type: 'exercise', id: '50' }, options: {} },
        },
      };
      const result = await handler.request(ctx, async () => ({
        content: {
          data: {
            id: 50,
            signals: [],
            tasks: [
              {
                id: 500,
                exerciseMechanism: 'WORDS',
                answerOptions: [
                  { id: 1, word: 'мама', wordType: 'OBJECT' },
                  { id: 2, word: 'папа', wordType: 'OBJECT' },
                ],
              },
            ],
          },
        },
      }));
      const task = result.included[0];
      assert.strictEqual(task.type, 'task/single-simple-words');
      assert.ok(Array.isArray(task.attributes.answerOptions), 'WORDS keeps flat array');
      assert.strictEqual(task.attributes.answerOptions.length, 2);
    });
  });
});
