import { create, visitable, clickable, count } from 'ember-cli-page-object';

const page = create({
  goToSeriesPage: visitable('/groups/1/series/1'),
  goToFirstExercisePage: visitable('/groups/1/series/1/exercise/1'),
  startTask: clickable('[data-test-start-task-button]'),
  chooseRightAnswer: clickable('[data-test-right-answer]'),
  exerciseGroupsCount: count('[data-test-exercises-name-group]'),
  exercisesCount: count('[data-test-series-navigation-list-link]'),

  firstLevelExercisesCount: count('[data-test-exercise-level="1"]'),
});

export default page;
