import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import { LinkTo } from '@ember/routing';
import UiConfirmDialog from 'brn/components/ui/confirm-dialog';
import type { Headphone } from 'brn/schemas/headphone';
import type IntlService from 'ember-intl/services/intl';
import type NetworkService from 'brn/services/network';
import type Router from '@ember/routing/router-service';

interface AudiometryTask {
  id: string;
  frequencyZone?: number;
  audiometryGroup?: string;
}

interface AudiometryTestData {
  id: string;
  name: string;
  description: string;
  audiometryType: string;
  audiometryTasks: AudiometryTask[];
}

type TestPhase = 'setup' | 'testing' | 'results';

interface AudiometryTestPlayerSignature {
  Args: {
    test: AudiometryTestData;
    headphones: Headphone[];
  };
  Element: HTMLElement;
}

export default class AudiometryTestPlayerComponent extends Component<AudiometryTestPlayerSignature> {
  @service('intl') intl!: IntlService;
  @service('network') network!: NetworkService;
  @service('router') router!: Router;

  @tracked phase: TestPhase = 'setup';
  @tracked selectedHeadphoneId = '';
  @tracked currentTaskIndex = 0;
  @tracked rightAnswers = 0;
  @tracked startTime = '';
  @tracked error = '';
  @tracked showAbandonConfirm = false;

  get tasks(): AudiometryTask[] {
    return this.args.test.audiometryTasks || [];
  }

  get totalTasks(): number {
    return this.tasks.length;
  }

  get currentTask(): AudiometryTask | undefined {
    return this.tasks[this.currentTaskIndex];
  }

  get progressPercent(): number {
    if (this.totalTasks === 0) return 0;
    return Math.round((this.currentTaskIndex / this.totalTasks) * 100);
  }

  get hasHeadphones(): boolean {
    return this.args.headphones.length > 0;
  }

  get canStart(): boolean {
    return this.hasHeadphones && this.selectedHeadphoneId !== '' && this.totalTasks > 0;
  }

  get accuracy(): number {
    if (this.totalTasks === 0) return 0;
    return Math.round((this.rightAnswers / this.totalTasks) * 100);
  }

  @action
  onHeadphoneSelect(e: Event & { target: HTMLSelectElement }) {
    this.selectedHeadphoneId = e.target.value;
  }

  @action
  startTest() {
    this.phase = 'testing';
    this.currentTaskIndex = 0;
    this.rightAnswers = 0;
    this.startTime = new Date().toISOString();
  }

  @action
  answer(heard: boolean) {
    if (heard) {
      this.rightAnswers++;
    }
    if (this.currentTaskIndex + 1 >= this.totalTasks) {
      this.finishTest();
    } else {
      this.currentTaskIndex++;
    }
  }

  @action
  async finishTest() {
    this.phase = 'results';
    const endTime = new Date().toISOString();
    const executionSeconds = Math.round(
      (new Date(endTime).getTime() - new Date(this.startTime).getTime()) / 1000,
    );

    const firstTask = this.tasks[0];
    if (!firstTask) return;

    try {
      await this.network.postAudiometryHistory({
        audiometryTaskId: String(firstTask.id),
        startTime: this.startTime,
        endTime,
        executionSeconds,
        tasksCount: this.totalTasks,
        rightAnswers: this.rightAnswers,
        headphones: this.selectedHeadphoneId,
      });
    } catch (error: any) {
      this.error = error.message || this.intl.t('audiometry.save_failed');
      console.error('Failed to save audiometry history:', error);
    }
  }

  @action
  backToList() {
    if (this.phase === 'testing') {
      this.showAbandonConfirm = true;
      return;
    }
    this.router.transitionTo('audiometry');
  }

  @action
  confirmAbandon() {
    this.showAbandonConfirm = false;
    this.router.transitionTo('audiometry');
  }

  @action
  cancelAbandon() {
    this.showAbandonConfirm = false;
  }

