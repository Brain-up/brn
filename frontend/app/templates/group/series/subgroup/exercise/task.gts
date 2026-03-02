import RouteTemplate from 'ember-route-template';

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
