import Component from '@glimmer/component';
import { action } from '@ember/object';
import StudyConfigService from 'brn/services/study-config';
import { inject as service } from '@ember/service';

interface IImageDisplayBlockArgs {
  pictureFileUrl?: string;
  label: string;
}

export default class ImageDisplayBlock extends Component<IImageDisplayBlockArgs> {
  @service('study-config') studyConfig !: StudyConfigService;
  @action setStyle(element: HTMLDivElement) {
    if (this.studyConfig.showImages && this.args.pictureFileUrl) {
      element.style.setProperty(
        '--word-picture-url',
        `url(${this.args.pictureFileUrl})`,
      );
    }
  }
}
