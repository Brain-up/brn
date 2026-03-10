import Component from '@glimmer/component';
import { service } from '@ember/service';
import IntlService from 'ember-intl/services/intl';
import { t } from 'ember-intl';

interface MonthlyDetailSignature {
  Args: {
    data: Record<string, unknown>[] | null;
    isLoading: boolean;
    monthLabel: string;
  };
  Element: HTMLElement;
}

function formatTime(seconds: unknown): string {
  const s = Math.floor(Number(seconds) || 0);
  const mins = Math.floor(s / 60);
  const secs = s % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

function asString(value: unknown): string {
  return value != null ? String(value) : '';
}

function formatAccuracy(tasksCount: unknown, wrongAnswers: unknown): string {
  const tasks = Number(tasksCount) || 0;
  const wrong = Number(wrongAnswers) || 0;
  if (tasks === 0) return '-';
  const accuracy = ((tasks - wrong) / tasks) * 100;
  return `${Math.round(accuracy)}%`;
}

function formatDate(dateString: unknown): string {
  if (!dateString) return '-';
  const date = new Date(String(dateString));
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export default class MonthlyDetailComponent extends Component<MonthlyDetailSignature> {
  @service('intl') intl!: IntlService;

  get sortedEntries(): Record<string, unknown>[] {
    if (!this.args.data) return [];
    return [...this.args.data].sort((a, b) => {
      const dateA = a.startTime ? new Date(a.startTime as string).getTime() : 0;
      const dateB = b.startTime ? new Date(b.startTime as string).getTime() : 0;
      return dateB - dateA;
    });
  }

  <template>
    <div class="mt-6" ...attributes>
      <h3 class="mb-3 text-sm font-bold text-gray-700 uppercase tracking-wider">
        {{t "profile.monthly_detail.title" month=@monthLabel}}
      </h3>

      {{#if @isLoading}}
        <div class="animate-pulse space-y-2">
          <div class="h-8 bg-gray-200 rounded"></div>
          <div class="h-8 bg-gray-200 rounded"></div>
          <div class="h-8 bg-gray-200 rounded"></div>
        </div>
      {{else if this.sortedEntries.length}}
        <div class="overflow-x-auto">
          <table data-test-monthly-detail-table class="min-w-full text-sm border border-gray-200 rounded-lg">
            <thead class="bg-gray-50">
              <tr>
                <th class="sticky left-0 bg-gray-50 px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                  {{t "profile.monthly_detail.date"}}
                </th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                  {{t "profile.monthly_detail.exercise"}}
                </th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                  {{t "profile.monthly_detail.time"}}
                </th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                  {{t "profile.monthly_detail.accuracy"}}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              {{#each this.sortedEntries as |entry|}}
                <tr data-test-monthly-detail-row class="hover:bg-gray-50">
                  <td class="sticky left-0 bg-white px-3 py-2 text-gray-700 whitespace-nowrap">
                    {{if entry.startTime (formatDate entry.startTime) "-"}}
                  </td>
                  <td class="px-3 py-2 text-gray-600">
                    #{{asString entry.exerciseId}}
                  </td>
                  <td class="px-3 py-2 text-gray-600 whitespace-nowrap">
                    {{if entry.executionSeconds (formatTime entry.executionSeconds) "-"}}
                  </td>
                  <td class="px-3 py-2 text-gray-600">
                    {{formatAccuracy entry.tasksCount entry.wrongAnswers}}
                  </td>
                </tr>
              {{/each}}
            </tbody>
          </table>
        </div>
      {{else}}
        <p class="text-sm text-gray-400">{{t "profile.monthly_detail.empty"}}</p>
      {{/if}}
    </div>
  </template>
}
