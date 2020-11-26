import { attr } from '@ember-data/model';
import BaseTask from '../task';
import { computed } from '@ember/object';
import shuffleArray from 'brn/utils/shuffle-array';
import deepCopy from '../../utils/deep-copy';

function createTasks([first, ...tail], acc = []) {
  const results = [];
  const finalResults = [];

  first.forEach((i)=>{
    results.push([i]);
  });

  acc.forEach((row)=>{
    results.forEach((result)=>{
      finalResults.push(row.concat(result));
    })
  });
  if (tail.length) {
    return createTasks(tail, acc.length ? finalResults : results);
  }

  return acc.length ? finalResults : results;
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
  // eslint-disable-next-line ember/require-computed-property-dependencies
  possibleTasks: computed('answerOptions.[]', function() {
    const options = Object.keys(this.answerOptions);
    const taskPartsOptions = this.selectedItemsOrder.filter((key)=>options.includes(key)).map(
      (orderItemName) => this.answerOptions[orderItemName] || [],
    );
    return shuffleArray(createTasks(taskPartsOptions));
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
