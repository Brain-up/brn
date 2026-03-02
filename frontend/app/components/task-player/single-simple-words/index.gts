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
import { task, Task as TaskGenerator } from 'ember-concurrency';
import type AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from 'brn/services/stats';
import AnswerOption from 'brn/utils/answer-option';
import type { TaskSingleSimpleWords as SingleSimpleWordTask } from 'brn/schemas/task/single-simple-words';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { hash } from '@ember/helper';
import { t } from 'ember-intl';
import UiTaskContent from 'brn/components/ui/task-content';
import TaskPlayerSingleSimpleWordsOption from 'brn/components/task-player/single-simple-words/option';

export interface WordsSequencesSignature<T> {
  Args: {
  task: T;
  mode: Mode;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(word: string): void;
  onRightAnswer(): void;
  onWrongAnswer(params?: { skipRetry: true }): void;
  };
  Element: HTMLElement;
}

export default class SingleSimpleWordsComponent extends Component<WordsSequencesSignature<SingleSimpleWordTask>> {
  // --- Services (from WordsSequencesComponent) ---
  @service audio!: AudioService;
  @service stats!: StatsService;

  // --- Tracked properties (from WordsSequencesComponent) ---
  @tracked tasksCopy: TaskItem[] = [];
  @tracked isCorrect = false;

  // --- Own tracked properties ---
  @tracked currentAnswer: string[] = [];

  // --- Getters from WordsSequencesComponent ---
  get task(): SingleSimpleWordTask {
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

  // --- Methods from WordsSequencesComponent ---
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

  // --- Actions from WordsSequencesComponent ---
  @action onInsert() {
    this.updateLocalTasks();
    this.startTask();
  }

  @action
  async checkMaybe(selectedData: string) {
    this.showTaskResult.perform(selectedData);
  }

  // --- Own overrides ---
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
  // Overrides parent WordsSequencesComponent.audioFiles to use single audio URL
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
  get sortedAnswerOptions() {
    const opts = this.task.answerOptions;
    // If all options have unassigned columns (-1), skip column sorting entirely
    if (opts.every((o: AnswerOption) => (o.columnNumber ?? 0) < 0)) {
      return opts;
    }
    type Answers = typeof this.task.answerOptions;
    const acc: Record<string, Answers> = {};
    const groupedOptions = opts.reduce((acc: Record<string, Answers>, option: AnswerOption) => {
      const colId = (option.columnNumber ?? 0).toString();
      if (!acc[colId]) {
        acc[colId] = [];
      }
      acc[colId].push(option);
      return acc;
    }, acc);
    const groupIds = Object.keys(groupedOptions)
      .map((el) => parseInt(el))
      .sort();
    const amountOfColumns = groupIds.length;
    if (this.amountOfColumns !== amountOfColumns) {
      console.warn(
        `Incorrect amount of group options resolved: ${this.amountOfColumns} vs ${amountOfColumns}`,
      );
      return opts;
    }
    const itemsPerGroup = groupedOptions[groupIds[0].toString()].length;

    if (groupIds.some((el) => groupedOptions[el].length !== itemsPerGroup)) {
      console.warn(`Incorrect amount of options per groups`);
      return opts;
    }
    const results: Answers = [];

    for (let i = 0; i < itemsPerGroup; i++) {
      groupIds.forEach((id) => {
        const option = groupedOptions[id].shift();
        if (option) {
          results.push(option);
        }
      });
    }

    return results;
  }
  get amountOfColumns() {
    const cols = this.task.exercise.wordsColumns;
    const optCount = this.task.answerOptions?.length ?? 0;
    // Don't use more columns than there are options
    return optCount > 0 ? Math.min(cols, optCount) : cols;
  }
  get showTip() {
    if (!this.firstUncompletedTask) {
      return;
    }
    return (
      this.mode === MODES.TASK && this.firstUncompletedTask?.answer.length > 1
    );
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

  @(task(function* (this: SingleSimpleWordsComponent, selected: string) {
    this.currentAnswer = [...this.currentAnswer, selected].filter(
      (e) => e.length,
    );

    if (
      this.currentAnswer.length !== this.firstUncompletedTask?.answer.length
    ) {
      return;
    }

    const isCorrect = deepEqual(
      this.currentAnswer.join(''),
      this.firstUncompletedTask.answer.map((e) => e.word).join(''),
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
    this.markNextAttempt(this.firstUncompletedTask as TaskItem);
    this.updateLocalTasks();
    await customTimeout(1000);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
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
          <ul class="task-player__options sm:mx-8 mx-2 mt-2">
            {{#each this.sortedAnswerOptions key="word" as |answerOption|}}
              <TaskPlayerSingleSimpleWordsOption
                data-cols={{this.amountOfColumns}}
                class="flex"
                @activeWord={{@activeWord}}
                @answerOption={{answerOption}}
                @checkMaybe={{this.checkMaybe}}
                @disableAnswers={{@disableAnswers}}
                @mode={{@mode}}
                @isCorrect={{this.isCorrect}}
                @onPlayText={{@onPlayText}}
              />
            {{/each}}
          </ul>

          {{#if this.showTip}}
            <div
              class="sm:mx-8 sm:mt-8 flex self-end px-4 py-3 mx-2 mt-4 text-sm leading-normal text-blue-700 bg-blue-100 rounded-md"
              role="alert"
            >
              {{t
                "audio_player.selected_words_of"
                total=this.firstUncompletedTask.answer.length
                selected=this.currentAnswer.length
              }}
            </div>
          {{/if}}
        </UiTaskContent>

        {{yield (hash audioFileUrl=this.audioFileUrl) to="footer"}}
      {{/if}}
    </div>
  </template>
}
