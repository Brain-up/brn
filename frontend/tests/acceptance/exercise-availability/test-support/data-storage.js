export function getTestData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      exerciseType: 'SINGLE_WORDS',
      answerOptions: [{ word: 'test option', audioFileUrl: '' }],
      correctAnswer: { word: 'test option', audioFileUrl: '' },
    },
    {
      serialNumber: 2,
      id: 2,
      name: 'default',
      exerciseType: 'SINGLE_WORDS',
      answerOptions: [{ word: '', audioFileUrl: '' }],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 3,
      id: 3,
      name: 'default',
      exerciseType: 'SINGLE_WORDS',
      answerOptions: [{ word: '', audioFileUrl: '' }],
      correctAnswer: { word: '', audioFileUrl: '' },
    },
    {
      serialNumber: 4,
      id: 4,
      name: 'default',
      exerciseType: 'SINGLE_WORDS',
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
      tasks: [{ id: 1, type: 'task/SINGLE_WORDS' }],
    },
    {
      order: 2,
      id: 2,
      name: 'exercise 1',
      level: 2,
      tasks: [{ id: 2, type: 'task/SINGLE_WORDS' }],
    },
    {
      order: 3,
      id: 3,
      name: 'exercise 2',
      level: 1,
      tasks: [{ id: 3, type: 'task/SINGLE_WORDS' }],
    },
    {
      order: 4,
      id: 4,
      name: 'exercise 2',
      level: 2,
      tasks: [{ id: 4, type: 'task/SINGLE_WORDS' }],
    },
  ];
  const series = [
    {
      order: 1,
      id: 1,
      name: 'default',
      exerciseGroupId: 1,
      exercises: [1, 2, 3, 4],
    },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  return { tasks, exercises, series, groups };
}
