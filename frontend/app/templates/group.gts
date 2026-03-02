import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import modelForRoute from 'brn/helpers/model-for-route';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Breadcrumbs from 'brn/components/breadcrumbs/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SubgroupNavigation from 'brn/components/subgroup-navigation';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import GroupNavigation from 'brn/components/group-navigation/template';

export default RouteTemplate(
  <template>
    {{pageTitle @controller.group.name}}
    <div class="flex flex-col flex-grow">
      {{#unless @controller.headerAndNavShown}}
        <div class="sm:ml-4 flex items-center ml-2">
          <Breadcrumbs />
        </div>
      {{/unless}}
      {{#unless @controller.headerAndNavShown}}
        {{#if @controller.isInSubgroupView}}
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
          {{outlet}}
        </div>
      </div>
    </div>
  </template>
);
