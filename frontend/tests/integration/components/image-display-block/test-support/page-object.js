import { create, attribute } from 'ember-cli-page-object';

const page = create({
  ariaLabel: attribute('aria-label', '[data-test-image-block]'),
  imageAttribute: attribute('style', '[data-test-component-container]'),
});

export default page;
