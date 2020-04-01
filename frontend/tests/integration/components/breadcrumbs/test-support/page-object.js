import {
  create,
  collection,
  attribute,
  isVisible,
  text,
} from 'ember-cli-page-object';

const page = create({
  groupLinks: collection('[data-test-group-breadcrumb]', {
    anchor: collection('a', {
      href: attribute('href'),
      text: text(),
    }),
  }),
  seriesLinks: collection('[data-test-series-breadcrumb]', {
    anchor: collection('a', {
      href: attribute('href'),
      text: text(),
    }),
  }),
  seriesLinkExists: isVisible('[data-test-series-breadcrumb]'),
});

export default page;
