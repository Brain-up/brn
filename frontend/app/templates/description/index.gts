import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { array, concat } from '@ember/helper';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';

export default RouteTemplate(
  <template>
    <section class="space-y-5">
      <h1 id="descriptionPageHeader" class="text-3xl font-semibold">
        {{t "description.users.header" htmlSafe=true}}
      </h1>
      <p class="font-openSans font-semibold">
        {{t "description.users.intro"}}
      </p>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="targetAudienceHeader">
      <h2 id="targetAudienceHeader" class="text-2xl font-semibold">
        {{t "description.users.audience.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2) as |i|}}
          <li>
            {{t (concat "description.users.audience." i) htmlSafe=true}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="whyImportantHeader">
      <h2 id="whyImportantHeader" class="text-2xl font-semibold">
        {{t "description.users.importance.header"}}
      </h2>
      <p class="font-openSans">
        {{t "description.users.importance.text"}}
      </p>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="howItWorksHeader">
      <h2 id="howItWorksHeader" class="text-2xl font-semibold">
        {{t "description.users.how_it_works.header"}}
      </h2>
      <ol class="font-openSans space-y-3 list-decimal list-inside">
        {{#each (array 0 1 2 3) as |i|}}
          <li>
            {{t (concat "description.users.how_it_works." i) htmlSafe=true}}
          </li>
        {{/each}}
      </ol>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="exercisesHeader">
      <h2 id="exercisesHeader" class="text-2xl font-semibold">
        {{t "description.users.exercises.header"}}
      </h2>
      <p class="font-openSans">
        {{t "description.users.exercises.intro"}}
      </p>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3) as |i|}}
          <li>
            {{t (concat "description.users.exercises." i) htmlSafe=true}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="recommendationsHeader">
      <h2 id="recommendationsHeader" class="text-2xl font-semibold">
        {{t "description.users.recommendations.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3) as |i|}}
          <li>
            {{t (concat "description.users.recommendations." i) htmlSafe=true}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="whoWeAreHeader">
      <h2 id="whoWeAreHeader" class="text-2xl font-semibold">
        {{t "description.users.who_we_are.header"}}
      </h2>
      <p class="font-openSans">
        {{t "description.users.who_we_are.intro"}}
      </p>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2) as |i|}}
          <li>
            {{t (concat "description.users.who_we_are." i) htmlSafe=true}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="helpProjectHeader">
      <h2 id="helpProjectHeader" class="text-2xl font-semibold">
        {{t "description.users.help.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        <li>
          {{t "description.users.help.0"}}
        </li>
        <li>
          {{t "description.users.help.1"}}
        </li>
        <li>
          {{t "description.users.help.2"}}
          <a
            href="https://t.me/ElenaBrainUp"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:no-underline text-blue-light underline"
          >
            https://t.me/ElenaBrainUp
          </a>
        </li>
      </ul>
    </section>

    <hr class="my-10" />

    <section
      class="space-y-5"
      role="contentinfo"
      aria-labelledby="supportHeader"
    >
      <h2 id="supportHeader" class="text-2xl font-semibold">
        {{t "description.support.header"}}
      </h2>

      <ul class="flex flex-wrap justify-around p-6 -mx-4 bg-gray-100">
        <li class="flex items-center p-3">
          <a
            class="block"
            href="https://www.epam.com"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              class="w-32"
              src="/content/pages/description/epam-logo.png"
              alt="EPAM"
            />
          </a>
        </li>

        <li class="flex items-center p-3">
          <a
            class="block"
            href="https://selectel.ru/"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              class="w-40"
              src="/content/pages/description/selectel-logo.svg"
              alt="Selectel"
            />
          </a>
        </li>

        <li class="flex items-center p-3">
          <a
            class="block"
            href="https://www.jetbrains.com"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              class="w-16"
              src="/content/pages/description/jetbrains-logo.svg"
              alt="JetBrains"
            />
          </a>
        </li>

        <li class="flex items-center p-3">
          <a
            class="block"
            href="https://cloud.yandex.ru/"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              class="w-40"
              src="/content/pages/description/yandex-cloud-logo.svg"
              alt="YandexCloud"
            />
          </a>
        </li>

        <li class="flex items-center p-3">
          <a
            class="block"
            href="https://github.com"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              class="w-16"
              src="/content/pages/description/github-logo.svg"
              alt="GitHub"
            />
          </a>
        </li>
      </ul>
    </section>

    <hr class="my-10" />

    <section>
      <p class="my-10 text-2xl font-semibold text-center">
        {{t "description.users.welcome"}}
      </p>
      <p class="font-openSans text-center">
        <LinkTo
          @route="registration"
          class="hover:bg-indigo-700 inline-block px-8 py-3 text-lg font-semibold text-white transition-colors bg-indigo-600 rounded-full"
        >
          {{t "description.users.cta"}}
        </LinkTo>
      </p>
    </section>

    <hr class="my-10" />
  </template>
);
