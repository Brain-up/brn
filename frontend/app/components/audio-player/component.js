import Component from '@ember/component';
import { set } from '@ember/object';

export default Component.extend({
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
  actions: {
    playAudio() {
      return this.audioElement.play();
    },
  },
});
