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
    return Math.floor(this.progressContainerWidth / 36) - 5;
  }),
  itemsLength: reads('progressItems.length'),
  completedItems: array.filterBy(
    'progressItems',
    raw('completedInCurrentCycle'),
    true,
  ),
  currentItemInProgress: computed('completedItems.[]', function() {
    return this.itemsLength - this.completedItems.length - 1;
  }),
  progressContainerWidth: reads('progressContainer.offsetWidth'),
  shouldHideExtraItems: computed(
    'maxAmount',
    'progressContainerWidth',
    function() {
      return this.maxAmount < this.itemsLength;
    },
  ),
  itemsToHideCount: computed(
    'progressItems.@each.completedInCurrentCycle',
    function() {
      const completedToHide =
        this.completedItems.length - Math.floor(this.maxAmount / 2);
      return completedToHide >= 0 ? completedToHide : 0;
    },
  ),
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
