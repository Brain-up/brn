import ApplicationSerializer from './application';
import AnswerOption from 'brn/utils/answer-option';

export default ApplicationSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({
    order: 'serialNumber',
    type: 'exerciseType',
  }),
  normalize(typeClass, hash) {
    if (hash.answerOptions) {
      let opts = [];
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
      hash.normalizedAnswerOptions = opts.map(
        (el) => new AnswerOption(el),
      );
    } else {
      hash.normalizedAnswerOptions = [];
    }
    return this._super(typeClass, hash);
  },
});
