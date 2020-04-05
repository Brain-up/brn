import Ember from 'ember';
import Component from '@ember/component';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { timeout, task } from 'ember-concurrency';
import { next } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';
import { createSource, createNoizeBuffer, loadAudioFiles, createAudioContext, toSeconds, toMilliseconds, TIMINGS } from 'brn/utils/audio-api';
import Service from '@ember/service';
export default class AudioService extends Service {

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


  @action async startPlayTask() {
    if (this.isPlaying) {
      return;
    }
    await this.setAudioElements(this.filesToPlay);
    await this.playAudio();
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
  }).keepLatest().maxConcurrency(1))
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
