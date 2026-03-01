import Model from '@ember-data/model';

export default class Exercise extends Model {
  trackTime(type = 'start') {
    if (type === 'start' || type === 'end') {
      this.startTime = new Date();
    }
  }

  set parent(value) {
    this.exercise = value;
  }
}
