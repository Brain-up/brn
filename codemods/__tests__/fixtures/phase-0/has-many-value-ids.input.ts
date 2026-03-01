import Model from '@ember-data/model';

export default class Exercise extends Model {
  get isCompleted() {
    const tasksIds = this.hasMany('tasks').ids();
    const tasks = this.hasMany('tasks').value();
    return tasksIds.length > 0 && tasks !== null;
  }
}
