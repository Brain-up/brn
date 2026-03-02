import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import LoginForm from 'brn/components/login-form';

export default RouteTemplate(
  <template>
    {{pageTitle (t "login.title")}}

    <div class="w-full max-w-lg mx-auto">
      <LoginForm />
    </div>
  </template>
);
