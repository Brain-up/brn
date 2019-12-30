import Component from '@ember/component';
import { computed } from '@ember/object';
import { reads } from '@ember/object/computed';
import { array, raw } from 'ember-awesome-macros';

export default Component.extend({
  didInsertElement() {
    this._super(...arguments);
    this.set(
      'progressContainer',
      this.element.querySelector('#progressContainer'),
    );
  },
  progressContainer: null,
  progressItems: null,
  maxAmount: 10,
  itemsLength: reads('progressItems.length'),
  completedItemsLength: array.filterBy(
    'progressItems',
    raw('isCompleted'),
    true,
  ),
  progressContainerWidth: reads('progressContainer.offsetWidth'),
  shouldHideExtraItems: computed(
    'itemsLength',
    'progressContainerWidth',
    function() {
      return this.progressContainerWidth < this.itemsLength * 36 + 118;
    },
  ),
  hiddenCompletedCount: computed('progressItems.@each.isCompleted', function() {
    const completedToHide = this.completedItemsLength.length - 5;
    return completedToHide >= 0 ? completedToHide : 0;
  }),
  hiddenUncompletedCount: computed(
    'itemsLength',
    'hiddenCompletedCount',
    function() {
      return this.itemsLength - this.hiddenCompletedCount - this.maxAmount;
    },
  ),
  betweenPadding: computed('progressContainerWidth', function() {
    return this.shouldHideExtraItems
      ? this.progressContainerWidth - 36 * this.maxAmount - 5
      : 0;
  }),
});
