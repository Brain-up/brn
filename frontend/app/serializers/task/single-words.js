import BaseTaskSerializer from '../task';

export default BaseTaskSerializer.extend({
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: '/audio/' + hash.correctAnswer.audioFileUrl,
      pictureFileUrl: '/' + hash.correctAnswer.pictureFileUrl,
    };
    return this._super(typeClass, hashCopy);
  },
});
