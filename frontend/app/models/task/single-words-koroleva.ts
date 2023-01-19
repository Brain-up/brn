import SingleWordsModel from './single-simple-words';

export default class TaskSingleWordsKorolevaModel extends SingleWordsModel {
  exerciseMechanism = 'WORDS';
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/single-words-koroleva': SingleWordsModel;
  }
}
