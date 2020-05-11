import Component from '@ember/component';

export default class ImageDisplayBlock extends Component {
  didInsertElement() {
    this.pictureFileUrl &&
      this.element.style.setProperty(
        '--word-picture-url',
        `url(${this.pictureFileUrl})`,
      );
  }
}
