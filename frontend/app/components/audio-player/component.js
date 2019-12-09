import Component from '@ember/component';
import { set } from '@ember/object';
import { inject as service } from '@ember/service';

export default Component.extend({
  audio: service(),
  init() {
    this._super(...arguments);
    this.audio.register(this);
  },
  updateIsPlaying() {
    set(
      this,
      'isPlaying',
      this.audioElement &&
        !this.audioElement.paused &&
        this.audioElement.currentTime < this.audioElement.duration &&
        !this.audioElement.ended,
    );
  },
  playAudio() {
    return this.audioElement.play();
  },
});
