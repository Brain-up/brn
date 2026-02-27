import BaseTask from '../task';
import { belongsTo } from '@warp-drive-mirror/legacy/model';
import { Type } from '@warp-drive-mirror/core/types/symbols';
import SignalModel from '../signal';

export default class TaskSignalModel extends BaseTask {
  declare [Type]: 'task/signal';
  @belongsTo('signal', { async: false, inverse: null }) signal!: SignalModel;
}
