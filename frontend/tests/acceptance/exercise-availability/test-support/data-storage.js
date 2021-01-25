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
      tasks: [{ id: 1 }],
    },
    {
      order: 2,
      id: 2,
      name: 'exercise 1',
      level: 2,
      tasks: [{ id: 2 }],
    },
    {
      order: 3,
      id: 3,
      name: 'exercise 2',
      level: 1,
      tasks: [{ id: 3 }],
    },
    {
      order: 4,
      id: 4,
      name: 'exercise 2',
      level: 2,
      tasks: [{ id: 4 }],
    },
  ];
  const series = [
    {
      order: 1,
      id: 1,
      name: 'default',
      type: 'SINGLE_WORDS',
      exerciseGroupId: 1,
      subgroups: [1, 2],
    },
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
      exercises: [1, 2, 3, 4]
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

  return { tasks, subgroups, exercises, series, groups, availableExercises: ['1','3'] };
}
