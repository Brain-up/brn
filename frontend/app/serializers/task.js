import ApplicationSerializer from './application';

export default ApplicationSerializer.extend({
  normalize(typeClass, hash) {
    hash = {
      ...hash,
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: hash.correctAnswer.audioFileUrl,
      order: hash.serialNumber,
    };
    return this._super(typeClass, hash);
  },
});
