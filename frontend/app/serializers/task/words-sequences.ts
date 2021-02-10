import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';

export default class TaskWordsSequencesSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    const hashCopy = {
      ...hash,
      wrongAnswers: [],
    };
    return super.normalize(typeClass, hashCopy);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/words-sequences': TaskWordsSequencesSerializer;
  }
}
