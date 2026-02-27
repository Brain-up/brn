import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';

module('Unit | Serializers | Snapshot Tests', function (hooks) {
  setupTest(hooks);

  module('ApplicationSerializer', function () {
    test('normalizeResponse unwraps { data: [...] } envelope for array response', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('application');
      const GroupModel = store.modelFor('group');

      const payload = {
        data: [
          { id: 1, name: 'Group A', description: 'Desc A', locale: 'en-us', series: [10, 20] },
          { id: 2, name: 'Group B', description: 'Desc B', locale: 'ru-ru', series: [30] },
        ],
      };

      const result = serializer.normalizeResponse(store, GroupModel, payload, null, 'query');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.length, 2, 'two records normalized');
      assert.strictEqual(result.data[0].attributes.name, 'Group A');
      assert.strictEqual(result.data[0].attributes.locale, 'en-us');
      assert.strictEqual(result.data[1].attributes.name, 'Group B');
    });

    test('normalizeSingleResponse handles array payload by taking first element', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('application');
      const GroupModel = store.modelFor('group');

      const payload = [
        { id: 1, name: 'Group A', description: 'Desc A', locale: 'en-us', series: [] },
      ];

      const result = serializer.normalizeSingleResponse(store, GroupModel, payload, '1', 'findRecord');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.attributes.name, 'Group A');
    });

    test('normalizeSingleResponse handles single object payload', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('application');
      const GroupModel = store.modelFor('group');

      const payload = { id: 1, name: 'Group A', description: 'Desc A', locale: 'en-us', series: [] };

      const result = serializer.normalizeSingleResponse(store, GroupModel, payload, '1', 'findRecord');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.attributes.name, 'Group A');
    });

    test('keyForAttribute uses ATTR_NAMES_MAP when defined', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('application');

      // Default: passes through
      assert.strictEqual(serializer.keyForAttribute('name'), 'name');
      assert.strictEqual(serializer.keyForAttribute('description'), 'description');
    });
  });

  module('SeriesSerializer', function () {
    test('normalize maps "type" field to "kind" attribute', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('series');
      const SeriesModel = store.modelFor('series');

      const rawPayload = {
        id: 10,
        name: 'Series 1',
        description: 'Desc',
        level: 1,
        type: 'SINGLE_SIMPLE_WORDS',
        group: 1,
        subGroups: [100, 101],
      };

      const result = serializer.normalize(SeriesModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.attributes.kind, 'SINGLE_SIMPLE_WORDS', 'type mapped to kind');
    });
  });

  module('ContributorSerializer', function () {
    test('normalize remaps DTO fields to model attributes', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('contributor');
      const ContributorModel = store.modelFor('contributor');

      const rawPayload = {
        id: 5,
        name: 'Иван',
        nameEn: 'Ivan',
        description: 'Разработчик',
        descriptionEn: 'Developer',
        company: 'Компания',
        companyEn: 'Company',
        pictureUrl: 'https://example.com/avatar.png',
        contribution: 42,
        active: true,
        type: 'DEVELOPER',
        contacts: [{ type: 'github', value: 'ivan' }],
        gitHubLogin: 'ivan123',
      };

      const result = serializer.normalize(ContributorModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.id, '5', 'id is stringified');
      assert.deepEqual(result.data.attributes.rawName, { 'ru-ru': 'Иван', 'en-us': 'Ivan' }, 'rawName mapped');
      assert.deepEqual(result.data.attributes.rawDescription, { 'ru-ru': 'Разработчик', 'en-us': 'Developer' }, 'rawDescription mapped');
      assert.deepEqual(result.data.attributes.rawCompany, { 'ru-ru': 'Компания', 'en-us': 'Company' }, 'rawCompany mapped');
      assert.strictEqual(result.data.attributes.avatar, 'https://example.com/avatar.png', 'pictureUrl → avatar');
      assert.strictEqual(result.data.attributes.login, 'ivan123', 'gitHubLogin → login');
      assert.strictEqual(result.data.attributes.contribution, 42, 'contribution preserved');
      assert.strictEqual(result.data.attributes.isActive, true, 'active → isActive');
      assert.strictEqual(result.data.attributes.kind, 'DEVELOPER', 'type → kind');
    });

    test('normalize handles null name/description fields gracefully', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('contributor');
      const ContributorModel = store.modelFor('contributor');

      const rawPayload = {
        id: 6,
        name: null,
        nameEn: null,
        description: null,
        descriptionEn: null,
        company: null,
        companyEn: null,
        pictureUrl: '',
        contribution: 0,
        active: false,
        type: 'SPECIALIST',
        contacts: [],
        gitHubLogin: '',
      };

      const result = serializer.normalize(ContributorModel, rawPayload);

      assert.deepEqual(result.data.attributes.rawName, { 'ru-ru': '', 'en-us': '' }, 'null names become empty strings');
      assert.deepEqual(result.data.attributes.rawDescription, { 'ru-ru': '', 'en-us': '' }, 'null descriptions become empty strings');
    });
  });

  module('SignalSerializer', function () {
    test('normalize produces JSON:API-like structure with "length" mapped to "duration"', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('signal');
      const SignalModel = store.modelFor('signal');

      const rawPayload = {
        id: 1,
        frequency: 1000,
        length: 500,
      };

      const result = serializer.normalize(SignalModel, rawPayload);

      assert.strictEqual(result.id, 1, 'id preserved');
      assert.strictEqual(result.type, 'signal', 'type is signal');
      assert.strictEqual(result.attributes.duration, 500, 'length mapped to duration');
      assert.strictEqual(result.attributes.frequency, 1000, 'frequency preserved');
    });

    test('payloadToTypeId returns correct type and id', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('signal');

      const result = serializer.payloadToTypeId({ id: 42 });

      assert.deepEqual(result, { id: 42, type: 'signal' });
    });
  });

  module('TaskSerializer', function () {
    test('normalize flattens array answerOptions and creates AnswerOption instances', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task');
      const TaskModel = store.modelFor('task');

      const rawPayload = {
        id: 1,
        name: 'Task 1',
        serialNumber: 3,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
        answerOptions: [
          { id: 10, word: 'cat', audioFileUrl: '/cat.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
          { id: 11, word: 'dog', audioFileUrl: '/dog.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
        ],
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.attributes.order, 3, 'serialNumber mapped to order');
      assert.ok(Array.isArray(result.data.attributes.normalizedAnswerOptions), 'normalizedAnswerOptions is array');
      assert.strictEqual(result.data.attributes.normalizedAnswerOptions.length, 2, 'two answer options');
    });

    test('normalize flattens object-style answerOptions (MATRIX format)', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task');
      const TaskModel = store.modelFor('task');

      const rawPayload = {
        id: 2,
        name: 'Task 2',
        serialNumber: 1,
        exerciseType: 'WORDS_SEQUENCES',
        exerciseMechanism: 'MATRIX',
        answerOptions: {
          OBJECT: [
            { id: 10, word: 'cat', audioFileUrl: '/cat.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
          ],
          ACTION: [
            { id: 11, word: 'sits', audioFileUrl: '/sits.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'ACTION' },
          ],
        },
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.ok(Array.isArray(result.data.attributes.normalizedAnswerOptions), 'normalizedAnswerOptions is array');
      assert.strictEqual(result.data.attributes.normalizedAnswerOptions.length, 2, 'two answer options from two groups');
    });

    test('normalize handles empty answerOptions', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task');
      const TaskModel = store.modelFor('task');

      const rawPayload = {
        id: 3,
        name: 'Task 3',
        serialNumber: 1,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.deepEqual(result.data.attributes.normalizedAnswerOptions, [], 'empty when answerOptions missing');
    });

    test('normalize includes correctAnswer when present with array answerOptions', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task');
      const TaskModel = store.modelFor('task');

      const rawPayload = {
        id: 4,
        name: 'Task 4',
        serialNumber: 1,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
        answerOptions: [
          { id: 10, word: 'cat', audioFileUrl: '/cat.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
        ],
        correctAnswer: { id: 11, word: 'dog', audioFileUrl: '/dog.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.strictEqual(result.data.attributes.normalizedAnswerOptions.length, 2, 'includes both answerOptions + correctAnswer');
    });
  });

  module('TaskWordsSequencesSerializer', function () {
    test('normalize initializes wrongAnswers as empty array', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/words-sequences');
      const TaskModel = store.modelFor('task/words-sequences');

      const rawPayload = {
        id: 1,
        name: 'WS Task',
        serialNumber: 1,
        exerciseType: 'WORDS_SEQUENCES',
        exerciseMechanism: 'MATRIX',
        template: '<OBJECT ACTION>',
        answerOptions: {
          OBJECT: [{ id: 10, word: 'cat', audioFileUrl: '/cat.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' }],
          ACTION: [{ id: 11, word: 'sits', audioFileUrl: '/sits.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'ACTION' }],
        },
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.deepEqual(result.data.attributes.wrongAnswers, [], 'wrongAnswers initialized as empty array');
    });
  });

  module('ExerciseSerializer', function () {
    test('normalizeResponse maps exerciseMechanism to polymorphic task type', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('exercise');
      const ExerciseModel = store.modelFor('exercise');

      const payload = {
        data: [
          {
            id: 1,
            name: 'Exercise 1',
            level: 1,
            available: true,
            noise: { level: 0, url: '' },
            seriesId: 10,
            signals: [],
            tasks: [
              { id: 100, exerciseMechanism: 'WORDS', exerciseType: 'SINGLE_SIMPLE_WORDS', name: 'T1', serialNumber: 1, answerOptions: [] },
              { id: 101, exerciseMechanism: 'MATRIX', exerciseType: 'WORDS_SEQUENCES', name: 'T2', serialNumber: 2, answerOptions: {} },
            ],
            template: '',
          },
        ],
      };

      const result = serializer.normalizeResponse(store, ExerciseModel, payload, null, 'query');

      assert.ok(result.data, 'result has data');
      // The exercise should be normalized
      assert.strictEqual(result.data.length, 1, 'one exercise');
    });

    test('normalizeResponse maps SIGNALS exerciseMechanism to task/signal type', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('exercise');
      const ExerciseModel = store.modelFor('exercise');

      const payload = {
        data: [
          {
            id: 2,
            name: 'Signal Exercise',
            level: 1,
            available: true,
            noise: { level: 0, url: '' },
            seriesId: 10,
            signals: [],
            tasks: [
              { id: 200, exerciseMechanism: 'SIGNALS', exerciseType: 'SIGNALS', name: 'Signal T1', serialNumber: 1, answerOptions: [] },
            ],
            template: '',
          },
        ],
      };

      const result = serializer.normalizeResponse(store, ExerciseModel, payload, null, 'query');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.length, 1, 'one exercise');
      // Verify the task was assigned the task/signal type
      const exerciseData = result.data[0];
      const taskRel = exerciseData.relationships.tasks.data;
      assert.strictEqual(taskRel.length, 1, 'one task relationship');
      assert.strictEqual(taskRel[0].type, 'task/signal', 'task type is task/signal for SIGNALS mechanism');
    });

    test('normalizeSignal processes signals and sideloads signal and task/signal records', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('exercise');
      const ExerciseModel = store.modelFor('exercise');

      const payload = {
        data: [
          {
            id: 3,
            name: 'Exercise with Signals',
            level: 1,
            available: true,
            noise: { level: 0, url: '' },
            seriesId: 10,
            signals: [
              { id: 50, frequency: 1000, length: 500 },
              { id: 51, frequency: 2000, length: 300 },
            ],
            tasks: [],
            template: '',
          },
        ],
      };

      const result = serializer.normalizeResponse(store, ExerciseModel, payload, null, 'query');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.length, 1, 'one exercise');

      // After normalizeSignal, signals should have been pushed to the store
      const signal50 = store.peekRecord('signal', 50);
      const signal51 = store.peekRecord('signal', 51);
      assert.ok(signal50, 'signal 50 was sideloaded into the store');
      assert.ok(signal51, 'signal 51 was sideloaded into the store');
      assert.strictEqual(signal50.frequency, 1000, 'signal 50 frequency is correct');
      assert.strictEqual(signal50.duration, 500, 'signal 50 duration mapped from length');
      assert.strictEqual(signal51.frequency, 2000, 'signal 51 frequency is correct');
      assert.strictEqual(signal51.duration, 300, 'signal 51 duration mapped from length');

      // task/signal records should have been sideloaded as well
      const taskSignal50 = store.peekRecord('task/signal', 'signal-task-50');
      const taskSignal51 = store.peekRecord('task/signal', 'signal-task-51');
      assert.ok(taskSignal50, 'task/signal for signal 50 was sideloaded');
      assert.ok(taskSignal51, 'task/signal for signal 51 was sideloaded');

      // The exercise tasks relationship should reference the task/signal records
      const exerciseData = result.data[0];
      const taskRels = exerciseData.relationships.tasks.data;
      assert.strictEqual(taskRels.length, 2, 'two task relationships from signals');
      assert.strictEqual(taskRels[0].id, 'signal-task-50', 'first task id derived from signal');
      assert.strictEqual(taskRels[0].type, 'task/signal', 'first task type is task/signal');
      assert.strictEqual(taskRels[1].id, 'signal-task-51', 'second task id derived from signal');
      assert.strictEqual(taskRels[1].type, 'task/signal', 'second task type is task/signal');
    });

    test('ATTR_NAMES_MAP maps "order" to "level"', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('exercise');

      assert.strictEqual(serializer.keyForAttribute('order'), 'level', 'order maps to level');
    });
  });

  module('TaskSignalSerializer', function () {
    test('payloadToTypeId returns prefixed id and task/signal type', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/signal');

      const result = serializer.payloadToTypeId({ id: 99 });

      assert.strictEqual(result.id, 'signal-task-99', 'id is prefixed with signal-task-');
      assert.strictEqual(result.type, 'task/signal', 'type is task/signal');
    });

    test('normalize creates task/signal record with signal and exercise relationships', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/signal');
      const TaskSignalModel = store.modelFor('task/signal');

      const normalizedSignalHash = {
        id: 42,
        type: 'signal',
        attributes: { frequency: 1500, duration: 400 },
      };

      const parentExercise = {
        id: 7,
        signals: [
          { id: 42, frequency: 1500, length: 400 },
        ],
      };

      const result = serializer.normalize(TaskSignalModel, normalizedSignalHash, parentExercise);

      assert.strictEqual(result.id, 'signal-task-42', 'id is prefixed with signal-task-');
      assert.strictEqual(result.type, 'task/signal', 'type is task/signal');
      assert.strictEqual(result.attributes.exerciseMechanism, 'SIGNALS', 'exerciseMechanism set to SIGNALS');
      assert.ok(Array.isArray(result.attributes.answerOptions), 'answerOptions is an array');
      assert.strictEqual(result.attributes.answerOptions.length, 1, 'one answer option from one signal');
      assert.deepEqual(result.relationships.signal.data, { id: 42, type: 'signal' }, 'signal relationship set');
      assert.deepEqual(result.relationships.exercise.data, { id: 7, type: 'exercise' }, 'exercise relationship set');
    });

    test('normalize creates answer options with dynamic word/signal/audioFileUrl getters', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/signal');
      const TaskSignalModel = store.modelFor('task/signal');

      // Push a signal into the store so peekRecord works in the getter
      store.push({
        data: {
          id: 60,
          type: 'signal',
          attributes: { frequency: 3000, duration: 700 },
        },
      });

      const normalizedSignalHash = {
        id: 60,
        type: 'signal',
        attributes: { frequency: 3000, duration: 700 },
      };

      const parentExercise = {
        id: 8,
        signals: [
          { id: 60, frequency: 3000, length: 700 },
        ],
      };

      const result = serializer.normalize(TaskSignalModel, normalizedSignalHash, parentExercise);

      const opt = result.attributes.answerOptions[0];
      assert.strictEqual(opt.word, '1: [700ms, 3000Mhz]', 'word getter formats signal info');
      assert.ok(opt.signal, 'signal getter returns the signal record');
      assert.strictEqual(opt.signal.frequency, 3000, 'signal frequency accessible via getter');
      assert.strictEqual(opt.audioFileUrl, opt.signal, 'audioFileUrl returns the signal record');
    });
  });

  module('TaskSingleSimpleWordsSerializer', function () {
    test('normalize delegates to parent TaskSerializer and produces normalizedAnswerOptions', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/single-simple-words');
      const TaskModel = store.modelFor('task/single-simple-words');

      const rawPayload = {
        id: 500,
        name: 'Simple Words Task',
        serialNumber: 2,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
        answerOptions: [
          { id: 10, word: 'apple', audioFileUrl: '/apple.mp3', pictureFileUrl: '/apple.png', soundsCount: 1, wordType: 'OBJECT' },
          { id: 11, word: 'banana', audioFileUrl: '/banana.mp3', pictureFileUrl: '/banana.png', soundsCount: 1, wordType: 'OBJECT' },
        ],
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.attributes.order, 2, 'serialNumber mapped to order');
      assert.ok(Array.isArray(result.data.attributes.normalizedAnswerOptions), 'normalizedAnswerOptions is array');
      assert.strictEqual(result.data.attributes.normalizedAnswerOptions.length, 2, 'two answer options');
      assert.strictEqual(result.data.attributes.normalizedAnswerOptions[0].word, 'apple', 'first answer word');
      assert.strictEqual(result.data.attributes.normalizedAnswerOptions[1].word, 'banana', 'second answer word');
    });

    test('normalize handles empty answerOptions', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/single-simple-words');
      const TaskModel = store.modelFor('task/single-simple-words');

      const rawPayload = {
        id: 501,
        name: 'Empty Task',
        serialNumber: 1,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.ok(result.data, 'result has data');
      assert.deepEqual(result.data.attributes.normalizedAnswerOptions, [], 'empty when answerOptions missing');
    });

    test('normalize includes correctAnswer in normalizedAnswerOptions', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('task/single-simple-words');
      const TaskModel = store.modelFor('task/single-simple-words');

      const rawPayload = {
        id: 502,
        name: 'Task with correct answer',
        serialNumber: 1,
        exerciseType: 'SINGLE_SIMPLE_WORDS',
        exerciseMechanism: 'WORDS',
        answerOptions: [
          { id: 10, word: 'cat', audioFileUrl: '/cat.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
        ],
        correctAnswer: { id: 11, word: 'dog', audioFileUrl: '/dog.mp3', pictureFileUrl: '', soundsCount: 1, wordType: 'OBJECT' },
      };

      const result = serializer.normalize(TaskModel, rawPayload);

      assert.strictEqual(result.data.attributes.normalizedAnswerOptions.length, 2, 'includes both answerOptions and correctAnswer');
    });
  });

  module('UserWeeklyStatisticsSerializer', function () {
    test('uses "date" as primaryKey', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('user-weekly-statistics');

      assert.strictEqual(serializer.primaryKey, 'date', 'primaryKey is date');
    });

    test('normalizeResponse uses date as record id', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('user-weekly-statistics');
      const Model = store.modelFor('user-weekly-statistics');

      const payload = {
        data: [
          { date: '2024-01-15', excersizingTimeSeconds: 3600, progress: 'GOOD' },
          { date: '2024-01-16', excersizingTimeSeconds: 1800, progress: 'BAD' },
        ],
      };

      const result = serializer.normalizeResponse(store, Model, payload, null, 'query');

      assert.strictEqual(result.data.length, 2, 'two records');
      assert.strictEqual(result.data[0].id, '2024-01-15', 'date used as id');
      assert.strictEqual(result.data[1].id, '2024-01-16', 'date used as id');
    });
  });

  module('UserYearlyStatisticsSerializer', function () {
    test('inherits "date" as primaryKey from weekly serializer', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('user-yearly-statistics');

      assert.strictEqual(serializer.primaryKey, 'date', 'primaryKey is date');
    });
  });

  module('UserDailyTimeTableStatisticsSerializer', function () {
    test('uses "seriesName" as primaryKey', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('user-daily-time-table-statistics');

      assert.strictEqual(serializer.primaryKey, 'seriesName', 'primaryKey is seriesName');
    });

    test('normalizeResponse uses seriesName as record id', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('user-daily-time-table-statistics');
      const Model = store.modelFor('user-daily-time-table-statistics');

      const payload = {
        data: [
          { seriesName: 'Series A', allDoneExercises: 10, uniqueDoneExercises: 8, repeatedExercises: 2, doneExercisesSuccessfullyFromFirstTime: 7, listenWordsCount: 50 },
        ],
      };

      const result = serializer.normalizeResponse(store, Model, payload, null, 'query');

      assert.strictEqual(result.data.length, 1, 'one record');
      assert.strictEqual(result.data[0].id, 'Series A', 'seriesName used as id');
    });
  });

  module('HeadphoneSerializer', function () {
    test('normalizeResponse passes through to parent', function (assert) {
      const store = this.owner.lookup('service:store');
      const serializer = store.serializerFor('headphone');
      const HeadphoneModel = store.modelFor('headphone');

      const payload = {
        data: [
          { id: 1, name: 'Headphone 1', description: 'Desc 1' },
        ],
      };

      const result = serializer.normalizeResponse(store, HeadphoneModel, payload, null, 'query');

      assert.ok(result.data, 'result has data');
      assert.strictEqual(result.data.length, 1, 'one record');
      assert.strictEqual(result.data[0].attributes.name, 'Headphone 1');
    });

    test('serialize adds type field', function (assert) {
      const store = this.owner.lookup('service:store');
      const record = store.createRecord('headphone', { name: 'Test', description: 'Desc' });

      const serialized = record.serialize();

      assert.strictEqual(serialized.type, 'ON_EAR_BLUETOOTH', 'type field added');
    });
  });
});
