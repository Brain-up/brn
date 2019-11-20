import ApplicationSerializer from './application';

const ATTR_NAMES_VOCABULARY = {
  order: 'serialNumber',
};

export default ApplicationSerializer.extend({
  keyForAttribute(attrKey) {
    return ATTR_NAMES_VOCABULARY[attrKey] || attrKey;
  },
  normalize(typeClass, hash) {
    hash = {
      ...hash,
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: hash.correctAnswer.audioFileUrl,
    };
    return this._super(typeClass, hash);
  },
});
