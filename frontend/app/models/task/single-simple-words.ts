import { attr } from '@ember-data/model';
import BaseTask from '../task';
import shuffleArray from 'brn/utils/shuffle-array';
import { cached } from 'tracked-toolbox';

interface IRawAnswerOption {
  audioFileUrl: string;
  description: string;
  id: number;
  columnNumber: number;
  pictureFileUrl: string;
  soundsCount: number;
  word: string;
  wordType: 'OBJECT';
}
export default class TaskSingleSimpleWordsModel extends BaseTask {
  @attr() answerOptions!: IRawAnswerOption[];
  exerciseMechanism = 'WORDS';
  @cached
  get tasksToSolve() {
    const playWordsCount = this.exercise.playWordsCount ?? 1;
    return [
      ...shuffleArray(this.answerOptions, 1),
      ...shuffleArray(this.answerOptions, 2),
      ...shuffleArray(this.answerOptions, 3),
    ].map((item, index) => {
      let answers = [];
      if (playWordsCount === 1) {
        answers.push(item);
      } else {
        const refs = [
          ...shuffleArray(this.answerOptions, 4),
          ...shuffleArray(this.answerOptions, 5),
          ...shuffleArray(this.answerOptions, 6),
        ];
        answers = refs.slice(0, playWordsCount);
      }
      return {
        answer: answers,
        order: index,
      };
    });
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your models.
declare module 'ember-data/types/registries/model' {
  export default interface ModelRegistry {
    'task/single-simple-words': TaskSingleSimpleWordsModel;
  }
}
