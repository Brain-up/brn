export default function(server) {
  const task1 = server.create('task', {
    serialNumber: 1,
    id: 1,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [{ word: 'test option', audioFileUrl: '' }],
    correctAnswer: { word: 'test option', audioFileUrl: '' },
  });
  const task2 = server.create('task', {
    serialNumber: 2,
    id: 2,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [{ word: '', audioFileUrl: '' }],
    correctAnswer: { word: '', audioFileUrl: '' },
  });
  const task3 = server.create('task', {
    serialNumber: 3,
    id: 3,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [{ word: '', audioFileUrl: '' }],
    correctAnswer: { word: '', audioFileUrl: '' },
  });
  const task4 = server.create('task', {
    serialNumber: 4,
    id: 4,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [{ word: '', audioFileUrl: '' }],
    correctAnswer: { word: '', audioFileUrl: '' },
  });

  const exercise1 = server.create('exercise', {
    order: 1,
    id: 1,
    name: 'exercise 1',
    level: 1,
    tasks: [task1],
  });
  const exercise2 = server.create('exercise', {
    order: 2,
    id: 2,
    name: 'exercise 1',
    level: 2,
    tasks: [task2],
  });
  const exercise3 = server.create('exercise', {
    order: 3,
    id: 3,
    name: 'exercise 2',
    level: 1,
    tasks: [task3],
  });
  const exercise4 = server.create('exercise', {
    order: 4,
    id: 4,
    name: 'exercise 2',
    level: 2,
    tasks: [task4],
  });

  const series = server.create('series', {
    order: 1,
    id: 1,
    name: 'default',
    exerciseGroupId: 1,
    exercises: [exercise1, exercise2, exercise3, exercise4],
  });

  server.create('group', {
    order: 1,
    id: 1,
    name: 'default',
    description: '123',
    series: [series],
  });
}
