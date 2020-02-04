import {
  create,
  collection,
  attribute,
  is,
  count,
  isPresent,
  text,
} from 'ember-cli-page-object';

const page = create({
  progressIndicators: collection('[data-test-progress-indicator-item]', {
    indicatorNum: attribute('data-test-progress-indicator-item-number'),
    ariaCurrent: is('[aria-current]'),
    style: attribute('style'),
  }),
  maxItemsAmount: attribute('data-test-max-amount', '[data-test-items-list]'),
  indicatorItemsCount: count('[data-test-progress-indicator-item]'),
  shadedItems: attribute(
    'data-test-progress-indicator-item-number',
    '[data-test-shaded-progress-circle-element]',
    {
      multiple: true,
    },
  ),
  hasAnyShadedItems: isPresent('[data-test-shaded-progress-circle-element]', {
    multiple: true,
  }),
  hiddenUncompletedIndicatorValue: text('[data-test-hidden-uncompleted]'),
  hiddenCompletedIndicatorValue: text('[data-test-hidden-completed]'),
});

export default page;
