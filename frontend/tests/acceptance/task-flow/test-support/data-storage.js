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
        { id: 1 },
        { id: 2 },
      ],
    },
    {
      order: 2,
      id: 2,
      exerciseType: 'SINGLE_WORDS',
      name: 'default',
      tasks: [{ id: 3 }],
    },
  ];
  const series = [
    { order: 1, id: 1, name: 'default', exerciseGroupId: 1,  type: 'SINGLE_WORDS' },
  ];
  const groups = [
    { order: 1, id: 1, name: 'default', description: '123', series: [1] },
  ];

  const subgroups = [
    {
      seriesId:1,
      id:1,
      level:1,
      name:"Семья",
      pictureUrl:"pictures/theme/family.svg",
      description:"Слова про семью",
      exercises: [1, 2]
    },
    {
      seriesId:1,
      id:2,
      level:2,
      name:"Любимый дом",
      pictureUrl:"pictures/theme/home.svg",
      description:"Слова про дом",
      exercises:[]
    }
  ];
  return { tasks, subgroups, exercises, series, groups };
}
