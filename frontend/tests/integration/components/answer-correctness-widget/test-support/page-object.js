import { create, attribute } from 'ember-cli-page-object';

const page = create({
  widgetStyleAttribute: attribute(
    'style',
    '[data-test-answer-correctness-widget]',
  ),
});

export default page;
