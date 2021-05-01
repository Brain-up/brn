import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { SumTimeTrackComponent } from './components/sum-time-track/sum-time-track.component';
import { UnitTimeTrackComponent } from './components/unit-time-track/unit-time-track.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsComponent } from './statistics.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [StatisticsComponent, StatisticsInfoDialogComponent, UnitTimeTrackComponent, SumTimeTrackComponent],
  imports: [StatisticsRoutingModule, MatButtonModule, MatDialogModule, MatIconModule],
})
export class StatisticsModule {}
