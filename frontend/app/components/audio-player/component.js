import Ember from 'ember';
import Component from '@ember/component';
import { isArray } from '@ember/array';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import customTimeout from '../../utils/custom-timeout';
import { timeout, task } from 'ember-concurrency';
import { next } from '@ember/runloop';
import { tracked } from '@glimmer/tracking';

export default class AudioPlayerComponent extends Component {
  tagName = '';
  init() {
    super.init(...arguments);
    next(() => {
      this.audio.register(this);
    });
  }

  @service audio;

  @(task(function*() {
    this.defineProgressValue(this);
    yield timeout(16);

    this.updateProgressTask.perform();
  }).enqueue())
  updateProgressTask;

  @tracked audioElements = [];

  @tracked autoplay = false;

  @tracked isPlaying = false;

  @tracked previousPlayedUrls;

  @tracked audioFileUrl;

  async didReceiveAttrs() {
    await this.setAudioElements();
    if (this.autoplay && this.previousPlayedUrls !== this.audioFileUrl) {
      await this.playAudio();
    }
  }
  willDestroyElement() {
    this.audioElements = [];
    this.animationInterval && clearInterval(this.animationInterval);
  }
  get filesToPlay() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }

  get audioElementsLength() {
    return (this.audioElements || []).reduce((totalLenght, audioElement) => {
      totalLenght = totalLenght + audioElement.duration;
      return totalLenght;
    }, 0);
  }

  async setAudioElements() {
    this.audioElements = this.filesToPlay.map((src) => {
      const audio = new Audio(src);
      audio.preload = 'metadata';
      return audio;
    });
    await Promise.all(
      this.audioElements.map(
        (a) =>
          new Promise((resolve) => {
            a.oncanplaythrough = () => {
              a.onplay = this.updateIsPlaying.bind(this, a);
              a.onended = this.updateIsPlaying.bind(this, a);
              resolve(true);
            };
          }),
      ),
    );
  }

  isPlayingElement(element) {
    return (
      !element.paused &&
      element.currentTime < element.duration &&
      !element.ended
    );
  }

  updateIsPlaying(actionElement) {
    if (!this.isDestroyed && !this.isDestroying) {
      const playStatus =
        this.audioElements.length &&
        this.audioElements.reduce((result, element) => {
          result =
            result ||
            this.isPlayingElement(element) ||
            this.isPlayingElement(actionElement);
          return result;
        }, false);
      this.isPlaying = playStatus;
      if (!this.isPlaying && this.audioPlayingProgress === 100) {
        this.audioElements.forEach((element) => (element.currentTime = 0));
      }
    }
  }

  @action
  async playAudio() {
    this.updateProgressTask.perform();
    /* eslint-disable no-unused-vars */
    for (let audioElement of this.audioElements) {
      if (!this.isDestroyed && !this.isDestroying) {
        if (Ember.testing) {
          this.isPlaying = true;
        } else {
          audioElement.play();
        }

        await customTimeout(audioElement.duration * 1000);
      }
    }

    !this.isDestroyed && !this.isDestroying
      ? this.set('previousPlayedUrls', this.audioFileUrl)
      : '';
    if (Ember.testing) {
      this.isPlaying = false;
    }
  }

  defineProgressValue() {
    const playedTime = this.audioElements.reduce(
      (currentPlayedTime, audioElement) => {
        currentPlayedTime = currentPlayedTime + audioElement.currentTime;
        return currentPlayedTime;
      },
      0,
    );
    !this.isDestroyed && !this.isDestroying
      ? this.setProgress((playedTime * 100) / this.audioElementsLength)
      : '';
  }

  setProgress(progress) {
    window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
    this.set('audioPlayingProgress', progress);

    if (progress === 100) {
      this.updateProgressTask.cancelAll();
    } else if (progress >= 99 || Ember.testing) {
      this.setProgress(100);
    }
  }
}
