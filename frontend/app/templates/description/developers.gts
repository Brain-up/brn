import RouteTemplate from 'ember-route-template';

export default RouteTemplate(
  <template>
    <section class="space-y-5">
      <h1 class="text-3xl font-semibold">
        {{t "description.dev.header" htmlSafe=true}}
      </h1>
      <p class="font-openSans font-semibold">
        {{t "description.dev.intro"}}
      </p>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="whatIsDoneHeader">
      <h2 id="whatIsDoneHeader" class="text-2xl font-semibold">
        {{t "description.dev.done.header"}}
      </h2>
      <p class="font-openSans">
        {{t "description.dev.done.text"}}
      </p>
      <h3 class="text-xl font-semibold">
        {{t "description.dev.done.features_header"}}
      </h3>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3 4) as |i|}}
          <li>
            {{t (concat "description.dev.done.features." i)}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="usersHeader">
      <h2 id="usersHeader" class="text-2xl font-semibold">
        {{t "description.dev.target_users.header"}}
      </h2>
      <ol class="font-openSans space-y-3 list-decimal list-inside">
        {{#each (array 0 1 2) as |i|}}
          <li>
            {{t (concat "description.dev.target_users." i)}}
          </li>
        {{/each}}
      </ol>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="uniquenessHeader">
      <h2 id="uniquenessHeader" class="text-2xl font-semibold">
        {{t "description.dev.uniqueness.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3) as |i|}}
          <li>
            {{t (concat "description.dev.uniqueness." i)}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="growthHeader">
      <h2 id="growthHeader" class="text-2xl font-semibold">
        {{t "description.dev.growth.header"}}
      </h2>
      <p class="font-openSans">
        {{t "description.dev.growth.intro"}}
      </p>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3) as |i|}}
          <li>
            {{t (concat "description.dev.growth.tasks." i)}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="volunteersHeader">
      <h2 id="volunteersHeader" class="text-2xl font-semibold">
        {{t "description.dev.volunteers.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2 3 4 5) as |i|}}
          <li>
            {{t (concat "description.dev.volunteers." i)}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="benefitsHeader">
      <h2 id="benefitsHeader" class="text-2xl font-semibold">
        {{t "description.dev.benefits.header"}}
      </h2>
      <ul class="font-openSans space-y-3 list-disc list-inside">
        {{#each (array 0 1 2) as |i|}}
          <li>
            {{t (concat "description.dev.benefits." i)}}
          </li>
        {{/each}}
      </ul>
    </section>

    <hr class="my-10" />

    <section
      class="space-y-5"
      role="contentinfo"
      aria-labelledby="teamHeader"
    >
      <h2 id="teamHeader" class="text-2xl font-semibold">
        {{t "description.team.header"}}
      </h2>

      <section class="font-openSans">
        <p class="mb-10">
          {{t "description.team.thanks_words"}}
        </p>

        <ul class="sm:p-10 flex flex-wrap justify-between p-4 -mx-4 bg-gray-100">
          {{#each @controller.persons.teamMembers as |teamMember i|}}
            <li class="w-24 h-24 m-2 my-4 overflow-hidden rounded-full">
              <img
                src={{teamMember.img}}
                alt={{t "description.team.image_alt" i=i}}
              />
            </li>
          {{/each}}
        </ul>
      </section>
    </section>

    <hr class="my-10" />

    <section class="space-y-5" aria-labelledby="joinCtaHeader">
      <h2 id="joinCtaHeader" class="text-2xl font-semibold">
        {{t "description.dev.cta.header"}}
      </h2>
      <ol class="font-openSans space-y-3 list-decimal list-inside">
        <li>
          {{t "description.dev.cta.0"}}
          <a
            href="https://brainup.site"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:no-underline text-blue-light underline"
          >
            www.brainup.site
          </a>
        </li>
        <li>
          {{t "description.dev.cta.1"}}
        </li>
        <li>
          {{t "description.dev.cta.2"}}
        </li>
        <li>
          {{t "description.dev.cta.3"}}
          <a
            href="https://github.com/Brain-up/brn"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:no-underline text-blue-light underline"
          >
            https://github.com/Brain-up/brn
          </a>
        </li>
        <li>
          {{t "description.dev.cta.4"}}
          <a
            href="https://t.me/ElenaBrainUp"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:no-underline text-blue-light underline"
          >
            https://t.me/ElenaBrainUp
          </a>
        </li>
      </ol>
    </section>

    <hr class="my-10" />

    <section>
      <p class="my-10 text-2xl font-semibold text-center">
        {{t "description.dev.closing"}}
      </p>
      <p class="font-openSans text-center">
        {{t "description.dev.signature"}}
      </p>
    </section>

    <hr class="my-10" />
  </template>
);