  <template>
    <div class="max-w-2xl mx-auto p-4 sm:p-6" ...attributes>
      {{#if (isPhase this.phase "setup")}}
        <div class="text-center">
          <h1 class="text-2xl font-bold text-gray-800 mb-4">{{@test.name}}</h1>
          <p class="text-sm text-gray-500 mb-6">{{@test.description}}</p>

          {{#if this.hasHeadphones}}
            <div class="max-w-sm mx-auto mb-6">
              <label class="block mb-2 text-sm font-medium text-gray-700" for="headphone-select">
                {{t "audiometry.select_headphones"}}
              </label>
              <select
                data-test-headphone-select
                id="headphone-select"
                class="focus:ring-indigo-500 focus:border-indigo-500 block w-full px-3 py-2 text-sm border border-gray-300 rounded-md"
                {{on "change" this.onHeadphoneSelect}}
              >
                <option value="">--</option>
                {{#each @headphones as |hp|}}
                  <option value={{hp.id}}>{{hp.name}}</option>
                {{/each}}
              </select>
            </div>

            {{#if (isEqual this.totalTasks 0)}}
              <p class="text-sm text-gray-400">{{t "audiometry.no_tasks"}}</p>
            {{else}}
              <button
                data-test-start-audiometry
                type="button"
                disabled={{isNot this.canStart}}
                class="btn-press px-6 py-3 text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
                {{on "click" this.startTest}}
              >
                {{t "audiometry.start_test"}}
              </button>
            {{/if}}
          {{else}}
            <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <p class="text-sm text-yellow-700">
                {{t "audiometry.no_headphones_warning"}}
                <LinkTo @route="profile" class="underline font-medium">Profile</LinkTo>
              </p>
            </div>
          {{/if}}
        </div>

      {{else if (isPhase this.phase "testing")}}
        <div>
          <div class="flex items-center justify-between mb-4">
            <button
              type="button"
              class="btn-press text-sm text-gray-500 hover:text-gray-700"
              {{on "click" this.backToList}}
            >
              {{t "audiometry.back"}}
            </button>
            <h2 class="text-lg font-semibold text-gray-800">{{@test.name}}</h2>
            <span class="text-sm text-gray-500">
              {{t "audiometry.progress" current=(addOne this.currentTaskIndex) total=this.totalTasks}}
            </span>
          </div>

          <div
            class="w-full bg-gray-200 rounded-full h-2 mb-6"
            role="progressbar"
            aria-valuenow={{this.progressPercent}}
            aria-valuemin={{0}}
            aria-valuemax={{100}}
            aria-label={{t "audiometry.progress" current=(addOne this.currentTaskIndex) total=this.totalTasks}}
          >
            <div
              class="bg-indigo-600 h-2 rounded-full transition-all duration-300"
              style="width: {{this.progressPercent}}%"
            ></div>
          </div>

          <div class="text-center p-8 bg-gray-50 rounded-lg">
            {{#if this.currentTask}}
              <p class="text-lg font-medium text-gray-700 mb-8">
                {{t "audiometry.can_you_hear"}}
              </p>
              <div class="flex justify-center gap-4">
                <button
                  data-test-answer-yes
                  type="button"
                  class="btn-press px-8 py-4 text-lg font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 min-w-[120px]"
                  {{on "click" (fn this.answer true)}}
                >
                  {{t "audiometry.yes"}}
                </button>
                <button
                  data-test-answer-no
                  type="button"
                  class="btn-press px-8 py-4 text-lg font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 min-w-[120px]"
                  {{on "click" (fn this.answer false)}}
                >
                  {{t "audiometry.no"}}
                </button>
              </div>
            {{/if}}
          </div>
        </div>

      {{else}}
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800 mb-4">{{t "audiometry.results_title"}}</h2>

          <div class="inline-flex items-center justify-center w-32 h-32 rounded-full border-4 border-indigo-200 mb-6" role="img" aria-label="{{this.accuracy}}% accuracy">
            <span class="text-3xl font-bold text-indigo-600">{{this.accuracy}}%</span>
          </div>

          <div class="text-sm text-gray-500 mb-2">
            {{this.rightAnswers}} / {{this.totalTasks}}
          </div>

          {{#if this.error}}
            <p class="text-sm text-red-500 mb-4">{{this.error}}</p>
          {{/if}}

          <button
            data-test-back-to-list
            type="button"
            class="btn-press px-6 py-3 text-white bg-indigo-600 rounded-lg hover:bg-indigo-700"
            {{on "click" this.backToList}}
          >
            {{t "audiometry.back"}}
          </button>
        </div>
      {{/if}}

      {{#if this.showAbandonConfirm}}
        <UiConfirmDialog
          @message={{t "audiometry.confirm_abandon"}}
          @onConfirm={{this.confirmAbandon}}
          @onCancel={{this.cancelAbandon}}
        />
      {{/if}}
    </div>
  </template>
}

function isPhase(current: string, expected: string): boolean {
  return current === expected;
}

function isEqual(a: number, b: number): boolean {
  return a === b;
}

function isNot(value: boolean): boolean {
  return !value;
}

function addOne(value: number): number {
  return value + 1;
}
