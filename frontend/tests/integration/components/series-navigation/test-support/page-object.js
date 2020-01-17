import { create, collection, text } from 'ember-cli-page-object';

const page = create({
  links: collection('[data-test-series-navigation-list-link]', {
    text: text(),
  }),
  headers: collection('[data-test-series-navigation-header]', {
    text: text(),
  }),
});

export default page;
