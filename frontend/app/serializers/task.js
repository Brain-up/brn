import ApplicationSerializer from './application';

export default ApplicationSerializer.extend({
  ATTR_NAMES_MAP: Object.freeze({
    order: 'serialNumber',
  }),

  normalize(typeClass, hash) {
    hash = {
      ...hash,
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: '/audio/' + hash.correctAnswer.audioFileUrl,
      pictureFileUrl: hash.correctAnswer.pictureFileUrl,
    };
    return this._super(typeClass, hash);
  },
});
