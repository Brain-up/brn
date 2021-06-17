import Component from '@glimmer/component';
import { inject as service } from '@ember/service';
import NetworkService from '../../services/network';
import { task, timeout, Task as TaskGenerator } from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import Ember from 'ember';

export default class GlobalTimerComponent extends Component {
  constructor(owner: any, args: any) {
    super(owner, args);
    this.syncTask.perform();
  }
  @service('network') declare network: NetworkService;
  @tracked seconds = 0;
  get minutes() {
    const sec = this.seconds % 60;
    const min = Math.floor(this.seconds / 60);
    return `${(min || '00') + ' : ' + (sec || '00')}`;
  }
  get getColor() {
    if (this.seconds > 1200) {
      return 'bg-green-secondary';
    } else if (this.seconds > 960) {
      return 'bg-yellow-secondary';
    } else {
      return 'bg-pink-secondary';
    }
  }
  @(task(function* (this: GlobalTimerComponent) {
    do {
      try {
        if (!Ember.testing) {
          const response = yield this.network.request(
            'study-history/todayTimer',
          );
          const { data } = yield response.json();
          this.seconds = data;
        } else {
          break;
        }
      } catch {
        // ok
      }
      yield timeout(10000);
    } while (true);
  }).keepLatest())
  syncTask!: TaskGenerator<any, any>;
}
