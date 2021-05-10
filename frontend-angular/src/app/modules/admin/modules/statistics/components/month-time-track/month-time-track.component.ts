import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';

@Component({
  selector: 'app-month-time-track',
  templateUrl: './month-time-track.component.html',
  styleUrls: ['./month-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthTimeTrackComponent {
  @Input()
  public data: any;

  public getMonthTimeTrackItemsData(): IMonthTimeTrackItemData[] {
    return [
      {
        level: 0,
        totalTime: '3:44:55',
        daysNumber: 10,
        isCurrentMonth: false,
        monthName: 'Декабрь',
        isShowYearNumber: true,
        yearNumber: 2020,
      },
      {
        level: 1,
        totalTime: '5:25:00',
        daysNumber: 25,
        isCurrentMonth: false,
        monthName: 'Январь',
        isShowYearNumber: true,
        yearNumber: 2021,
      },
      {
        level: 1,
        totalTime: '5:35:26',
        daysNumber: 6,
        isCurrentMonth: false,
        monthName: 'Февраль',
        isShowYearNumber: false,
        yearNumber: 2021,
      },
      {
        level: 2,
        totalTime: '13:04:15',
        daysNumber: 44,
        isCurrentMonth: false,
        monthName: 'Март',
        isShowYearNumber: false,
        yearNumber: 2021,
      },
      {
        level: 0,
        totalTime: '1:32:21',
        daysNumber: 2,
        isCurrentMonth: true,
        monthName: 'Апрель',
        isShowYearNumber: false,
        yearNumber: 2021,
      },
    ];
  }
}
