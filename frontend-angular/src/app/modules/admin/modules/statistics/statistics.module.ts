import { NgModule } from '@angular/core';
import { SumTimeTrackComponent } from './components/sum-time-track/sum-time-track.component';
import { UnitTimeTrackComponent } from './components/unit-time-track/unit-time-track.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsComponent } from './statistics.component';

@NgModule({
  declarations: [
    StatisticsComponent,
    SumTimeTrackComponent,
    UnitTimeTrackComponent,
  ],
  imports: [StatisticsRoutingModule],
})
export class StatisticsModule {}
