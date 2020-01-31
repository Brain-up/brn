import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { run } from '@ember/runloop';
import podNames from 'ember-component-css/pod-names';

export default class ProgressIndicatorComponent extends Component {
  get styleNamespace() {
    return podNames['progress-indicator'];
  }
  @tracked progressContainerWidth = 0;
  @action
  setOffsetWidth(value, node) {
    this.progressContainerWidth = value;
    run('next', () => {
      node.setAttribute('data-test-inidicator-root', '');
    });
  }
  get progressItems() {
    return this.args.progressItems || [];
  }
  get maxAmount() {
    return Math.floor(this.progressContainerWidth / 36) - 5;
  }
  get itemsLength() {
    return this.progressItems.length;
  }
  get completedItems() {
    return this.progressItems.filter(
      ({ completedInCurrentCycle }) => completedInCurrentCycle === true,
    );
  }
  get currentItemInProgress() {
    return this.itemsLength - this.completedItems.length - 1;
  }
  get shouldHideExtraItems() {
    return this.maxAmount < this.itemsLength;
  }
  get itemsToHideCount() {
    const completedToHide =
      this.completedItems.length - Math.floor(this.maxAmount / 2);
    return completedToHide >= 0 ? completedToHide : 0;
  }
  get hiddenUncompletedCount() {
    const amount = this.itemsLength - this.itemsToHideCount - this.maxAmount;
    return amount;
  }
  get negativeHiddenUncompletedCount() {
    return this.hiddenUncompletedCount < 0 ? this.hiddenUncompletedCount : 0;
  }
  get positiveHiddenUncompletedCount() {
    return this.hiddenUncompletedCount > 0 ? this.hiddenUncompletedCount : 0;
  }
  get hiddenCompletedCount() {
    return this.itemsToHideCount + this.negativeHiddenUncompletedCount;
  }
  get betweenPadding() {
    return this.shouldHideExtraItems
      ? this.progressContainerWidth - 36 * this.maxAmount - 5
      : 0;
  }
}
