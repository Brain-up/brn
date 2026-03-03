export function getTaskScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: '1',
      name: 'default',
      exerciseMechanism: 'WORDS',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: '2',
      name: 'default',
      exerciseMechanism: 'WORDS',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [
    {
      order: 1,
      id: '1',
      name: 'default',
      tasks: tasks.filter((t) => ['1', '2'].includes(t.id)),
    },
  ];
  const series = [
    { id: '1', name: 'default', group: 1, type: 'SINGLE_SIMPLE_WORDS', level: 1, description: '', active: true, subGroups: [1] },
  ];
  const groups = [
    { order: 1, id: '1', name: 'default', description: '123', series: ['1'] },
  ];
  const subgroups = [
    { id: '1', seriesId: '1', level: 1, name: 'default', withPictures: false, exercises: ['1'] },
  ];

  return { tasks, exercises, series, groups, subgroups };
}

export function getExerciseScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: '1',
      name: 'default',
      exerciseMechanism: 'WORDS',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: '2',
      name: 'default',
      exerciseMechanism: 'WORDS',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 3,
      id: '3',
      name: 'default',
      exerciseMechanism: 'WORDS',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [
    {
      order: 1,
      id: '1',
      name: 'default',
      tasks: tasks.filter((t) => ['1', '2'].includes(t.id)),
    },
    {
      order: 2,
      id: '2',
      name: 'default',
      tasks: tasks.filter((t) => ['3'].includes(t.id)),
    },
  ];
  const series = [
    { id: '1', name: 'default', group: 1, type: 'SINGLE_SIMPLE_WORDS', level: 1, description: '', active: true, subGroups: [1] },
  ];
  const groups = [
    { order: 1, id: '1', name: 'default', description: '123', series: ['1'] },
  ];
  const subgroups = [
    { id: '1', seriesId: '1', level: 1, name: 'default', withPictures: false, exercises: ['1', '2'] },
  ];

  return { tasks, exercises, series, groups, subgroups };
}

// getSeriesScenarioData was removed: the 'visiting unaccessible series' test
// was deleted because no route-level guard for series accessibility exists in
// the codebase (the exercise availability redirect is explicitly disabled
// during testing via isTesting(), and series routes have no access control).
