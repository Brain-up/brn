import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    {{page-title (t "registration.title")}}
    <RegistrationForm />
  </template>
);
