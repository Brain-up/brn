import * as dayjs from 'dayjs';
import { ActivatedRoute } from '@angular/router';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { Dayjs } from 'dayjs';
import { finalize, shareReplay, takeUntil } from 'rxjs/operators';
import { HOME_PAGE_URL } from '@shared/constants/common-constants';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { Subject } from 'rxjs';
import { TokenService } from '@root/services/token.service';
import { User } from '@admin/models/user.model';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatisticsComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();
  private readonly userId: number;

  private statisticsInfoDialogRef: MatDialogRef<
    StatisticsInfoDialogComponent,
    void
  >;

  public selectedMonth = dayjs();
  public readonly HOME_PAGE_URL = HOME_PAGE_URL;
  public isLoadingWeekTimeTrackData = true;
  public weekTimeTrackData: UserWeeklyStatistics[];
  public isLoadingMonthTimeTrackData = true;
  public monthTimeTrackData: UserYearlyStatistics[];
  public userData: any;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly adminApiService: AdminApiService,
    private readonly cdr: ChangeDetectorRef,
    private tokenService: TokenService,
    public readonly matDialog: MatDialog,
  ) {
    this.userId = Number(this.activatedRoute.snapshot.params.userId);
  }

  ngOnInit(): void {
    this.getUserInfo();
    this.getWeekTimeTrackData();
    this.getMonthTimeTrackData();
  }

  ngOnDestroy(): void {
    this.statisticsInfoDialogRef?.close();
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public openStatisticsInfoDialog(): void {
    this.statisticsInfoDialogRef = this.matDialog.open(
      StatisticsInfoDialogComponent,
      {
        width: '650px',
      },
    );
  }

  public selectMonth(date: Dayjs): void {
    this.selectedMonth = date;

    this.getWeekTimeTrackData();
  }

  public loadPrevYear(): void {
    this.selectedMonth = this.selectedMonth.subtract(1, 'year');

    this.getWeekTimeTrackData();
    this.getMonthTimeTrackData();
  }

  public loadNextYear(): void {
    this.selectedMonth = this.selectedMonth.add(1, 'year');

    this.getWeekTimeTrackData();
    this.getMonthTimeTrackData();
  }

  public loadPrevMonth(): void {
    this.selectedMonth = this.selectedMonth.subtract(1, 'month');

    this.getWeekTimeTrackData();
    // this.getHistoriesData();
  }

  public loadNextMonth(): void {
    this.selectedMonth = this.selectedMonth.add(1, 'month');

    this.getWeekTimeTrackData();
  }

  private getWeekTimeTrackData(): void {
    const fromMonth = this.selectedMonth.startOf('month');
    const toMonth = this.selectedMonth.endOf('month');

    this.isLoadingWeekTimeTrackData = true;
    this.adminApiService
      .getUserWeeklyStatistics(this.userId, fromMonth, toMonth)
      .pipe(
        finalize(() => {
          this.isLoadingWeekTimeTrackData = false;
          this.cdr.detectChanges();
        }),
        takeUntil(this.destroyer$),
      )
      .subscribe(
        (weekTimeTrackData) => (this.weekTimeTrackData = weekTimeTrackData),
      );
  }

  private getMonthTimeTrackData(): void {
    const fromYear = this.selectedMonth.startOf('year');
    const toYear = this.selectedMonth.endOf('year');

    this.isLoadingMonthTimeTrackData = true;
    this.adminApiService
      .getUserYearlyStatistics(this.userId, fromYear, toYear)
      .pipe(
        finalize(() => {
          this.isLoadingMonthTimeTrackData = false;
          this.cdr.detectChanges();
        }),
        takeUntil(this.destroyer$),
      )
      .subscribe((monthTimeTrackData) => {
        this.monthTimeTrackData = monthTimeTrackData;

        if (!monthTimeTrackData.length) {
          return;
        }

        const lastMonth = dayjs(
          monthTimeTrackData[monthTimeTrackData.length - 1].date,
        );

        if (lastMonth.month() >= this.selectedMonth.month()) {
          return;
        }

        this.selectedMonth = lastMonth;
        this.getWeekTimeTrackData();
      });
  }

  private getUserInfo(): void {
    this.userData = this.tokenService.getToken<User>('SELECTED_USER');
  }
}
