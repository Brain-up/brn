import { create, collection, attribute } from 'ember-cli-page-object';

const page = create({
  options: collection('[data-test-task-answer]', {
    optionValue: attribute('data-test-task-answer-option'),
  }),
});

export default page;
