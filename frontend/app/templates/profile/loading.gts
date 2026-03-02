import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonCircle from 'brn/components/skeleton/circle';

export default RouteTemplate(
  <template>
    <section class="skeleton-page border-4 border-gray-100 rounded-md">
      <div class="bg-gradient-to-r from-blue-100 to-purple-100 lg:flex justify-between p-4">
        <SkeletonCircle @class="w-32 h-32 m-auto" />
      </div>
      <div class="sm:p-8 lg:p-12 p-4">
        <div class="mb-4">
          <SkeletonLine @class="w-24 h-4 mb-2" />
          <SkeletonLine @class="w-full h-8" />
          <SkeletonLine @class="w-full h-8" />
        </div>
        <div class="mb-4">
          <SkeletonLine @class="w-24 h-4 mb-2" />
          <SkeletonLine @class="w-full h-8" />
        </div>
        <div class="mb-4">
          <SkeletonLine @class="w-24 h-4 mb-2" />
          <div class="flex space-x-4">
            <SkeletonLine @class="w-20 h-5" />
            <SkeletonLine @class="w-20 h-5" />
          </div>
        </div>
        <div class="mb-4">
          <SkeletonLine @class="w-24 h-4 mb-2" />
          <SkeletonLine @class="w-full h-8" />
        </div>
        <div class="mb-4">
          <SkeletonLine @class="w-48 h-5" />
        </div>
      </div>
    </section>
  </template>
);
