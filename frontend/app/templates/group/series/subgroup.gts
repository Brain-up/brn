import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { or } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SeriesNavigation from 'brn/components/series-navigation';
import type GroupSeriesSubgroupController from 'brn/controllers/group/series/subgroup';
import type { Exercise } from 'brn/schemas/exercise';

interface Signature {
  Args: {
    model: Exercise[] & { exercises?: Exercise[] };
    controller: GroupSeriesSubgroupController;
  };
}

const tpl: TOC<Signature> = <template>
    <SeriesNavigation
      @exercises={{or @model.exercises @model}}
      @available={{@controller.availableExercises}}
    />

    {{outlet}}
  </template>;

export default RouteTemplate(tpl);
