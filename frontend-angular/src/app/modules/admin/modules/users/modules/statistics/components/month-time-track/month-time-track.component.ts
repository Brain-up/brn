import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';
import { secondsTo } from '@shared/helpers/seconds-to';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  input
} from '@angular/core';

@Component({
    selector: 'app-month-time-track',
    templateUrl: './month-time-track.component.html',
    styleUrls: ['./month-time-track.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MonthTimeTrackComponent {
  public monthTimeTrackItemsData: IMonthTimeTrackItemData[];

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  public readonly isLoading = input(true);

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  public readonly selectedMonth = input<Dayjs>(undefined);

  // TODO: Skipped for migration because:
  //  Accessor inputs cannot be migrated as they are too complex.
  @Input()
  public set data(data: UserYearlyStatistics[] | undefined) {
    if (!data) {
      return;
    }

    const monthsData: IMonthTimeTrackItemData[] = data.map((rawItem) => {
      const date = dayjs(rawItem.date);
      return {
        date,
        days: rawItem.exercisingDays,
        month: date.format('MMMM'),
        progress: rawItem.progress,
        time: secondsTo(rawItem.exercisingTimeSeconds, 'h:m:s'),
        year: date.year(),
      };
    });

    this.fillEmptyMonths(monthsData);
  }

  @Output()
  public selectMonthEvent = new EventEmitter<Dayjs>();

  @Output()
  public loadPrevYearEvent = new EventEmitter<void>();

  @Output()
  public loadNextYearEvent = new EventEmitter<void>();

  private fillEmptyMonths(monthsData: IMonthTimeTrackItemData[]): void {
    const allmonths = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
    this.monthTimeTrackItemsData = [];

    allmonths.forEach((month) => {
      const item = monthsData.find(
        (monthItem) => monthItem.date.month() === month,
      );
      if (item) {
        this.monthTimeTrackItemsData.push(item);
      } else {
        this.monthTimeTrackItemsData.push({
          date: dayjs(new Date(dayjs().year(), month, 1)),
          days: 0,
          month: null,
          progress: null,
          time: '00:00:00',
          year: null,
        });
      }
    });
  }

  public selectMonth(data: IMonthTimeTrackItemData): void {
    if (data.days) {
      this.selectMonthEvent.emit(data.date.clone());
    }
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

  public isSelectedMonth(data: IMonthTimeTrackItemData): boolean {
    return (
      data.date.year() === this.selectedMonth().year() &&
      data.date.month() === this.selectedMonth().month() &&
      data.days !== 0
    );
  }

  public isAllowNextYear(): boolean {
    return this.selectedMonth().add(1, 'year').year() <= dayjs().year();
  }

  public hasValidTrackItem(): boolean {
    return this.monthTimeTrackItemsData.some(item => item.days !== 0);
  }
}
