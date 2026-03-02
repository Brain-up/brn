import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonBlock from 'brn/components/skeleton/block';

export default RouteTemplate(
  <template>
    <div class="skeleton-page w-full">
      <div class="sm:grid-cols-4 gap-y-2 sm:gap-y-3 grid w-full grid-cols-3 mx-2 mb-4">
        {{#each (array 1 2 3 4 5 6) as |_|}}
          <div class="sm:m-2 flex flex-col m-1 overflow-hidden border-2 border-gray-200 rounded-lg shadow-lg">
            <SkeletonBlock @class="sm:h-40 h-16 rounded-none" />
            <div class="sm:px-2 sm:py-2 flex items-center justify-center px-1 py-1 bg-gray-200">
              <SkeletonLine @class="w-20 h-4" />
            </div>
          </div>
        {{/each}}
      </div>
    </div>
  </template>
);
