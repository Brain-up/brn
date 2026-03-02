import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <div class="skeleton-page flex flex-col flex-grow">
      <div class="sm:text-base sm:mt-6 flex justify-center mt-0 text-sm">
        <Skeleton::Line @class="w-48 h-6" />
      </div>
      <ol class="series-container sm:flex-row flex flex-col items-center justify-center mt-3">
        <li class="list-item sm:mr-6 mr-0">
          <Skeleton::Block @class="skeleton-exercise-card rounded-lg" />
        </li>
        <li class="list-item sm:mt-0 mt-3">
          <Skeleton::Block @class="skeleton-exercise-card rounded-lg" />
        </li>
      </ol>
    </div>
  </template>
);
