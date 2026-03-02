import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    {{page-title (t "profile.statistics.title")}}
    <Statistics @initialSelectedMonth={{@controller.initialSelectedMonth}} />
  </template>
);
