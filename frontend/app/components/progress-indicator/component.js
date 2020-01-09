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
  maxAmount: computed('itemsLength', 'progressContainerWidth', function() {
    return Math.floor(this.progressContainerWidth / 36) - 2;
  }),
  itemsLength: reads('progressItems.length'),
  completedItemsLength: array.filterBy(
    'progressItems',
    raw('isCompleted'),
    true,
  ),
  progressContainerWidth: reads('progressContainer.offsetWidth'),
  shouldHideExtraItems: computed(
    'maxAmount',
    'progressContainerWidth',
    function() {
      return this.maxAmount < this.itemsLength;
    },
  ),
  itemsToHideCount: computed('progressItems.@each.isCompleted', function() {
    const completedToHide = this.completedItemsLength.length - 5;
    return completedToHide >= 0 ? completedToHide : 0;
  }),
  hiddenUncompletedCount: computed(
    'itemsLength',
    'itemsToHideCount',
    function() {
      const amount = this.itemsLength - this.itemsToHideCount - this.maxAmount;
      return amount;
    },
  ),
  negativeHiddenUncompletedCount: computed(
    'hiddenUncompletedCount',
    function() {
      return this.hiddenUncompletedCount < 0 ? this.hiddenUncompletedCount : 0;
    },
  ),
  positiveHiddenUncompletedCount: computed(
    'hiddenUncompletedCount',
    function() {
      return this.hiddenUncompletedCount > 0 ? this.hiddenUncompletedCount : 0;
    },
  ),
  hiddenCompletedCount: computed(
    'itemsToHideCount',
    'negativeHiddenUncompletedCount',
    function() {
      return this.itemsToHideCount + this.negativeHiddenUncompletedCount;
    },
  ),
  betweenPadding: computed('progressContainerWidth', function() {
    return this.shouldHideExtraItems
      ? this.progressContainerWidth - 36 * this.maxAmount - 5
      : 0;
  }),
});
