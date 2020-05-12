import { attr } from '@ember-data/model';
import BaseTask from '../task';
import { computed } from '@ember/object';
import shuffleArray from 'brn/utils/shuffle-array';

export default class TaskSingleSimpleWordsModel extends BaseTask.extend({
  answerOptions: attr(),
  exerciseType: 'single-simple-words',
  tasksToSolve: computed('answerOptions.[]', function() {
    return [
      ...shuffleArray(this.answerOptions),
      ...shuffleArray(this.answerOptions),
    ].map((item, index) => {
      return {
        answer: [item],
        order: index,
      };
    });
  }),
}) {}
