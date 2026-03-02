import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    {{page-title @controller.group.name}}
    <div class="flex flex-col flex-grow">
      {{#unless @controller.headerAndNavShown}}
        <div class="flex items-center ml-4">
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
              <SubgroupNavigation @group={{model-for-route "group.series"}} />
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
        class="series-container w-7/8 md:w-5/6 lg:w-3/4 xl:w-3/4 container flex mx-auto"
      >
        <div class="series-page--canvas flex justify-center flex-grow">
          {{outlet}}
        </div>
      </div>
    </div>
  </template>
);
