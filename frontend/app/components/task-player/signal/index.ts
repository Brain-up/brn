import Component from '@glimmer/component';
import type { TaskSignal as TaskSignalModel } from 'brn/schemas/task/signal';
import { MODES } from 'brn/utils/task-modes';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import type { Signal as SignalModel } from 'brn/schemas/signal';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import StatsService, { StatEvents } from 'brn/services/stats';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { task, Task as TaskGenerator } from 'ember-concurrency';
import AudioService from 'brn/services/audio';

interface ISignalComponentArgs {
  task: TaskSignalModel;
  mode: keyof typeof MODES;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(): void;
  onRightAnswer(config?: any): void;
  onWrongAnswer(config?: any): void;
}

export default class TaskPlayerSignalComponent extends Component<ISignalComponentArgs> {
  @service('stats') stats!: StatsService;
  @service('audio') audio!: AudioService;
  get tasksCopy() {
    const tasks = this.task?.parent?.tasks;
    return tasks ? Array.from(tasks) : [];
  }

  get onWrongAnswer() {
    return this.args.onWrongAnswer;
  }
  get onRightAnswer() {
    return this.args.onRightAnswer;
  }

  @action checkMaybe(answerOption: { signal: SignalModel }) {
    // console.log(answerOption);
    this.showTaskResult.perform(this.audioFileUrl === answerOption.signal);
    // if (this.audioFileUrl === answerOption.signal) {
    //   console.log('good', answerOption.signal, this.audioFileUrl);
    // } else {
    //   console.log('bad', answerOption.signal, this.audioFileUrl);
    // }
  }

  @(task(function* (this: TaskPlayerSignalComponent, isCorrect: boolean) {
    if (isCorrect) {
      this.stats.addEvent(StatEvents.RightAnswer);
      yield this.handleCorrectAnswer();
    } else {
      this.stats.addEvent(StatEvents.WrongAnswer);
      yield this.handleWrongAnswer();
    }
  }).drop())
  showTaskResult!: TaskGenerator<any, any>;

  async handleCorrectAnswer() {
    // await customTimeout(1000);
    // this.startNewTask();
    // if (!this.firstUncompletedTask) {
    // await customTimeout(3000);
    this.onRightAnswer();
    // }
  }

  startNewTask() {
    this.startTask();
  }

  startTask() {
    if (this.args.mode === MODES.TASK) {
      // @ts-expect-error SignalModel
      this.audio.startPlayTask(this.audioFileUrl);
    }
  }

  async handleWrongAnswer() {
    // this.markNextAttempt(this.firstUncompletedTask);
    // this.updateLocalTasks();
    // await customTimeout(1000);
    // this.startTask();
    // { skipRetry: true }
    this.onWrongAnswer();
  }

  get task() {
    return this.args.task;
  }

  get audioFileUrl() {
    return this.task?.signal;
  }

  @action onInsert() {
    // EOL
  }
}
