import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from '../../services/stats';
import { ref } from 'ember-ref-bucket';

export interface ToneObject {
  duration: number;
  frequency: number;
}
interface IAudioPlayerArguments {
  audioFileUrl: string | ToneObject;
  transparent: boolean;
}

export default class AudioPlayerComponent extends Component<IAudioPlayerArguments> {
  animationId: number = 0;

  @ref('buttonElement') buttonElement!: HTMLElement;
  @service('audio') audio!: AudioService;
  @service('stats') stats!: StatsService;

  willDestroy() {
    super.willDestroy(...arguments);
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
    this.stats.addEvent(StatEvents.Repeat);
  }

  @action onUpdateSource(_: HTMLElement, [url]: string | ToneObject[]) {
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
