import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { not } from 'ember-truth-helpers';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import isActive from 'brn/helpers/is-active';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import htmlSafe from 'brn/helpers/html-safe';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Header from 'brn/components/header/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import Footer from 'brn/components/footer/template';

export default RouteTemplate(
  <template>
    {{pageTitle "BrainUp"}}
    <Header />
    <main
      class="{{if
          (not (isActive "index"))
          "flex-col w-full mx-auto mt-2 sm:mt-4 rounded px-2 sm:px-4 pt-1 pb-1 relative"
        }}"
      style="
        {{if (not (isActive "index")) (htmlSafe "max-width: 1280px;")}}"
    >
      {{outlet}}
    </main>
    <Footer />
  </template>
);
