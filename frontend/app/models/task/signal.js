import BaseTask from '../task';
import { belongsTo } from '@ember-data/model';

export default class TaskSignalModel extends BaseTask {
  @belongsTo('signal', { async: false}) signal;
}
