import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import isActive from 'brn/helpers/is-active';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ExerciseGroup from 'brn/components/exercise-group';

export default RouteTemplate(
  <template>
    <div class="w-full">
      {{#if (isActive "group.series.index")}}
        {{pageTitle @model.name}}
        <div class="sm:grid-cols-4 gap-y-2 sm:gap-y-3 grid grid-cols-3 mx-2 mb-4">
          {{#each @controller.exerciseSubGroups as |group|}}
            <ExerciseGroup @group={{group}} />
          {{/each}}
        </div>
      {{/if}}
      {{outlet}}
    </div>
  </template>
);
