import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Statistics from 'brn/components/statistics';
import type ProfileStatisticsController from 'brn/controllers/profile/statistics';

interface Signature {
  Args: {
    controller: ProfileStatisticsController;
  };
}

const tpl: TOC<Signature> = <template>
    {{pageTitle (t "profile.statistics.title")}}
    <Statistics @initialSelectedMonth={{@controller.initialSelectedMonth}} />
  </template>;

export default RouteTemplate(tpl);
