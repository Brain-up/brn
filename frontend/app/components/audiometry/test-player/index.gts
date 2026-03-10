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
import Audiogram from 'brn/components/audiometry/audiogram';
import { createAudioContext, createSource, createNoizeBuffer, loadAudioFiles } from 'brn/utils/audio-api';
import {
  createThresholdState,
  processResponse,
  getNextDB,
  dBHLtoToneDB,
  getMaskingLevel,
  classifyHearing,
  calculatePTA,
  INITIAL_DB,
  MAX_DB,
} from 'brn/utils/audiometry-algorithm';
import { saveHistoryEntry, generateEntryId } from 'brn/utils/audiometry-history-storage';
import type { ThresholdState } from 'brn/utils/audiometry-algorithm';
import type { Headphone } from 'brn/schemas/headphone';
import type { AudiometryTask, SignalsTask, SpeechTask, SpeechAnswerOption } from 'brn/schemas/audiometry';
import type IntlService from 'ember-intl/services/intl';
import type NetworkService from 'brn/services/network';
import type UserDataService from 'brn/services/user-data';
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
  @service('user-data') userData!: UserDataService;
  @service('router') router!: Router;

  // Shared state
  @tracked phase: TestPhase = 'setup';
  @tracked selectedHeadphoneId = '';
  @tracked startTime = '';
  @tracked error = '';
  @tracked showAbandonConfirm = false;

  // SIGNALS state — adaptive threshold
  @tracked signalTaskIndex = 0;
  @tracked signalFreqIndex = 0;
  @tracked isPlayingTone = false;
  @tracked thresholdStates: Map<string, ThresholdState> = new Map();
  @tracked currentTrialDB: number = INITIAL_DB;

  // SPEECH state
  @tracked speechTaskIndex = 0;
  @tracked speechRoundIndex = 0;
  @tracked isPlayingAudio = false;
  @tracked speechDisplayWords: SpeechAnswerOption[] = [];
  @tracked speechCorrectWord: SpeechAnswerOption | null = null;
  @tracked speechResults: { taskId: string; correct: number; total: number }[] = [];

  _isProcessing = false;
  _synth: any = null;
  _panner: any = null;
  _audioContext: AudioContext | null = null;
  _audioSource: AudioBufferSourceNode | null = null;
  _maskingSource: AudioBufferSourceNode | null = null;

  get currentUserId(): string | undefined {
    return this.userData.userModel?.id;
  }

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

  get autoSelectedHeadphoneId(): string {
    if (this.args.headphones.length === 1) {
      return String(this.args.headphones[0]!.id ?? '');
    }
    return '';
  }

  get effectiveHeadphoneId(): string {
    return this.selectedHeadphoneId || this.autoSelectedHeadphoneId;
  }

  get canStart(): boolean {
    if (this.isMatrix) return false;
    if (!this.hasHeadphones || this.effectiveHeadphoneId === '') return false;
    return this.tasks.length > 0;
  }

  // SIGNALS getters
  get currentSignalTask(): SignalsTask | undefined {
    return this.signalTasks[this.signalTaskIndex];
  }

  get currentEar(): string {
    return this.currentSignalTask?.ear ?? '';
  }

  get currentEarLabel(): string {
    return this.earLabelFor(this.currentEar);
  }

  get currentFrequency(): number {
    const task = this.currentSignalTask;
    if (!task) return 0;
    return task.frequencies[this.signalFreqIndex] ?? 0;
  }

  get currentThresholdKey(): string {
    const task = this.currentSignalTask;
    if (!task) return '';
    return `${task.id}:${this.currentFrequency}`;
  }

  get currentTrialPercent(): number {
    return Math.round((this.currentTrialDB / MAX_DB) * 100);
  }

  get signalProgress(): string {
    const totalFreqs = this.signalTasks.reduce((sum, t) => sum + t.frequencies.length, 0);
    const completedFreqs = [...this.thresholdStates.values()].filter((s) => s.isComplete).length;
    const current = Math.min(completedFreqs + 1, totalFreqs);
    return `${current} / ${totalFreqs}`;
  }

  get signalProgressPercent(): number {
    const totalFreqs = this.signalTasks.reduce((sum, t) => sum + t.frequencies.length, 0);
    if (totalFreqs === 0) return 0;
    const completedFreqs = [...this.thresholdStates.values()].filter((s) => s.isComplete).length;
    return Math.round((completedFreqs / totalFreqs) * 100);
  }

  // SPEECH getters
  get currentSpeechTask(): SpeechTask | undefined {
    return this.speechTasks[this.speechTaskIndex];
  }

  get currentSpeechFrequencyZone(): string | undefined {
    return this.currentSpeechTask?.frequencyZone;
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

  earLabelFor(ear: string): string {
    if (ear === 'LEFT') return this.intl.t('audiometry.ear_left');
    if (ear === 'RIGHT') return this.intl.t('audiometry.ear_right');
    return ear;
  }

  // ── SIGNALS results for display ──────────────────────────────────────────────

  get leftEarThresholds(): Record<number, number> {
    return this.getThresholdsForEar('LEFT');
  }

  get rightEarThresholds(): Record<number, number> {
    return this.getThresholdsForEar('RIGHT');
  }

  private getThresholdsForEar(ear: string): Record<number, number> {
    const result: Record<number, number> = {};
    for (const task of this.signalTasks) {
      if (task.ear !== ear) continue;
      for (const freq of task.frequencies) {
        const state = this.thresholdStates.get(`${task.id}:${freq}`);
        if (state?.threshold !== null && state?.threshold !== undefined) {
          result[freq] = state.threshold;
        }
      }
    }
    return result;
  }

  get signalResultsSummary(): { ear: string; earLabel: string; frequencies: { freq: number; threshold: number | null }[] }[] {
    const results: { ear: string; earLabel: string; frequencies: { freq: number; threshold: number | null }[] }[] = [];
    for (const task of this.signalTasks) {
      const frequencies: { freq: number; threshold: number | null }[] = [];
      for (const freq of task.frequencies) {
        const state = this.thresholdStates.get(`${task.id}:${freq}`);
        frequencies.push({ freq, threshold: state?.threshold ?? null });
      }
      results.push({ ear: task.ear, earLabel: this.earLabelFor(task.ear), frequencies });
    }
    return results;
  }

  get ptaLeft(): number | null {
    return calculatePTA(this.leftEarThresholds);
  }

  get ptaRight(): number | null {
    return calculatePTA(this.rightEarThresholds);
  }

  get hearingClassificationLeft(): string | null {
    if (this.ptaLeft === null) return null;
    return classifyHearing(this.ptaLeft);
  }

  get hearingClassificationRight(): string | null {
    if (this.ptaRight === null) return null;
    return classifyHearing(this.ptaRight);
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
      this.thresholdStates = new Map();
      this.currentTrialDB = INITIAL_DB;
      await this.playSignalTone(this.currentTrialDB);
    } else if (this.isSpeech) {
      this.speechTaskIndex = 0;
      this.speechRoundIndex = 0;
      this.speechResults = [];
      await this.startSpeechRound();
    }
  }

  // ── SIGNALS ──────────────────────────────────────────────────────────────────

  async playSignalTone(dBLevel: number) {
    if (isTesting()) {
      return;
    }

    this.isPlayingTone = true;
    try {
      const Tone = await import('tone');
      await Tone.start();

      this.disposeSynth();

      // Create synth with volume mapped to dB level
      const synth = new Tone.Synth({
        oscillator: { type: 'sine' as const },
      });
      synth.volume.value = dBHLtoToneDB(dBLevel);
      this._synth = synth;

      // Stereo panning: route to correct ear
      const panValue = this.currentEar === 'LEFT' ? -1 : this.currentEar === 'RIGHT' ? 1 : 0;
      const panner = new Tone.Panner(panValue).toDestination();
      synth.connect(panner);
      this._panner = panner;

      // Start contralateral masking noise
      if (this.currentEar !== 'BOTH' && this.currentEar !== '') {
        await this.startMaskingNoise(panValue * -1, getMaskingLevel(dBLevel));
      }

      const freq = this.currentFrequency;
      synth.triggerAttackRelease(freq, TONE_DURATION_SEC, Tone.now());

      await new Promise((resolve) => setTimeout(resolve, TONE_DURATION_SEC * 1000));
    } catch (e) {
      console.error('Failed to play audiometry tone:', e);
    } finally {
      this.disposeSynth();
      this.stopMaskingNoise();
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlayingTone = false;
      }
    }
  }

  async startMaskingNoise(panValue: number, levelDB: number) {
    this.stopMaskingNoise();
    try {
      if (!this._audioContext || this._audioContext.state === 'closed') {
        this._audioContext = createAudioContext();
      }
      const ctx = this._audioContext;
      // createNoizeBuffer already applies `level * 0.01` internally,
      // so pass the 0-100 scale level and set gainNode to 1.0 (no double-scaling).
      const noiseLevel = Math.round((levelDB / MAX_DB) * 100);
      const noiseBuffer = createNoizeBuffer(ctx, TONE_DURATION_SEC + 0.5, noiseLevel);
      const source = ctx.createBufferSource();
      source.buffer = noiseBuffer;
      const gainNode = ctx.createGain();
      gainNode.gain.value = 1.0;
      const panner = ctx.createStereoPanner();
      panner.pan.value = panValue;
      source.connect(gainNode).connect(panner).connect(ctx.destination);
      source.start(0);
      this._maskingSource = source;
    } catch (e) {
      console.error('Failed to start masking noise:', e);
    }
  }

  stopMaskingNoise() {
    if (this._maskingSource) {
      try { this._maskingSource.stop(); } catch (_e) { /* already stopped */ }
      this._maskingSource = null;
    }
  }

  disposeSynth() {
    if (this._panner) {
      try { this._panner.dispose(); } catch (_e) { /* may already be disposed */ }
      this._panner = null;
    }
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
  async replayCurrentTone() {
    if (this.isPlayingTone) return;
    await this.playSignalTone(this.currentTrialDB);
  }

  @action
  async answerSignal(heard: boolean) {
    if (this.isPlayingTone || this._isProcessing) return;
    this._isProcessing = true;

    try {
      const task = this.currentSignalTask;
      if (!task) return;

      const key = this.currentThresholdKey;

      // Get or create threshold state for this frequency
      let state = this.thresholdStates.get(key) || createThresholdState();
      state = processResponse(state, heard);

      const updatedStates = new Map(this.thresholdStates);
      updatedStates.set(key, state);
      this.thresholdStates = updatedStates;

      if (state.isComplete) {
        // Threshold found for this frequency, advance to next
        await this.advanceToNextFrequency();
      } else {
        // Continue adaptive loop at new dB level
        this.currentTrialDB = getNextDB(state);
        await this.playSignalTone(this.currentTrialDB);
      }
    } finally {
      this._isProcessing = false;
    }
  }

  private async advanceToNextFrequency() {
    const task = this.currentSignalTask;
    if (!task) return;

    if (this.signalFreqIndex + 1 < task.frequencies.length) {
      this.signalFreqIndex++;
      this.currentTrialDB = INITIAL_DB;
      await this.playSignalTone(this.currentTrialDB);
    } else {
      // Next ear/task
      if (this.signalTaskIndex + 1 < this.signalTasks.length) {
        this.signalTaskIndex++;
        this.signalFreqIndex = 0;
        this.currentTrialDB = INITIAL_DB;
        await this.playSignalTone(this.currentTrialDB);
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
      const sinAudiometryResults: Record<number, number> = {};
      let heardCount = 0;

      for (const freq of task.frequencies) {
        const state = this.thresholdStates.get(`${task.id}:${freq}`);
        if (state?.threshold !== null && state?.threshold !== undefined) {
          sinAudiometryResults[freq] = state.threshold;
          heardCount++;
        }
      }

      try {
        await this.network.postAudiometryHistory({
          audiometryTaskId: String(task.id),
          startTime: this.startTime,
          endTime,
          executionSeconds,
          tasksCount: task.frequencies.length,
          rightAnswers: heardCount,
          headphones: this.effectiveHeadphoneId,
          sinAudiometryResults,
        });
      } catch (error: any) {
        this.error = error.message || this.intl.t('audiometry.save_failed');
        console.error('Failed to save audiometry history:', error);
      }
    }

    // Save to local history for progress tracking
    saveHistoryEntry({
      id: generateEntryId(),
      date: new Date().toISOString(),
      testId: this.args.test.id,
      testName: this.args.test.name,
      audiometryType: 'SIGNALS',
      headphoneId: this.effectiveHeadphoneId,
      executionSeconds,
      leftEarThresholds: this.leftEarThresholds,
      rightEarThresholds: this.rightEarThresholds,
      ptaLeft: this.ptaLeft,
      ptaRight: this.ptaRight,
      classificationLeft: this.hearingClassificationLeft,
      classificationRight: this.hearingClassificationRight,
    }, this.currentUserId);
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
      if (buffer) {
        const { source } = createSource(context, buffer);
        this._audioSource = source;
        source.start(0);
        await new Promise<void>((resolve) => {
          source.onended = () => resolve();
        });
      } else {
        // Fallback to browser speech synthesis (same as AudioService.playTask)
        const text = new URL(url).searchParams.get('text');
        if (text) {
          await this.nativePlayText(text);
        }
      }
    } catch (e) {
      console.error('Failed to play speech audio:', e);
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlayingAudio = false;
      }
    }
  }

  nativePlayText(text: string): Promise<void> {
    const lang = this.intl.primaryLocale;
    const voices = speechSynthesis.getVoices().filter((e) => e.lang.toLowerCase() === lang);
    const voicesToPlay: SpeechSynthesisVoice[] = ([
      voices.find((el) => el.default === true),
      voices.find((el) => el.localService === false),
      ...voices,
    ]).filter((el) => el !== undefined) as SpeechSynthesisVoice[];

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.voice = voicesToPlay[0] ?? null;
    return new Promise<void>((resolve) => {
      utterance.onend = () => resolve();
      utterance.onerror = () => resolve();
      speechSynthesis.speak(utterance);
    });
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
    if (this.isPlayingAudio || this._isProcessing) return;
    this._isProcessing = true;

    try {
      const task = this.currentSpeechTask;
      if (!task || !this.speechCorrectWord) return;

      const correct = option.word === this.speechCorrectWord.word;
      const taskId = String(task.id);

      // Find or create result entry for this task
      const updatedResults = [...this.speechResults];
      let resultEntry = updatedResults.find((r) => r.taskId === taskId);
      if (!resultEntry) {
        resultEntry = { taskId, correct: 0, total: 0 };
        updatedResults.push(resultEntry);
      }
      resultEntry.total++;
      if (correct) {
        resultEntry.correct++;
      }
      this.speechResults = updatedResults;

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
    } finally {
      this._isProcessing = false;
    }
  }

  async finishSpeechTest() {
    this.disposeAllAudio();
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
          headphones: this.effectiveHeadphoneId,
        });
      } catch (error: any) {
        this.error = error.message || this.intl.t('audiometry.save_failed');
        console.error('Failed to save audiometry history:', error);
      }
    }

    // Save to local history for progress tracking
    saveHistoryEntry({
      id: generateEntryId(),
      date: new Date().toISOString(),
      testId: this.args.test.id,
      testName: this.args.test.name,
      audiometryType: 'SPEECH',
      headphoneId: this.effectiveHeadphoneId,
      executionSeconds,
      leftEarThresholds: {},
      rightEarThresholds: {},
      ptaLeft: null,
      ptaRight: null,
      classificationLeft: null,
      classificationRight: null,
      speechResults: {
        correct: this.speechTotalCorrect,
        total: this.speechTotalRounds,
      },
    }, this.currentUserId);
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
    this.stopMaskingNoise();
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
                {{#if (isMultipleHeadphones @headphones)}}
                  <option value="">--</option>
                {{/if}}
                {{#each @headphones as |hp|}}
                  <option value={{hp.id}} selected={{isSelected hp.id this.effectiveHeadphoneId}}>{{hp.name}}</option>
                {{/each}}
              </select>
            </div>

            {{#if this.isSignals}}
              <div class="max-w-sm mx-auto mb-6 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p class="text-xs text-blue-700">
                  {{t "audiometry.adaptive_info"}}
                </p>
              </div>
            {{/if}}

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
                {{t "audiometry.ear_testing" ear=this.currentEarLabel}}
              </p>
              <p class="text-xs text-gray-400 mb-1" data-test-frequency-info>
                {{t "audiometry.frequency" freq=this.currentFrequency}}
              </p>
              <p class="text-xs text-gray-400 mb-2" data-test-db-level>
                {{t "audiometry.current_level" level=this.currentTrialDB}}
              </p>

              {{! Volume level indicator }}
              <div class="w-48 mx-auto bg-gray-200 rounded-full h-1.5 mb-4">
                <div
                  class="bg-indigo-400 h-1.5 rounded-full transition-all duration-300"
                  style="width: {{this.currentTrialPercent}}%"
                ></div>
              </div>

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
                <p class="text-lg font-medium text-gray-700 mb-4">
                  {{t "audiometry.can_you_hear"}}
                </p>

                <button
                  data-test-replay-tone
                  type="button"
                  disabled={{this.isPlayingTone}}
                  class="btn-press mb-4 px-4 py-2 text-sm text-indigo-600 border border-indigo-300 rounded-lg hover:bg-indigo-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  {{on "click" this.replayCurrentTone}}
                >
                  {{t "audiometry.replay"}}
                </button>
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
              {{#if this.currentSpeechFrequencyZone}}
                <p class="text-xs text-gray-400 mb-2">
                  {{t "audiometry.speech_zone" zone=this.currentSpeechFrequencyZone}}
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

              <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 max-w-md mx-auto">
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
            {{! Audiogram chart }}
            <Audiogram
              @leftEarThresholds={{this.leftEarThresholds}}
              @rightEarThresholds={{this.rightEarThresholds}}
            />

            {{! Per-ear threshold details }}
            <div class="space-y-4 mb-6 text-left max-w-md mx-auto">
              {{#each this.signalResultsSummary as |r|}}
                <div data-test-signal-ear-result>
                  <p class="text-sm font-medium text-gray-700 mb-1">{{r.earLabel}}</p>
                  {{#each r.frequencies as |f|}}
                    <p class="text-xs text-gray-500 ml-2">
                      {{f.freq}} Hz:
                      {{#if (isNotNull f.threshold)}}
                        {{t "audiometry.threshold_at" threshold=f.threshold}}
                      {{else}}
                        {{t "audiometry.no_response"}}
                      {{/if}}
                    </p>
                  {{/each}}
                </div>
              {{/each}}
            </div>

            {{! WHO classification }}
            {{#if this.hearingClassificationLeft}}
              <div class="mb-2">
                <span class="text-sm text-gray-500">{{t "audiometry.ear_left"}}:</span>
                <span class="text-sm font-medium text-gray-700 ml-1">
                  {{t (classificationKey this.hearingClassificationLeft)}}
                  {{#if (isNotNull this.ptaLeft)}}
                    <span class="text-xs text-gray-400">(PTA: {{this.ptaLeft}} dB)</span>
                  {{/if}}
                </span>
              </div>
            {{/if}}
            {{#if this.hearingClassificationRight}}
              <div class="mb-6">
                <span class="text-sm text-gray-500">{{t "audiometry.ear_right"}}:</span>
                <span class="text-sm font-medium text-gray-700 ml-1">
                  {{t (classificationKey this.hearingClassificationRight)}}
                  {{#if (isNotNull this.ptaRight)}}
                    <span class="text-xs text-gray-400">(PTA: {{this.ptaRight}} dB)</span>
                  {{/if}}
                </span>
              </div>
            {{/if}}

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

function isNotNull(value: unknown): boolean {
  return value !== null && value !== undefined;
}

function taskCount(tasks: AudiometryTask[]): number {
  return tasks.length;
}

function isMultipleHeadphones(headphones: Headphone[]): boolean {
  return headphones.length > 1;
}

function isSelected(id: string | null, selectedId: string): boolean {
  return String(id ?? '') === selectedId;
}

function classificationKey(classification: string | null): string {
  if (!classification) return '';
  return `audiometry.hearing_${classification}`;
}

function shuffleArray<T>(array: T[]): T[] {
  const result = [...array];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j]!, result[i]!];
  }
  return result;
}
