import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonLine from 'brn/components/skeleton/line';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import SkeletonBlock from 'brn/components/skeleton/block';

export default RouteTemplate(
  <template>
    <div class="skeleton-page flex flex-col flex-grow">
      <div class="sm:text-base sm:mt-6 flex justify-center mt-0 text-sm">
        <SkeletonLine @class="w-48 h-6" />
      </div>
      <ol class="series-container sm:flex-row flex flex-col items-center justify-center mt-3">
        <li class="list-item sm:mr-6 mr-0">
          <SkeletonBlock @class="skeleton-exercise-card rounded-lg" />
        </li>
        <li class="list-item sm:mt-0 mt-3">
          <SkeletonBlock @class="skeleton-exercise-card rounded-lg" />
        </li>
      </ol>
    </div>
  </template>
);
