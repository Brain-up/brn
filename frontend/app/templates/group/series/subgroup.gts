import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <SeriesNavigation
      @exercises={{or @model.exercises @model}}
      @available={{@controller.availableExercises}}
    />

    {{outlet}}
  </template>
);
