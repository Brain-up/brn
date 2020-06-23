const task = {
  exerciseType: 'SINGLE_SIMPLE_WORDS',
  type: 'task/SINGLE_SIMPLE_WORDS',
  name: '',
  wrongAnswers: [],
  correctAnswer: 'вить',
  answerOptions: [
    {
      id: 345,
      audioFileUrl: '',
      word: 'линь',
      pictureFileUrl: 'pictures/линь.jpg',
      soundsCount: 0,
    },
    {
      id: 344,
      audioFileUrl: '',
      word: 'вить',
      pictureFileUrl: 'pictures/вить.jpg',
      soundsCount: 0,
    },
    {
      id: 343,
      audioFileUrl: '',
      word: 'дуб',
      pictureFileUrl: 'pictures/дуб.jpg',
      soundsCount: 0,
    },
  ],
  tasksToSolve: [
    {
      answer: [
        {
          audioFileUrl: 'no_noise/линь.mp3',
          id: 222,
          pictureFileUrl: 'pictures/линь.jpg',
          soundsCount: 0,
          word: 'линь',
          wordType: 'OBJECT',
        },
      ],
      order: 1,
    },
    {
      answer: [
        {
          audioFileUrl: 'no_noise/дуб.mp3',
          id: 221,
          pictureFileUrl: 'pictures/дуб.jpg',
          soundsCount: 0,
          word: 'дуб',
          wordType: 'OBJECT',
        },
      ],
      order: 1,
    },
  ],
};

export { task };
// const task = {
//   exerciseType: 'SINGLE_SIMPLE_WORDS',
//   type: 'task/SINGLE_SIMPLE_WORDS',
//   name: 'Вить',
//   id: 1,
//   answerOptions: [
//     {
//       id: 2,
//       audioFileUrl: '',
//       word: 'вить',
//       pictureFileUrl: '',
//       soundsCount: 1,
//     },
//     {
//       id: 3,
//       audioFileUrl: '',
//       word: 'сад',
//       pictureFileUrl: '',
//       soundsCount: 1,
//     },
//     {
//       id: 4,
//       audioFileUrl: '',
//       word: 'быль',
//       pictureFileUrl: '',
//       soundsCount: 1,
//     },
//   ],
//   correctAnswer: {
//     id: 5,
//     audioFileUrl: '',
//     word: 'вить',
//     pictureFileUrl:
//       'https://klike.net/uploads/posts/2019-07/1564314090_3.jpg',
//     soundsCount: 1,
//   },
//   tasksToSolve: [
//     {
//       answer: [
//         {
//           audioFileUrl: 'no_noise/линь.mp3',
//           id: 6,
//           pictureFileUrl: 'pictures/линь.jpg',
//           soundsCount: 0,
//           word: 'вить',
//           wordType: 'OBJECT',
//         },
//       ],
//       order: 0,
//     },
//     {
//       answer: [
//         {
//           audioFileUrl: 'no_noise/вить.mp3',
//           id: 7,
//           pictureFileUrl: 'pictures/вить.jpg',
//           soundsCount: 0,
//           word: 'дуб',
//           wordType: 'OBJECT',
//         },
//       ],
//       order: 1,
//     },
//   ],
// };

// export { task };
