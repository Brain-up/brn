import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';

export default RouteTemplate(
  <template>
    <div class="not-accessable">
      <h3>
        Страница, которую вы хотите посетить, недоступна.
      </h3>
      <h5>
        Вы можете перейти на
        <LinkTo @route="index">главную страницу</LinkTo>
      </h5>
    </div>
  </template>
);
