import { create, visitable, isVisible } from 'ember-cli-page-object';

const page = create({
  goToAccessibleTask: visitable('/groups/1/series/1/exercise/1/task/1'),
  goToUnaccessibleTask: visitable('/groups/1/series/1/exercise/1/task/2'),

  goToRightTaskInTheExercise: visitable('/groups/1/series/1/exercise/2/task/3'),
  goToWrongTaskInTheExercise: visitable('/groups/1/series/1/exercise/2/task/1'),

  goToAccessibleSeries: visitable('/groups/1/series/1/exercise/1/task/1'),
  goToUnaccessibleSeries: visitable('/groups/1/series/2/exercise/1/task/1'),

  taskPlayerIsPresent: isVisible('[data-test-task-player]'),
});

export default page;
