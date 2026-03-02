import Component from '@glimmer/component';
import { action } from '@ember/object';
import { service } from '@ember/service';
import ImageLocatorService from 'brn/services/image-locator';
import StudyConfigService from 'brn/services/study-config';

interface TextImageButtonSignature {
  Args: {
  pictureFileUrl: string;
  word: string;
  disabled: boolean;
  };
  Element: HTMLElement;
}

export default class TextImageButton extends Component<TextImageButtonSignature> {
  @service('image-locator') imageLocator!: ImageLocatorService;
  @service('study-config') studyConfig!: StudyConfigService;
  declare element: HTMLDivElement;
  shouldLoadSymbol() {
    if (!this.studyConfig.showImages) {
      return false;
    }
    return this.args.word.trim().split(' ').length === 1;
  }
  @action setStyle(element: HTMLDivElement, [pictureFileUrl]: string[]) {
    this.element = element;
    element.style.setProperty('--word-picture-url', `url(${pictureFileUrl})`);
    const img = new Image(10, 10);
    img.src = pictureFileUrl;
    img.onerror = async () => {
      if (this.isDestroying || this.isDestroyed) {
        return;
      }
      if (this.shouldLoadSymbol()) {
        const dataURL = await this.imageLocator.getPictureForWordAsDataURL(this.args.word);
        if (!dataURL) {
          return;
        }
        element.style.setProperty('--word-picture-url', `url(${dataURL})`);
      }
    };
  }
  get button(): HTMLButtonElement | null {
    return this.element && this.element.querySelector('button');
  }
  @action addFrame(klass: string) {
    this.button && this.button.classList.add(klass);
  }
  @action removeFrame(klass: string) {
    this.button && this.button.classList.remove(klass);
  }

  <template>
    <div
      class="{{unless this.studyConfig.showImages "text-mode"}}"
      data-test-text-image-button
      {{did-insert this.setStyle @pictureFileUrl}}
      {{did-update this.setStyle @pictureFileUrl}}
    >
      <button
        data-test-task-answer
        data-test-task-answer-option={{@word}}
        disabled={{@disabled}}
        type="button"
        {{on "click" @clickAction}}
        class="btn-press bg-transparent rounded text-black mb-2 focus:outline-none focus:shadow-outline transition-all duration-500 ease-in-out border-2
          {{if @isSelected "selected"}}
          {{if @disabled "opacity-50"}}"
      >
        <span class="word" data-test-word>{{@word}}</span>
        {{#if (and @checked @isSelected)}}
          {{#if @isCorrect}}
            <FaIcon
              class="correctness-indicator right"
              @icon="check-circle"
              {{did-insert (fn this.addFrame "border-green-500")}}
              {{will-destroy (fn this.removeFrame "border-green-500")}}
            />
          {{else}}
            <FaIcon
              class="correctness-indicator wrong"
              @icon="times-circle"
              {{did-insert (fn this.addFrame "border-red-500")}}
              {{will-destroy (fn this.removeFrame "border-red-500")}}
            />
          {{/if}}
        {{/if}}
      </button>
    </div>
  </template>
}
