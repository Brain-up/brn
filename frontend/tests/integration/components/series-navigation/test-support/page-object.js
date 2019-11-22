import { create, collection, text } from 'ember-cli-page-object';

const page = create({
  links: collection('[data-test-series-navigation-list-link]', {
    text: text(),
  }),
});

export default page;
