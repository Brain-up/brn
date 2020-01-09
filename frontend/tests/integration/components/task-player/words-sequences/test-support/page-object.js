import { create, collection, attribute } from 'ember-cli-page-object';

const page = create({
  buttons: collection('[data-test-task-answer]', {
    word: attribute('data-test-task-answer-option'),
  }),
});

export default page;
