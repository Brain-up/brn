import BaseTaskSerializer from '../task';

export default class TaskSingleSimpleWordsSerializer extends BaseTaskSerializer {
  normalize(typeClass, hash) {
    const hashCopy = {
      ...hash,
    };
    return super.normalize(typeClass, hashCopy);
  }
}
