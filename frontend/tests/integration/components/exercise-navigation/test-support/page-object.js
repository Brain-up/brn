import { create, collection, text, is } from 'ember-cli-page-object';

const page = create({
  navLinks: collection('[data-test-pagination-link]', {
    linkText: text(),
    ariaCurrent: is('[aria-current]'),
  }),
});

export default page;
