import Component from '@glimmer/component';
import { action } from '@ember/object';

export default class TextImageButton extends Component {
  element = null;
  @action setStyle(element, [pictureFileUrl]) {
    this.element = element;
    element.style.setProperty(
      '--word-picture-url',
      `url(${pictureFileUrl})`,
    );
  }
  get button() {
    return this.element && this.element.querySelector('button');
  }
  @action addFrame(klass) {
    this.button && this.button.classList.add(klass);
  }
  @action removeFrame(klass) {
    this.button && this.button.classList.remove(klass);
  }
}
