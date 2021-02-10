import BaseTask from '../task';
import { belongsTo } from '@ember-data/model';
import SignalModel from '../signal';

export default class TaskSignalModel extends BaseTask {
  @belongsTo('signal', { async: false}) signal!: SignalModel
}



// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/signal': TaskSignalModel;
  }
}
