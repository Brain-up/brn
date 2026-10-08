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
import { dropTask } from 'ember-concurrency';
import AudioService from 'brn/services/audio';
import customTimeout from 'brn/utils/custom-timeout';
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
  heardWords?: Set<string>;
  onPlayText(): void;
  onRightAnswer(config?: { skipRetry?: boolean }): void;
  onWrongAnswer(config?: { skipRetry?: boolean }): void;
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
    this.showTaskResult.perform(this.audioFileUrl === answerOption.signal);
  }

  showTaskResult = dropTask(async (isCorrect: boolean) => {
    if (isCorrect) {
      this.stats.addEvent(StatEvents.RightAnswer);
      await this.handleCorrectAnswer();
    } else {
      this.stats.addEvent(StatEvents.WrongAnswer);
      await this.handleWrongAnswer();
    }
  });

  async handleCorrectAnswer() {
    this.onRightAnswer();
  }

  startTask() {
    if (this.args.mode === MODES.TASK) {
      // @ts-expect-error SignalModel — audioFileUrl is a signal record, not a string
      this.audio.startPlayTask([this.audioFileUrl]);
    }
  }

  async handleWrongAnswer() {
    await customTimeout(1000);
    // Stop any in-flight audio so startPlayTask's isBusy guard
    // does not block the replay of the current task's audio.
    await this.audio.stop();
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
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
                  class="btn-press task-player__option-button py-2 px-4 rounded text-purple-primary
                    {{if
                      (eq @activeWord answerOption.word)
                      "border-2 text-white bg-purple-primary"
                      "border-2 border-purple-primary/25 bg-transparent"
                    }}
                    {{if
                      @disableAnswers
                      "opacity-50 cursor-default"
                      "hover:bg-purple-primary hover:text-white hover:border-transparent"
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
                  <canvas aria-label={{answerOption.word}} {{frequencyVisualizer answerOption}}></canvas>
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
