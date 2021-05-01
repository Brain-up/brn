import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatisticsComponent implements OnDestroy {
  private statisticsInfoDialogRef: MatDialogRef<StatisticsInfoDialogComponent, void>;

  constructor(public readonly matDialog: MatDialog) {}

  ngOnDestroy(): void {
    this.statisticsInfoDialogRef?.close();
  }

  public openStatisticsInfoDialog(): void {
    this.statisticsInfoDialogRef = this.matDialog.open(StatisticsInfoDialogComponent, {
      width: '650px',
    });
  }
}
