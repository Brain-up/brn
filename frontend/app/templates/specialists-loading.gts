import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonCircle from 'brn/components/skeleton/circle';

export default RouteTemplate(
  <template>
    <section class="skeleton-page py-16">
      <div class="flex justify-center mb-6">
        <SkeletonLine @class="w-64 h-10" />
      </div>
      <div class="flex justify-center mb-16">
        <SkeletonLine @class="w-96 h-6" />
      </div>
      <div class="xl:grid-cols-3 lg:grid-cols-3 md:grid-cols-2 sm:grid-cols-1 grid max-w-screen-xl m-auto">
        {{#each (array 1 2 3 4 5 6) as |_|}}
          <div class="lg:mb-0 lg:flex-1 md:flex lg:mr-12 p-12 mt-6 mb-12 border-4 border-gray-200 rounded-lg">
            <div class="flex-initial w-24 mr-4">
              <SkeletonCircle @class="w-24 h-24" />
            </div>
            <div class="flex-1">
              <SkeletonLine @class="w-32 h-5 mb-4" />
              <SkeletonLine @class="w-48 h-4 mb-2" />
              <SkeletonLine @class="w-full h-4" />
            </div>
          </div>
        {{/each}}
      </div>
    </section>
  </template>
);
