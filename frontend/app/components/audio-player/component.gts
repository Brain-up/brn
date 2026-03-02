import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import AudioService from 'brn/services/audio';
import StatsService, { StatEvents } from '../../services/stats';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { ref } from 'ember-ref-bucket';

export interface ToneObject {
  duration: number;
  frequency: number;
}
interface AudioPlayerSignature {
  Args: {
  audioFileUrl: string | ToneObject;
  transparent: boolean;
  };
  Element: HTMLElement;
}

export default class AudioPlayerComponent extends Component<AudioPlayerSignature> {
  animationId = 0;

  @ref('buttonElement') buttonElement!: HTMLElement;
  @service('audio') audio!: AudioService;
  @service('stats') stats!: StatsService;

  willDestroy() {
    super.willDestroy();
    this.audio.stop();
  }

  get isPlaying() {
    return this.audio.isBusy;
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

  <template>
    <div
      class="flex mr-1 ml-1"
      ...attributes
      {{did-update this.onUpdateProgress this.audioPlayingProgress}}
      {{did-insert this.onUpdateProgress this.audioPlayingProgress}}
      {{did-update this.onUpdateSource @audioFileUrl}}
      {{did-insert this.onUpdateSource @audioFileUrl}}
    >
      {{! template-lint-configure no-invalid-interactive false }}
      {{#let this.isPlaying as |disablePlayButton|}}
        <Ui::Button
          class="play-button
            {{if disablePlayButton "opacity-50" "hover:bg-blue-700"}}
            {{if @transparent "invisible"}}
            w-full leading-none"
          data-test-play-audio-button
          data-test-playing-progress={{this.audioPlayingProgress}}
          disabled={{disablePlayButton}}
          {{on "click" this.playAudio}}
          {{create-ref "buttonElement"}}
        >
          {{! template-lint-disable no-inline-styles }}
          <svg
            style="display:inline;"
            width="32"
            height="32"
            viewBox="0 0 32 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M14.6666 6.6665L7.99996 11.9998H2.66663V19.9998H7.99996L14.6666 25.3332V6.6665Z"
              stroke="white"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ></path>
            <path
              d="M25.4266 6.57324C27.9262 9.07361 29.3305 12.4644 29.3305 15.9999C29.3305 19.5354 27.9262 22.9262 25.4266 25.4266M20.72 11.2799C21.9698 12.5301 22.6719 14.2255 22.6719 15.9932C22.6719 17.761 21.9698 19.4564 20.72 20.7066"
              stroke="white"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ></path>
          </svg>
          <span class="sm:pl-6 sm:text-base inline-block text-xs tracking-wider">{{t
              "audio_player.listen"
            }}</span>
        </Ui::Button>
      {{/let}}
    </div>
  </template>
}
