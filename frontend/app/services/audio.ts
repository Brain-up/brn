import { isTesting } from '@embroider/macros';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
import {
  task,
  enqueueTask,
  keepLatestTask,
  timeout,
  TaskInstance,
} from 'ember-concurrency';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
import { getOwner } from '@ember/application';
import {
  createSource,
  createNoizeBuffer,
  loadAudioFiles,
  createAudioContext,
  audioBufferToWavBlob,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  toSeconds,
  toMilliseconds,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  TIMINGS,
  ISource,
  preloadAudioFile,
} from 'brn/utils/audio-api';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Service, { service } from '@ember/service';
import TimerComponent from 'brn/components/timer';
import NetworkService from './network';
import StatsService, { StatEvents } from './stats';
import { ToneObject } from 'brn/components/audio-player';
import type { Signal as SignalModel } from 'brn/schemas/signal';
import Intl from 'ember-intl/services/intl';
import { PolySynth, Synth, SynthOptions } from 'tone';
import UserDataService from './user-data';
import StudyingTimerService from './studying-timer';
import type { Exercise } from 'brn/schemas/exercise';

type ISourceCollection = (ISource | IToneSource | null)[];
export interface IToneSource {
  source: {
    instance: PolySynth<Synth<SynthOptions>>;
    buffer: {
      duration: number;
    };
    start: () => void;
    stop: () => void;
  };
}
export default class AudioService extends Service {
  @service('network') declare network: NetworkService;
  @service('stats') declare stats: StatsService;
  @service('intl') declare intl: Intl;
  @service('user-data') declare userData: UserDataService;
  @service('studying-timer') declare studyingTimer: StudyingTimerService;
  context!: AudioContext;

  willDestroy(): void {
    super.willDestroy();
    if (this.context && this.context.state !== 'closed') {
      this.context.close();
    }
  }
  @tracked
  player: null | TimerComponent = null;
  register(player: TimerComponent) {
    this.player = player;
  }
  buffers: (AudioBuffer | null | ToneObject)[] = [];
  startTime: null | number = 0;
  totalDuration = 0;
  noiseNode!: ISource | null;
  sources!: ISourceCollection;
  // The <audio> element currently playing a pitch-preserved (rate != 1) clip,
  // tracked so it can be stopped when playback is cancelled.
  activePitchAudio: HTMLAudioElement | null = null;
  noiseTaskInstance!: TaskInstance<void>;
  @tracked isPlaying = false;
  @tracked isProcessing = false;

  get isBusy() {
    return this.isPlaying || this.isProcessing;
  }

  @tracked audioPlayingProgress = 0;

  @tracked audioFileUrl: null | string | string[] | ToneObject = null;

