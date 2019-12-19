export function getData() {
  const tasks = [
    {
      serialNumber: 1,
      id: 1,
      name: 'default',
      answerOptions: [
        {
          id: 4,
          audioFileUrl: 'no_noise/вить.mp3',
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
          audioFileUrl: 'no_noise/быль.mp3',
          word: 'быль',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 5,
          audioFileUrl: 'no_noise/бум.mp3',
          word: 'бум',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 3,
          audioFileUrl: 'no_noise/гад.mp3',
          word: 'гад',
          pictureFileUrl: '',
          soundsCount: 1,
        },
      ],
      correctAnswer: {
        id: 4,
        audioFileUrl: 'no_noise/вить.mp3',
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
      answerOptions: [
        {
          id: 4,
          audioFileUrl: 'no_noise/вить.mp3',
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
          audioFileUrl: 'no_noise/быль.mp3',
          word: 'быль',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 5,
          audioFileUrl: 'no_noise/бум.mp3',
          word: 'бум',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 3,
          audioFileUrl: 'no_noise/гад.mp3',
          word: 'гад',
          pictureFileUrl: '',
          soundsCount: 1,
        },
      ],
      correctAnswer: {
        id: 4,
        audioFileUrl: 'no_noise/вить.mp3',
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
      answerOptions: [
        {
          id: 4,
          audioFileUrl: 'no_noise/вить.mp3',
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
          audioFileUrl: 'no_noise/быль.mp3',
          word: 'быль',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 5,
          audioFileUrl: 'no_noise/бум.mp3',
          word: 'бум',
          pictureFileUrl: '',
          soundsCount: 1,
        },
        {
          id: 3,
          audioFileUrl: 'no_noise/гад.mp3',
          word: 'гад',
          pictureFileUrl: '',
          soundsCount: 1,
        },
      ],
      correctAnswer: {
        id: 4,
        audioFileUrl: 'no_noise/вить.mp3',
        word: 'вить',
        pictureFileUrl:
          'https://klike.net/uploads/posts/2019-07/1564314090_3.jpg',
        soundsCount: 1,
      },
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
