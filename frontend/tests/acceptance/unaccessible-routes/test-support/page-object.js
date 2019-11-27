import { create, visitable, isVisible } from 'ember-cli-page-object';

const page = create({
  goToAccessibleTask: visitable('/series/1/exercise/1/task/1'),
  goToUnaccessibleTask: visitable('/series/1/exercise/1/task/2'),

  goToAccessibleExercise: visitable('/series/1/exercise/1/task/1'),
  goToUnaccessibleExercise: visitable('/series/1/exercise/2/task/1'),

  goToAccessibleSeries: visitable('/series/1/exercise/1/task/1'),
  goToUnaccessibleSeries: visitable('/series/2/exercise/1/task/1'),

  taskPlayerIsPresent: isVisible('[data-test-task-player]'),
});

export default page;
