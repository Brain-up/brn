import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-sum-time-track',
  templateUrl: './sum-time-track.component.html',
  styleUrls: ['./sum-time-track.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SumTimeTrackComponent {}
