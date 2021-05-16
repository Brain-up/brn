import { AdminApiService } from '@admin/services/api/admin-api.service';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import * as dayjs from 'dayjs';
import { UserWeeklyStatistics } from '@admin/models/user-weekly-statistics';
import { Observable } from 'rxjs';
import { UserYearlyStatistics } from '@admin/models/user-yearly-statistics';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatisticsComponent implements OnInit, OnDestroy {
  private readonly userId: number;

  private statisticsInfoDialogRef: MatDialogRef<StatisticsInfoDialogComponent, void>;

  public selectedMonth = dayjs();
  public weekTimeTrackData$: Observable<UserWeeklyStatistics[]>;
  public monthTimeTrackData$: Observable<UserYearlyStatistics[]>;

  constructor(
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
  }

  public openStatisticsInfoDialog(): void {
    this.statisticsInfoDialogRef = this.matDialog.open(StatisticsInfoDialogComponent, {
      width: '650px',
    });
  }

  public selectMonth(date: string): void {
    this.selectedMonth = dayjs(date);

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
    this.weekTimeTrackData$ = this.adminApiService.getUserWeeklyStatistics(this.userId, fromMonth, toMonth);
  }

  private getMonthTimeTrackData(): void {
    const fromYear = this.selectedMonth.startOf('year');
    const toYear = this.selectedMonth.endOf('year');
    this.monthTimeTrackData$ = this.adminApiService.getUserYearlyStatistics(this.userId, fromYear, toYear);
  }
}
