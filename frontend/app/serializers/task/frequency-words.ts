import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';

export default class TaskFrequencyWordsSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    const hashCopy = {
      ...hash,
    };
    return super.normalize(typeClass, hashCopy);
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/frequency-words': TaskFrequencyWordsSerializer;
  }
}
