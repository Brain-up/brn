import Component from 'brn/components/task-player/words-sequences/component';
import { tracked } from '@glimmer/tracking';
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { urlForAudio } from 'brn/utils/file-url';
import deepCopy from 'brn/utils/deep-copy';
import { TaskItem } from 'brn/utils/task-item';
import { MODES } from 'brn/utils/task-modes';
import { task, Task as TaskGenerator } from 'ember-concurrency';
import { StatEvents } from 'brn/services/stats';
import AnswerOption from 'brn/utils/answer-option';
import SingleSimpleWordTask from 'brn/models/task/single-simple-words';
export default class SingleSimpleWordsComponent extends Component<SingleSimpleWordTask> {
  @tracked currentAnswer: string[] = [];
  get audioFileUrl() {
    const task = this.firstUncompletedTask;
    if (!task) {
      return null;
    }
    if (!this.args.task) {
      return null;
    }
    const answer = task.answer[0] as AnswerOption;
    const useGeneratedUrl =
      this.args.task.usePreGeneratedAudio && answer.audioFileUrl;
    const url = useGeneratedUrl
      ? urlForAudio(answer.audioFileUrl)
      : this.audio.audioUrlForText(task.answer.map((e: AnswerOption) => e.word).join(' '));
    return url;
  }
  startTask() {
    this.isCorrect = false;
    if (this.mode === MODES.TASK && this.uncompletedTasks.length > 0) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }
  get amountOfColumns() {
    return this.task.exercise.wordsColumns;
  }
  get showTip() {
    if (!this.firstUncompletedTask) {
      return;
    }
    return this.mode === MODES.TASK && this.firstUncompletedTask?.answer.length > 1;
  }
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filterBy('completedInCurrentCycle', true)
      .mapBy('order');
    const tasksCopy = deepCopy(this.task.tasksToSolve).map(
      (copy: { order: string }) => {
        const completedInCurrentCycle = completedOrders.includes(copy.order);
        const copyEquivalent: any = this.tasksCopy.findBy('order', copy.order);
        return new TaskItem({
          ...copy,
          completedInCurrentCycle,
          nextAttempt: copyEquivalent && !!copyEquivalent.nextAttempt,
          canInteract: true,
        });
      },
    );
    this.tasksCopy = tasksCopy;
  }

  @(task(function* (this: SingleSimpleWordsComponent, selected) {
    this.currentAnswer = [...this.currentAnswer, selected].filter(e => e.length);

    if (this.currentAnswer.length !==  this.firstUncompletedTask?.answer.length) {
      return;
    }

    const isCorrect = deepEqual(
      this.currentAnswer.join(''),
      this.firstUncompletedTask.answer.map(e => e.word).join(''),
    );

    this.isCorrect = isCorrect;

    if (isCorrect) {
      this.stats.addEvent(StatEvents.RightAnswer);
      yield this.handleCorrectAnswer();
    } else {
      this.stats.addEvent(StatEvents.WrongAnswer);
      yield this.handleWrongAnswer();
    }

    this.currentAnswer = [];
  }).drop())
  showTaskResult!: TaskGenerator<any, any>;

  async handleWrongAnswer() {
    this.markNextAttempt(this.firstUncompletedTask);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
  }
}
