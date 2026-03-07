export const mockUser = {
  data: [
    {
      roles: ['ADMIN'],
      id: '1',
      userId: 'f89e5760-0caf-4a95-9810-cd6aa4a8261e',
      name: 'admin',
      email: 'admin@admin.com',
      bornYear: 1999,
      gender: 'MALE',
      active: true,
      created: '2021-07-30T08:13:15.587',
      changed: '2021-12-01T12:04:10.65',
      avatar: '18',
      headphones: [
        {
          id: '1',
          name: 'first',
          active: true,
          type: 'ON_EAR_BLUETOOTH',
          description: 'first',
          userAccount: '1',
        },
      ],
    },
  ],
  errors: [],
  meta: [],
};

export const mockGroups = {
  data: [
    {
      id: '1',
      locale: 'en-us',
      name: 'Speech Exercises',
      description: 'Exercises for speech development',
      series: ['1', '2'],
    },
    {
      id: '2',
      locale: 'en-us',
      name: 'Cognitive Exercises',
      description: 'Exercises for cognitive development',
      series: ['3', '4'],
    },
  ],
};

export const mockSeries = {
  data: [
    {
      id: '1',
      name: 'Words Series',
      description: 'Word recognition exercises',
      group: '1',
      type: 'SINGLE_SIMPLE_WORDS',
      subgroups: ['1', '2'],
    },
    {
      id: '2',
      name: 'Sentences Series',
      description: 'Sentence construction exercises',
      group: '1',
      type: 'SENTENCE',
      subgroups: ['3'],
    },
    {
      id: '3',
      name: 'Frequency Series',
      description: 'Frequency discrimination exercises',
      group: '2',
      type: 'FREQUENCY',
      subgroups: [],
    },
    {
      id: '4',
      name: 'Duration Series',
      description: 'Duration discrimination exercises',
      group: '2',
      type: 'DURATION',
      subgroups: [],
    },
  ],
};

export const mockSubgroups = {
  data: [
    {
      id: '1',
      name: 'Family',
      level: 1,
      code: 'family',
      description: 'Family words',
      series: '1',
      exercises: ['1', '2'],
      pictureUrl: '/pictures/theme/family.svg',
    },
    {
      id: '2',
      name: 'Animals',
      level: 1,
      code: 'animals',
      description: 'Animal words',
      series: '1',
      exercises: ['3'],
      pictureUrl: '/pictures/theme/animals.svg',
    },
    {
      id: '3',
      name: 'Daily Phrases',
      level: 1,
      code: 'daily-phrases',
      description: 'Common daily phrases',
      series: '2',
      exercises: [],
      pictureUrl: '/pictures/theme/phrases.svg',
    },
  ],
};

export const mockExercises = {
  data: [
    {
      id: '1',
      name: 'Exercise 1',
      level: 1,
      noise: { level: 0, url: '' },
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      series: '1',
      parent: '1',
      available: true,
    },
    {
      id: '2',
      name: 'Exercise 2',
      level: 2,
      noise: { level: 0, url: '' },
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      series: '1',
      parent: '1',
      available: true,
    },
    {
      id: '3',
      name: 'Exercise 3',
      level: 1,
      noise: { level: 0, url: '' },
      exerciseType: 'SINGLE_SIMPLE_WORDS',
      exerciseMechanism: 'WORDS',
      series: '1',
      parent: '2',
      available: false,
    },
  ],
};

/**
 * Detailed exercise response with embedded tasks for findRecord endpoint.
 * The BrnApiHandler normalizes this REST response into JSON:API with included tasks.
 */
export function mockExerciseWithTasks(exerciseId: string) {
  const exercise = mockExercises.data.find((e) => e.id === exerciseId);
  if (!exercise) return { data: {} };

  return {
    data: {
      ...exercise,
      signals: [],
      tasks: [
        {
          id: 101,
          serialNumber: 1,
          exerciseMechanism: 'WORDS',
          answerOptions: [
            { word: 'dog', audioFileUrl: '', pictureFileUrl: '' },
            { word: 'bird', audioFileUrl: '', pictureFileUrl: '' },
            { word: 'fish', audioFileUrl: '', pictureFileUrl: '' },
          ],
          correctAnswer: {
            word: 'cat',
            audioFileUrl: '',
            pictureFileUrl: '',
          },
        },
        {
          id: 102,
          serialNumber: 2,
          exerciseMechanism: 'WORDS',
          answerOptions: [
            { word: 'red', audioFileUrl: '', pictureFileUrl: '' },
            { word: 'green', audioFileUrl: '', pictureFileUrl: '' },
            { word: 'yellow', audioFileUrl: '', pictureFileUrl: '' },
          ],
          correctAnswer: {
            word: 'blue',
            audioFileUrl: '',
            pictureFileUrl: '',
          },
        },
      ],
    },
  };
}

export const mockContributors = {
  data: [
    {
      id: '1',
      name: 'Elena Moshnikova',
      description: 'Founder & Neuropsychologist',
      pictureUrl: '/pictures/contributors/elena.jpg',
      company: 'BRN',
      contribution: 'founder',
      active: true,
    },
    {
      id: '2',
      name: 'Test Developer',
      description: 'Full Stack Developer',
      pictureUrl: '/pictures/contributors/dev.jpg',
      company: 'BRN',
      contribution: 'developer',
      active: true,
    },
  ],
};

export const emptyResponse = { data: [] };
export const emptyObject = { data: {} };
