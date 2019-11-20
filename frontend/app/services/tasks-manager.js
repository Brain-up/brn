import Service from '@ember/service';
import { A } from '@ember/array';

export default Service.extend({
  init() {
    this._super(...arguments);
    this.set('completedTasks', A());
  },
  saveAsCompleted(task) {
    this.completedTasks.pushObject(task);
  },
  isCompleted(task) {
    return this.completedTasks.includes(task);
  },
});
