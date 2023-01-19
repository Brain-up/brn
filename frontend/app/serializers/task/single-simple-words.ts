import BaseTaskSerializer from '../task';
import Model from '@ember-data/model';
import { urlForImage, urlForAudio } from 'brn/utils/file-url';

export default class TaskSingleSimpleWordsSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    const hashCopy = {
      ...hash,
      // todo - refactor it or remove
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: urlForAudio(hash.correctAnswer.audioFileUrl),
      pictureFileUrl: urlForImage(hash.correctAnswer.pictureFileUrl),
    };
    return super.normalize(typeClass, hashCopy);
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task/single-simple-words': TaskSingleSimpleWordsSerializer;
  }
}
