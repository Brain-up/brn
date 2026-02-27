import BaseTaskSerializer from '../task';
import Model from '@warp-drive-mirror/legacy/model';

export default class TaskWordsSequencesSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    const hashCopy = {
      ...hash,
      wrongAnswers: [],
    };
    return super.normalize(typeClass, hashCopy);
  }
}

