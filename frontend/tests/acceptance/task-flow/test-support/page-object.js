import { create, visitable, attribute, clickable } from 'ember-cli-page-object';

const page = create({
  goToFirstTask: visitable('/groups/1/series/1/subgroup/1/exercise/1/task/1'),
  goToFirstTaskSecondExercise: visitable(
    '/groups/1/series/1/subgroup/1/exercise/2/task/3',
  ),
  currentTaskId: attribute('data-test-task-id', '[data-test-task-player]'),
  startTask: clickable('[data-test-start-task-button]'),
});

export default page;
