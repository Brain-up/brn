import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-statistics-info-dialog',
  templateUrl: './statistics-info-dialog.component.html',
  styleUrls: ['./statistics-info-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatisticsInfoDialogComponent {}
