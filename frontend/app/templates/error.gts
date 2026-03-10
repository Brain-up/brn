import RouteTemplate from 'ember-route-template';
import ErrorPage from 'brn/components/error-page';

export default RouteTemplate<{ model: unknown }>(
  <template>
    <ErrorPage @model={{@model}} />
  </template>
);
