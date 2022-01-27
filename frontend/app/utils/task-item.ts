import { tracked } from '@glimmer/tracking';
import AnswerOption from './answer-option';

export class TaskItem {
  @tracked isCompleted!: boolean;
  @tracked canInteract!: boolean;
  @tracked order!: number;
  @tracked completedInCurrentCycle!: boolean;
  @tracked nextAttempt: unknown;
  @tracked answer: AnswerOption[] = [];
  @tracked normalizedAnswerOptions = [];
  constructor(params = {}) {
    Object.assign(this, params);
  }
  serialize() {
    const obj: Omit<TaskItem, 'serialize'> = {
      isCompleted: this.isCompleted,
      canInteract: this.canInteract,
      order: this.order,
      completedInCurrentCycle: this.completedInCurrentCycle,
      normalizedAnswerOptions: this.normalizedAnswerOptions,
      nextAttempt: this.nextAttempt,
      answer: this.answer.slice(0),
    };
    Object.keys(this).forEach((key: keyof Omit<TaskItem, 'serialize'>) => {
      // @ts-expect-error spread
      obj[key] = typeof this[key] === 'object' ? { ...this[key] } : this[key];
    });
    return obj;
  }
}
