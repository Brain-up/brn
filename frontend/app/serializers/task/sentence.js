import BaseTaskSerializer from '../task';

export default class TaskSentenceSerializer extends BaseTaskSerializer {
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
      correctAnswer: hash.correctAnswer.word,
    };
    return super.normalize(typeClass, hashCopy);
  }
}
