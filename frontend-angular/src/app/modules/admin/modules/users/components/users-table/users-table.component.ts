import { User, UserMapped } from '@admin/models/user.model';
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
import { ActivatedRoute, Router } from '@angular/router';
import { DataShareService } from '@shared/services/data-share.service';
import * as dayjs from 'dayjs';
import { ILastWeekChartDataItem } from '../../../../models/last-week-chart-data-item';

@Component({
  selector: 'app-users-table',
  templateUrl: './users-table.component.html',
  styleUrls: ['./users-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersTableComponent implements OnInit, AfterViewInit {
  private chartsData: ILastWeekChartDataItem[][];
  private usersListMappedData: UserMapped[];
  public dataSource: MatTableDataSource<UserMapped>;
  public displayedColumns: string[] = [
    'name',
    'firstVisit',
    'lastVisit',
    'currentWeek',
    'workingDaysInLastMonth',
    'progress',
    'favorite',
  ];
  public filterFavorites = false;

  filterValues: any = {};
  fav: boolean;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @Input()
  public set userList(userList: User[] | undefined) {
    if (!userList) {
      return;
    }

    this.chartsData = [];
    this.usersListMappedData = userList.map((user, i) => {
      this.chartsData.push(
        user.lastWeek.map(({ exercisingTimeSeconds, progress }) => ({
          y: exercisingTimeSeconds,
          progress,
        })),
      );

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

  constructor(
    private activatedRoute: ActivatedRoute,
    private dataShareService: DataShareService<User>,
    private router: Router,
  ) {}

  public ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.usersListMappedData);
  }

  public ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  public applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  public applyFavoriteFilter(column: string, filterValue: string): void {
    this.filterFavorites = !this.filterFavorites;

    this.filterValues[column] = filterValue;

    this.dataSource.filter = JSON.stringify(this.filterValues);

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  public navigateToSelectedUser(user): void {
    this.dataShareService.addData(user);
    this.router.navigate([user.id, 'statistics'], {
      relativeTo: this.activatedRoute,
    });
  }
}
