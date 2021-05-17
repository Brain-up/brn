import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';

@Component({
  selector: 'app-month-time-track-item',
  templateUrl: './month-time-track-item.component.html',
  styleUrls: ['./month-time-track-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthTimeTrackItemComponent {
  public readonly USER_EXERCISING_PROGRESS_STATUS_COLOR = USER_EXERCISING_PROGRESS_STATUS_COLOR;

  @Input()
  @HostBinding('class.selected')
  public isSelected = false;

  @Input()
  public data: IMonthTimeTrackItemData;
}
