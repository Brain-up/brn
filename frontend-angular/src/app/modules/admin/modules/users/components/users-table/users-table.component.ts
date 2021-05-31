import { GenderType } from '@admin/models/gender';
import { SortType } from '@admin/models/sort';
import { User } from '@admin/models/user';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import * as dayjs from 'dayjs';
import { ILastWeekChartDataItem } from '../../models/last-week-chart-data-item';
import { IUsersTableItem } from '../../models/users-table-item';

@Component({
  selector: 'app-users-table',
  templateUrl: './users-table.component.html',
  styleUrls: ['./users-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersTableComponent {
  private chartsData: ILastWeekChartDataItem[][];

  public usersTableData: IUsersTableItem[];

  @Input()
  public sortByName: SortType = 'asc';

  @Input()
  public set data(data: User[] | undefined) {
    if (!data) {
      return;
    }

    this.chartsData = [];

    this.usersTableData = data.map((rawItem, i) => {
      const firstVisit = dayjs(rawItem.firstDone);
      const lastVisit = dayjs(rawItem.lastDone);

      this.chartsData.push(rawItem.lastWeek.map(({ value, progress }) => ({ y: value, progress })));

      return {
        id: rawItem.id,
        name: rawItem.name,
        yearsOld: dayjs().year() - rawItem.bornYear,
        gender: rawItem.gender.toLowerCase() as Lowercase<GenderType>,
        firstVisit: {
          date: firstVisit.format('MMMM D, YYYY'),
          time: firstVisit.format('h'),
        },
        lastVisit: {
          date: lastVisit.format('MMMM D, YYYY'),
          time: lastVisit.format('h'),
        },
        lastWeek: {
          data: [['data', ...rawItem.lastWeek.map(({ value }) => value)]],
          option: {
            colors: {
              data: (dataItem) => USER_EXERCISING_PROGRESS_STATUS_COLOR[this.chartsData[i][dataItem.index].progress],
            },
            axis: { x: { show: false }, y: { show: false } },
            size: { height: 60, width: 140 },
            legend: { show: false },
            tooltip: { show: false },
            bar: { width: 8, radius: 4 },
          },
        },
        workingDaysInLastMonth: rawItem.workDayByLastMonth,
        hasProgress: rawItem.diagnosticProgress.SIGNALS,
        isFavorite: rawItem.isFavorite,
      };
    });
  }

  @Output()
  public sortByNameEvent = new EventEmitter<SortType>();
}
