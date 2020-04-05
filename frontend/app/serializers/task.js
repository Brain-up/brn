import ApplicationSerializer from './application';
import AnswerOption from 'brn/utils/answer-option';
import shuffleArray from 'brn/utils/shuffle-array';

export default ApplicationSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({
    order: 'serialNumber',
    type: 'exerciseType',
  }),
  normalize(typeClass, hash) {
    if (hash.answerOptions) {
      let opts = [];
      if (!Array.isArray(hash.answerOptions)) {
        Object.keys(hash.answerOptions).forEach((key)=>{
          if (Array.isArray(hash.answerOptions[key])) {
            opts = [...opts, ...hash.answerOptions[key]];
          }
        })
      } else {
        opts = [...hash.answerOptions, hash.correctAnswer];
      }
      hash.normalizedAnswerOptions = shuffleArray(opts).map((el) => new AnswerOption(el));
    }
    return this._super(typeClass, hash);
  },
});
