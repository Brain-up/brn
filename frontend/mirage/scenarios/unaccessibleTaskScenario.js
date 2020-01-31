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
  const exercise1 = server.create('exercise', {
    order: 1,
    id: 1,
    name: 'default',
    tasks: [
      task1,
      task2
    ],
  });
  const series1 = server.create('series', {
    order: 1,
    id: 1,
    name: 'default',
    exerciseGroupId: 1,
    exercises: [exercise1]
  });

  server.create('group', {
    order: 1,
    id: 1,
    name: 'default',
    description: '123',
    series: [series1]
  });
}
