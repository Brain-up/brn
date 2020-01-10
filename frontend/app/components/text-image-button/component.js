import Component from '@ember/component';

export default Component.extend({
  didInsertElement() {
    this._super(...arguments);
    this.element.style.setProperty(
      '--word-picture-url',
      `url(${this.pictureFileUrl})`,
    );
  },
  pictureFileUrl: null,
  word: '',
});
