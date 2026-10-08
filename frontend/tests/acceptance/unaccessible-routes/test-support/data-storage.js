export function getTaskScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: '1',
      name: 'default',
      level: 0,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      shouldBeWithPictures: true,
      answerOptions: [],
      correctAnswer: {
        id: '101',
        word: '',
        wordPronounce: '',
        wordType: 'OBJECT',
        audioFileUrl: '',
        pictureFileUrl: '',
        soundsCount: 0,
        description: '',
        columnNumber: -1,
      },
    },
    {
      serialNumber: 2,
      id: '2',
      name: 'default',
      level: 0,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      shouldBeWithPictures: true,
      answerOptions: [],
      correctAnswer: {
        id: '102',
        word: '',
        wordPronounce: '',
        wordType: 'OBJECT',
        audioFileUrl: '',
        pictureFileUrl: '',
        soundsCount: 0,
        description: '',
        columnNumber: -1,
      },
    },
  ];
  const exercises = [
    {
      seriesId: 1,
      id: '1',
      name: 'default',
      level: 1,
      exerciseMechanism: 'WORDS',
      noise: { level: 0, url: null },
      template: '',
      available: true,
      tasks: tasks.filter((t) => ['1', '2'].includes(t.id)),
      signals: [],
      active: true,
      changedBy: 'InitialDataLoader',
      changedWhen: '2021-11-05T18:00:00',
      isAudioFileUrlGenerated: false,
      playWordsCount: 1,
      wordsColumns: 3,
    },
  ];
  const series = [
    { id: '1', name: 'default', group: 1, type: 'SINGLE_SIMPLE_WORDS', level: 1, description: '', active: true, subGroups: [1] },
  ];
  const groups = [
    { order: 1, id: '1', locale: 'ru-ru', name: 'default', description: '123', series: ['1'] },
  ];
  const subgroups = [
    {
      id: '1',
      seriesId: '1',
      level: 1,
      name: 'default',
      pictureUrl: 'pictures/theme/default.svg',
      description: 'default subgroup',
      withPictures: false,
      exercises: ['1'],
    },
  ];

  return { tasks, exercises, series, groups, subgroups };
}

export function getExerciseScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: '1',
      name: 'default',
      level: 0,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      shouldBeWithPictures: true,
      answerOptions: [],
      correctAnswer: {
        id: '201',
        word: '',
        wordPronounce: '',
        wordType: 'OBJECT',
        audioFileUrl: '',
        pictureFileUrl: '',
        soundsCount: 0,
        description: '',
        columnNumber: -1,
      },
    },
    {
      serialNumber: 2,
      id: '2',
      name: 'default',
      level: 0,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      shouldBeWithPictures: true,
      answerOptions: [],
      correctAnswer: {
        id: '202',
        word: '',
        wordPronounce: '',
        wordType: 'OBJECT',
        audioFileUrl: '',
        pictureFileUrl: '',
        soundsCount: 0,
        description: '',
        columnNumber: -1,
      },
    },
    {
      serialNumber: 3,
      id: '3',
      name: 'default',
      level: 0,
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      shouldBeWithPictures: true,
      answerOptions: [],
      correctAnswer: {
        id: '203',
        word: '',
        wordPronounce: '',
        wordType: 'OBJECT',
        audioFileUrl: '',
        pictureFileUrl: '',
        soundsCount: 0,
        description: '',
        columnNumber: -1,
      },
    },
  ];
  const exercises = [
    {
      seriesId: 1,
      id: '1',
      name: 'default',
      level: 1,
      exerciseMechanism: 'WORDS',
      noise: { level: 0, url: null },
      template: '',
      available: true,
      tasks: tasks.filter((t) => ['1', '2'].includes(t.id)),
      signals: [],
      active: true,
      changedBy: 'InitialDataLoader',
      changedWhen: '2021-11-05T18:00:00',
      isAudioFileUrlGenerated: false,
      playWordsCount: 1,
      wordsColumns: 3,
    },
    {
      seriesId: 1,
      id: '2',
      name: 'default',
      level: 2,
      exerciseMechanism: 'WORDS',
      noise: { level: 0, url: null },
      template: '',
      available: true,
      tasks: tasks.filter((t) => ['3'].includes(t.id)),
      signals: [],
      active: true,
      changedBy: 'InitialDataLoader',
      changedWhen: '2021-11-05T18:00:00',
      isAudioFileUrlGenerated: false,
      playWordsCount: 1,
      wordsColumns: 3,
    },
  ];
  const series = [
    { id: '1', name: 'default', group: 1, type: 'SINGLE_SIMPLE_WORDS', level: 1, description: '', active: true, subGroups: [1] },
  ];
  const groups = [
    { order: 1, id: '1', locale: 'ru-ru', name: 'default', description: '123', series: ['1'] },
  ];
  const subgroups = [
    {
      id: '1',
      seriesId: '1',
      level: 1,
      name: 'default',
      pictureUrl: 'pictures/theme/default.svg',
      description: 'default subgroup',
      withPictures: false,
      exercises: ['1', '2'],
    },
  ];

  return { tasks, exercises, series, groups, subgroups };
}

// getSeriesScenarioData was removed: the 'visiting unaccessible series' test
// was deleted because no route-level guard for series accessibility exists in
// the codebase (the exercise availability redirect is explicitly disabled
// during testing via isTesting(), and series routes have no access control).
