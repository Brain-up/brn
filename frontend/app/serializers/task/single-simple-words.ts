import BaseTaskSerializer from '../task';
import Model from '@warp-drive-mirror/legacy/model';
// import { urlForImage, urlForAudio } from 'brn/utils/file-url';

export default class TaskSingleSimpleWordsSerializer extends BaseTaskSerializer {
  normalize(typeClass: Model, hash: any) {
    return super.normalize(typeClass, hash);
  }
}

