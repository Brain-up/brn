import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MONTHS_IN_YEAR } from '@shared/constants/common-constants';
import { secondsTo } from '@shared/helpers/seconds-to';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';

@Component({
  selector: 'app-month-time-track',
  templateUrl: './month-time-track.component.html',
  styleUrls: ['./month-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthTimeTrackComponent {
  public monthTimeTrackItemsData: IMonthTimeTrackItemData[];

  @Input()
  public isLoading = true;

  @Input()
  public selectedMonth: Dayjs;

  @Input()
  public set data(data: UserYearlyStatistics[] | undefined) {
    if (!data) {
      return;
    }

    this.monthTimeTrackItemsData = data.map((rawItem) => {
      const date = dayjs(rawItem.date);

      return {
        progress: rawItem.progress,
        time: secondsTo(rawItem.exercisingTimeSeconds, 'hms'),
        days: rawItem.days,
        month: date.format('MMMM'),
        year: date.year(),
        date,
      };
    });
  }

  @Output()
  public selectMonthEvent = new EventEmitter<Dayjs>();

  @Output()
  public loadPrevYearEvent = new EventEmitter<void>();

  @Output()
  public loadNextYearEvent = new EventEmitter<void>();

  public selectMonth(date: Dayjs): void {
    this.selectMonthEvent.emit(date.clone());
  }

  public loadPrevYear(): void {
    this.loadPrevYearEvent.emit();
  }

  public loadNextYear(): void {
    if (!this.isAllowNextYear()) {
      return;
    }

    this.loadNextYearEvent.emit();
  }

  public isSelectedMonth(date: Dayjs): boolean {
    return date.year() === this.selectedMonth.year() && date.month() === this.selectedMonth.month();
  }

  public isAllowNextYear(): boolean {
    return this.selectedMonth.add(1, 'year').year() <= dayjs().year();
  }

  public isIncompleteYear(): boolean {
    return this.monthTimeTrackItemsData?.length < MONTHS_IN_YEAR;
  }
}
