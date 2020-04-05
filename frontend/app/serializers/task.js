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
      hash.answerOptions = shuffleArray([...hash.answerOptions, hash.correctAnswer]).map((el) => new AnswerOption(el));
    }
    return this._super(typeClass, hash);
  },
});
