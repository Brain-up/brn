import { attr } from '@ember-data/model';
import BaseTask from '../task';
import { computed } from '@ember/object';
import shuffleArray from 'brn/utils/shuffle-array';
import deepCopy from '../../utils/deep-copy';

function createTasks(firstTaskPart, ...restPartsOptions) {
  let result = [];
  firstTaskPart.forEach((firstPartItem) => {
    result = result.concat(
      restPartsOptions[0].map((nextTaskPart) => {
        return [firstPartItem, nextTaskPart];
      }),
    );
  });
  if (restPartsOptions.length > 1) {
    result = result.concat(createTasks(...restPartsOptions));
  }
  return result;
}

export default class WordsSequences extends BaseTask.extend({
  template: attr('string'),
  answerOptions: attr(),
  wrongAnswers: attr('array'),
  exerciseType: 'words-sequences',
  selectedItemsOrder: computed('template', function() {
    return this.template
      .split('<')[1]
      .split('>')[0]
      .split(' ');
  }),
  possibleTasks: computed('answerOptions.[]', function() {
    const taskPartsOptions = this.selectedItemsOrder.map(
      (orderItemName) => this.answerOptions[orderItemName],
    );
    return createTasks(...taskPartsOptions);
  }),
  doubledTasks: computed('possibleTasks.[]', function() {
    return [].concat(
      deepCopy(this.possibleTasks),
      deepCopy(this.possibleTasks),
    );
  }),
  tasksSequence: computed('doubledTasks.[]', function() {
    return shuffleArray(this.doubledTasks).map((item, index) => {
      return {
        answer: [...item],
        order: index,
      };
    });
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
