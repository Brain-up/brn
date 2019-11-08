import { create, count } from 'ember-cli-page-object';

const page = create({
  linksAmount: count('[data-test-series-navigation-list-link]'),
});

export default page;
