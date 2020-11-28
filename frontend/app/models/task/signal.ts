import BaseTask from '../task';
import { belongsTo } from '@ember-data/model';
import SignalModel from '../signal';

export default class TaskSignalModel extends BaseTask {
  @belongsTo('signal', { async: false}) signal!: SignalModel
}
