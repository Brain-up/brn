import BaseTaskSerializer from '../task';

export default class TaskWordsSequencesSerializer extends BaseTaskSerializer {
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
      wrongAnswers: [],
    };
    return super.normalize(typeClass, hashCopy);
  }
}
