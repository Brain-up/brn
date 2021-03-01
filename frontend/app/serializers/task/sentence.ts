import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';

export default class TaskSentenceSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    return super.normalize(typeClass, hash);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/sentence': TaskSentenceSerializer;
  }
}
