import { create, collection, text, attribute } from 'ember-cli-page-object';

const page = create({
  navLinks: collection('[data-test-pagination-link]', {
    linkText: text(),
    ariaCurrent: attribute('aria-current'),
  }),
});

export default page;
