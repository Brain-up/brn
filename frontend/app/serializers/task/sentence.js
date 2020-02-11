import BaseTaskSerializer from '../task';

export default BaseTaskSerializer.extend({
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
      correctAnswer: hash.correctAnswer.word,
      answer: hash.correctAnswer,
      words: hash.answerOptions,
    };
    return this._super(typeClass, hashCopy);
  },
});
