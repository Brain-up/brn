export function getTestData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      exerciseMechanism: 'WORDS',
      type: 'WORDS',
      answerOptions: [{ word: 'test option', audioFileUrl: '' }],
      correctAnswer: { word: 'test option', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: 2,
      name: 'default',
      type: 'WORDS',

      exerciseMechanism: 'WORDS',
      answerOptions: [{ word: '', audioFileUrl: '' }],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 3,
      id: 3,
      name: 'default',
      type: 'WORDS',

      exerciseMechanism: 'WORDS',
      answerOptions: [{ word: '', audioFileUrl: '' }],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 4,
      id: 4,
      name: 'default',
      type: 'WORDS',

      exerciseMechanism: 'WORDS',
      answerOptions: [{ word: '', audioFileUrl: '' }],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
  ];
  const exercises = [
    {
      order: 1,
      id: 1,
      name: 'exercise 1',
      level: 1,
      tasks: tasks.filter((t) => [1].includes(t.id)),
    },
    {
      order: 2,
      id: 2,
      name: 'exercise 1',
      level: 2,
      tasks: tasks.filter((t) => [2].includes(t.id)),
    },
    {
      order: 3,
      id: 3,
      name: 'exercise 2',
      level: 1,
      tasks: tasks.filter((t) => [3].includes(t.id)),
    },
    {
      order: 4,
      id: 4,
      name: 'exercise 2',
      level: 2,
      tasks: tasks.filter((t) => [4].includes(t.id)),
    },
  ];
  const series = [
    {
      order: 1,
      id: 1,
      name: 'default',
      exerciseGroupId: 1,
      subgroups: [1, 2],
    },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  const subgroups = [
    {
      seriesId: 1,
      id: 1,
      level: 1,
      name: 'Семья',
      pictureUrl: 'pictures/theme/family.svg',
      description: 'Слова про семью',
      exercises: [1, 2, 3, 4],
    },
    {
      seriesId: 1,
      id: 2,
      level: 2,
      name: 'Любимый дом',
      pictureUrl: 'pictures/theme/home.svg',
      description: 'Слова про дом',
      exercises: [],
    },
  ];

  return {
    tasks,
    subgroups,
    exercises,
    series,
    groups,
    availableExercises: ['1', '3'],
  };
}
