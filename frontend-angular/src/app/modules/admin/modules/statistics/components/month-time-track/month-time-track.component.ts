import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { secondsTo } from '@shared/helpers/seconds-to';
import * as dayjs from 'dayjs';
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
  public currentYear: string | number = '-';

  @Input()
  public set data(data: UserYearlyStatistics[] | null) {
    if (!data) return;

    this.monthTimeTrackItemsData = data.map((rawItem) => ({
      progress: rawItem.progress,
      time: secondsTo(rawItem.exercisingTimeSeconds, 'hms'),
      days: rawItem.days,
      month: dayjs(rawItem.date).format('MMMM'),
      year: dayjs(rawItem.date).year(),
      date: rawItem.date,
    }));
  }

  @Output()
  public selectMonthEvent = new EventEmitter<string>();

  @Output()
  public loadPrevYearEvent = new EventEmitter<void>();

  @Output()
  public loadNextYearEvent = new EventEmitter<void>();

  public selectMonth(date: string): void {
    this.selectMonthEvent.emit(date);
  }

  public loadPrevYear(): void {
    this.loadPrevYearEvent.emit();
  }

  public loadNextYear(): void {
    this.loadNextYearEvent.emit();
  }
}
