import Component from '@ember/component';
import { computed } from '@ember/object';
import { dasherize } from '@ember/string';

export default Component.extend({
  task: null,
  componentType: computed('task.exerciseType', function() {
    return `task-player/${dasherize(this.task.exerciseType)}`;
  }),
  onRightAnswer() {},
  afterCompleted() {},
});