  trackProgress = enqueueTask(async () => {
    try {
      this.startTime = Date.now();
      this.setProgress(0);
      while (this.isPlaying) {
        this.updatePlayingProgress();
        await timeout(32);
      }
      await timeout(100);
      this.setProgress(0);
    } catch (_e) {
      // NOP
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.setProgress(0);
        this.startTime = null;
      }
    }
  });

  // for tests
  _lastText: null | string = null;

  audioUrlForText(text: string) {
    this._lastText = text;
    const exercise = this.currentExercise;
    return (
      window.location.protocol +
      '//' +
      window.location.host +
      `/api/audio?text=${encodeURIComponent(text)}&locale=${encodeURIComponent(
        this.intl.primaryLocale,
      )}&exerciseId=${encodeURIComponent(exercise?.id ?? '0')}`
    );
  }

  @action async startPlayTask(filesToPlay = this.filesToPlay) {
    if (this.isBusy) {
      return;
    }
    this.isProcessing = true;
    try {
      this.stats.addEvent(StatEvents.PlayAudio);
      await this.setAudioElements(filesToPlay as string[]);
      await this.playAudio();
    } catch (e) {
      // Log and swallow errors: callers invoke startPlayTask fire-and-forget
      // without awaiting, matching the pattern used in playAudio().
      console.error(e);
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.isProcessing = false;
      }
    }
  }

  get currentExerciseNoiseUrl() {
    if (isTesting()) {
      return null;
    }
    return this.currentExercise?.noiseUrl ?? null;
  }
  get currentExercise(): Exercise | null {
    if (isTesting()) {
      return null;
    }
    const owner = getOwner(this)!;
    const route = owner.lookup('route:application') as { modelFor(name: string): unknown } | undefined;
    const model = route?.modelFor('group.series.subgroup.exercise');
    if (!model) {
      return null;
    }
    return model as Exercise;
  }
  get currentExerciseNoiseLevel() {
    if (isTesting()) {
      return 0;
    }
    return this.currentExercise?.noiseLevel ?? 0;
  }

  updatePlayingProgress() {
    this.setProgress(
      (100 / this.totalDuration) * (Date.now() - (this.startTime as number)),
    );
  }

  get filesToPlay() {
    return Array.isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  @tracked audioElements: (string | ToneObject)[] = [];

  async setAudioElements(filesToPlay: Array<string | ToneObject>) {
    this.audioElements = filesToPlay;
    if (!this.context || this.context.state === 'closed') {
      this.context = createAudioContext();
    } else if (this.context.state === 'suspended' && !isTesting()) {
      await this.context.resume();
    }
    if (isTesting()) {
      this.buffers = [];
      return;
    }
    if (filesToPlay.filter((el) => typeof el === 'string').length) {
      this.buffers = await loadAudioFiles(
        this.context,
        filesToPlay as string[],
        () => this.network.token ?? '',
      );
    } else {
      this.buffers = filesToPlay as ToneObject[];
    }
  }

  @action
  startNoise() {
    this.noiseTaskInstance = this.startNoiseTask.perform();
  }

  @action
  stopNoise() {
    try {
      if (this.noiseNode) {
        this.noiseNode.source.stop();
      }
    } catch (_e) {
      // EOL
    }
    if (this.noiseTaskInstance) {
      this.noiseTaskInstance.cancel();
    }
  }

  @action
  async playAudio() {
    this.studyingTimer.resetIdle();
    try {
      if (!isTesting()) {
        await this.playTask.perform();
      } else {
        await this.fakePlayTask.perform();
      }
    } catch (_e) {
      // EOL
    }
  }

  @action
  async stop() {
    if (!isTesting()) {
      await this.playTask.cancelAll();
    } else {
      await this.fakePlayTask.cancelAll();
    }
  }

  async preloadNoiseAudio() {
    const url = this.currentExerciseNoiseUrl;
    if (!url) {
      return;
    }
    await preloadAudioFile(url, this.network.token ?? '');
  }

  async getNoise(duration: number, level: number, url: null | string = null) {
    if (url !== null) {
      // Reuse this.context instead of creating a separate AudioContext
      // to avoid leaking an unclosed context.
      if (!this.context || this.context.state === 'closed') {
        this.context = createAudioContext();
      }
      const noiseBuffers = await loadAudioFiles(
        this.context,
        [url],
        () => this.network.token ?? '',
      );
      if (noiseBuffers.some((n) => n === null)) {
        throw new Error('Unable to resolve noise');
      }
      const source = await createSource(
        this.context,
        noiseBuffers[0] as AudioBuffer,
      );
      source.source.loop = true;
      source.gainNode.gain.value = level * 0.01;
      return source;
    } else {
      return await createSource(
        this.context,
        createNoizeBuffer(this.context, duration, level),
      );
    }
  }

  async createToneSources(items: SignalModel[]): Promise<IToneSource[]> {
    const Tone = await import('tone');
    return items.map((el) => {
      const { duration, frequency } = el;

      return {
        source: {
          instance: new Tone.PolySynth(Tone.Synth).toDestination(),
          buffer: {
            duration: duration / 100,
          },
          start() {
            this.instance.triggerAttack(frequency, Tone.now(), 0.5);
          },
          stop() {
            this.instance.dispose();
          },
        },
      };
    });
  }

  isToneObject(item: AudioBuffer | ToneObject | null): boolean {
    if (item === null) {
      return false;
    }
    if (this.isAudioBuffer(item)) {
      return false;
    }
    if (item.duration && 'frequency' in item) {
      return true;
    }
    return false;
  }
  isAudioBuffer(item: AudioBuffer | ToneObject | null) {
    return item instanceof AudioBuffer;
  }

  async createSources(
    context: AudioContext,
    buffers: (AudioBuffer | ToneObject | null)[],
  ): Promise<ISourceCollection> {
    const results: ISourceCollection = [];
    for (const buffer of buffers) {
      if (this.isAudioBuffer(buffer)) {
        results.push(createSource(context, buffer as AudioBuffer));
      } else if (this.isToneObject(buffer)) {

        results.push(
          (
            await this.createToneSources([buffer] as unknown as SignalModel[])
          )[0],
        );
      } else if (buffer === null) {
        // here is place for auto-generated sound using speech kit
        results.push(null);
      }
    }
    return results;
  }

  calcDurationForSources(sources: ISourceCollection, rate = 1) {
    return sources.reduce((result, item) => {
      if (item === null) {
        return result;
      }
      if (item.source.buffer) {
        // Only real audio buffers honour the playback-rate preference; tone /
        // signal sources play at their natural rate.
        const effectiveRate =
          item.source instanceof AudioBufferSourceNode ? rate : 1;
        return result + toMilliseconds(item.source.buffer.duration) / effectiveRate;
      } else {
        return result;
      }
    }, 0);
  }

  startNoiseTask = task(async () => {
    let noise = null;
    let started = false;
    const timeInSeconds = 10;
    try {
      const [level, url] = [
        this.currentExerciseNoiseLevel,
        this.currentExerciseNoiseUrl,
      ];
      if (!level) {
        return;
      }
      noise = await this.getNoise(timeInSeconds, level, url);
      // Mirror the word-playback path: a fresh AudioContext (e.g. right after a
      // page refresh) starts suspended under the browser autoplay policy, and
      // source.start(0) on a suspended context queues silently. Resume before
      // starting so background noise plays on first load — not only after a
      // lesson restart, which happened to reuse an already-resumed context.
      if (this.context && this.context.state === 'suspended' && !isTesting()) {
        await this.context.resume();
      }
      noise.source.start(0);
      started = true;
      this.noiseNode = noise;
      if (url) {
        await timeout(toMilliseconds(6000));
      } else {
        await timeout(toMilliseconds(timeInSeconds) - 3);
        this.startNoise();
      }
    } finally {
      // Only stop a source that actually started. The context.resume() above
      // adds an await between creating and starting the source, so a cancel
      // (e.g. stopNoise) or a resume rejection in that window would otherwise
      // call stop() on a never-started node and throw InvalidStateError.
      if (noise && started) {
        noise.source.stop();
      }
    }
  });

  // `rate` defaults to the live preference, but playTask passes its own
  // snapshot so every word in one playback pass uses the same rate even if
  // the user changes the setting mid-pass.
  nativePlayText(txt: string, rate = this.userData.audioPlaybackRate) {
    const lang = this.userData.activeLocale;
    const voices = speechSynthesis.getVoices().filter((e) => e.lang.toLowerCase() === lang);
    const voicesToPlay: SpeechSynthesisVoice[] = [
      voices.find(el => el.default === true), // default
      voices.find(el => el.localService === false), // cloud
      ...voices,
    ].filter((el) => el !== undefined) as SpeechSynthesisVoice[];

    const v = new SpeechSynthesisUtterance(txt);
    v.voice = voicesToPlay[0];
    // SpeechSynthesis accepts a rate of 0.1–10; our presets sit well inside it.
    v.rate = rate;
    const p = new Promise((resolve) => {
      v.onend = resolve;
      // A synthesis error ('not-allowed', 'interrupted', …) never fires
      // onend; without this the awaiting playTask hangs forever, isBusy
      // stays true and the whole exercise wedges with disabled buttons.
      v.onerror = resolve;
    });
    speechSynthesis.speak(v);
    return p;
  }

  // Encoded-WAV cache for pitch-preserved playback, keyed by source URL.
  // Decoded AudioBuffers are re-created on every play (only raw bytes are
  // cached), so without this the O(samples) PCM encode would run on the main
  // thread for every click of every word. Bounded; Map preserves insertion
  // order, so eviction drops the oldest entry.
  wavBlobCache = new Map<string, Blob>();
  static WAV_CACHE_LIMIT = 32;

  wavBlobFor(buffer: AudioBuffer, cacheKey?: string): Blob {
    if (!cacheKey) {
      return audioBufferToWavBlob(buffer);
    }
    const cached = this.wavBlobCache.get(cacheKey);
    if (cached) {
      return cached;
    }
    const blob = audioBufferToWavBlob(buffer);
    if (this.wavBlobCache.size >= AudioService.WAV_CACHE_LIMIT) {
      const oldest = this.wavBlobCache.keys().next().value;
      if (oldest !== undefined) {
        this.wavBlobCache.delete(oldest);
      }
    }
    this.wavBlobCache.set(cacheKey, blob);
    return blob;
  }

  // Stop the given pitch-preserved <audio> element. Shared by playBufferAtRate
  // and playTask's cancellation cleanup so the teardown stays in one place.
  stopPitchAudioElement(el: HTMLAudioElement) {
    try {
      el.pause();
    } catch (e) {
      console.error('failed to stop pitch-preserving audio', e);
    }
    if (this.activePitchAudio === el) {
      this.activePitchAudio = null;
    }
  }

  // Play a decoded clip at a non-default speed while keeping its natural pitch.
  // AudioBufferSourceNode.playbackRate would pitch-shift; an <audio> element
  // with preservesPitch time-stretches instead. We re-encode the buffer to a
  // WAV blob so the element can decode it on any browser.
  //
  // Returns true when playback went through (or was deliberately stopped),
  // false when it failed to start — the caller then falls back to Web Audio
  // playback so the user hears the word instead of silence.
  async playBufferAtRate(
    buffer: AudioBuffer,
    rate: number,
    cacheKey?: string,
  ): Promise<boolean> {
    const url = URL.createObjectURL(this.wavBlobFor(buffer, cacheKey));
    const el = new Audio();
    el.src = url;
    // preservesPitch defaults to true where supported; set the legacy-prefixed
    // flags too for older WebKit/Gecko. Falls back to pitch-shifted playback
    // (today's behaviour) if the browser ignores it.
    el.preservesPitch = true;
    const legacyEl = el as unknown as {
      mozPreservesPitch?: boolean;
      webkitPreservesPitch?: boolean;
    };
    legacyEl.mozPreservesPitch = true;
    legacyEl.webkitPreservesPitch = true;
    // Set defaultPlaybackRate as well: the media load algorithm applies
    // defaultPlaybackRate on resource selection, so a playbackRate set before
    // metadata can otherwise be reset to 1x on some browsers (Safari) — which
    // would silently drop the speed change.
    el.defaultPlaybackRate = rate;
    el.playbackRate = rate;
    this.activePitchAudio = el;
    let startFailed = false;
    let resolveEnded: () => void = () => {};
    const ended = new Promise<void>((resolve) => {
      resolveEnded = resolve;
      el.onended = () => resolve();
      el.onerror = () => resolve();
      // pause() — from a cancelled playTask's cleanup — must release the race
      // promptly; otherwise this detached await would hold the object URL for
      // the full rate-scaled safety window after every Stop. (Natural end
      // fires 'pause' just before 'ended', so this is also just an earlier
      // resolution of the same completion.)
      el.onpause = () => resolve();
    });
    try {
      await el.play().catch((e) => {
        resolveEnded();
        if ((e as DOMException)?.name === 'AbortError') {
          // pause() landed while play() was still pending — a routine stop /
          // next-word interruption, not a playback failure.
          return;
        }
        // Autoplay-policy rejection (gesture-strict browsers like Safari) or
        // a real media failure. Neither fires onerror, so signal the caller
        // to fall back to (pitch-shifted) Web Audio playback — a silently
        // "successful" no-op here would mark words as heard without a sound.
        console.error('pitch-preserving audio playback failed', e);
        startFailed = true;
      });
      if (!startFailed) {
        // Real clip length grows as rate shrinks; +1s mirrors the buffer path.
        const safety = toMilliseconds(buffer.duration) / rate + 1000;
        await Promise.race([ended, timeout(safety)]);
      }
    } finally {
      this.stopPitchAudioElement(el);
      URL.revokeObjectURL(url);
    }
    return !startFailed;
  }

  playTask = keepLatestTask({ maxConcurrency: 1 }, async (noizeSeconds = 0) => {
    const startedSources = [];
    const hasNoize = false;
    if (hasNoize) {
      noizeSeconds = 0.3;
    }
    try {
      const playbackRate = this.userData.audioPlaybackRate;
      this.sources = await this.createSources(this.context, this.buffers || []);
      this.totalDuration =
        this.calcDurationForSources(this.sources, playbackRate) +
        toMilliseconds(noizeSeconds);
      this.isPlaying = true;
      this.trackProgress.perform();
      if (hasNoize) {
        const noize = await this.getNoise(
          noizeSeconds ? toSeconds(this.totalDuration) : 0,
          this.currentExerciseNoiseLevel,
        );
        noize.source.start(0);
        startedSources.push(noize);
        await timeout(toMilliseconds(noizeSeconds / 2));
      }
      let index = -1;
      for (const item of this.sources) {
        index++;
        if (item) {
          if (item.source.buffer) {
            // Browsers (Safari, and Chrome under throttling) suspend idle
            // AudioContexts. source.start(0) on a suspended context queues
            // playback instead of playing it — without this resume, later
            // words fall silent until a user gesture wakes the context.
            if (this.context.state === 'suspended' && !isTesting()) {
              await this.context.resume();
            }
            const rawSource = item.source as unknown;
            // When the user changed the speech speed, play real audio buffers
            // through an <audio> element so the pitch stays natural (see
            // playBufferAtRate). Tone/signal sources are not buffer nodes and
            // always play at their own rate.
            let sourceRate = 1;
            if (
              rawSource instanceof AudioBufferSourceNode &&
              playbackRate !== 1 &&
              rawSource.buffer
            ) {
              const element = this.audioElements[index];
              const played = await this.playBufferAtRate(
                rawSource.buffer,
                playbackRate,
                typeof element === 'string' ? element : undefined,
              );
              if (played) {
                // The pre-built Web Audio nodes for this clip were bypassed —
                // drop them from the graph instead of leaving them connected.
                (item as ISource).gainNode.disconnect();
                continue;
              }
              // <audio> playback failed to start (autoplay policy, media
              // error): fall back to Web Audio at the same rate. Pitch-shifted,
              // but audible — silence while marking words heard is worse.
              sourceRate = playbackRate;
              rawSource.playbackRate.value = playbackRate;
            }
            const duration =
              toMilliseconds(item.source.buffer.duration) / sourceRate;
            // Prefer onended over wall-clock timeout: setTimeout keeps
            // ticking when the context suspends mid-clip, so a timer-only
            // loop would advance over silent words instead of waiting
            // for real playback to finish.
            const ended = rawSource instanceof AudioBufferSourceNode
              ? new Promise<void>((resolve) => {
                  rawSource.onended = () => resolve();
                })
              : null;
            let startFailed = false;
            try {
              item.source.start(0);
              startedSources.push(item);
            } catch (e) {
              // A sync throw (closed context, source already started, etc.)
              // would otherwise strand the loop in the safety-net timeout.
              startFailed = true;
              console.error('source.start failed', e);
            }
            if (startFailed) {
              // nothing playing — move on immediately
            } else if (ended) {
              await Promise.race([ended, timeout(duration + 1000)]);
            } else {
              await timeout(duration);
            }
          } else {
            console.error('there is no buffer for source');
          }
        } else {
          // experimental branch to use browser audio api
          if (typeof this.audioElements[index] === 'string') {
            // likely we have to move it into method
            const text = new URL(
              this.audioElements[index] as string,
            ).searchParams.get('text');
            if (text) {
              await this.nativePlayText(text, playbackRate);
            } else {
              // wrong url;
            }
          }

          // here is place for await of end of speech
        }
      }
      if (hasNoize) {
        await timeout(toMilliseconds(noizeSeconds / 2));
      }
      await timeout(10);
      this.isPlaying = false;
    } catch (e) {
      console.error(e);
      // NOP
    } finally {
      startedSources.forEach(({ source }) => {
        source.stop(0);
      });
      // Defensive: playBufferAtRate normally stops its own element, but on
      // cancellation this task unwinds first — pausing here also fires the
      // element's 'pause' listener, which promptly releases the detached
      // playBufferAtRate await (and with it the object URL).
      if (this.activePitchAudio) {
        this.stopPitchAudioElement(this.activePitchAudio);
      }
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlaying = false;
        this.totalDuration = 0;
      }
    }
  });

  fakePlayTask = enqueueTask(async () => {
    this.totalDuration = TIMINGS.FAKE_AUDIO;
    this.isPlaying = true;
    this.trackProgress.perform();
    await timeout(TIMINGS.FAKE_AUDIO);
    this.isPlaying = false;
    this.totalDuration = 0;
  });

  setProgress(progress: number) {
    this.audioPlayingProgress = progress;
    if (progress !== 100 && (progress >= 99 || isTesting())) {
      this.setProgress(100);
      return;
    }
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    audio: AudioService;
  }
}
