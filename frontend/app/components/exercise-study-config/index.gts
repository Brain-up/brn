import Component from '@glimmer/component';
import { service } from '@ember/service';
import StudyConfigService from 'brn/services/study-config';
import UserDataService, { AUDIO_PLAYBACK_RATES } from 'brn/services/user-data';
import { action } from '@ember/object';
import { on } from '@ember/modifier';
import { eq } from 'ember-truth-helpers';
import { t } from 'ember-intl';
import FaIcon from '@fortawesome/ember-fontawesome/components/fa-icon';

export default class ExerciseStudyConfigComponent extends Component {
    @service('study-config') studyConfig!: StudyConfigService;
    @service('user-data') userData!: UserDataService;

    rates = AUDIO_PLAYBACK_RATES;

    get playbackRate() {
      return this.userData.audioPlaybackRate;
    }

    @action
    onPlaybackRateChange(e: Event & { target: HTMLSelectElement }) {
      this.userData.setAudioPlaybackRate(Number.parseFloat(e.target.value));
    }


  <template>
    {{#if this.studyConfig.showImageToggler}}
      <div class="mt-2 mb-2 ml-2 mr-2">
        <button
          data-test-toggle-image-visibility
          type="button"
          class="btn-press hover:bg-blue-700 hover:text-white focus:ring-4 focus:outline-none focus:ring-blue-300 dark:border-blue-500 dark:text-blue-500 dark:hover:text-white dark:focus:ring-blue-800 inline-flex items-center mt-2 text-sm font-medium text-center text-blue-700 border border-blue-700 rounded-full"
          {{on "click" this.studyConfig.toggleImageVisibility}}
        >

          <FaIcon
            {{! template-lint-disable no-inline-styles }}
            style="width:24px;height:24px;padding:4px"
            @icon={{if this.studyConfig.showImages "align-justify" "image"}}
          />
        </button>
      </div>
    {{/if}}

    <div class="mt-2 mb-2 ml-2 mr-2 flex items-center gap-2">
      <label
        for="exercise-speech-rate"
        class="hidden sm:block text-sm font-medium text-gray-700"
      >
        {{t "study_config.speech_rate"}}
      </label>
      <select
        data-test-speech-rate
        id="exercise-speech-rate"
        aria-label={{t "study_config.speech_rate"}}
        title={{t "study_config.speech_rate"}}
        class="focus:ring-blue-300 focus:border-blue-500 py-1 pl-2 pr-6 text-sm text-blue-700 bg-white border border-blue-700 rounded-full cursor-pointer"
        {{on "change" this.onPlaybackRateChange}}
      >
        {{#each this.rates as |rate|}}
          <option value={{rate}} selected={{eq this.playbackRate rate}}>
            {{rate}}×
          </option>
        {{/each}}
      </select>
    </div>
  </template>
}
