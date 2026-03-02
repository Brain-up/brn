import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { or } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SeriesNavigation from 'brn/components/series-navigation/component';

export default RouteTemplate(
  <template>
    <SeriesNavigation
      @exercises={{or @model.exercises @model}}
      @available={{@controller.availableExercises}}
    />

    {{outlet}}
  </template>
);
