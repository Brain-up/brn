import 'billboard.js/dist/billboard.min.css';
import Component from '@glimmer/component';
import { action } from '@ember/object';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import type { Chart, ChartOptions } from 'billboard.js';
import willDestroy from '@ember/render-modifiers/modifiers/will-destroy';
import didInsert from '@ember/render-modifiers/modifiers/did-insert';
import didUpdate from '@ember/render-modifiers/modifiers/did-update';
import { t } from 'ember-intl';
import type IntlService from 'ember-intl/services/intl';

const STANDARD_FREQUENCIES = [125, 250, 500, 1000, 2000, 4000, 8000];
const FREQ_LABELS = ['125', '250', '500', '1K', '2K', '4K', '8K'];

interface AudiogramSignature {
  Args: {
    leftEarThresholds: Record<number, number>;
    rightEarThresholds: Record<number, number>;
  };
  Element: HTMLElement;
}

export default class AudiogramComponent extends Component<AudiogramSignature> {
  @service('intl') intl!: IntlService;

  private chart: Chart | undefined;
  private chartId = `audiogram-${Math.random().toString(36).slice(2, 8)}`;

  get chartElemRef(): HTMLDivElement | null {
    return document.getElementById(this.chartId) as HTMLDivElement | null;
  }

  private get leftLabel(): string {
    return `${this.intl.t('audiometry.ear_left')} (X)`;
  }

  private get rightLabel(): string {
    return `${this.intl.t('audiometry.ear_right')} (O)`;
  }

  private buildColumns(): [string, ...(number | null)[]][] {
    const leftData: (number | null)[] = STANDARD_FREQUENCIES.map(
      (f) => this.args.leftEarThresholds[f] ?? null,
    );
    const rightData: (number | null)[] = STANDARD_FREQUENCIES.map(
      (f) => this.args.rightEarThresholds[f] ?? null,
    );
    return [
      [this.leftLabel, ...leftData],
      [this.rightLabel, ...rightData],
    ];
  }

  @action
  async buildChart() {
    const el = this.chartElemRef;
    if (!el) return;

    const { line, bb } = await import('billboard.js');

    // Guard against component being destroyed during async import
    if (this.isDestroyed || this.isDestroying) return;

    // Eagerly capture all i18n strings before passing to billboard.js
    // to avoid accessing this.intl in callbacks after component destruction
    const leftLabel = this.leftLabel;
    const rightLabel = this.rightLabel;
    const freqAxisLabel = this.intl.t('audiometry.audiogram_freq_axis');
    const dbAxisLabel = this.intl.t('audiometry.audiogram_db_axis');
    const normalLabel = this.intl.t('audiometry.hearing_normal');

    const columns = this.buildColumns();

    const options: ChartOptions = {
      bindto: el,
      data: {
        type: line(),
        columns,
        colors: {
          [leftLabel]: '#2563EB',
          [rightLabel]: '#DC2626',
        },
        // connectNull is supported by billboard.js but missing from the type definitions
        connectNull: true,
      } as ChartOptions['data'],
      axis: {
        x: {
          type: 'category' as const,
          categories: FREQ_LABELS,
          label: {
            text: freqAxisLabel,
            position: 'outer-center',
          },
        },
        y: {
          inverted: true,
          min: -10,
          max: 100,
          label: {
            text: dbAxisLabel,
            position: 'outer-middle',
          },
          tick: { values: [-10, 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100] },
        },
      },
      grid: {
        y: {
          show: true,
          lines: [
            {
              value: 20,
              text: normalLabel,
              class: 'audiogram-threshold-line',
            },
          ],
        },
        x: { show: true },
      },
      point: { r: 5 },
      legend: { show: true },
      tooltip: {
        format: {
          value: (value: number) => `${value} ${dbAxisLabel}`,
        },
      },
      regions: [
        { axis: 'y', start: -10, end: 20, class: 'audiogram-region-normal' },
        { axis: 'y', start: 20, end: 40, class: 'audiogram-region-mild' },
        { axis: 'y', start: 40, end: 55, class: 'audiogram-region-moderate' },
        { axis: 'y', start: 55, end: 70, class: 'audiogram-region-mod-severe' },
        { axis: 'y', start: 70, end: 100, class: 'audiogram-region-severe' },
      ],
    };

    this.chart = bb.generate(options);
  }

  @action
  updateChart() {
    if (!this.chart) return;
    const columns = this.buildColumns();
    this.chart.load({ columns });
  }

  @action
  onWillDestroy() {
    this.chart?.destroy();
  }

  <template>
    <div class="audiogram-container" data-test-audiogram>
      <div
        id={{this.chartId}}
        class="audiogram-chart"
        ...attributes
        {{willDestroy this.onWillDestroy}}
        {{didInsert this.buildChart}}
        {{didUpdate this.updateChart @leftEarThresholds @rightEarThresholds}}
      ></div>
      <div class="flex flex-wrap justify-center gap-3 mt-3 text-xs text-gray-500">
        <span class="inline-flex items-center gap-1">
          <span class="inline-block w-3 h-3 rounded-sm bg-green-100 border border-green-300"></span>
          {{t "audiometry.audiogram_normal"}}
        </span>
        <span class="inline-flex items-center gap-1">
          <span class="inline-block w-3 h-3 rounded-sm bg-yellow-100 border border-yellow-300"></span>
          {{t "audiometry.audiogram_mild"}}
        </span>
        <span class="inline-flex items-center gap-1">
          <span class="inline-block w-3 h-3 rounded-sm bg-orange-100 border border-orange-300"></span>
          {{t "audiometry.audiogram_moderate"}}
        </span>
        <span class="inline-flex items-center gap-1">
          <span class="inline-block w-3 h-3 rounded-sm bg-red-100 border border-red-300"></span>
          {{t "audiometry.audiogram_severe"}}
        </span>
      </div>
    </div>
  </template>
}
