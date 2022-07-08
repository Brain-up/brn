import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { AdminApiService } from '@admin/services/api/admin-api.service';
import { Dayjs } from 'dayjs';
import { UserDailyDetailStatistics } from '@admin/models/user-daily-detail-statistics';
import { finalize, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-daily-time-table',
  templateUrl: './daily-time-table.component.html',
  styleUrls: ['./daily-time-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DailyTimeTableComponent implements OnInit, OnDestroy, OnChanges {
  private readonly destroyer$ = new Subject<void>();

  @Input()
  public userId: number;

  @Input()
  public day: Dayjs;

  public isLoadingWeekTimeTrackData = true;

  public userDailyDetailsData: UserDailyDetailStatistics[];

  public dataSource: MatTableDataSource<UserDailyDetailStatistics>;
  public readonly displayedColumns: string[] = [
    'seriesName',
    'allDoneExercises',
    'uniqueDoneExercises',
    'repeatedExercises',
    'doneExercisesSuccessfullyFromFirstTime',
    'listenWordsCount'
  ];

  constructor(
    private readonly adminApiService: AdminApiService,
    private readonly cdr: ChangeDetectorRef,
  ) {
  }

  ngOnInit(): void {
    this.loadData();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.loadData();
  }

  public ngOnDestroy(): void {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private loadData() {
    this.isLoadingWeekTimeTrackData = true;
    this.adminApiService.getUserDailyDetailStatistics(
      this.userId,
      this.day
    ).pipe(
      finalize(() => {
        this.isLoadingWeekTimeTrackData = false;
        this.cdr.detectChanges();
      }),
      takeUntil(this.destroyer$),
    ).subscribe(
      (userDailyDetailsData) => {
        this.userDailyDetailsData = userDailyDetailsData;
        this.dataSource = new MatTableDataSource(userDailyDetailsData);
      },
    );
  }

}
