import { create, attribute } from 'ember-cli-page-object';

const page = create({
  groupsLink: attribute('href', '[data-test-group-link]'),
  logoLink: attribute('href', '[data-test-logo]'),
});

export default page;
