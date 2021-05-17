import { AdminApiService } from '@admin/services/api/admin-api.service';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import * as dayjs from 'dayjs';
import { Dayjs } from 'dayjs';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { Subject } from 'rxjs';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';
import { finalize, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatisticsComponent implements OnInit, OnDestroy {
  private readonly destroyer$ = new Subject<void>();
  private readonly userId: number;

  private statisticsInfoDialogRef: MatDialogRef<StatisticsInfoDialogComponent, void>;

  public selectedMonth = dayjs();
  public isLoadingWeekTimeTrackData = true;
  public weekTimeTrackData: UserWeeklyStatistics[];
  public isLoadingMonthTimeTrackData = true;
  public monthTimeTrackData: UserYearlyStatistics[];

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly adminApiService: AdminApiService,
    private readonly activatedRoute: ActivatedRoute,
    public readonly matDialog: MatDialog
  ) {
    this.userId = Number(this.activatedRoute.snapshot.params.userId);
  }

  ngOnInit(): void {
    this.getWeekTimeTrackData();
    this.getMonthTimeTrackData();
  }

  ngOnDestroy(): void {
    this.statisticsInfoDialogRef?.close();
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public openStatisticsInfoDialog(): void {
    this.statisticsInfoDialogRef = this.matDialog.open(StatisticsInfoDialogComponent, {
      width: '650px',
    });
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
        takeUntil(this.destroyer$)
      )
      .subscribe((weekTimeTrackData) => (this.weekTimeTrackData = weekTimeTrackData));
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
        takeUntil(this.destroyer$)
      )
      .subscribe((monthTimeTrackData) => (this.monthTimeTrackData = monthTimeTrackData));
  }
}
