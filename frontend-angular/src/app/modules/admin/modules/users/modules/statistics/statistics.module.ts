import { AdminApiService } from '@admin/services/api/admin-api.service';
import { BarChartModule } from '@shared/components/bar-chart/bar-chart.module';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MonthTimeTrackComponent } from './components/month-time-track/month-time-track.component';
import { MonthTimeTrackItemComponent } from './components/month-time-track-item/month-time-track-item.component';
import { NgModule } from '@angular/core';
import { StatisticsComponent } from './statistics.component';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { TranslateModule } from '@ngx-translate/core';
import { WeekTimeTrackComponent } from './components/week-time-track/week-time-track.component';
import { DailyTimeTableComponent } from './components/daily-time-table/daily-time-table.component';
import { MatTableModule } from '@angular/material/table';

@NgModule({
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
        MonthTimeTrackComponent,
        MonthTimeTrackItemComponent,
        StatisticsComponent,
        StatisticsInfoDialogComponent,
        WeekTimeTrackComponent,
        DailyTimeTableComponent,
    ],
    providers: [AdminApiService],
})
export class StatisticsModule {}
