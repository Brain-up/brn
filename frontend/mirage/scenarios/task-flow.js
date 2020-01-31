export default function(server) {
  const task1 = server.create('task', {
    serialNumber: 1,
    id: 1,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [
      {
        id: 4,
        audioFileUrl: '',
        word: 'вить',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 6,
        audioFileUrl: '',
        word: 'сад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 2,
        audioFileUrl: '',
        word: 'быль',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 5,
        audioFileUrl: '',
        word: 'бум',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 3,
        audioFileUrl: '',
        word: 'гад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
    ],
    correctAnswer: {
      id: 4,
      audioFileUrl: '',
      word: 'вить',
      pictureFileUrl: '',
      soundsCount: 1,
    },
  });
  const task2 = server.create('task', {
    serialNumber: 2,
    id: 2,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [
      {
        id: 4,
        audioFileUrl: '',
        word: 'вить',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 6,
        audioFileUrl: '',
        word: 'сад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 2,
        audioFileUrl: '',
        word: 'быль',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 5,
        audioFileUrl: '',
        word: 'бум',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 3,
        audioFileUrl: '',
        word: 'гад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
    ],
    correctAnswer: {
      id: 4,
      audioFileUrl: '',
      word: 'вить',
      pictureFileUrl:
        'https://klike.net/uploads/posts/2019-07/1564314090_3.jpg',
      soundsCount: 1,
    },
  });
  const task3 = server.create('task', {
    serialNumber: 3,
    id: 3,
    name: 'default',
    exerciseType: 'SINGLE_WORDS',
    answerOptions: [
      {
        id: 4,
        audioFileUrl: '',
        word: 'вить',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 6,
        audioFileUrl: '',
        word: 'сад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 2,
        audioFileUrl: '',
        word: 'быль',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 5,
        audioFileUrl: '',
        word: 'бум',
        pictureFileUrl: '',
        soundsCount: 1,
      },
      {
        id: 3,
        audioFileUrl: '',
        word: 'гад',
        pictureFileUrl: '',
        soundsCount: 1,
      },
    ],
    correctAnswer: {
      id: 4,
      audioFileUrl: '',
      word: 'вить',
      pictureFileUrl:
        'https://klike.net/uploads/posts/2019-07/1564314090_3.jpg',
      soundsCount: 1,
    },
  });

  const exercise1 = server.create('exercise', {
    order: 1,
    id: 1,
    exerciseType: 'SINGLE_WORDS',
    name: 'default',
    tasks: [task1, task2],
  });
  const exercise2 = server.create('exercise', {
    order: 2,
    id: 2,
    exerciseType: 'SINGLE_WORDS',
    name: 'default',
    tasks: [task3],
  });

  const series = server.create('series', {
    order: 1,
    id: 1,
    name: 'default',
    exerciseGroupId: 1,
    exercises: [exercise1, exercise2],
  });

  server.create('group', {
    order: 1,
    id: 1,
    name: 'default',
    description: '123',
    series: [series],
  });
}
