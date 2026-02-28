const task = {
  exerciseMechanism: 'WORDS',
  type: 'task/WORDS',
  name: '',
  wrongAnswers: [],
  correctAnswer: 'вить',
  answerOptions: [
    {
      id: 345,
      audioFileUrl: '',
      word: 'вить',
      pictureFileUrl: 'pictures/линь.jpg',
      soundsCount: 0,
    },
  ],
};

const taskWithPreGeneratedAudio = {
  exerciseMechanism: 'WORDS',
  type: 'task/WORDS',
  name: '',
  wrongAnswers: [],
  correctAnswer: 'вить',
  answerOptions: [
    {
      id: 345,
      audioFileUrl: '/audio/no_noise/вить.mp3',
      word: 'вить',
      pictureFileUrl: 'pictures/линь.jpg',
      soundsCount: 0,
    },
  ],
};

export { task, taskWithPreGeneratedAudio };
