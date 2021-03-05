import { attr } from '@ember-data/model';
import BaseTask from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import deepCopy from '../../utils/deep-copy';
import { cached } from 'tracked-toolbox';

function createTasks([first, ...tail]: Array<string[]>, acc: Array<string[]> = []): Array<string[]> {
  const results: Array<string[]> = [];
  const finalResults: Array<string[]> = [];

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

export default class WordsSequences extends BaseTask {
  @attr('string') template!: string;
  @attr() answerOptions!: string;
  @attr('array', { defaultValue() {
    return [];
  }}) wrongAnswers!: unknown[];
  exerciseType = 'words-sequences';
  @cached
  get selectedItemsOrder() {
    return this.template
      .split('<')[1]
      .split('>')[0]
      .split(' ');
  };
  @cached
  get possibleTasks() {
    const options = Object.keys(this.answerOptions);
    const taskPartsOptions = this.selectedItemsOrder.filter((key)=>options.includes(key)).map(
      (orderItemName) => this.answerOptions[orderItemName] || [],
    );
    return shuffleArray(createTasks(taskPartsOptions));
  }
  @cached
  get doubledTasks() {
    return [].concat(
      deepCopy(this.possibleTasks),
      deepCopy(this.possibleTasks),
    );
  }
  @cached
  get tasksSequence() {
    return shuffleArray(this.doubledTasks).map((item, index) => {
      return {
        answer: [...item],
        order: index,
      };
    });
  }
  @cached
  get tasksToSolve() {
    return shuffleArray(this.tasksSequence, 10).concat(
      this.wrongAnswers.map((wrongAnswer: any, index: number) => {
        return {
          ...wrongAnswer,
          order: this.tasksSequence.length + index,
        };
      }),
    ).slice(0, 30);
  }
}


// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/words-sequences': WordsSequences;
  }
}
