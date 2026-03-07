import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import UiIconLogo from 'brn/components/ui/icon/logo';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import LoadingSpinner from 'brn/components/loading-spinner';

export default RouteTemplate(
  <template>
    <div class="flex flex-col items-center justify-center flex-1 w-full min-h-screen">
      <UiIconLogo />
      <div class="mt-6">
        <LoadingSpinner />
      </div>
    </div>
  </template>
);
