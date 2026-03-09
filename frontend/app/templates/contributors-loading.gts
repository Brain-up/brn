import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonCircle from 'brn/components/skeleton/circle';

export default RouteTemplate(
  <template>
    <section class="skeleton-page bg-gradient-to-r from-blue-100 to-purple-100 sm:p-8 lg:p-16 p-4 rounded-lg">
      <div class="flex justify-center sm:mb-6 mb-4">
        <SkeletonLine @class="sm:w-64 w-48 sm:h-10 h-8" />
      </div>
      <div class="flex justify-center sm:mb-16 mb-8">
        <SkeletonLine @class="sm:w-96 w-64 sm:h-6 h-5" />
      </div>
      <div class="md:flex flex-wrap max-w-screen-xl m-auto">
        {{#each (array 1 2 3 4 5 6) as |_|}}
          <div class="team-member md:flex-1 sm:px-6 sm:py-10 px-4 py-6 text-center bg-white rounded-lg">
            <div class="flex justify-center mb-2">
              <SkeletonCircle @class="w-24 h-24" />
            </div>
            <SkeletonLine @class="w-32 h-5 mx-auto mb-1" />
            <SkeletonLine @class="w-48 h-4 mx-auto mb-1" />
            <SkeletonLine @class="w-40 h-4 mx-auto" />
          </div>
        {{/each}}
      </div>
    </section>
  </template>
);
