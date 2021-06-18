import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { IMonthTimeTrackItemData } from 'brn/models/user-weekly-statistics';
import UserYearlyStatisticsModel from 'brn/models/user-yearly-statistics';
import moment from 'moment';
import { action } from '@ember/object';

interface IMonthTimeTrackComponentArgs {
  isLoading: boolean;
  selectedMonth: moment.Moment;
  data: UserYearlyStatisticsModel[];
  onSelectMonth(): void;
  onLoadPrevYear(): void;
  onLoadNextYear(): void;
}

export default class MonthTimeTrackComponent<
  IMonthTimeTrackComponentArgs,
> extends Component {
  @tracked monthTimeTrackItemsData: IMonthTimeTrackItemData[];
  @tracked isLoading: boolean = true;
  @tracked selectedMonth: moment.Moment;

  @action
  didUpdateData(): void {
    const data = this.args.data;
    if (!data) {
      return;
    }

    this.monthTimeTrackItemsData = data.map((rawItem: any) => {
      const date = moment(rawItem.date);

      return {
        progress: rawItem.progress,
        time: moment()
          .startOf('day')
          .seconds(rawItem.exercisingTimeSeconds)
          .format('h:mm:ss'),
        days: rawItem.exercisingDays,
        month: date.format('MMMM'),
        year: date.year(),
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

  isAllowedNextYear(): boolean {
    return this.selectedMonth.clone().add(1, 'year').year() <= moment().year();
  }

  isSelectedMonth(date: moment.Moment): boolean {
    return (
      date.year() === this.selectedMonth.year() &&
      date.month() === this.selectedMonth.month()
    );
  }

  isIncompleteYear(): boolean {
    return this.monthTimeTrackItemsData?.length < 12;
  }
}
