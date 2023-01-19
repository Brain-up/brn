import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';
// import { urlForImage, urlForAudio } from 'brn/utils/file-url';

export default class TaskSingleSimpleWordsSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    return super.normalize(typeClass, hash);
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/single-simple-words': TaskSingleSimpleWordsSerializer;
  }
}
