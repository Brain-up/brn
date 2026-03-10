import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonBlock from 'brn/components/skeleton/block';

export default RouteTemplate(
  <template>
    <div class="max-w-3xl mx-auto p-4 sm:p-6">
      <SkeletonLine @class="w-48 h-8 mb-4" />
      <SkeletonBlock @class="h-20 mb-3 rounded-lg" />
      <SkeletonBlock @class="h-20 mb-3 rounded-lg" />
    </div>
  </template>
);
