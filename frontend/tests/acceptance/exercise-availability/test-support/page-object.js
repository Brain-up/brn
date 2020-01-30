import { create, visitable, clickable } from 'ember-cli-page-object';

const page = create({
  goToSeriesPage: visitable('/groups/1/series/1'),
  goToFirstExercisePage: visitable('/groups/1/series/1/exercise/1'),
  startTask: clickable('[data-test-start-task-button]'),
});

export default page;
