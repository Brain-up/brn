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
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import groupBy from 'brn/helpers/group-by';
import type { Contributor } from 'brn/schemas/contributor';

interface Signature {
  Args: {
    model: Contributor[];
  };
}

const tpl: TOC<Signature> = <template>
    {{pageTitle (t "header.contributors")}}
    <section class="bg-gradient-to-r from-blue-100 to-purple-100 sm:p-8 lg:p-16 p-4 rounded-lg">
      <h2 class="sm:mb-6 sm:text-4xl mb-4 text-2xl font-semibold text-center">
        {{t "contributors.title"}}
      </h2>
      <div class="font-openSans sm:mb-16 sm:text-lg mb-8 text-base text-center">
        {{t "contributors.subtitle"}}
      </div>

      {{#each-in (groupBy "kind" @model) as |kindType group|}}
        <h3 class="pb-2 text-xl font-medium">{{t
            (concat "contributors.kinds." kindType)
          }}</h3>
        <div
          class="md:flex max-w-screen-xl m-auto flex-wrap
           "
        >
          {{#each group as |i|}}
            <div
              class="team-member md:flex-1 sm:px-6 sm:py-10 px-4 py-6 text-center bg-white rounded-lg"
            >
              <div>
                <img
                  src={{or i.avatar 'https://avatars.githubusercontent.com/u/34234'}}
                  alt="user avatar"
                  class="w-24 m-auto mb-2 rounded-full"
                />
              </div>
              {{#if i.login}}
                <a
                  class="mb-1 text-lg font-medium"
                  target="_blank"
                  rel="noopener noreferrer"
                  href={{concat "https://github.com/" i.login}}
                >{{i.name}}</a>
              {{else}}
                <div class="mb-1 text-lg font-medium">
                  {{i.name}}
                </div>
              {{/if}}
              <div class="font-openSans leading-5">
                {{i.company}}<br />
                {{i.description}}
              </div>
            </div>
          {{/each}}
        </div>
      {{/each-in}}

      <div class="mt-20 text-center">
        <a
          href="#"
          target="blank"
          rel="noopener"
          class="hover:text-blue-600 text-blue-light text-lg font-semibold underline capitalize"
        >
          {{t "contributors.all"}}
        </a>
      </div>
    </section>
  </template>;

export default RouteTemplate(tpl);
