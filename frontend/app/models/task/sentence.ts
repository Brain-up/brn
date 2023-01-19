import WordsSequences from './words-sequences';

export default class TaskSentenceModel extends WordsSequences {
  exerciseMechanism = 'MATRIX';
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/sentence': TaskSentenceModel;
  }
}
