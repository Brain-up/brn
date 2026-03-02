import type { TOC } from '@ember/component/template-only';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { concat } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { or } from 'ember-truth-helpers';
import type { Contributor } from 'brn/schemas/contributor';

interface Signature {
  Args: {
    model: Contributor[];
  };
}

const tpl: TOC<Signature> = <template>
    {{pageTitle (t "header.specialists")}}
    <section class="sm:py-16 py-8">
      <h2 class="sm:mb-6 sm:text-4xl mb-4 text-2xl font-semibold text-center">
        {{t "specialists.title"}}
      </h2>
      <div class="font-openSans sm:mb-16 sm:text-lg max-w-screen-lg m-auto mb-8 text-base text-center">
        {{t "specialists.subtitle"}}
      </div>

      <div
        class="xl:grid-cols-3 lg:grid-cols-3 md:grid-cols-2 sm:grid-cols-1 grid max-w-screen-xl m-auto"
      >
        {{#each @model as |i|}}
          <div
            class="lg:mb-0 lg:flex-1 md:flex lg:mr-12 sm:p-8 lg:p-12 sm:mt-6 sm:mb-12 p-4 mt-4 mb-4 border-4 border-gray-200 rounded-lg"
          >
            <div class="flex-initial w-24 mr-4">
              <img
                src={{or i.avatar "/pictures/avatars/avatar 1.png"}}
                alt="user avatar"
                class="rounded-full"
              />
            </div>
            <div class="flex-1">
              {{#if i.login}}
                <a
                  class="mb-4 font-semibold"
                  target="_blank"
                  rel="noopener noreferrer"
                  href={{concat "https://github.com/" i.login}}
                >{{i.name}}</a>

              {{else}}
                <div class="mb-4 font-semibold">
                  {{i.name}}
                </div>

              {{/if}}

              <div class="font-openSans">
                <p class="mb-2">
                  {{i.company}}
                </p>
                <p>
                  {{i.description}}
                </p>
              </div>
            </div>
          </div>
        {{/each}}
      </div>
      <div class="mt-20 text-center">
        <a
          href="#"
          target="blank"
          rel="noopener"
          class="hover:text-blue-600 text-blue-light text-lg font-semibold underline capitalize"
        >
          {{t "specialists.all"}}
        </a>
      </div>
    </section>
  </template>;

export default RouteTemplate(tpl);
