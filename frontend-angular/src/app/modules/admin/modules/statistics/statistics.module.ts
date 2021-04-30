import { NgModule } from '@angular/core';
import { SumTimeTrackComponent } from './components/sum-time-track/sum-time-track.component';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsComponent } from './statistics.component';

@NgModule({
  declarations: [StatisticsComponent, SumTimeTrackComponent],
  imports: [StatisticsRoutingModule],
})
export class StatisticsModule {}
