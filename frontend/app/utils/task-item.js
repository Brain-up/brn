import { tracked } from '@glimmer/tracking';

export class TaskItem {
  @tracked isCompleted;
  @tracked canInteract;
  @tracked order;
  @tracked completedInCurrentCycle;
  @tracked nextAttempt;
  @tracked answer = [];
  @tracked normalizedAnswerOptions = [];
  constructor(params = {}) {
    Object.assign(this, params);
  }
  serialize() {
    let obj = {
      isCompleted: this.isCompleted,
      canInteract: this.canInteract,
      order: this.order,
      completedInCurrentCycle: this.completedInCurrentCycle,
      normalizedAnswerOptions: this.normalizedAnswerOptions,
      nextAttempt: this.nextAttempt,
      answer: this.answer.length ? this.answer.slice(0) : this.answer,
    };
    Object.keys(this).forEach((key) => {
      obj[key] = typeof this[key] === 'object' ? { ...this[key] } : this[key];
    });
    return obj;
  }
}
