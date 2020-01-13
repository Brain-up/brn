import Component from '@ember/component';
import { set } from '@ember/object';
import { isArray } from '@ember/array';
import { computed } from '@ember/object';
import { inject as service } from '@ember/service';
import customTimeout from '../../utils/custom-timeout';

export default Component.extend({
  audio: service(),
  init() {
    this._super(...arguments);
    this.set('playAudio', this.playAudio.bind(this));
    this.audio.register(this);
  },
  async didReceiveAttrs() {
    this._super(...arguments);

    await this.setAudioElements();
    if (this.autoplay) {
      await this.playAudio();
    }
  },
  willDestroyElement() {
    this._super(...arguments);
    this.set('audioElements', []);
  },
  autoplay: false,
  filesToPlay: computed('audioFileUrl', 'audioFileUrl.[]', function() {
    return isArray(this.audioFileUrl) ? this.audioFileUrl : [this.audioFileUrl];
  }),
  async setAudioElements() {
    this.set(
      'audioElements',
      this.filesToPlay.map((src) => {
        const audio = new Audio(src);
        audio.onplay = this.updateIsPlaying.bind(this);
        audio.onended = this.updateIsPlaying.bind(this);
        audio.preload = 'metadata';
        return audio;
      }),
    );
    await Promise.all(
      this.audioElements.map(
        (a) =>
          new Promise((resolve) => {
            a.oncanplaythrough = () => resolve(true);
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
  updateIsPlaying() {
    set(
      this,
      'isPlaying',
      this.audioElements.length &&
        this.audioElements.reduce((result, element) => {
          result = result || this.isPlayingElement(element);
          return result;
        }, false),
    );
  },
  async playAudio() {
    /* eslint-disable no-unused-vars */
    for (let audioElement of this.audioElements) {
      if (!this.isDestroyed && !this.isDestroying) {
        audioElement.play();
        await customTimeout(audioElement.duration * 1000);
      }
    }
  },
});
