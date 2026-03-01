import Model from '@ember-data/model';

export default class Exercise extends Model {
  get isCompleted() {
    const tasksIds = (this.tasks || []).map(r => r.id);
    const tasks = this.tasks;
    return tasksIds.length > 0 && tasks !== null;
  }
}
