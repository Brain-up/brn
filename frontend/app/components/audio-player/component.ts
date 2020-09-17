import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import AudioService from 'brn/services/audio';

interface IAudioPlayerArguments {
  audioFileUrl: string;
  transparent: boolean;
}

export default class AudioPlayerComponent extends Component<IAudioPlayerArguments> {
  animationId: number = 0;

  buttonElement!: HTMLElement;

  @service('audio') audio!: AudioService;

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

  @action onUpdateSource(_: HTMLElement, [url]: string[]) {
    // @to-do remove this source control logic
    this.audio.audioFileUrl = url;
  }

  @action onUpdateProgress(_: HTMLElement, [progress]: number[]) {
    cancelAnimationFrame(this.animationId);
    this.animationId = window.requestAnimationFrame(() => {
      if (this.buttonElement) {
        this.buttonElement.style.setProperty('--progress', `${progress}%`);
      }
    });
  }
}
