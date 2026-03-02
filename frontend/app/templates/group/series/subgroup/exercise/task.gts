import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import queue from 'brn/helpers/queue';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import TaskPlayer from 'brn/components/task-player';

export default RouteTemplate(
  <template>
    <TaskPlayer
      data-test-task-player
      data-test-task-id={{@model.id}}
      data-test-task-exercise-id={{@model.exercise.id}}
      @onRightAnswer={{queue @controller.nextTaskTransition @model.savePassed}}
      @task={{@model}}
    />
  </template>
);
