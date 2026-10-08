export enum ExerciseMechanism {
  WORDS = 'WORDS', // show words in random places, play one after another min n*2+1 times
  MATRIX = 'MATRIX', // show words by exercise template, play whole sentence by random words in columns
  SIGNALS = 'SIGNALS',
  PHONEME_PAIRS = 'PHONEME_PAIRS',
  AUDITORY_SEQUENCE = 'AUDITORY_SEQUENCE',
  PROSODY = 'PROSODY',
  ENVIRONMENTAL_SOUNDS = 'ENVIRONMENTAL_SOUNDS',
}

export type ExerciseDTOType =
  | 'WORDS_SEQUENCES'
  | 'SENTENCE'
  | 'SINGLE_SIMPLE_WORDS'
  | 'SINGLE_WORDS_KOROLEVA'
  | 'PHRASES'
  | 'DI'
  | 'DURATION_SIGNALS'
  | 'FREQUENCY_SIGNALS'
  | 'PHONEME_PAIRS'
  | 'AUDITORY_SEQUENCE'
  | 'PROSODY'
  | 'ENVIRONMENTAL_SOUNDS';
