import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { isTesting } from '@embroider/macros';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import { LinkTo } from '@ember/routing';
import UiConfirmDialog from 'brn/components/ui/confirm-dialog';
import { createAudioContext, createSource, loadAudioFiles } from 'brn/utils/audio-api';
import type { Headphone } from 'brn/schemas/headphone';
import type { AudiometryTask, SignalsTask, SpeechTask, SpeechAnswerOption } from 'brn/schemas/audiometry';
import type IntlService from 'ember-intl/services/intl';
import type NetworkService from 'brn/services/network';
import type Router from '@ember/routing/router-service';

interface AudiometryTestData {
  id: string;
  name: string;
  description: string;
  audiometryType: string;
  audiometryTasks: AudiometryTask[];
}

type TestPhase = 'setup' | 'testing' | 'results';

const TONE_DURATION_SEC = 1.5;
const SIGNAL_VOLUME_DB = 50;

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

  // Shared state
  @tracked phase: TestPhase = 'setup';
  @tracked selectedHeadphoneId = '';
  @tracked startTime = '';
  @tracked error = '';
  @tracked showAbandonConfirm = false;

  // SIGNALS state
  @tracked signalTaskIndex = 0;
  @tracked signalFreqIndex = 0;
  @tracked isPlayingTone = false;
  signalResults: Map<string, { frequency: number; heard: boolean }[]> = new Map();

  // SPEECH state
  @tracked speechTaskIndex = 0;
  @tracked speechRoundIndex = 0;
  @tracked isPlayingAudio = false;
  @tracked speechDisplayWords: SpeechAnswerOption[] = [];
  @tracked speechCorrectWord: SpeechAnswerOption | null = null;
  speechResults: { taskId: string; correct: number; total: number }[] = [];

  _synth: any = null;
  _audioContext: AudioContext | null = null;
  _audioSource: AudioBufferSourceNode | null = null;

  get isSignals(): boolean {
    return this.args.test.audiometryType === 'SIGNALS';
  }

  get isSpeech(): boolean {
    return this.args.test.audiometryType === 'SPEECH';
  }

  get isMatrix(): boolean {
    return this.args.test.audiometryType === 'MATRIX';
  }

  get tasks(): AudiometryTask[] {
    return this.args.test.audiometryTasks || [];
  }

  get signalTasks(): SignalsTask[] {
    return this.tasks as SignalsTask[];
  }

  get speechTasks(): SpeechTask[] {
    return this.tasks as SpeechTask[];
  }

  get hasHeadphones(): boolean {
    return this.args.headphones.length > 0;
  }

  get canStart(): boolean {
    if (this.isMatrix) return false;
    if (!this.hasHeadphones || this.selectedHeadphoneId === '') return false;
    return this.tasks.length > 0;
  }

  // SIGNALS getters
  get currentSignalTask(): SignalsTask | undefined {
    return this.signalTasks[this.signalTaskIndex];
  }

  get currentEar(): string {
    return this.currentSignalTask?.ear ?? '';
  }

  get currentFrequency(): number {
    const task = this.currentSignalTask;
    if (!task) return 0;
    return task.frequencies[this.signalFreqIndex] ?? 0;
  }

  get signalProgress(): string {
    const task = this.currentSignalTask;
    if (!task) return '';
    const totalFreqs = this.signalTasks.reduce((sum, t) => sum + t.frequencies.length, 0);
    let done = 0;
    for (let i = 0; i < this.signalTaskIndex; i++) {
      done += this.signalTasks[i]!.frequencies.length;
    }
    done += this.signalFreqIndex;
    return `${done + 1} / ${totalFreqs}`;
  }

  get signalProgressPercent(): number {
    const totalFreqs = this.signalTasks.reduce((sum, t) => sum + t.frequencies.length, 0);
    if (totalFreqs === 0) return 0;
    let done = 0;
    for (let i = 0; i < this.signalTaskIndex; i++) {
      done += this.signalTasks[i]!.frequencies.length;
    }
    done += this.signalFreqIndex;
    return Math.round((done / totalFreqs) * 100);
  }

  // SPEECH getters
  get currentSpeechTask(): SpeechTask | undefined {
    return this.speechTasks[this.speechTaskIndex];
  }

  get speechProgress(): string {
    const task = this.currentSpeechTask;
    if (!task) return '';
    return `${this.speechRoundIndex + 1} / ${task.count}`;
  }

  get speechProgressPercent(): number {
    const totalRounds = this.speechTasks.reduce((sum, t) => sum + t.count, 0);
    if (totalRounds === 0) return 0;
    let done = 0;
    for (let i = 0; i < this.speechTaskIndex; i++) {
      done += this.speechTasks[i]!.count;
    }
    done += this.speechRoundIndex;
    return Math.round((done / totalRounds) * 100);
  }

  // SIGNALS results for display
  get signalResultsSummary(): { ear: string; heard: number; total: number }[] {
    const results: { ear: string; heard: number; total: number }[] = [];
    for (const task of this.signalTasks) {
      const entries = this.signalResults.get(String(task.id)) || [];
      const heard = entries.filter((e) => e.heard).length;
      results.push({ ear: task.ear, heard, total: task.frequencies.length });
    }
    return results;
  }

  get signalTotalHeard(): number {
    return this.signalResultsSummary.reduce((sum, r) => sum + r.heard, 0);
  }

  get signalTotalFreqs(): number {
    return this.signalResultsSummary.reduce((sum, r) => sum + r.total, 0);
  }

  get signalAccuracy(): number {
    if (this.signalTotalFreqs === 0) return 0;
    return Math.round((this.signalTotalHeard / this.signalTotalFreqs) * 100);
  }

  // SPEECH results for display
  get speechTotalCorrect(): number {
    return this.speechResults.reduce((sum, r) => sum + r.correct, 0);
  }

  get speechTotalRounds(): number {
    return this.speechResults.reduce((sum, r) => sum + r.total, 0);
  }

  get speechAccuracy(): number {
    if (this.speechTotalRounds === 0) return 0;
    return Math.round((this.speechTotalCorrect / this.speechTotalRounds) * 100);
  }

  @action
  onHeadphoneSelect(e: Event & { target: HTMLSelectElement }) {
    this.selectedHeadphoneId = e.target.value;
  }

  @action
  async startTest() {
    this.phase = 'testing';
    this.startTime = new Date().toISOString();
    this.error = '';

    if (this.isSignals) {
      this.signalTaskIndex = 0;
      this.signalFreqIndex = 0;
      this.signalResults = new Map();
      await this.playSignalTone();
    } else if (this.isSpeech) {
      this.speechTaskIndex = 0;
      this.speechRoundIndex = 0;
      this.speechResults = [];
      await this.startSpeechRound();
    }
  }

  // ── SIGNALS ──────────────────────────────────────────────────────────────────

  async playSignalTone() {
    if (isTesting()) {
      return;
    }

    this.isPlayingTone = true;
    try {
      const Tone = await import('tone');
      await Tone.start();

      this.disposeSynth();
      const synth = new Tone.Synth({
        oscillator: { type: 'sine' as const },
      }).toDestination();
      this._synth = synth;

      const freq = this.currentFrequency;
      synth.triggerAttackRelease(freq, TONE_DURATION_SEC, Tone.now(), 0.5);

      await new Promise((resolve) => setTimeout(resolve, TONE_DURATION_SEC * 1000));
    } catch (e) {
      console.error('Failed to play audiometry tone:', e);
    } finally {
      this.disposeSynth();
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlayingTone = false;
      }
    }
  }

  disposeSynth() {
    if (this._synth) {
      try {
        this._synth.dispose();
      } catch (_e) {
        // synth may already be disposed
      }
      this._synth = null;
    }
  }

  @action
  async answerSignal(heard: boolean) {
    if (this.isPlayingTone) return;

    const task = this.currentSignalTask;
    if (!task) return;

    const taskId = String(task.id);
    if (!this.signalResults.has(taskId)) {
      this.signalResults.set(taskId, []);
    }
    this.signalResults.get(taskId)!.push({
      frequency: this.currentFrequency,
      heard,
    });

    // Advance to next frequency
    if (this.signalFreqIndex + 1 < task.frequencies.length) {
      this.signalFreqIndex++;
      await this.playSignalTone();
    } else {
      // Next ear/task
      if (this.signalTaskIndex + 1 < this.signalTasks.length) {
        this.signalTaskIndex++;
        this.signalFreqIndex = 0;
        await this.playSignalTone();
      } else {
        await this.finishSignalsTest();
      }
    }
  }

  async finishSignalsTest() {
    this.disposeAllAudio();
    this.phase = 'results';
    const endTime = new Date().toISOString();
    const executionSeconds = Math.round(
      (new Date(endTime).getTime() - new Date(this.startTime).getTime()) / 1000,
    );

    for (const task of this.signalTasks) {
      const entries = this.signalResults.get(String(task.id)) || [];
      const sinAudiometryResults: Record<number, number> = {};
      for (const entry of entries) {
        if (entry.heard) {
          sinAudiometryResults[entry.frequency] = SIGNAL_VOLUME_DB;
        }
      }
      const heardCount = entries.filter((e) => e.heard).length;

      try {
        await this.network.postAudiometryHistory({
          audiometryTaskId: String(task.id),
          startTime: this.startTime,
          endTime,
          executionSeconds,
          tasksCount: task.frequencies.length,
          rightAnswers: heardCount,
          headphones: this.selectedHeadphoneId,
          sinAudiometryResults,
        });
      } catch (error: any) {
        this.error = error.message || this.intl.t('audiometry.save_failed');
        console.error('Failed to save audiometry history:', error);
      }
    }
  }

  // ── SPEECH ───────────────────────────────────────────────────────────────────

  audioUrlForWord(word: string, locale: string): string {
    return (
      window.location.protocol + '//' + window.location.host +
      `/api/audio?text=${encodeURIComponent(word)}&locale=${encodeURIComponent(locale)}&exerciseId=0`
    );
  }

  async startSpeechRound() {
    const task = this.currentSpeechTask;
    if (!task) return;

    // Pick a random correct word for this round
    const options = task.answerOptions;
    const correctIndex = Math.floor(Math.random() * options.length);
    const correctWord = options[correctIndex]!;
    this.speechCorrectWord = correctWord;

    // Select showSize words to display (including the correct one), shuffled
    const others = options.filter((o) => o.word !== correctWord.word);
    const picked = [correctWord, ...shuffleArray(others).slice(0, task.showSize - 1)];
    this.speechDisplayWords = shuffleArray(picked);

    // Play the word audio via TTS endpoint
    const locale = correctWord.locale ?? this.intl.primaryLocale;
    await this.playSpeechAudio(this.audioUrlForWord(correctWord.word, locale));
  }

  async playSpeechAudio(url: string) {
    if (isTesting()) {
      return;
    }

    this.isPlayingAudio = true;
    try {
      if (!this._audioContext || this._audioContext.state === 'closed') {
        this._audioContext = createAudioContext();
      }
      const context = this._audioContext;
      const getToken = () => this.network.token;
      const buffers = await loadAudioFiles(context, [url], getToken);
      const buffer = buffers[0];
      if (!buffer) {
        console.error('Failed to load audio:', url);
        return;
      }
      const { source } = createSource(context, buffer);
      this._audioSource = source;
      source.start(0);

      await new Promise<void>((resolve) => {
        source.onended = () => resolve();
      });
    } catch (e) {
      console.error('Failed to play speech audio:', e);
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlayingAudio = false;
      }
    }
  }

  @action
  async replaySpeechWord() {
    if (this.isPlayingAudio) return;
    const word = this.speechCorrectWord;
    if (!word) return;
    const locale = word.locale ?? this.intl.primaryLocale;
    await this.playSpeechAudio(this.audioUrlForWord(word.word, locale));
  }

  @action
  async answerSpeech(option: SpeechAnswerOption) {
    if (this.isPlayingAudio) return;

    const task = this.currentSpeechTask;
    if (!task || !this.speechCorrectWord) return;

    const correct = option.word === this.speechCorrectWord.word;
    const taskId = String(task.id);

    // Find or create result entry for this task
    let resultEntry = this.speechResults.find((r) => r.taskId === taskId);
    if (!resultEntry) {
      resultEntry = { taskId, correct: 0, total: 0 };
      this.speechResults.push(resultEntry);
    }
    resultEntry.total++;
    if (correct) {
      resultEntry.correct++;
    }

    // Advance round
    if (this.speechRoundIndex + 1 < task.count) {
      this.speechRoundIndex++;
      await this.startSpeechRound();
    } else {
      // Next task
      if (this.speechTaskIndex + 1 < this.speechTasks.length) {
        this.speechTaskIndex++;
        this.speechRoundIndex = 0;
        await this.startSpeechRound();
      } else {
        await this.finishSpeechTest();
      }
    }
  }

  async finishSpeechTest() {
    this.phase = 'results';
    const endTime = new Date().toISOString();
    const executionSeconds = Math.round(
      (new Date(endTime).getTime() - new Date(this.startTime).getTime()) / 1000,
    );

    for (const result of this.speechResults) {
      try {
        await this.network.postAudiometryHistory({
          audiometryTaskId: result.taskId,
          startTime: this.startTime,
          endTime,
          executionSeconds,
          tasksCount: result.total,
          rightAnswers: result.correct,
          headphones: this.selectedHeadphoneId,
        });
      } catch (error: any) {
        this.error = error.message || this.intl.t('audiometry.save_failed');
        console.error('Failed to save audiometry history:', error);
      }
    }
  }

  // ── Shared ───────────────────────────────────────────────────────────────────

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
    this.disposeAllAudio();
    this.showAbandonConfirm = false;
    this.router.transitionTo('audiometry');
  }

  @action
  cancelAbandon() {
    this.showAbandonConfirm = false;
  }

  disposeAllAudio() {
    this.disposeSynth();
    if (this._audioSource) {
      try { this._audioSource.stop(); } catch (_e) { /* already stopped */ }
      this._audioSource = null;
    }
    if (this._audioContext) {
      try { this._audioContext.close(); } catch (_e) { /* already closed */ }
      this._audioContext = null;
    }
  }

  willDestroy() {
    super.willDestroy();
    this.disposeAllAudio();
  }

  <template>
    <div class="max-w-2xl mx-auto p-4 sm:p-6" ...attributes>
      {{#if (isPhase this.phase "setup")}}
        <div class="text-center">
          <h1 class="text-2xl font-bold text-gray-800 mb-4">{{@test.name}}</h1>
          <p class="text-sm text-gray-500 mb-6">{{@test.description}}</p>

          {{#if this.isMatrix}}
            <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg" data-test-matrix-unavailable>
              <p class="text-sm text-yellow-700">
                {{t "audiometry.matrix_coming_soon"}}
              </p>
            </div>
          {{else if this.hasHeadphones}}
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

            {{#if (isEqual (taskCount this.tasks) 0)}}
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
              {{#if this.isSignals}}
                {{this.signalProgress}}
              {{else}}
                {{this.speechProgress}}
              {{/if}}
            </span>
          </div>

          <div
            class="w-full bg-gray-200 rounded-full h-2 mb-6"
            role="progressbar"
            aria-valuenow={{if this.isSignals this.signalProgressPercent this.speechProgressPercent}}
            aria-valuemin={{0}}
            aria-valuemax={{100}}
          >
            <div
              class="bg-indigo-600 h-2 rounded-full transition-all duration-300"
              style="width: {{if this.isSignals this.signalProgressPercent this.speechProgressPercent}}%"
            ></div>
          </div>

          {{#if this.isSignals}}
            {{! ── SIGNALS testing UI ── }}
            <div class="text-center p-8 bg-gray-50 rounded-lg">
              <p class="text-sm font-medium text-indigo-600 mb-2" data-test-ear-label>
                {{t "audiometry.ear_testing" ear=(earLabel this.currentEar)}}
              </p>
              <p class="text-xs text-gray-400 mb-2" data-test-frequency-info>
                {{t "audiometry.frequency" freq=this.currentFrequency}}
              </p>

              {{#if this.isPlayingTone}}
                <div class="mb-8" data-test-playing-indicator>
                  <div class="inline-flex items-center gap-2 text-indigo-600">
                    <svg class="animate-pulse w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
                    </svg>
                    <span class="text-lg font-medium">{{t "audiometry.playing"}}</span>
                  </div>
                </div>
              {{else}}
                <p class="text-lg font-medium text-gray-700 mb-8">
                  {{t "audiometry.can_you_hear"}}
                </p>
              {{/if}}

              <div class="flex justify-center gap-4">
                <button
                  data-test-answer-yes
                  type="button"
                  disabled={{this.isPlayingTone}}
                  class="btn-press px-8 py-4 text-lg font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed min-w-[120px]"
                  {{on "click" (fn this.answerSignal true)}}
                >
                  {{t "audiometry.yes"}}
                </button>
                <button
                  data-test-answer-no
                  type="button"
                  disabled={{this.isPlayingTone}}
                  class="btn-press px-8 py-4 text-lg font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed min-w-[120px]"
                  {{on "click" (fn this.answerSignal false)}}
                >
                  {{t "audiometry.no"}}
                </button>
              </div>
            </div>

          {{else if this.isSpeech}}
            {{! ── SPEECH testing UI ── }}
            <div class="text-center p-8 bg-gray-50 rounded-lg">
              {{#if this.currentSpeechTask.frequencyZone}}
                <p class="text-xs text-gray-400 mb-2">
                  {{t "audiometry.speech_zone" zone=this.currentSpeechTask.frequencyZone}}
                </p>
              {{/if}}

              <p class="text-lg font-medium text-gray-700 mb-4">
                {{t "audiometry.speech_select_word"}}
              </p>

              <button
                data-test-replay-word
                type="button"
                disabled={{this.isPlayingAudio}}
                class="btn-press mb-6 px-4 py-2 text-sm text-indigo-600 border border-indigo-300 rounded-lg hover:bg-indigo-50 disabled:opacity-50 disabled:cursor-not-allowed"
                {{on "click" this.replaySpeechWord}}
              >
                {{t "audiometry.replay"}}
              </button>

              <div class="grid grid-cols-3 gap-3 max-w-md mx-auto">
                {{#each this.speechDisplayWords as |option|}}
                  <button
                    data-test-speech-word
                    type="button"
                    disabled={{this.isPlayingAudio}}
                    class="btn-press px-4 py-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-indigo-50 hover:border-indigo-300 disabled:opacity-50 disabled:cursor-not-allowed"
                    {{on "click" (fn this.answerSpeech option)}}
                  >
                    {{option.word}}
                  </button>
                {{/each}}
              </div>
            </div>
          {{/if}}
        </div>

      {{else}}
        {{! ── Results ── }}
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800 mb-4">{{t "audiometry.results_title"}}</h2>

          {{#if this.isSignals}}
            <div class="inline-flex items-center justify-center w-32 h-32 rounded-full border-4 border-indigo-200 mb-6" role="img" aria-label="{{this.signalAccuracy}}% accuracy">
              <span class="text-3xl font-bold text-indigo-600">{{this.signalAccuracy}}%</span>
            </div>

            <div class="space-y-2 mb-6">
              {{#each this.signalResultsSummary as |r|}}
                <p class="text-sm text-gray-500" data-test-signal-ear-result>
                  {{earLabel r.ear}}: {{t "audiometry.heard_count" heard=r.heard total=r.total}}
                </p>
              {{/each}}
            </div>

          {{else if this.isSpeech}}
            <div class="inline-flex items-center justify-center w-32 h-32 rounded-full border-4 border-indigo-200 mb-6" role="img" aria-label="{{this.speechAccuracy}}% accuracy">
              <span class="text-3xl font-bold text-indigo-600">{{this.speechAccuracy}}%</span>
            </div>

            <div class="text-sm text-gray-500 mb-2">
              {{this.speechTotalCorrect}} / {{this.speechTotalRounds}}
            </div>
          {{/if}}

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

function taskCount(tasks: AudiometryTask[]): number {
  return tasks.length;
}

function earLabel(ear: string): string {
  return ear;
}

function shuffleArray<T>(array: T[]): T[] {
  const result = [...array];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j]!, result[i]!];
  }
  return result;
}
