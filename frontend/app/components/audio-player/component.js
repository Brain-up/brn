import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';

export default class AudioPlayerComponent extends Component {
  @service audio;
  
  get isPlaying() {
    return this.audio.isPlaying;
  }

  get audioPlayingProgress() {
    return this.audio.audioPlayingProgress;
  }

  @action playAudio() {
    this.audio.playAudio();
  }

  @action onUpdateSource(_, url) {
    this.audio.audioFileUrl = url;
  }

  @action onUpdateProgress(_, progress) {
     window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
  }
}
