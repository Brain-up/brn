import Ember from 'ember';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { timeout, task } from 'ember-concurrency';
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
} from 'brn/utils/audio-api';
import Service, { inject as service } from '@ember/service';
export default class AudioService extends Service {
  @service('network') network;
  context = createAudioContext();
  @tracked
  player = null;
  register(player) {
    this.player = player;
  }

  @tracked isPlaying = false;

  @tracked audioPlayingProgress = 0;

  @tracked audioFileUrl;

  @(task(function*() {
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
  trackProgress;

  @action async startPlayTask(filesToPlay = this.filesToPlay) {
    if (this.isPlaying) {
      return;
    }
    await this.setAudioElements(filesToPlay);
    await this.playAudio();
  }

  get currentExerciseNoiseUrl() {
    if (Ember.testing) {
      return 0;
    }
    const owner = getOwner(this);
    const model = owner.lookup('route:application').modelFor('group.series.exercise');
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
    const model = owner.lookup('route:application').modelFor('group.series.exercise');
    if (!model) {
      return 0;
    }
    return model.noiseLevel;
  }

  updatePlayingProgress() {
    this.setProgress(
      (100 / this.totalDuration) * (Date.now() - this.startTime),
    );
  }

  get filesToPlay() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  async setAudioElements(filesToPlay) {
    this.context = createAudioContext();
    if (Ember.testing) {
      this.buffers = [];
      return;
    }
    this.buffers = await loadAudioFiles(this.context, filesToPlay);
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
    } catch(e) {
      // EOL
    }
    if (this.noiseTaskInstance) {
      this.noiseTaskInstance.cancel();
    }
  }

  @action
  async playAudio() {
    if (!Ember.testing) {
      await this.playTask.perform();
    } else {
      await this.fakePlayTask.perform();
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

  async getNoise(duration, level, url = null) {
    if (url) {
      const noiseContext = createAudioContext();
      // const base = await this.network.cloudUrl();
      // console.log(`${base}/audio/${url}`);
      const noiseBuffers = await loadAudioFiles(noiseContext, [`/audio/${url}`]);
      const source = createSource(noiseContext, noiseBuffers[0]);
      source.source.loop = true;
      source.gainNode.gain.value = level * 0.01;
      return source;
    } else {
      return createSource(
        this.context,
        createNoizeBuffer(this.context, duration, level),
      );
    }
  }

  createSources(context, buffers) {
    return buffers.map((buffer) => createSource(context, buffer));
  }

  calcDurationForSources(sources) {
    return sources.reduce((result, item) => {
      return result + toMilliseconds(item.source.buffer.duration);
    }, 0);
  }

  noiseTaskInstance = null;
  noiseNode = null;

  @(task(function* playNoise(){
    let noise = null;
    let timeInSeconds = 10;
    try {
      const [ level, url ] = [this.currentExerciseNoiseLevel, this.currentExerciseNoiseUrl];
      if (!level) {
        return;
      }
      noise = yield this.getNoise(timeInSeconds,
        level, url
      );
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
  })) startNoiseTask;

  @(task(function* playAudio(noizeSeconds = 0) {
    let startedSources = [];
    const hasNoize = false;
    if (hasNoize) {
      noizeSeconds = 0.3;
    }
    try {
      this.sources = this.createSources(this.context, this.buffers || []);
      this.totalDuration =
        this.calcDurationForSources(this.sources) +
        toMilliseconds(noizeSeconds);
      this.isPlaying = true;
      this.trackProgress.perform();
      if (hasNoize) {
        const noize = yield this.getNoise(
          noizeSeconds ? toSeconds(this.totalDuration) : 0,
          this.currentExerciseNoiseLevel
        );
        noize.source.start(0);
        startedSources.push(noize);
        yield timeout(toMilliseconds(noizeSeconds / 2));
      }
      for (const item of this.sources) {
        const duration = toMilliseconds(item.source.buffer.duration);
        item.source.start(0);
        startedSources.push(item);
        yield timeout(duration);
      }
      if (hasNoize) {
        yield timeout(toMilliseconds(noizeSeconds / 2));
      }
      yield timeout(10);
      this.isPlaying = false;
    } catch (e) {
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
  playTask;

  @(task(function* fakePlayAudio() {
    this.totalDuration = TIMINGS.FAKE_AUDIO;
    this.isPlaying = true;
    this.trackProgress.perform();
    yield timeout(TIMINGS.FAKE_AUDIO);
    this.isPlaying = false;
    this.totalDuration = 0;
  }).enqueue())
  fakePlayTask;

  setProgress(progress) {
    this.audioPlayingProgress = progress;
    if (progress !== 100 && (progress >= 99 || Ember.testing)) {
      this.setProgress(100);
      return;
    }
  }
}
