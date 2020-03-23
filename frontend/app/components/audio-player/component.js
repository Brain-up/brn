import Ember from 'ember';
import Component from '@ember/component';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { timeout, task } from 'ember-concurrency';
import { next } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
import { BufferLoader, createSource, createAudioContext, toSeconds, toMilliseconds, TIMINGS } from 'brn/utils/audio-api';

export default class AudioPlayerComponent extends Component {
  tagName = '';
  init() {
    super.init(...arguments);
    this.context = createAudioContext();
    next(() => {
      this.audio.register(this);
    });
  }

  @service audio;

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
      //
    } finally {
      if (!this.isDestroyed && !this.isDestroying) {
        this.setProgress(0);
        this.startTime = null;
      }
    }
  }).enqueue())
  trackProgress;

  @tracked autoplay = false;

  @tracked isPlaying = false;

  @tracked audioPlayingProgress = 0;

  @tracked previousPlayedUrls;

  @tracked audioFileUrl;

  async didReceiveAttrs() {
    await this.setAudioElements();
    if (this.autoplay && this.previousPlayedUrls !== this.audioFileUrl) {
      await this.playAudio();
    }
  }

  updatePlayingProgress() {
    this.setProgress(
      (100 / this.totalDuration) * (Date.now() - this.startTime),
    );
  }

  willDestroyElement() {
    this.animationInterval && clearInterval(this.animationInterval);
  }
  get filesToPlay() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  async setAudioElements() {
    if (Ember.testing) {
      this.buffers = [];
      return;
    }
    const buffers = await new Promise((resolve) => {
      let bufferLoader = new BufferLoader(
        this.context,
        [...this.filesToPlay],
        resolve,
      );
      bufferLoader.load();
    });
    this.buffers = buffers;
  }

  @action
  async playAudio() {
    if (!Ember.testing) {
      this.playTask.perform();
    } else {
      this.fakePlayTask.perform();
    }
  }

  getNoize(duration) {
    let channels = 2;
    let frameCount = this.context.sampleRate * duration;
    let myArrayBuffer = this.context.createBuffer(
      channels,
      frameCount,
      this.context.sampleRate,
    );

    for (let channel = 0; channel < channels; channel++) {
      let nowBuffering = myArrayBuffer.getChannelData(channel);
      for (let i = 0; i < frameCount; i++) {
        nowBuffering[i] = (Math.random() * 2 - 1) * 0.01;
      }
    }

    return createSource(this.context, myArrayBuffer);
  }

  createSources(context, buffers) {
    return buffers.map((buffer)=>createSource(context, buffer));
  }

  calcDurationForSources(sources) {
    return sources.reduce((result, item) => {
      return result + toMilliseconds(item.source.buffer.duration);
    }, 0);
  }

  @(task(function* playAudio(noizeSeconds = 0) {
    let startedSources = [];
    const hasNoize = noizeSeconds !== 0;
    try {
      this.sources = this.createSources(this.context, this.buffers || []);
      this.totalDuration = this.calcDurationForSources(this.sources) + toMilliseconds(noizeSeconds);
      this.isPlaying = true;
      this.trackProgress.perform();
      if (hasNoize) {
        const noize = this.getNoize(
          noizeSeconds ? toSeconds(this.totalDuration) : 0,
        );
        noize.source.start(0);
        startedSources.push(noize);
        yield timeout(toMilliseconds((noizeSeconds / 2)));
      }
      for (const item of this.sources) {
        const duration = toMilliseconds(item.source.buffer.duration);
        item.source.start(0);
        startedSources.push(item);
        yield timeout(duration);
      }
      if (hasNoize) {
        yield timeout(toMilliseconds((noizeSeconds / 2)));
      }
      yield timeout(10);
      this.isPlaying = false;
      this.previousPlayedUrls = this.audioFileUr;
    } catch (e) {
      //
    } finally {
      startedSources.forEach(({ source }) => {
        source.stop(0);
      });
      if (!this.isDestroyed && !this.isDestroying) {
        this.isPlaying = false;
        this.totalDuration = 0;
      }
    }
  }).enqueue())
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
    window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
    this.audioPlayingProgress = progress;

    if (progress === 100) {
      //
    } else if (progress >= 99 || Ember.testing) {
      this.setProgress(100);
    }
  }
}
