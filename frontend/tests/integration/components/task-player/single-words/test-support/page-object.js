import {
  create,
  collection,
  attribute,
  isVisible,
  clickable,
} from 'ember-cli-page-object';

const page = create({
  options: collection('[data-test-task-answer]', {
    optionValue: attribute('data-test-task-answer-option'),
  }),
  hasRightAnswer: isVisible('[data-test-right-answer-notification]'),
  startTask: clickable('[data-test-start-task-button]'),
  chooseRightAnswer: clickable('[data-test-right-answer]'),
  wrongAnswers: collection('[data-test-wrong-answer]', {
    choose: clickable(),
  }),
});

export default page;
