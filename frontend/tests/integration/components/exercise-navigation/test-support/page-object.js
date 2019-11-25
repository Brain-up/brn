import { create, collection, attribute, is } from 'ember-cli-page-object';

const page = create({
  navLinks: collection('[data-test-pagination-link]', {
    linkNum: attribute('data-test-pagination-link-number'),
    ariaCurrent: is('[aria-current]'),
  }),
});

export default page;
