import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import RegistrationForm from 'brn/components/registration-form/component';

export default RouteTemplate(
  <template>
    {{pageTitle (t "registration.title")}}
    <RegistrationForm />
  </template>
);
