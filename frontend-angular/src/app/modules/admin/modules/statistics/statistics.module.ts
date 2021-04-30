import { NgModule } from '@angular/core';
import { StatisticsInfoDialogComponent } from './components/statistics-info-dialog/statistics-info-dialog.component';
import { SumTimeTrackComponent } from './components/sum-time-track/sum-time-track.component';
import { UnitTimeTrackComponent } from './components/unit-time-track/unit-time-track.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsComponent } from './statistics.component';

@NgModule({
  declarations: [
    StatisticsComponent,
    SumTimeTrackComponent,
    UnitTimeTrackComponent,
    StatisticsInfoDialogComponent,
  ],
  imports: [StatisticsRoutingModule],
})
export class StatisticsModule {}
