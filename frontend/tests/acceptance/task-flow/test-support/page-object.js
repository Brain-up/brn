import { create, visitable, attribute, clickable } from 'ember-cli-page-object';

const page = create({
  goToFirstTask: visitable('/series/1/exercise/1/task/1'),
  goToFirstTaskSecondExercise: visitable('/series/1/exercise/2/task/3'),
  currentTaskId: attribute('data-test-task-id', '[data-test-task-player]'),
  startTask: clickable('[data-test-start-task-button]'),
});

export default page;
