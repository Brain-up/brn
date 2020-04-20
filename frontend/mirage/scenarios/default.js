export default function(server) {

  const series = server.create('series',{
    id: 1,
    name: 'распознавание слов',
    // description: 'A series of audio exercises',
  });

  const groups = server.createList('group',8)
  groups[0].series.add( series );
  groups[0].save();
  series.group = groups[0];
  series.save();

  const exercise1 = server.create('exercise',{
    id: 1,
    series,
    name: 'однослоговые слова',
  });

  const exercise2 = server.create('exercise',{
    id: 2,
    series,
    name: 'двуслоговые слова',
  });

  const exercise3 = server.create('exercise',{
    id: 3,
    series,
    name: 'сложные слова',
  });

  server.create('task',{
    "id": "1",
    "word": "бал",
    "order": 1,
    "audioFileId": "no_noise/бал.mp3",
    "words": [
        "бал",
        "бум",
        "быль",
        "боль",
        "зал",
        "мал"
    ],
    exercise:exercise1,
  });

  server.create('task',{
    "id": "2",
    "word": "бум",
    "order": 2,
    "audioFileId": "no_noise/бум.mp3",
    "words": [
        "бум",
        "зум",
        "кум",
        "лунь",
        "дума",
        "куб"
    ],
    exercise:exercise1,
  });

  server.create('task',{
    "id": "3",
    "word": "быль",
    "order": 3,
    "audioFileId": "no_noise/быль.mp3",
    "words": [
        "быль",
        "даль",
        "жаль",
        "лунь",
        "сеть",
        "топь"
    ],
    exercise:exercise1,
  });

  server.create('task',{
    "id": "4",
    "word": "вить",
    "order": 4,
    "audioFileId": "no_noise/вить.mp3",
    "words": [
        "вить",
        "быть",
        "ныть",
        "жить",
        "сеть",
        "пить"
    ],
    exercise:exercise1,
  });

  server.create('task',{
    "id": "5",
    "word": "гад",
    "order": 5,
    "audioFileId": "no_noise/гад.mp3",
    "words": [
        "гад",
        "мат",
        "клад",
        "пат",
        "дать",
        "спать"
    ],
    exercise:exercise1,
  });

  server.create('task',{
    "id": "6",
    "word": "бал",
    "order": 1,
    "audioFileId": "noise_0db/бал.mp3",
    "words": [
        "бал",
        "бум",
        "быль",
        "боль",
        "зал",
        "мал"
    ],
    exercise:exercise2,
  });

  server.create('task',{
    "id": "7",
    "word": "бум",
    "order": 2,
    "audioFileId": "noise_0db/бум.mp3",
    "words": [
        "бум",
        "зум",
        "кум",
        "лунь",
        "дума",
        "куб"
    ],
    exercise:exercise2,
  });

  server.create('task',{
    "id": "8",
    "word": "быль",
    "order": 3,
    "audioFileId": "noise_0db/быль.mp3",
    "words": [
        "быль",
        "даль",
        "жаль",
        "лунь",
        "сеть",
        "топь"
    ],
    exercise:exercise2,
  });

  server.create('task',{
    "id": "9",
    "word": "вить",
    "order": 4,
    "audioFileId": "noise_0db/вить.mp3",
    "words": [
        "вить",
        "быть",
        "ныть",
        "жить",
        "сеть",
        "пить"
    ],
    exercise:exercise2,
  });

  server.create('task',{
    "id": "10",
    "word": "гад",
    "order": 5,
    "audioFileId": "noise_0db/гад.mp3",
    "words": [
        "гад",
        "мат",
        "клад",
        "пат",
        "дать",
        "спать"
    ],
    exercise:exercise2,
  });

  server.create('task',{
    "id": "16",
    "word": "бал",
    "order": 1,
    "audioFileId": "noise_6db/бал.mp3",
    "words": [
        "бал",
        "бум",
        "быль",
        "боль",
        "зал",
        "мал"
    ],
    exercise:exercise3,
  });

  server.create('task',{
    "id": "17",
    "word": "бум",
    "order": 2,
    "audioFileId": "noise_6db/бум.mp3",
    "words": [
        "бум",
        "зум",
        "кум",
        "лунь",
        "дума",
        "куб"
    ],
    exercise:exercise3,
  });

  server.create('task',{
    "id": "18",
    "word": "быль",
    "order": 3,
    "audioFileId": "noise_6db/быль.mp3",
    "words": [
        "быль",
        "даль",
        "жаль",
        "лунь",
        "сеть",
        "топь"
    ],
    exercise:exercise3,
  });

  server.create('task',{
    "id": "19",
    "word": "вить",
    "order": 4,
    "audioFileId": "noise_6db/вить.mp3",
    "words": [
        "вить",
        "быть",
        "ныть",
        "жить",
        "сеть",
        "пить"
    ],
    exercise:exercise3,
  });

  server.create('task',{
    "id": "110",
    "word": "гад",
    "order": 5,
    "audioFileId": "noise_6db/гад.mp3",
    "words": [
        "гад",
        "мат",
        "клад",
        "пат",
        "дать",
        "спать"
    ],
    exercise:exercise3,
  });
}
