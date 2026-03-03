const task = {
  exerciseMechanism: 'WORDS',
  exerciseType: 'SINGLE_SIMPLE_WORDS',
  type: 'task/WORDS',
  name: '',
  level: 0,
  shouldBeWithPictures: true,
  wrongAnswers: [],
  correctAnswer: 'вить',
  answerOptions: [
    {
      id: 345,
      audioFileUrl: '',
      word: 'вить',
      wordPronounce: 'вить',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/линь.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
  ],
};

const taskWithPreGeneratedAudio = {
  exerciseMechanism: 'WORDS',
  exerciseType: 'SINGLE_SIMPLE_WORDS',
  type: 'task/WORDS',
  name: '',
  level: 0,
  shouldBeWithPictures: true,
  wrongAnswers: [],
  correctAnswer: 'вить',
  answerOptions: [
    {
      id: 345,
      audioFileUrl: '/audio/no_noise/вить.mp3',
      word: 'вить',
      wordPronounce: 'вить',
      wordType: 'OBJECT',
      pictureFileUrl: 'pictures/линь.jpg',
      soundsCount: 0,
      description: '',
      columnNumber: -1,
    },
  ],
};

export { task, taskWithPreGeneratedAudio };
