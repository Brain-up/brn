import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BarChartModule } from '@shared/components/bar-chart/bar-chart.module';
import { CommonModule } from '@angular/common';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { MonthTimeTrackComponent } from './components/month-time-track/month-time-track.component';
import { MonthTimeTrackItemComponent } from './components/month-time-track-item/month-time-track-item.component';
import { NgModule } from '@angular/core';
import { StatisticsComponent } from './statistics.component';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { TranslateModule } from '@ngx-translate/core';
import { WeekTimeTrackComponent } from './components/week-time-track/week-time-track.component';
import { DailyTimeTableComponent } from './components/daily-time-table/daily-time-table.component';
import { MatLegacyTableModule as MatTableModule } from '@angular/material/legacy-table';

@NgModule({
  declarations: [
    MonthTimeTrackComponent,
    MonthTimeTrackItemComponent,
    StatisticsComponent,
    StatisticsInfoDialogComponent,
    WeekTimeTrackComponent,
    DailyTimeTableComponent,
  ],
  imports: [
    BarChartModule,
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
    MatTabsModule,
    StatisticsRoutingModule,
    TranslateModule,
    MatTableModule,
  ],
  providers: [AdminApiService],
})
export class StatisticsModule {}
