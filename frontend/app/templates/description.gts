import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import isActive from 'brn/helpers/is-active';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';

export default RouteTemplate(
  <template>
    <article role="contentinfo">
      <nav class="flex mb-8 border-b border-gray-200" aria-label="About page navigation">
        <LinkTo
          @route="description.index"
          class="px-3 sm:px-6 py-3 text-base sm:text-lg font-semibold border-b-2 transition-colors
            {{if (isActive "description.index") "border-indigo-600 text-indigo-600" "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}}"
        >
          {{t "description.tabs.users"}}
        </LinkTo>
        <LinkTo
          @route="description.developers"
          class="px-3 sm:px-6 py-3 text-base sm:text-lg font-semibold border-b-2 transition-colors
            {{if (isActive "description.developers") "border-indigo-600 text-indigo-600" "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}}"
        >
          {{t "description.tabs.developers"}}
        </LinkTo>
      </nav>

      {{outlet}}
    </article>
  </template>
);
