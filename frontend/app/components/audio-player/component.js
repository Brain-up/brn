import Component from '@ember/component';
import ENV from 'brn/config/environment';
import { set } from '@ember/object';
import { eq } from 'ember-awesome-macros';

export default Component.extend({
  envConfig: ENV,
  testEnv: eq('envConfig.environment', 'test'),
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
