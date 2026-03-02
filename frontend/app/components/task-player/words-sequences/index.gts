import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { set, action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import deepCopy from 'brn/utils/deep-copy';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import deepEqual from 'brn/utils/deep-equal';
import customTimeout from 'brn/utils/custom-timeout';
import { TaskItem } from 'brn/utils/task-item';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { MODES, type Mode } from 'brn/utils/task-modes';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { task, Task as TaskGenerator } from 'ember-concurrency';
import type AudioService from 'brn/services/audio';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import StatsService, { StatEvents } from 'brn/services/stats';
import type { TaskWordsSequences as WordsSequences } from 'brn/schemas/task/words-sequences';
import type { TaskBase as Task } from 'brn/schemas/task';
import type AnswerOption from 'brn/utils/answer-option';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import { hash } from '@ember/helper';
import { fn } from '@ember/helper';
import { get } from '@ember/helper';
import { or } from 'ember-truth-helpers';
import { eq } from 'ember-truth-helpers';
import { not } from 'ember-truth-helpers';
import arrayLast from 'brn/helpers/array-last';
import UiTaskContent from 'brn/components/ui/task-content';
import TextImageButton from 'brn/components/text-image-button';

function getEmptyTemplate(
  selectedItemsOrder: string[] = [],
): Record<string, null> {
  return selectedItemsOrder.reduce((result, currentKey) => {
    result[currentKey] = null;
    return result;
  }, {} as Record<string, null>);
}

export interface WordsSequencesSignature<T> {
  Args: {
  task: T;
  mode: Mode;
  disableAnswers: boolean;
  activeWord: string;
  disableAudioPlayer: boolean;
  onPlayText(): void;
  onRightAnswer(): void;
  onWrongAnswer(params?: { skipRetry: true }): void;
  };
  Element: HTMLElement;
}

export default class WordsSequencesComponent<
  T extends Task = WordsSequences,
> extends Component<WordsSequencesSignature<T>> {
  @action onInsert() {
    this.updateLocalTasks();
    this.startTask();
  }
  @service audio!: AudioService;
  @service stats!: StatsService;
  @tracked tasksCopy: TaskItem[] = [];
  @tracked currentAnswerObject: null | Record<string, string | null> = null;
  @tracked isCorrect = false;
  @tracked correctnessPerType: Record<string, boolean> = {};
  get task(): T {
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
  willDestroy(): void {
    super.willDestroy();
    document.body.dataset.correctAnswer = '';
  }
  get firstUncompletedTask() {
    const item = this.uncompletedTasks[0];
    const words = item?.answer.map((a: { word: string }) => a.word);
    document.body.dataset.correctAnswer = words?.join(',') ?? '';
    return item;
  }
  get audioFiles() {
    if (!this.firstUncompletedTask) {
      return [];
    }

    const text = this.firstUncompletedTask.answer
      .map(({ wordPronounce }) => wordPronounce)
      .join(' ');
    return [this.audio.audioUrlForText(text)];
  }
  get answerCompleted() {
    return Object.values(this.currentAnswerObject as any).reduce(
      (isCompleted, currentValue) => {
        isCompleted = isCompleted && !!currentValue;
        return isCompleted;
      },
      true,
    );
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
  startTask() {
    this.isCorrect = false;
    this.correctnessPerType = {};
    const wsTask = this.task as unknown as WordsSequences;
    this.currentAnswerObject = getEmptyTemplate(wsTask.selectedItemsOrder);
    if (this.mode === MODES.TASK) {
      this.audio.startPlayTask(this.audioFiles);
    }
  }
  updateLocalTasks() {
    const completedOrders = this.tasksCopy
      .filter((t) => t.completedInCurrentCycle)
      .map((t) => t.order);
    const wsTask = this.task as unknown as WordsSequences;
    const tasksCopy: TaskItem[] = deepCopy(wsTask.tasksToSolve).map(
      (copy: { order: number }) => {
        const completedInCurrentCycle = completedOrders.includes(copy.order);
        const copyEquivalent = this.tasksCopy.find((t) => t.order === copy.order);
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

  @(task(function* (
    this: WordsSequencesComponent,
    selected: { wordType: string; word: string },
  ) {
    this.currentAnswerObject = {
      ...(this.currentAnswerObject || {}),
      [selected.wordType]: selected.word,
    };
    if (this.answerCompleted) {
      const correctAnswerWords = this.firstUncompletedTask?.answer.map((a: { word: string }) => a.word) || [];
      const wsTask = this.task as unknown as WordsSequences;
      const userAnswerWords = wsTask.selectedItemsOrder.map(
        (orderName: string) =>
          (this.currentAnswerObject as any)[orderName] as string,
      );
      const isCorrect = deepEqual(userAnswerWords, correctAnswerWords);

      const correctnessPerType: Record<string, boolean> = {};
      wsTask.selectedItemsOrder.forEach((orderName: string, index: number) => {
        correctnessPerType[orderName] = userAnswerWords[index] === correctAnswerWords[index];
      });
      this.correctnessPerType = correctnessPerType;

      this.isCorrect = isCorrect;

      if (isCorrect) {
        this.stats.addEvent(StatEvents.RightAnswer);
        yield this.handleCorrectAnswer();
      } else {
        this.stats.addEvent(StatEvents.WrongAnswer);
        yield this.handleWrongAnswer();
      }
    }
  }).drop())
  showTaskResult!: TaskGenerator<any, any>;

  @action
  async checkMaybe(selectedData: AnswerOption) {
    this.showTaskResult.perform(selectedData);
  }

  async handleWrongAnswer() {
    const wsTask = this.task as unknown as WordsSequences;
    wsTask.wrongAnswers.push(this.firstUncompletedTask?.serialize());
    this.markNextAttempt(this.firstUncompletedTask as TaskItem);
    await customTimeout(300);
    this.startTask();
    this.onWrongAnswer({ skipRetry: true });
  }

  async handleCorrectAnswer() {
    await customTimeout(300);
    this.startNewTask();
    if (!this.firstUncompletedTask) {
      await customTimeout(3000);
      this.onRightAnswer();
    }
  }

  <template>
    <div
      class="flex-1 flex flex-col"
      ...attributes
      {{didInsert this.onInsert}}
    >
      {{yield (hash tasks=this.tasksCopy) to="header"}}
      {{#if this.tasksCopy.length}}
    
        <UiTaskContent>
          <div class="words-sequences-columns flex justify-center flex-grow mt-4">
            {{#each @task.selectedItemsOrder as |type|}}
              <div
                class="column flex-1 flex-col type-column
                  {{if (not (eq (arrayLast @task.selectedItemsOrder) type)) "mr-2"}}"
              >
                {{#each (get @task.answerOptions type) as |answerOption|}}
                  <TextImageButton
                    @checked={{this.answerCompleted}}
                    @isCorrect={{get this.correctnessPerType type}}
                    @disabled={{@disableAnswers}}
                    @isSelected={{or
                      (eq @activeWord answerOption.word)
                      (eq (get this.currentAnswerObject type) answerOption.word)
                    }}
                    @clickAction={{if
                      (eq @mode "interact")
                      (fn @onPlayText answerOption.word)
                      (fn this.checkMaybe answerOption)
                    }}
                    @pictureFileUrl={{answerOption.pictureFileUrl}}
                    @word={{answerOption.word}}
                  />
                {{/each}}
              </div>
            {{/each}}
          </div>
        </UiTaskContent>
    
        {{yield (hash audioFileUrl=this.audioFiles) to="footer"}}
      {{/if}}
    </div>
  </template>
}
