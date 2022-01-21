import Ember from 'ember';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import {
  task,
  timeout,
  Task as TaskGenerator,
  TaskInstance,
} from 'ember-concurrency';
import { tracked } from '@glimmer/tracking';
import { getOwner } from '@ember/application';
import {
  createSource,
  createNoizeBuffer,
  loadAudioFiles,
  createAudioContext,
  toSeconds,
  toMilliseconds,
  TIMINGS,
  ISource,
} from 'brn/utils/audio-api';
import Service, { inject as service } from '@ember/service';
import TimerComponent from 'brn/components/timer/component';
import NetworkService from './network';
import StatsService, { StatEvents } from './stats';
import { ToneObject } from 'brn/components/audio-player/component';
import SignalModel from 'brn/models/signal';
import Intl from 'ember-intl/services/intl';
import { PolySynth, Synth, SynthOptions } from 'tone';

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
  context = createAudioContext();
  @tracked
  player: null | TimerComponent = null;
  register(player: TimerComponent) {
    this.player = player;
  }
  buffers: (AudioBuffer | null | ToneObject)[] = [];
  startTime: null | number = 0;
  totalDuration = 0;
  noiseNode!: any;
  sources!: ISourceCollection;
  noiseTaskInstance!: TaskInstance<any>;
  @tracked isPlaying = false;

  @tracked audioPlayingProgress = 0;

  @tracked audioFileUrl: null | string | string[] | ToneObject = null;

  @(task(function* (this: AudioService) {
    try {
      this.startTime = Date.now();
      this.setProgress(0);
      while (this.isPlaying) {
        this.updatePlayingProgress();
        yield timeout(32);
      }
      yield timeout(100);
      this.setProgress(0);
    } catch (e) {
      // NOP
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.setProgress(0);
        this.startTime = null;
      }
    }
  }).enqueue())
  trackProgress!: TaskGenerator<any, any>;

  audioUrlForText(text: string) {
    return (
      window.location.protocol +
      '//' +
      window.location.host +
      `/api/audio?text=${encodeURIComponent(text)}&locale=${encodeURIComponent(
        this.intl.primaryLocale,
      )}`
    );
  }

  @action async startPlayTask(filesToPlay = this.filesToPlay) {
    if (this.isPlaying) {
      return;
    }
    this.stats.addEvent(StatEvents.PlayAudio);
    await this.setAudioElements(filesToPlay as string[]);
    await this.playAudio();
  }

  get currentExerciseNoiseUrl() {
    if (Ember.testing) {
      return 0;
    }
    const owner = getOwner(this);
    const model = owner
      .lookup('route:application')
      .modelFor('group.series.subgroup.exercise');
    if (!model) {
      return 0;
    }
    return model.noiseUrl;
  }
  get currentExerciseNoiseLevel() {
    if (Ember.testing) {
      return 0;
    }
    const owner = getOwner(this);
    const model = owner
      .lookup('route:application')
      .modelFor('group.series.subgroup.exercise');
    if (!model) {
      return 0;
    }
    return model.noiseLevel;
  }

  updatePlayingProgress() {
    this.setProgress(
      (100 / this.totalDuration) * (Date.now() - (this.startTime as number)),
    );
  }

  get filesToPlay() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  async setAudioElements(filesToPlay: Array<string | ToneObject>) {
    this.context = createAudioContext();
    if (Ember.testing) {
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
    } catch (e) {
      // EOL
    }
    if (this.noiseTaskInstance) {
      this.noiseTaskInstance.cancel();
    }
  }

  @action
  async playAudio() {
    try {
      if (!Ember.testing) {
        await this.playTask.perform();
      } else {
        await this.fakePlayTask.perform();
      }
    } catch (e) {
      // EOL
    }
  }

  @action
  stop() {
    if (!Ember.testing) {
      this.playTask.cancelAll();
    } else {
      this.fakePlayTask.cancelAll();
    }
  }

  async getNoise(duration: number, level: number, url: null | string = null) {
    if (url !== null) {
      const noiseContext = createAudioContext();
      const noiseBuffers = await loadAudioFiles(
        noiseContext,
        [url],
        () => this.network.token ?? '',
      );
      if (noiseBuffers.some((n) => n === null)) {
        throw new Error('Unable to resolve noise');
      }
      const source = await createSource(
        noiseContext,
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

  calcDurationForSources(sources: ISourceCollection) {
    return sources.reduce((result, item) => {
      if (item === null) {
        return result;
      }
      if (item.source.buffer) {
        return result + toMilliseconds(item.source.buffer.duration);
      } else {
        return result;
      }
    }, 0);
  }

  @task(function* playNoise(this: AudioService) {
    let noise = null;
    const timeInSeconds = 10;
    try {
      const [level, url] = [
        this.currentExerciseNoiseLevel,
        this.currentExerciseNoiseUrl,
      ];
      if (!level) {
        return;
      }
      noise = yield this.getNoise(timeInSeconds, level, url);
      noise.source.start(0);
      this.noiseNode = noise;
      if (url) {
        yield timeout(toMilliseconds(6000));
      } else {
        yield timeout(toMilliseconds(timeInSeconds) - 3);
        this.startNoise();
      }
    } finally {
      if (noise) {
        noise.source.stop();
      }
    }
  })
  startNoiseTask!: TaskGenerator<any, any>;

  @(task(function* playAudio(this: AudioService, noizeSeconds = 0) {
    const startedSources = [];
    const hasNoize = false;
    if (hasNoize) {
      noizeSeconds = 0.3;
    }
    try {
      this.sources = yield this.createSources(this.context, this.buffers || []);
      this.totalDuration =
        this.calcDurationForSources(this.sources) +
        toMilliseconds(noizeSeconds);
      this.isPlaying = true;
      this.trackProgress.perform();
      if (hasNoize) {
        const noize: any = yield this.getNoise(
          noizeSeconds ? toSeconds(this.totalDuration) : 0,
          this.currentExerciseNoiseLevel,
        );
        noize.source.start(0);
        startedSources.push(noize);
        yield timeout(toMilliseconds(noizeSeconds / 2));
      }
      for (const item of this.sources) {
        if (item) {
          if (item.source.buffer) {
            const duration = toMilliseconds(item.source.buffer.duration);
            item.source.start(0);
            startedSources.push(item);
            yield timeout(duration);
          } else {
            console.error('there is no buffer for source');
          }
        } else {
          // here is place for await of end of speech
        }
      }
      if (hasNoize) {
        yield timeout(toMilliseconds(noizeSeconds / 2));
      }
      yield timeout(10);
      this.isPlaying = false;
    } catch (e) {
      console.error(e);
      // NOP
    } finally {
      startedSources.forEach(({ source }) => {
        source.stop(0);
      });
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlaying = false;
        this.totalDuration = 0;
      }
    }
  })
    .keepLatest()
    .maxConcurrency(1))
  playTask!: TaskGenerator<any, any>;

  @(task(function* fakePlayAudio(this: AudioService) {
    this.totalDuration = TIMINGS.FAKE_AUDIO;
    this.isPlaying = true;
    this.trackProgress.perform();
    yield timeout(TIMINGS.FAKE_AUDIO);
    this.isPlaying = false;
    this.totalDuration = 0;
  }).enqueue())
  fakePlayTask!: TaskGenerator<any, any>;

  setProgress(progress: number) {
    this.audioPlayingProgress = progress;
    if (progress !== 100 && (progress >= 99 || Ember.testing)) {
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
