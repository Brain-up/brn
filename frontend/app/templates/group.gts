import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import modelForRoute from 'brn/helpers/model-for-route';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Breadcrumbs from 'brn/components/breadcrumbs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SubgroupNavigation from 'brn/components/subgroup-navigation';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import GroupNavigation from 'brn/components/group-navigation';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
import type GroupController from 'brn/controllers/group';

interface Signature {
  Args: {
    controller: GroupController;
  };
}

const tpl: TOC<Signature> = <template>
    {{pageTitle @controller.group.name}}
    <div class="flex flex-col flex-grow">
      {{#unless @controller.headerAndNavShown}}
        <div class="sm:ml-4 flex items-center ml-2">
          <Breadcrumbs />
        </div>
      {{/unless}}
      {{#unless @controller.headerAndNavShown}}
        {{#if @controller.isExerciseTypeSelection}}
          {{! On group.index, show exercise type selection cards instead of tabs }}
        {{else if @controller.isInSubgroupView}}
          <div class="flex justify-center w-full">
            <aside
              class="bg-gradient-to-b from-white sticky top-0 z-10 w-full rounded-bl rounded-br"
              style="max-width: 80vw;"
            >
              <SubgroupNavigation @group={{modelForRoute "group.series"}} />
            </aside>
          </div>
        {{else}}
          <div class="flex justify-center w-full">
            <aside
              class="bg-gradient-to-b from-white sticky top-0 z-10 w-full rounded-bl rounded-br"
              style="max-width: 80vw;"
            >
              <GroupNavigation @group={{@controller.group}} @series={{@controller.series}} />
            </aside>
          </div>
        {{/if}}
      {{/unless}}
      <div
        class="series-container md:w-5/6 lg:w-3/4 xl:w-3/4 container flex w-11/12 mx-auto"
      >
        <div class="series-page--canvas flex justify-center flex-grow">
          {{#if @controller.isExerciseTypeSelection}}
            <div class="w-full">
              <div class="sm:text-base sm:mt-2 flex justify-center mt-0 text-sm">
                <h3>{{t "groups.exercise_selection"}}</h3>
              </div>
              <div class="sm:grid-cols-3 md:grid-cols-4 gap-y-3 sm:gap-y-4 grid grid-cols-2 gap-3 mx-2 mt-4 mb-4">
                {{#each @controller.series as |seriesItem|}}
                  <LinkTo
                    @route="group.series"
                    @model={{seriesItem.id}}
                    class="flex flex-col overflow-hidden border-2 border-gray-200 rounded-lg shadow-lg hover:ring"
                    data-test-exercise-type-card
                  >
                    <div class="sm:px-4 sm:py-6 flex items-center justify-center flex-1 px-3 py-4 bg-white">
                      <div class="sm:text-lg text-base font-medium text-center text-gray-700">
                        {{seriesItem.name}}
                      </div>
                    </div>
                    {{#if seriesItem.description}}
                      <div class="sm:px-3 sm:py-2 px-2 py-1 bg-gray-100">
                        <div class="sm:text-sm text-xs text-center text-gray-500">
                          {{seriesItem.description}}
                        </div>
                      </div>
                    {{/if}}
                  </LinkTo>
                {{/each}}
              </div>
            </div>
          {{else}}
            {{outlet}}
          {{/if}}
        </div>
      </div>
    </div>
  </template>;

export default RouteTemplate(tpl);
