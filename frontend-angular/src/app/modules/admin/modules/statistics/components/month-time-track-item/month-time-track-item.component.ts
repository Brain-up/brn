import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { LEVEL_COLOR } from '../../models/level-color';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';

@Component({
  selector: 'app-month-time-track-item',
  templateUrl: './month-time-track-item.component.html',
  styleUrls: ['./month-time-track-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthTimeTrackItemComponent {
  public readonly LEVEL_COLOR = LEVEL_COLOR;

  @Input()
  public data: IMonthTimeTrackItemData;
}
