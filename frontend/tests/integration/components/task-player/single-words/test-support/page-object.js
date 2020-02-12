import {
  create,
  collection,
  attribute,
  isVisible,
  clickable,
  isPresent,
} from 'ember-cli-page-object';

const page = create({
  options: collection('[data-test-task-answer]', {
    optionValue: attribute('data-test-task-answer-option'),
  }),
  hasRightAnswer: isVisible('[data-test-right-answer-notification]'),
  startTask: clickable('[data-test-start-task-button]'),
  hasWrongAnswer: isPresent('[data-test-not-correct-answer]'),
});

export default page;
