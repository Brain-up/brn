import Service from '@ember/service';
import { A } from '@ember/array';

export default Service.extend({
  init() {
    this._super(...arguments);
    this.set('completedTasks', A());
    this.clearCurrentCycleTaks();
  },
  saveAsCompleted(task) {
    this.completedTasks.pushObject(task);
    this.completedCycleTasks.pushObject(task);
  },
  isCompleted(task) {
    return this.completedTasks.includes(task);
  },
  isCompletedInCurrentCycle(task) {
    return this.completedCycleTasks.includes(task);
  },
  clearCurrentCycleTaks() {
    this.set('completedCycleTasks', A());
  },
});
