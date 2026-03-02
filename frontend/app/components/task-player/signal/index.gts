import Component from '@glimmer/component';
import type { TaskSignal as TaskSignalModel } from 'brn/schemas/task/signal';
import { MODES, type Mode } from 'brn/utils/task-modes';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import type { Signal as SignalModel } from 'brn/schemas/signal';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import StatsService, { StatEvents } from 'brn/services/stats';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { task, Task as TaskGenerator } from 'ember-concurrency';
import AudioService from 'brn/services/audio';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { hash } from '@ember/helper';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { eq } from 'ember-truth-helpers';
import UiTaskContent from 'brn/components/ui/task-content';
import frequencyVisualizer from 'brn/modifiers/frequency-visualizer';

interface TaskPlayerSignalSignature {
  Args: {
  task: TaskSignalModel;
  mode: Mode;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(): void;
  onRightAnswer(config?: any): void;
  onWrongAnswer(config?: any): void;
  };
  Blocks: {
    header: [{ tasks: unknown[] }];
    footer: [{ audioFileUrl: unknown }];
  };
  Element: HTMLElement;
}

export default class TaskPlayerSignalComponent extends Component<TaskPlayerSignalSignature> {
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
      // @ts-expect-error SignalModel — audioFileUrl is a signal record, not a string
      this.audio.startPlayTask([this.audioFileUrl]);
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

  <template>
    <div
      class="
        flex flex-grow flex-col"
      ...attributes
      {{didInsert this.onInsert}}
    >
      {{yield (hash tasks=this.tasksCopy) to="header"}}
      {{#if this.tasksCopy.length}}
    
        <UiTaskContent>
          <ul class="task-player__options mt-5">
            {{#each @task.answerOptions as |answerOption|}}
              <li class="task-player__option">
                <button
                  data-test-task-answer
                  data-test-task-answer-option={{answerOption.word}}
                  disabled={{@disableAnswers}}
                  title={{answerOption.word}}
                  type="button"
                  class="btn-press task-player__option-button bg-transparent py-2 px-4 rounded text-black
                    {{if
                      (eq @activeWord answerOption.word)
                      "border bg-blue-500"
                      "border border-blue-500"
                    }}
                    {{if
                      @disableAnswers
                      "opacity-50 cursor-default"
                      "hover:bg-blue-500 hover:text-white hover:border-transparent"
                    }}"
                  {{on
                    "click"
                    (if
                      (eq @mode "interact")
                      (fn @onPlayText answerOption.word)
                      (fn this.checkMaybe answerOption)
                    )
                  }}
                >
                  {{!-- template-lint-disable no-nested-interactive --}}
                  <canvas {{frequencyVisualizer answerOption}}></canvas>
                </button>
              </li>
            {{/each}}
          </ul>
    
        </UiTaskContent>
    
        {{yield (hash audioFileUrl=this.audioFileUrl) to="footer"}}
      {{/if}}
    </div>
  </template>
}
