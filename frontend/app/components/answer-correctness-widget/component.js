import Component from '@ember/component';
import { computed } from '@ember/object';

export default Component.extend({
  classNames: [
    'flex',
    'flex-wrap',
    'flex-1',
    'mt-5',
    'flex-col',
    'text-center',
  ],
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
