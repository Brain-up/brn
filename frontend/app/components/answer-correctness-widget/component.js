import Component from '@ember/component';
import { computed } from '@ember/object';

export default Component.extend({
  tagName: '',
  maxImagesNumber: 8,
  imagePath: computed(function() {
    return this.getImagePath();
  }),
  getImagePath() {
    let randomNumber = this.getAllowedRandomNumber();
    return `${
      this.isCorrect ? 'victory/victory' : 'regret/regret'
    }${randomNumber}`;
  },
  getAllowedRandomNumber() {
    const result = Math.floor(Math.random() * 10) || 1;
    return result <= this.maxImagesNumber
      ? result
      : this.getAllowedRandomNumber();
  },
});
