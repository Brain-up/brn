import Ember from 'ember';
import Component from '@ember/component';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { timeout, task } from 'ember-concurrency';
import { next } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
import { createSource, createNoizeBuffer, loadAudioFiles, createAudioContext, toSeconds, toMilliseconds, TIMINGS } from 'brn/utils/audio-api';

export default class AudioPlayerComponent extends Component {
  tagName = '';
  context = createAudioContext();
  init() {
    super.init(...arguments);
    this._attrs = {
      audioFileUrl: this.audioFileUrl,
      autoplay: this.autoplay,
      disabled: this.disabled
    }
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
      // NOP
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
    console.log('didReceiveAttrs', [this.previousPlayedUrls, this.audioFileUrl]);

    if (this._attrs.disabled !== this.disabled) {
      console.log('changed: disabled', this.disabled);
    }
    if (this._attrs.autoplay !== this.autoplay) {
      console.log('changed: autoplay', this.autoplay);
    }
    if (this._attrs.audioFileUrl !== this.audioFileUrl) {
      console.log('changed: audioFileUrl', this.audioFileUrl);
    }
    
    this._attrs = {
      audioFileUrl: this.audioFileUrl,
      autoplay: this.autoplay,
      disabled: this.disabled
    }
    
    if (this.isPlaying) {
      return;
    }
    await this.setAudioElements(this.context, this.filesToPlay);
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

  async setAudioElements(context, filesToPlay) {
    if (Ember.testing) {
      this.buffers = [];
      return;
    }
    this.buffers = await loadAudioFiles(context, filesToPlay);
  }

  @action
  async playAudio() {
    if (!Ember.testing) {
      this.playTask.perform();
    } else {
      this.fakePlayTask.perform();
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

  getNoize(duration) {
    return createSource(this.context, createNoizeBuffer(this.context, duration));
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
      this.previousPlayedUrls = this.audioFileUrl;
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
    this.audioPlayingProgress = progress;
    if (progress !== 100 && (progress >= 99 || Ember.testing)) {
      this.setProgress(100);
      return;
    }
    window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
  }
}
