import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    {{page-title "BrainUp"}}
    <Header />
    <main
      class="{{if
          (not (is-active "index"))
          "flex-col w-full mx-auto mt-2 sm:mt-4 rounded px-2 sm:px-4 pt-1 pb-1 relative"
        }}"
      style="
        {{if (not (is-active "index")) (html-safe "max-width: 1280px;")}}"
    >
      {{outlet}}
    </main>
    <Footer />
  </template>
);
