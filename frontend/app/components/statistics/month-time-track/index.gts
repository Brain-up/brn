import Component from '@glimmer/component';
import type { UserYearlyStatistics as UserYearlyStatisticsModel } from 'brn/schemas/user-yearly-statistics';
import { DateTime } from 'luxon';
import { action } from '@ember/object';
import { isNone } from '@ember/utils';
import { on } from '@ember/modifier';
import { fn } from '@ember/helper';
import { t } from 'ember-intl';
import { and } from 'ember-truth-helpers';
import { eq } from 'ember-truth-helpers';
import { not } from 'ember-truth-helpers';
import UiLeftArrow from 'brn/components/ui/left-arrow';
import UiRightArrow from 'brn/components/ui/right-arrow';
import LoadingSpinner from 'brn/components/loading-spinner';
import StatisticsMonthTimeTrackItem from 'brn/components/statistics/month-time-track-item';
interface MonthTimeTrackSignature {
  Args: {
  isLoading: boolean;
  selectedMonth: DateTime;
  data: UserYearlyStatisticsModel[];
  onSelectMonth(): void;
  onLoadPrevYear(): void;
  onLoadNextYear(): void;
  };
  Element: HTMLElement;
}

export default class MonthTimeTrackComponent extends Component<MonthTimeTrackSignature> {

  get monthTimeTrackItemsData(): UserYearlyStatisticsModel[] | null {
    return this.args.data;
  }

  @action
  loadPrevYear(): void {
    this.args.onLoadPrevYear();
  }

  @action
  loadNextYear(): void {
    if (!this.isAllowedNextYear) {
      return;
    }
    this.args.onLoadNextYear();
  }

  get isAllowedNextYear(): boolean {
    return this.args.selectedMonth
      ? this.args.selectedMonth.plus({ year: 1 }).year <= DateTime.now().year
      : false;
  }

  get isIncompleteYear(): boolean {
    if (isNone(this.monthTimeTrackItemsData)) {
      return true;
    }
    if (!Array.isArray(this.monthTimeTrackItemsData)) {
      return false;
    }
    return this.monthTimeTrackItemsData.length < 12;
  }

  <template>
    <div ...attributes>
      <div class="text-xs font-semibold leading-3 uppercase">
        {{t "profile.statistics.month_time_track.title_months"}}
      </div>
      <div class="justify-space h-200px sm:px-6 box-content flex items-center px-2 py-6">
        <button
          data-test-btn-prev class="btn-press focus:outline-none w-48px h-48px rounded-2xl border-purple-left bg-gradient-to-r from-purple-left to-purple-right m-w-0 flex items-center justify-center flex-shrink-0 text-white shadow-lg" type="button"
          {{on "click" @onLoadPrevYear}}
        >
          <UiLeftArrow />
        </button>
        <div
          data-test-month-items-wrap
          data-test-month-incomplete-year={{this.isIncompleteYear}}
          class="w-full my-0 mx-2 flex overflow-auto sm:mx-5
            {{if this.isIncompleteYear " justify-start" " justify-between"}}"
        >
          {{#if @isLoading}}
            <LoadingSpinner />
          {{else if this.monthTimeTrackItemsData.length}}
            {{#each this.monthTimeTrackItemsData as |itemData index|}}
              <StatisticsMonthTimeTrackItem
                data-test-month-track-item-index={{index}} class="last:mr-0 mr-2" @data={{itemData}}
                @isSelected={{and
                  (eq @selectedMonth.year itemData.date.year)
                  (eq @selectedMonth.month itemData.date.month)
                }}
                {{on "click" (fn @onSelectMonth itemData.date)}}
              />
            {{/each}}
          {{else}}
            <div data-test-empty-data class="w-full text-center">
              <span class="text-xs font-semibold leading-3 uppercase">
                {{t
                  "profile.statistics.month_time_track.empty_data"
                  year=@selectedMonth.year
                }}
              </span>
            </div>
          {{/if}}
        </div>
        <button
          data-test-btn-next class="btn-press focus:outline-none w-48px h-48px rounded-2xl border-purple-left bg-gradient-to-r from-purple-left disabled:opacity-25 to-purple-right m-w-0 disabled:cursor-not-allowed disabled:shadow-none flex items-center justify-center flex-shrink-0 text-white shadow-lg" disabled={{not this.isAllowedNextYear}}
          type="button"
          {{on "click" @onLoadNextYear}}
        >
          <UiRightArrow />
        </button>
      </div>
    </div>
  </template>
}
