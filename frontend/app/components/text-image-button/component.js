import Component from '@glimmer/component';
import { action } from '@ember/object';

export default class TextImageButton extends Component {
  element = null;
  @action setStyle(element, pictureFileUrl) {
    this.element = element;
    element.style.setProperty(
      '--word-picture-url',
      `url(${pictureFileUrl})`,
    );
  }
  @action addFrame(klass) {
    this.element.querySelector('button').classList.add(klass);
  }
  @action removeFrame(klass) {
    this.element.querySelector('button').classList.remove(klass);
  }
}
