import Component from '@glimmer/component';
import { action } from '@ember/object';
import UserDataService from 'brn/services/user-data';
import { inject as service } from '@ember/service';

interface ITextImageButtonArgs {
  pictureFileUrl: string;
  word: string;
  disabled: boolean;
}

export default class TextImageButton extends Component<ITextImageButtonArgs> {
  @service('user-data') userData!: UserDataService;
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
      if (this.shouldLoadSymbol()) {
        const symbols = await fetch(
          'https://www.opensymbols.org/api/v1/symbols/search?q=' +
            encodeURIComponent(this.args.word) +
            '&locale=' +
            encodeURIComponent(this.userData.activeLocale.split('-')[0]),
        );
        const data = await symbols.json();
        if (data.length) {
          const img = new Image();
          img.crossOrigin = 'anonymous';
          img.src = data[0].image_url;
          img.onload = () => {
            const canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height;
            const ctx = canvas.getContext('2d');
            ctx?.drawImage(img, 0, 0);
            const dataURL = canvas.toDataURL('image/png');
            element.style.setProperty('--word-picture-url', `url(${dataURL})`);
          };
        }
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
