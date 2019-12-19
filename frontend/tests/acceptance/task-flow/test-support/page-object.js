import { create, visitable, attribute } from 'ember-cli-page-object';

const page = create({
  goToFirstTask: visitable('/series/1/exercise/1/task/1'),
  goToFirstTaskSecondExercise: visitable('/series/1/exercise/2/task/3'),
  currentTaskId: attribute('data-test-task-id', '[data-test-task-player]'),
});

export default page;
