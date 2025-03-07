import { USER_EXERCISING_PROGRESS_STATUS_COLOR } from '@admin/models/user-exercising-progress-status';
import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostBinding, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { IMonthTimeTrackItemData } from '../../models/month-time-track-item-data';

@Component({
  selector: 'app-month-time-track-item',
  templateUrl: './month-time-track-item.component.html',
  styleUrls: ['./month-time-track-item.component.scss'],
  imports: [NgClass, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MonthTimeTrackItemComponent {
  public readonly USER_EXERCISING_PROGRESS_STATUS_COLOR = USER_EXERCISING_PROGRESS_STATUS_COLOR;

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @HostBinding('class.selected')
  public readonly isSelected = input(false);

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  public readonly data = input<IMonthTimeTrackItemData>(undefined);
}
