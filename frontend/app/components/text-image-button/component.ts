import Component from '@glimmer/component';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import ImageLocatorService from 'brn/services/image-locator';

interface ITextImageButtonArgs {
  pictureFileUrl: string;
  word: string;
  disabled: boolean;
}

export default class TextImageButton extends Component<ITextImageButtonArgs> {
  @service('image-locator') imageLocator!: ImageLocatorService;
  declare element: HTMLDivElement;
  shouldLoadSymbol() {
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
}
