export enum ExerciseMechanism {
  WORDS = 'WORDS', // show words in random places, play one after another min n*2+1 times
  MATRIX = 'MATRIX', // show words by exercise template, play whole sentence by random words in columns
  SIGNALS = 'SIGNALS'
}

export type ExerciseDTOType =
  | 'WORDS_SEQUENCES'
  | 'SENTENCE'
  | 'SINGLE_SIMPLE_WORDS'
  | 'SINGLE_WORDS_KOROLEVA'
  | 'PHRASES'
  | 'DI'
  | 'DURATION_SIGNALS'
  | 'FREQUENCY_SIGNALS';
