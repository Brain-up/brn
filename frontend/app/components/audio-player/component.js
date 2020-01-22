import Ember from 'ember';
import Component from '@ember/component';
import { set } from '@ember/object';
import { isArray } from '@ember/array';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import customTimeout from '../../utils/custom-timeout';
import { timeout, task } from 'ember-concurrency';

export default Component.extend({
  audio: service(),
  init() {
    this._super(...arguments);
    this.set('playAudio', this.playAudio.bind(this));
    this.audio.register(this);
  },
  didInsertElement() {
    this._super(...arguments);
    const button = this.element.querySelector('button');
    this.set('setButtonProperty', button.style.setProperty.bind(button.style));
  },
  async didReceiveAttrs() {
    this._super(...arguments);
    await this.setAudioElements();
    if (this.autoplay && this.previousPlayedUrls !== this.audioFileUrl) {
      await this.playAudio();
    }
  },
  willDestroyElement() {
    this._super(...arguments);
    this.set('audioElements', []);
    this.animationInterval && clearInterval(this.animationInterval);
  },
  autoplay: false,
  filesToPlay: computed('audioFileUrl', 'audioFileUrl.[]', function() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }),
  audioElementsLength: computed('audioElements', function() {
    return this.audioElements.reduce((totalLenght, audioElement) => {
      totalLenght = totalLenght + audioElement.duration;
      return totalLenght;
    }, 0);
  }),

  async setAudioElements() {
    this.set(
      'audioElements',
      this.filesToPlay.map((src) => {
        const audio = new Audio(src);
        audio.preload = 'metadata';
        return audio;
      }),
    );
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
  },

  isPlayingElement(element) {
    return (
      !element.paused &&
      element.currentTime < element.duration &&
      !element.ended
    );
  },

  updateIsPlaying(actionElement) {
    if (!this.isDestroyed && !this.isDestroying) {
      set(
        this,
        'isPlaying',
        this.audioElements.length &&
          this.audioElements.reduce((result, element) => {
            result =
              result ||
              this.isPlayingElement(element) ||
              this.isPlayingElement(actionElement);
            return result;
          }, false),
      );
      if (!this.isPlaying) {
        this.audioElements.forEach((element) => (element.currentTime = 0));
      }
    }
  },

  async playAudio() {
    this.updateProgressTask.perform();
    /* eslint-disable no-unused-vars */
    for (let audioElement of this.audioElements) {
      if (!this.isDestroyed && !this.isDestroying) {
        if (Ember.testing) {
          this.set('isPlaying', true);
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
      this.set('isPlaying', false);
    }
  },

  updateProgressTask: task(function*() {
    this.defineProgressValue(this);
    yield timeout(16);

    this.updateProgressTask.perform();
  }).enqueue(),

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
  },

  setProgress(progress) {
    window.requestAnimationFrame(() =>
      this.setButtonProperty('--progress', progress + '%'),
    );
    this.set('audioPlayingProgress', progress);
    if (progress >= 99 || Ember.testing) {
      this.set('audioPlayingProgress', 100);
      this.updateProgressTask.cancelAll();
    }
  },
});
