import { User, UserMapped } from '@admin/models/user';
import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import {
  Component,
  ChangeDetectionStrategy,
  Input,
  ViewChild,
  AfterViewInit,
  OnInit,
} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import * as dayjs from 'dayjs';
import { ILastWeekChartDataItem } from '../../models/last-week-chart-data-item';

@Component({
  selector: 'app-users-table',
  templateUrl: './users-table.component.html',
  styleUrls: ['./users-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersTableComponent implements OnInit, AfterViewInit {
  public displayedColumns: string[] = [
    'name',
    'firstVisit',
    'lastVisit',
    'currentWeek',
    'workingDaysInLastMonth',
    'progress',
    'favorite',
  ];
  dataSource: MatTableDataSource<UserMapped>;
  public usersListMappedData: UserMapped[];
  public chartsData: ILastWeekChartDataItem[][];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  @Input()
  public set userList(userList: User[] | undefined) {
    if (!userList) {
      return;
    }
    this.chartsData = [];

    this.usersListMappedData = userList.map((user, i) => {
      return {
        age: dayjs().year() - user.bornYear,
        currentWeekChart: {
          data: [
            [
              'data',
              ...user.lastWeek.map(
                ({ exercisingTimeSeconds }) => exercisingTimeSeconds,
              ),
            ],
          ],
          option: {
            colors: {
              data: (dataItem) =>
                USER_EXERCISING_PROGRESS_STATUS_COLOR[
                  this.chartsData[i][dataItem.index].progress
                ],
            },
            axis: { x: { show: false }, y: { show: false } },
            size: { height: 60, width: 140 },
            legend: { show: false },
            tooltip: { show: false },
            bar: { width: 8, radius: 4 },
          },
        },
        ...user,
      };
    });
  }

  constructor() {}

  public ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.usersListMappedData);

    console.log('userList', this.userList);
    console.log('dataSource', this.dataSource);
    console.log('usersListMappedData', this.usersListMappedData);
  }

  public applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  public ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
}
