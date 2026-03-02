import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <section
      class="header lg:h-screen sm:pt-16 container relative flex flex-wrap items-center mx-auto" style="max-height:860px"
    >
      <div
        class="md:w-8/12 lg:w-6/12 xl:w-6/12 container flex flex-wrap items-center w-full px-4 mx-auto"
      >
        <div>
          <h2 class="text-4xl font-semibold text-gray-700">
            {{t "index.app_info"}}
          </h2>
          <p class="mt-4 text-lg leading-relaxed text-gray-600">
            {{t "index.app_description"}}
          </p>
          <div class="mt-12 mb-4">
            <Ui::Button
              data-test-registration-form class="sm:w-7/12 sm:px-32 flex items-center justify-center w-full p-2 px-4 text-lg" @route="login"
              @title={{t "login.title"}}
            />
          </div>
        </div>
      </div>
      <Ui::Icon::Persons />
    </section>

    <AboutUs />
  </template>
);
