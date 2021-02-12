import ApplicationSerializer from './application';
import AnswerOption from 'brn/utils/answer-option';
import Model from '@ember-data/model';

export default class TaskSerializer extends ApplicationSerializer {
  ATTR_NAMES_MAP = Object.freeze({
    order: 'serialNumber',
    type: 'exerciseType',
  });
  public normalize(typeClass: Model, hash: any) {
    if (hash.answerOptions) {
      let opts: any[] = [];
      if (!Array.isArray(hash.answerOptions)) {
        Object.keys(hash.answerOptions).forEach((key) => {
          if (Array.isArray(hash.answerOptions[key])) {
            opts = [...opts, ...hash.answerOptions[key]];
          }
        });
      } else if (hash.correctAnswer) {
        opts = [...hash.answerOptions, hash.correctAnswer];
      } else {
        opts = [...hash.answerOptions];
      }
      hash.normalizedAnswerOptions = opts.map((el) => new AnswerOption(el));
    } else {
      hash.normalizedAnswerOptions = [];
    }
    return super.normalize(typeClass, hash);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your serializers.
declare module 'ember-data/types/registries/serializer' {
  export default interface SerializerRegistry {
    'task': TaskSerializer;
  }
}
