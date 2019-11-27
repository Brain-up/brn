export function getTaskScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: 2,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [{ order: 1, id: 1, name: 'default', tasks: [1, 2] }];
  const series = [
    { order: 1, id: 1, name: 'default', exerciseGroupId: 1, exercises: [1] },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  return { tasks, exercises, series, groups };
}

export function getExerciseScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: 2,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 3,
      id: 3,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [
    { order: 1, id: 1, name: 'default', tasks: [1, 2] },
    { order: 2, id: 2, name: 'default', tasks: [3] },
  ];
  const series = [
    { order: 1, id: 1, name: 'default', exerciseGroupId: 1, exercises: [1, 2] },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  return { tasks, exercises, series, groups };
}

export function getSeriesScenarioData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: 2,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 3,
      id: 3,
      name: 'default',
      answerOptions: [],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [
    { order: 1, id: 1, name: 'default', tasks: [1, 2] },
    { order: 2, id: 2, name: 'default', tasks: [3] },
  ];
  const series = [
    { order: 1, id: 1, name: 'default', exerciseGroupId: 1, exercises: [1] },
    { order: 2, id: 2, name: 'default', exerciseGroupId: 1, exercises: [2] },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1, 2] },
  ];

  return { tasks, exercises, series, groups };
}
