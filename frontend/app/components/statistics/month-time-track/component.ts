import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { IMonthTimeTrackItemData } from 'brn/models/user-weekly-statistics';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import { DateTime } from 'luxon';
import { action } from '@ember/object';
import { secondsTo } from 'brn/utils/seconds-to';

interface IMonthTimeTrackComponentArgs {
  isLoading: boolean;
  selectedMonth: DateTime;
  data: UserYearlyStatisticsModel[];
  onSelectMonth(): void;
  onLoadPrevYear(): void;
  onLoadNextYear(): void;
}

export default class MonthTimeTrackComponent<
  IMonthTimeTrackComponentArgs,
> extends Component {
  @tracked monthTimeTrackItemsData: IMonthTimeTrackItemData[] | null = null;
  @tracked isLoading: boolean = true;
  @action
  didUpdateData(): void {
    const data = this.args.data;
    if (!data) {
      return;
    }

    this.monthTimeTrackItemsData = data.map((rawItem: any) => {
      const date: DateTime = DateTime.fromISO(rawItem.date);

      return {
        progress: rawItem.progress,
        time: secondsTo(rawItem.exercisingTimeSeconds, 'h:m:s'),
        days: rawItem.exercisingDays,
        month: date.toFormat('MMMM'),
        year: date.year,
        date,
      };
    });
  }

  @action
  loadPrevYear(): void {
    this.args.onLoadPrevYear();
  }

  @action
  loadNextYear(): void {
    if (!this.isAllowedNextYear()) {
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
    return this.monthTimeTrackItemsData?.length < 12;
  }
}
