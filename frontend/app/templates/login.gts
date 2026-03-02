import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    {{page-title (t "login.title")}}

    <div class="w-full max-w-lg mx-auto">
      <LoginForm />
    </div>
  </template>
);
