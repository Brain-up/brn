import Component from '@glimmer/component';
import { action } from '@ember/object';

interface IImageDisplayBlockArgs {
  pictureFileUrl?: string;
  label: string;
}

export default class ImageDisplayBlock extends Component<IImageDisplayBlockArgs> {
  @action setStyle(element: HTMLDivElement) {
    if (this.args.pictureFileUrl) {
      element.style.setProperty(
        '--word-picture-url',
        `url(${this.args.pictureFileUrl})`,
      );
    }
  }
}
