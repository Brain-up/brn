import Component from '@glimmer/component';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { tracked } from '@glimmer/tracking';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import { t } from 'ember-intl';
import type IntlService from 'ember-intl/services/intl';
import Audiogram from 'brn/components/audiometry/audiogram';
import { loadHistory, clearHistory } from 'brn/utils/audiometry-history-storage';
import type { AudiometryHistoryEntry } from 'brn/utils/audiometry-history-storage';

interface HistorySignature {
  Args: {};
  Element: HTMLElement;
}

export default class AudiometryHistoryComponent extends Component<HistorySignature> {
  @service('intl') intl!: IntlService;

  @tracked entries: AudiometryHistoryEntry[] = loadHistory();
  @tracked expandedId: string | null = null;

  get hasEntries(): boolean {
    return this.entries.length > 0;
  }

  get signalEntries(): AudiometryHistoryEntry[] {
    return this.entries.filter((e) => e.audiometryType === 'SIGNALS');
  }

  get speechEntries(): AudiometryHistoryEntry[] {
    return this.entries.filter((e) => e.audiometryType === 'SPEECH');
  }

  get latestPtaLeft(): number | null {
    const entry = this.signalEntries[0];
    return entry?.ptaLeft ?? null;
  }

  get latestPtaRight(): number | null {
    const entry = this.signalEntries[0];
    return entry?.ptaRight ?? null;
  }

  get ptaTrendLeft(): { date: string; pta: number }[] {
    return this.signalEntries
      .filter((e) => e.ptaLeft !== null)
      .map((e) => ({ date: e.date, pta: e.ptaLeft! }))
      .reverse();
  }

  get ptaTrendRight(): { date: string; pta: number }[] {
    return this.signalEntries
      .filter((e) => e.ptaRight !== null)
      .map((e) => ({ date: e.date, pta: e.ptaRight! }))
      .reverse();
  }

  @action
  toggleExpand(id: string) {
    this.expandedId = this.expandedId === id ? null : id;
  }

  @action
  handleClearHistory() {
    clearHistory();
    this.entries = [];
    this.expandedId = null;
  }

  @action
  refreshHistory() {
    this.entries = loadHistory();
  }

