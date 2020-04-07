import BaseTaskSerializer from '../task';
import { urlForImage, urlForAudio } from 'brn/utils/file-url';
export default BaseTaskSerializer.extend({
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
      words: hash.answerOptions.mapBy('word').concat(hash.correctAnswer.word),
      word: hash.correctAnswer.word,
      audioFileUrl: urlForAudio(hash.correctAnswer.audioFileUrl),
      pictureFileUrl: urlForImage(hash.correctAnswer.pictureFileUrl),
    };
    return this._super(typeClass, hashCopy);
  },
});
