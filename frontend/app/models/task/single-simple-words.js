import BaseTask from '../task';
import { computed } from '@ember/object';
import DS from 'ember-data';
const { attr } = DS;
import shuffleArray from 'brn/utils/shuffle-array';

export default class TaskSingleSimpleWordsModel extends BaseTask.extend({
  answerOptions: attr(),
  wrongAnswers: attr('array'),
  exerciseType: 'single-simple-words',
  tasksSequence: computed('answerOptions.[]', function() {
    return [
      ...shuffleArray(this.answerOptions),
      ...shuffleArray(this.answerOptions),
    ];
  }),
  tasksToSolve: computed('wrongAnswers.[]', 'tasksSequence.[]', function() {
    return this.tasksSequence.concat(
      this.wrongAnswers.map((wrongAnswer, index) => {
        return {
          ...wrongAnswer,
          order: this.tasksSequence.length + index,
        };
      }),
    );
  }),
}) {}
