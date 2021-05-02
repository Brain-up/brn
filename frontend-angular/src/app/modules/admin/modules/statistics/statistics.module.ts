import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { MonthTimeTrackComponent } from './components/month-time-track/month-time-track.component';
import { UnitTimeTrackComponent } from './components/unit-time-track/unit-time-track.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsComponent } from './statistics.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MonthTimeTrackItemComponent } from './components/month-time-track-item/month-time-track-item.component';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [
    StatisticsComponent,
    StatisticsInfoDialogComponent,
    UnitTimeTrackComponent,
    MonthTimeTrackComponent,
    MonthTimeTrackItemComponent,
  ],
  imports: [CommonModule, StatisticsRoutingModule, MatButtonModule, MatDialogModule, MatIconModule],
})
export class StatisticsModule {}
