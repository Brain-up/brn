import Service from '@ember/service';
import { A } from '@ember/array';

export default Service.extend({
  init() {
    this._super(...arguments);
    this.clearCompletedTasks();
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
  clearCompletedTasks() {
    this.set('completedTasks', A());
  },
  clearCurrentCycleTaks() {
    this.set('completedCycleTasks', A());
  },
});
