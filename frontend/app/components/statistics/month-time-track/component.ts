import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import { DateTime } from 'luxon';
import { action } from '@ember/object';
import { isNone } from '@ember/utils';
interface IMonthTimeTrackComponentArgs {
  isLoading: boolean;
  selectedMonth: DateTime;
  data: UserYearlyStatisticsModel[];
  onSelectMonth(): void;
  onLoadPrevYear(): void;
  onLoadNextYear(): void;
}

export default class MonthTimeTrackComponent extends Component<IMonthTimeTrackComponentArgs> {
  @tracked isLoading = true;

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
}
