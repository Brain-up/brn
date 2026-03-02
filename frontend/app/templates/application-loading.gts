import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <div class="flex flex-col items-center justify-center flex-1 w-full min-h-screen">
      <Ui::Icon::Logo />
      <div class="mt-6">
        <LoadingSpinner />
      </div>
    </div>
  </template>
);