  <template>
    <div class="mt-8" data-test-audiometry-history ...attributes>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-800">{{t "audiometry.history_title"}}</h2>
        {{#if this.hasEntries}}
          <button
            data-test-clear-history
            type="button"
            class="text-xs text-red-500 hover:text-red-700"
            {{on "click" this.handleClearHistory}}
          >
            {{t "audiometry.history_clear"}}
          </button>
        {{/if}}
      </div>

      {{#if this.hasEntries}}
        {{! PTA summary cards }}
        {{#if (hasSignalResults this.signalEntries)}}
          <div class="grid grid-cols-2 gap-3 mb-4">
            {{#if (isNotNull this.latestPtaLeft)}}
              <div class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-center" data-test-pta-left>
                <p class="text-xs text-blue-500 mb-1">{{t "audiometry.ear_left"}} PTA</p>
                <p class="text-xl font-bold text-blue-700">{{this.latestPtaLeft}} <span class="text-xs font-normal">dB</span></p>
                {{#if this.signalEntries.[0].classificationLeft}}
                  <p class="text-xs text-blue-600 mt-1">
                    {{t (classificationKey this.signalEntries.[0].classificationLeft)}}
                  </p>
                {{/if}}
              </div>
            {{/if}}
            {{#if (isNotNull this.latestPtaRight)}}
              <div class="p-3 bg-red-50 border border-red-200 rounded-lg text-center" data-test-pta-right>
                <p class="text-xs text-red-500 mb-1">{{t "audiometry.ear_right"}} PTA</p>
                <p class="text-xl font-bold text-red-700">{{this.latestPtaRight}} <span class="text-xs font-normal">dB</span></p>
                {{#if this.signalEntries.[0].classificationRight}}
                  <p class="text-xs text-red-600 mt-1">
                    {{t (classificationKey this.signalEntries.[0].classificationRight)}}
                  </p>
                {{/if}}
              </div>
            {{/if}}
          </div>

          {{! PTA trend }}
          {{#if (hasTrend this.ptaTrendLeft this.ptaTrendRight)}}
            <div class="mb-4 p-3 bg-gray-50 border border-gray-200 rounded-lg" data-test-pta-trend>
              <p class="text-xs font-medium text-gray-600 mb-2">{{t "audiometry.history_pta_trend"}}</p>
              <div class="flex gap-4 overflow-x-auto text-xs text-gray-500">
                {{#each (trendEntries this.ptaTrendLeft this.ptaTrendRight) as |point|}}
                  <div class="flex flex-col items-center shrink-0">
                    <span class="text-gray-400">{{formatDate point.date}}</span>
                    {{#if (isNotNull point.ptaLeft)}}
                      <span class="text-blue-600">L: {{point.ptaLeft}}</span>
                    {{/if}}
                    {{#if (isNotNull point.ptaRight)}}
                      <span class="text-red-600">R: {{point.ptaRight}}</span>
                    {{/if}}
                  </div>
                {{/each}}
              </div>
            </div>
          {{/if}}
        {{/if}}

        {{! History entries list }}
        <div class="space-y-2">
          {{#each this.entries as |entry|}}
            <div
              data-test-history-entry
              class="border border-gray-200 rounded-lg overflow-hidden"
            >
              <button
                data-test-history-toggle
                type="button"
                class="w-full flex items-center justify-between p-3 text-left hover:bg-gray-50 transition-colors"
                {{on "click" (fn this.toggleExpand entry.id)}}
              >
                <div class="flex items-center gap-2">
                  <span class="text-xs px-2 py-0.5 rounded-full {{typeColorClass entry.audiometryType}}">
                    {{entry.audiometryType}}
                  </span>
                  <span class="text-sm font-medium text-gray-700">{{entry.testName}}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-400">{{formatDate entry.date}}</span>
                  <svg class="w-4 h-4 text-gray-400 transition-transform {{if (isExpanded entry.id this.expandedId) 'rotate-180'}}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </button>

              {{#if (isExpanded entry.id this.expandedId)}}
                <div class="p-3 border-t border-gray-100 bg-gray-50" data-test-history-detail>
                  <div class="text-xs text-gray-500 mb-2">
                    {{t "audiometry.history_duration" seconds=entry.executionSeconds}}
                  </div>

                  {{#if (isSignalType entry.audiometryType)}}
                    <Audiogram
                      @leftEarThresholds={{entry.leftEarThresholds}}
                      @rightEarThresholds={{entry.rightEarThresholds}}
                    />
                    <div class="mt-2 flex gap-4 text-xs text-gray-500">
                      {{#if (isNotNull entry.ptaLeft)}}
                        <span>{{t "audiometry.ear_left"}} PTA: {{entry.ptaLeft}} dB</span>
                      {{/if}}
                      {{#if (isNotNull entry.ptaRight)}}
                        <span>{{t "audiometry.ear_right"}} PTA: {{entry.ptaRight}} dB</span>
                      {{/if}}
                    </div>
                  {{else if entry.speechResults}}
                    <div class="text-center">
                      <span class="text-2xl font-bold text-indigo-600">
                        {{speechAccuracy entry.speechResults}}%
                      </span>
                      <p class="text-xs text-gray-500">
                        {{entry.speechResults.correct}} / {{entry.speechResults.total}}
                      </p>
                    </div>
                  {{/if}}
                </div>
              {{/if}}
            </div>
          {{/each}}
        </div>
      {{else}}
        <p class="text-sm text-gray-400" data-test-no-history>{{t "audiometry.history_empty"}}</p>
      {{/if}}
    </div>
  </template>
}

function isNotNull(value: unknown): boolean {
  return value !== null && value !== undefined;
}

function hasSignalResults(entries: AudiometryHistoryEntry[]): boolean {
  return entries.length > 0;
}

function hasTrend(left: { date: string; pta: number }[], right: { date: string; pta: number }[]): boolean {
  return left.length > 1 || right.length > 1;
}

interface TrendPoint {
  date: string;
  ptaLeft: number | null;
  ptaRight: number | null;
}

function trendEntries(
  left: { date: string; pta: number }[],
  right: { date: string; pta: number }[],
): TrendPoint[] {
  const dates = new Set<string>();
  for (const e of left) dates.add(e.date);
  for (const e of right) dates.add(e.date);

  const leftMap = new Map(left.map((e) => [e.date, e.pta]));
  const rightMap = new Map(right.map((e) => [e.date, e.pta]));

  return [...dates]
    .sort()
    .map((date) => ({
      date,
      ptaLeft: leftMap.get(date) ?? null,
      ptaRight: rightMap.get(date) ?? null,
    }));
}

function formatDate(iso: string): string {
  try {
    const d = new Date(iso);
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch {
    return iso;
  }
}

function isExpanded(id: string, expandedId: string | null): boolean {
  return id === expandedId;
}

function typeColorClass(type: string): string {
  if (type === 'SIGNALS') return 'bg-blue-100 text-blue-700';
  if (type === 'SPEECH') return 'bg-purple-100 text-purple-700';
  return 'bg-gray-100 text-gray-700';
}

function isSignalType(type: string): boolean {
  return type === 'SIGNALS';
}

function speechAccuracy(results: { correct: number; total: number }): number {
  if (results.total === 0) return 0;
  return Math.round((results.correct / results.total) * 100);
}

function classificationKey(classification: string | null): string {
  if (!classification) return '';
  return `audiometry.hearing_${classification}`;
}
