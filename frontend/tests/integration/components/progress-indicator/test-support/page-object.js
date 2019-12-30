import { create, collection, attribute, is } from 'ember-cli-page-object';

const page = create({
  progressIndicators: collection('[data-test-progress-indicator-item]', {
    indicatorNum: attribute('data-test-progress-indicator-item-number'),
    ariaCurrent: is('[aria-current]'),
    style: attribute('style'),
  }),
});

export default page;
