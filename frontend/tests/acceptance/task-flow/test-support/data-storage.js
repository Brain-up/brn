export function getData() {
  const tasks = [
    {
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
        pictureFileUrl:
          'https://klike.net/uploads/posts/2019-07/1564314090_3.jpg',
        soundsCount: 1,
      },
    },
    {
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
    },
    {
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
    },
  ];
  const exercises = [
    {
      order: 1,
      id: 1,
      exerciseType: 'SINGLE_WORDS',
      name: 'default',
      tasks: [
        { id: 1, type: 'task/single-words' },
        { id: 2, type: 'task/single-words' },
      ],
    },
    {
      order: 2,
      id: 2,
      exerciseType: 'SINGLE_WORDS',
      name: 'default',
      tasks: [{ id: 3, type: 'task/single-words' }],
    },
  ];
  const series = [
    { order: 1, id: 1, name: 'default', exerciseGroupId: 1, exercises: [1, 2] },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  return { tasks, exercises, series, groups };
}
