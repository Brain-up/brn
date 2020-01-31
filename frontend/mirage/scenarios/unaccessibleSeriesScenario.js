export default function (server) {
  const task1 = server.create('task', {
    serialNumber: 1,
    id: 1,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [],
    correctAnswer: { word: '', audioFileUrl: '' },
  });
  const task2 = server.create('task', {
    serialNumber: 2,
    id: 2,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [],
    correctAnswer: { word: '', audioFileUrl: '' },
  });
  const task3 = server.create('task', {
    serialNumber: 3,
    id: 3,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [],
    correctAnswer: { word: '', audioFileUrl: '' },
  });
  const exercise1 = server.create('exercise', {
    order: 1,
    id: 1,
    name: 'default',
    tasks: [
      task1,
      task2
    ],
  });
  const exercise2 = server.create('exercise', {
    order: 2,
    id: 2,
    name: 'default',
    tasks: [task3],
  });
  const series1 = server.create('series', {
    order: 1,
    id: 1,
    name: 'default',
    exerciseGroupId: 1,
    exercises: [exercise1]
  });

  const series2 = server.create('series', {
    order: 2,
    id: 2,
    name: 'default',
    exerciseGroupId: 1,
    exercises: [exercise2]
  });

  server.create('group', {
    order: 1,
    id: 1,
    name: 'default',
    description: '123',
    series: [series1, series2]
  });
}
