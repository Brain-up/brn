import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonBlock from 'brn/components/skeleton/block';

export default RouteTemplate(
  <template>
    <div class="skeleton-page flex flex-col flex-grow">
      <div class="sm:ml-4 flex items-center ml-2">
        <SkeletonLine @class="w-64 h-4" />
      </div>
      <div class="flex justify-center w-full">
        <aside class="bg-gradient-to-b from-white sticky top-0 z-10 w-full rounded-bl rounded-br" style="max-width: 80vw;">
          <div class="flex items-center overflow-x-auto" style="padding: 10px 20px; min-height: 80px; gap: 10px;">
            <SkeletonLine @class="flex-shrink-0 rounded-lg" style="width: 140px; height: 48px;" />
            <SkeletonLine @class="flex-shrink-0 rounded-lg" style="width: 140px; height: 48px;" />
            <SkeletonLine @class="flex-shrink-0 rounded-lg" style="width: 140px; height: 48px;" />
            <SkeletonLine @class="flex-shrink-0 rounded-lg" style="width: 140px; height: 48px;" />
          </div>
        </aside>
      </div>
      <div class="series-container md:w-5/6 lg:w-3/4 xl:w-3/4 container flex w-11/12 mx-auto">
        <div class="series-page--canvas flex justify-center flex-grow">
          <div class="sm:grid-cols-4 gap-y-2 sm:gap-y-3 grid w-full grid-cols-3 mx-2 mb-4">
            {{#each (array 1 2 3 4 5 6 7 8) as |_|}}
              <div class="sm:m-2 flex flex-col m-1 overflow-hidden border-2 border-gray-200 rounded-lg shadow-lg">
                <SkeletonBlock @class="sm:h-40 h-16 rounded-none" />
                <div class="sm:px-2 sm:py-2 flex items-center justify-center px-1 py-1 bg-gray-200">
                  <SkeletonLine @class="w-20 h-4" />
                </div>
              </div>
            {{/each}}
          </div>
        </div>
      </div>
    </div>
  </template>
);
