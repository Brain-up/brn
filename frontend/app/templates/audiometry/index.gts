import Component from '@glimmer/component';
import RouteTemplate from 'ember-route-template';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import pageTitle from 'ember-page-title/helpers/page-title';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { t } from 'ember-intl';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { LinkTo } from '@ember/routing';
import AudiometryHistory from 'brn/components/audiometry/history';
import type { Headphone } from 'brn/schemas/headphone';

interface AudiometryTest {
  id: string;
  locale: string;
  name: string;
  audiometryType: string;
  description: string;
}

interface Signature {
  Args: {
    model: {
      tests: AudiometryTest[];
      headphones: Headphone[];
    };
  };
}

class AudiometryIndexTemplate extends Component<Signature> {
  get hasHeadphones(): boolean {
    return this.args.model.headphones.length > 0;
  }

  get tests(): AudiometryTest[] {
    return this.args.model.tests;
  }

  <template>
    {{pageTitle (t "audiometry.title")}}
    <div class="max-w-3xl mx-auto p-4 sm:p-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-1">{{t "audiometry.title"}}</h1>
      <p class="text-sm text-gray-500 mb-6">{{t "audiometry.subtitle"}}</p>

      {{#each this.tests as |test|}}
        <div data-test-audiometry-card class="mb-4 p-4 bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-start justify-between">
            <div>
              <h2 class="text-lg font-semibold text-gray-800" data-test-audiometry-name>{{test.name}}</h2>
              <p class="text-sm text-gray-500 mt-1">{{test.description}}</p>
            </div>
            {{#if this.hasHeadphones}}
              <LinkTo
                @route="audiometry.test"
                @model={{test.id}}
                data-test-start-test
                class="btn-press shrink-0 ml-4 px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700"
              >
                {{t "audiometry.start_test"}}
              </LinkTo>
            {{/if}}
          </div>
        </div>
      {{/each}}

      {{#unless this.hasHeadphones}}
        <div data-test-no-headphones-warning class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p class="text-sm text-yellow-700">
            {{t "audiometry.no_headphones_warning"}}
            <LinkTo @route="profile" class="underline font-medium hover:text-yellow-900">Profile</LinkTo>
          </p>
        </div>
      {{/unless}}

      <AudiometryHistory />
    </div>
  </template>
}

export default RouteTemplate(AudiometryIndexTemplate);
