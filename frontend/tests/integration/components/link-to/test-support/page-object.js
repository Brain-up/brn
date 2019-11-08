import { create, isVisible } from 'ember-cli-page-object';

const page = create({
  hasAriaCurrentAttribute: isVisible('[aria-current]'),
});

export default page;
