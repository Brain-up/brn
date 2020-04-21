import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';

export default class AudioPlayerComponent extends Component {
  animationId = null;

  @service audio;

  willDestroy() {
    this.audio.stop();
  }

  get isPlaying() {
    return this.audio.isPlaying;
  }

  get audioPlayingProgress() {
    return this.audio.audioPlayingProgress;
  }

  @action playAudio() {
    this.audio.startPlayTask();
  }

  @action onUpdateSource(_, [url]) {
    // @to-do remove this source control logic
    this.audio.audioFileUrl = url;
  }

  @action onUpdateProgress(_, [progress]) {
    cancelAnimationFrame(this.animationId);
    this.animationId = window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
  }
}
