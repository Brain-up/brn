import { create, collection, attribute, is } from 'ember-cli-page-object';

const page = create({
  navLinks: collection('[data-test-progress-indicator-item]', {
    linkNum: attribute('data-test-progress-indicator-item-number'),
    ariaCurrent: is('[aria-current]'),
  }),
});

export default page;
