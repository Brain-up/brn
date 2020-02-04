import { create, attribute, hasClass } from 'ember-cli-page-object';

const page = create({
  buttonText: attribute(
    'data-test-task-answer-option',
    '[data-test-task-answer]',
  ),
  buttonIsDisabled: attribute('disabled', '[data-test-task-answer]'),
  buttonIsSelected: hasClass('selected', '[data-test-task-answer]'),
});

export default page;
