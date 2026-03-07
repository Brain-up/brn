import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonCircle from 'brn/components/skeleton/circle';

export default RouteTemplate(
  <template>
    <section class="skeleton-page bg-gradient-to-r from-blue-100 to-purple-100 p-16 rounded-lg">
      <div class="flex justify-center mb-6">
        <SkeletonLine @class="w-64 h-10" />
      </div>
      <div class="flex justify-center mb-16">
        <SkeletonLine @class="w-96 h-6" />
      </div>
      <div class="md:flex flex-wrap max-w-screen-xl m-auto">
        {{#each (array 1 2 3 4 5 6) as |_|}}
          <div class="team-member md:flex-1 px-6 py-10 text-center bg-white rounded-lg">
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
