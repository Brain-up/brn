import Component from '@ember/component';
import { reads } from '@ember/object/computed';
import { inject } from '@ember/service';
import deepEqual from 'brn/utils/deep-equal';
import deepCopy from '../../utils/deep-copy';

export default Component.extend({
  init() {
    this._super(...arguments);
    this.set(
      'uncompletedTasks',
      this.task.tasksSequence.filterBy('done', false),
    );
  },
  audio: inject(),
  currentUserObjectChoice: null,
  currentUserActionChoice: null,
  firstUncompletedTask: reads('uncompletedTasks.firstObject'),
  currentExpectedObject: reads('firstUncompletedTask.object'),
  currentExpectedAction: reads('firstUncompletedTask.action'),
  startNewTask() {
    this.firstUncompletedTask.set('done', true);
    this.set('currentUserObjectChoice', null);
    this.set('currentUserActionChoice', null);
  },
  checkMaybe() {
    if (
      this.currentUserObjectChoice &&
      this.currentUserActionChoice &&
      deepEqual(this.currentUserObjectChoice, this.currentExpectedObject) &&
      deepEqual(this.currentUserActionChoice, this.currentExpectedAction)
    ) {
      return this.startNewTask();
    }
    this.uncompletedTasks.push(deepCopy(this.firstUncompletedTask));
    this.startNewTask();
  },
});
