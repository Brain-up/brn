import Component from '@glimmer/component';
import { action } from '@ember/object';
import StudyConfigService from 'brn/services/study-config';
import { service } from '@ember/service';

interface ImageDisplayBlockSignature {
  Args: {
  pictureFileUrl?: string;
  label: string;
  };
  Element: HTMLElement;
}

export default class ImageDisplayBlock extends Component<ImageDisplayBlockSignature> {
  @service('study-config') studyConfig !: StudyConfigService;
  @action setStyle(element: HTMLDivElement) {
    if (this.studyConfig.showImages && this.args.pictureFileUrl) {
      element.style.setProperty(
        '--word-picture-url',
        `url(${this.args.pictureFileUrl})`,
      );
    }
  }

  <template>
    <div {{did-insert this.setStyle}} ...attributes>
      <div class="flex flex-wrap flex-1 mt-5">
        {{#if @pictureFileUrl}}
          {{! template-lint-configure no-inline-styles false }}
          <div
            data-test-image-block
            aria-label={{html-safe (concat "изображение " @label)}}
            role="img"
            class="image-block flex-1 mx-auto rounded"
          ></div>
        {{/if}}
      </div>
    </div>
  </template>
}
