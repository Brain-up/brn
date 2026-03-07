import './index.css';
import Component from '@glimmer/component';
import { set, action } from '@ember/object';
import { service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import deepEqual from 'brn/utils/deep-equal';
import deepCopy from 'brn/utils/deep-copy';
import customTimeout from 'brn/utils/custom-timeout';
import { urlForAudio } from 'brn/utils/file-url';
import { TaskItem } from 'brn/utils/task-item';
import { MODES, type Mode } from 'brn/utils/task-modes';
import { dropTask } from 'ember-concurrency';
import type AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';
import AnswerOption from 'brn/utils/answer-option';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { hash } from '@ember/helper';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { eq } from 'ember-truth-helpers';
import UiTaskContent from 'brn/components/ui/task-content';

export interface PhonemePairsSignature {
  Args: {
    task: any;
    mode: Mode;
    disableAnswers: boolean;
    activeWord: string;
    disableAudioPlayer: boolean;
    onPlayText(word: string): void;
    onRightAnswer(): void;
    onWrongAnswer(params?: { skipRetry: true }): void;
  };
  Blocks: {
    header: [{ tasks: TaskItem[] }];
    footer: [{ audioFileUrl: string | null }];
  };
  Element: HTMLElement;
}

export default class PhonemePairsComponent extends Component<PhonemePairsSignature> {
  @service('audio') declare audio: AudioService;
  @service('stats') declare stats: StatsService;

  @tracked tasksCopy: TaskItem[] = [];
  @tracked isCorrect = false;
  @tracked currentAnswer = '';

  get task() {
    return this.args.task;
  }
  get mode() {
    return this.args.mode;
  }
  get onWrongAnswer() {
    return this.args.onWrongAnswer;
  }
  get onRightAnswer() {
    return this.args.onRightAnswer;
  }
  get uncompletedTasks(): TaskItem[] {
    return this.tasksCopy.filter(
      ({ completedInCurrentCycle }) => completedInCurrentCycle === false,
    );
  }
  get firstUncompletedTask() {
    const item = this.uncompletedTasks[0];
    const words = item?.answer.map((a: { word: string }) => a.word);
    document.body.dataset.correctAnswer = words?.join(',') ?? '';
    return item;
  }

  startNewTask() {
    this.markCompleted(this.firstUncompletedTask as TaskItem);
    this.startTask();
  }
  markCompleted(task: TaskItem) {
    set(task, 'completedInCurrentCycle', true);
    set(task, 'nextAttempt', false);
  }
  markNextAttempt(task: TaskItem) {
    set(task, 'nextAttempt', true);
  }

  async handleCorrectAnswer() {
    await customTimeout(300);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      await customTimeout(3000);
      this.onRightAnswer();
    }
  }

  @action onInsert() {
    this.updateLocalTasks();
    this.startTask();
  }
  @action onTaskChange() {
    this.tasksCopy = [];
    this.updateLocalTasks();
  }

  @action
  async checkAnswer(selectedWord: string) {
    this.showTaskResult.perform(selectedWord);
  }

  willDestroy(): void {
    super.willDestroy();
    if (this.audio.isBusy) {
      this.audio.stop();
    }
    document.body.dataset.correctAnswer = '';
  }

  get audioFileUrl() {
    const task = this.firstUncompletedTask;
    if (!task) {
      return null;
    }
    if (!this.args.task) {
      return null;
    }
    const answer = task.answer[0] as AnswerOption;
    document.body.dataset.correctAnswer = answer.word;
    const useGeneratedUrl =
      this.args.task.usePreGeneratedAudio && answer.audioFileUrl;
    const url = useGeneratedUrl
      ? urlForAudio(answer.audioFileUrl as string | null)
      : this.audio.audioUrlForText(
          task.answer.map((e: AnswerOption) => e.wordPronounce || e.word).join(' '),
        );
    return url;
  }

  get audioFiles(): string[] {
    const url = this.audioFileUrl;
    return url ? [url] : [];
  }

  startTask() {
    this.isCorrect = false;
    if (this.mode === MODES.TASK && this.uncompletedTasks.length > 0) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }

  get isDisabled() {
    return this.args.disableAnswers || this.args.mode === MODES.LISTEN || false;
  }

  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filter((t) => t.completedInCurrentCycle)
      .map((t) => t.order);
    const tasksCopy = deepCopy(this.task.tasksToSolve).map(
      (copy: { order: number }) => {
        const completedInCurrentCycle = completedOrders.includes(copy.order);
        const copyEquivalent = this.tasksCopy.find((t: TaskItem) => t.order === copy.order);
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

  showTaskResult = dropTask(async (selectedWord: string) => {
    this.currentAnswer = selectedWord;

    const isCorrect = deepEqual(
      this.currentAnswer,
      this.firstUncompletedTask?.answer.map((e) => e.word).join(''),
    );

    this.isCorrect = isCorrect;

    if (isCorrect) {
      this.stats.addEvent(StatEvents.RightAnswer);
      await this.handleCorrectAnswer();
    } else {
      this.stats.addEvent(StatEvents.WrongAnswer);
      await this.handleWrongAnswer();
    }

    this.currentAnswer = '';
  });

  async handleWrongAnswer() {
    this.markNextAttempt(this.firstUncompletedTask as TaskItem);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
  }

  <template>
    <div
      class="flex flex-grow flex-col"
      ...attributes
      {{didInsert this.onInsert}}
      {{didUpdate this.onTaskChange @task}}
    >
      {{yield (hash tasks=this.tasksCopy) to="header"}}
      {{#if this.tasksCopy.length}}

        <UiTaskContent>
          <ul
            class="phoneme-pairs__options sm:mx-8 mx-2 mt-2"
            data-test-phoneme-pairs-options
          >
            {{#each @task.answerOptions key="word" as |answerOption|}}
              <li class="phoneme-pairs__option">
                <button
                  data-test-task-answer
                  data-test-task-answer-option={{answerOption.word}}
                  disabled={{this.isDisabled}}
                  type="button"
                  class="phoneme-pairs__option-button btn-press bg-transparent py-2 px-4 rounded
                    {{if
                      (eq @activeWord answerOption.word)
                      "border-2 text-white bg-purple-primary"
                      "border-2 border-purple-primary/25 text-purple-primary"
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
                      (fn this.checkAnswer answerOption.word)
                    )
                  }}
                >
                  {{#if answerOption.pictureFileUrl}}
                    <div class="sm:w-28 w-20 m-auto">
                      <img
                        src={{answerOption.pictureFileUrl}}
                        alt={{answerOption.word}}
                        class="phoneme-pairs__image"
                      />
                    </div>
                  {{/if}}
                  <span class="phoneme-pairs__label">{{answerOption.word}}</span>
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
